package com.ebook.epub.fixedlayoutviewer.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ebook.epub.common.Defines;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.fixedlayoutviewer.manager.HighlightManager;
import com.ebook.epub.fixedlayoutviewer.manager.UserBookDataFileManager;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.AnnotationConst;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Bookmark;
import com.ebook.epub.viewer.ChromeClient;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.DeviceInfoUtil;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.PopupData;
import com.ebook.epub.viewer.ViewerBase;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.tts.Highlighter;
import com.ebook.tts.TTSDataInfoManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;

public class FixedLayoutWebview extends ViewerBase {

    private String TAG = "FixedLayoutWebview";

    private OnPageLoad mOnPageLoad;

    private Context mContext;

    private float mScale = 0.0f;

    private boolean mMergedAnnotation = false;

    private ArrayList<Highlight> mHighlights;

    private FixedLayoutPageData.ContentsData currentPageData;

    private ArrayList<Rect> mTTSHighlightRectList = new ArrayList<>();

    private Paint mPaint;

    private boolean isMergedMemo=false;

    private boolean selectionHandler=false;

    private ArrayList<Rect> selectionRectList = new ArrayList<>();

    private FixedLayoutWebview.OnWebviewCallbackInterface  mWebviewCallbackListener;

    public interface OnPageLoad {
        /**
         * webview page load finish
         */
        void onPageLoadFinished(WebView view);
    }

    public FixedLayoutWebview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public FixedLayoutWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public FixedLayoutWebview(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init(){
        setBackgroundColor(Color.WHITE);
        requestFocusFromTouch();

        addJavascriptInterface(new FixedLayoutJavaScriptInterface(), "fixedlayout");

        setOverScrollMode(View.OVER_SCROLL_NEVER);

        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setUseWideViewPort(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setSupportZoom(false);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setDisplayZoomControls(false);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            getSettings().setMediaPlaybackRequiresUserGesture(false);
            getSettings().setTextZoom(100);
        }

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        setWebViewClient(new MyWebView());
        setWebChromeClient(new ChromeClient(mContext));

        mHighlights = HighlightManager.getHighlightList();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(BookHelper.textSelectionColor);

        mWebviewInnerHandler = new WebviewInnerHandler(this);
    }

    public void setScale(float scale){
        mScale = scale;
    }

    // WebView ==> Screen 좌표변환
    public int Web2Scr(int value) {
        return Math.round( value * mScale );
    }

    // Screen ==> WebView 좌표변환
    public int Scr2Web(int value) {
        return Math.round( value / mScale );
    }

    public void setWebviewCallbackListener(FixedLayoutWebview.OnWebviewCallbackInterface listener){
        mWebviewCallbackListener = listener;
    }

    public void setOnPageLoadListener(OnPageLoad l){
        this.mOnPageLoad = l;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void showFullScreenVideo(String url){
        DebugSet.d("TAG", "showFullScreenVideo");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/*");
        ((Activity)mContext).startActivity(intent);
    }

    private class FixedLayoutJavaScriptInterface {

        @JavascriptInterface
        public void overflowedMemoContent(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_OVERFLOW_MEMO_CONTENT));
        }

        @JavascriptInterface
        public boolean checkMemoMaxLength(String ids){
            String allMemoText = getAllMemoText(ids);
            if(allMemoText.length() > 2000){
                return false;
            }
            return true;
        }

        @JavascriptInterface
        public void mergeAllMemoText(String ids){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_MERGE_ALL_MEMO, ids));
        }

        @JavascriptInterface
        public void finishTextSelectionMode(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_FINISH_TEXT_SELECTION,null));
        }

        @JavascriptInterface
        public void addDeleteHistory(String deleteAnnotationIds){
            try{
                JSONArray jsArray = new JSONArray(deleteAnnotationIds);
                for(int idx=0; idx<jsArray.length(); idx++){
                    String deleteTarget = (String) jsArray.get(idx);
                    for(Highlight high: mHighlights) {
                        if(high.highlightID.equals(deleteTarget)) {
                            mHighlights.remove(high);
                            if( BookHelper.useHistory ) {
                                UserBookDataFileManager.__hlHistory.remove(high.uniqueID);
                            }
//                            HighlightManager.setHighlightList(mHighlights);
                            break;
                        }
                    }
                }
                saveHighlights();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void drawSelectionRect(String rectList, boolean isExistHandler) {
            selectionHandler = isExistHandler;
            if(selectionHandler)
                mTextSelectionMode = true;

            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_DRAW_SELECTION_RECT, rectList));
        }

        @JavascriptInterface
        public void finishHighlight(){
        }

        @JavascriptInterface
        public String getMemoIconPath(){
            return BookHelper.memoIconPath;
        }

        @JavascriptInterface
        public void setAsidePopupStatus(boolean status){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_SET_ASIDEPOPUP_STATUS, status));   // TODO :: handler case 정의
        }

        @JavascriptInterface
        public void stopMediaOverlay(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_MEDIAOVERLAY_PAUSE));
        }

        @JavascriptInterface
        public void setIdListByPoint(final String json){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_PLAY_SELECTED_MEDIAOVERLAY, json));
        }

        @JavascriptInterface
        public void setBGMState(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_SET_BGM_STATE));
        }

        @JavascriptInterface
        public void didPlayPreventMedia(String id, String mediaType) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("mediaType", mediaType);
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_PREVENT_MEDIA_CONTROL, obj));
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void setSelectedText(String selectedText) {
            mWebviewCallbackListener.setSeletedText(selectedText);
        }

        @JavascriptInterface
        public void print(String _s) {
            DebugSet.d("TAG", _s);
        }

        @JavascriptInterface
        public void reportCurrentPagePath(String currentPageInfo) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_REPORT_CURRENT_PAGE_INFO, currentPageInfo));
        }

        @JavascriptInterface
        public void reportLinkClick(String href){
            String fileName = href.replaceAll(" ", "+").replaceAll("/", "%2F");
            String hrefValue = URLDecoder.decode(fileName);
            if( hrefValue.length() > 0 ) {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_LINK_CLICK, hrefValue));
            }
        }

        @JavascriptInterface
        public void playVideo(final String url){
            DebugSet.w("TAG", "playVideo ========================== "  + url);
        	new Thread(new Runnable() {

                @Override
                public void run() {
                    showFullScreenVideo(url);
                }

            }).start();
        }

        @JavascriptInterface
        public void reportVideoInfo( String src){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_REPORT_VIDEO_INFO, src));               // TODO :: handler case 정의
        }

        @JavascriptInterface
        public void reportTouchPosition(int x, int y) {
            if(mTextSelectionMode){
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_CONTEXT_MENU_HIDE,null));
            } else {
                int[] touchedPosition = new int[2];
                touchedPosition[0]=x;
                touchedPosition[1]=y;
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_REPORT_TOUCH_POSITION, touchedPosition));
            }
        }

        @JavascriptInterface
        public void checkMergeAnnotation(String jsonObject) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_CHECK_MERGE_ANNOTATION, jsonObject));

        }

        @JavascriptInterface
        public void saveHighlight(String json) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_ADD_HIGHLIGHTED_DATA, json));
        }

        @JavascriptInterface
        public void changeHighlightColor(String highlightID, int clrIndex, double percent) {

            for(Highlight h: mHighlights) {
                if( h.highlightID.equals(highlightID) ) {
                    h.colorIndex = clrIndex;
                    h.percent = percent;

                    if( BookHelper.useHistory ){ //색상 변경도 히스토리에 변경항목으로 추가(동기화를 위함)
                        long newID = System.currentTimeMillis() / 1000L;
                        UserBookDataFileManager.__hlHistory.modifyRemove(h.uniqueID, newID);
                        UserBookDataFileManager.__hlHistory.modifyAdd(newID);
                        h.uniqueID = newID;
                    }
                    break;
                }
            }
        }

        @JavascriptInterface
        public void showContextMenu(String highlightID, int menuTypeIndex, String contextMenuPosition,
                                    int endRight, int endTop, int endBottom, int startLeft, int startTop, int startBottom) {

            // #1. context menu draw 가능 여부 체크
            // #2. 위치 보정 및 전달

            if(!mTextSelectionMode )
                return;

            endRight = Web2Scr(endRight);
            endTop = Web2Scr(endTop);
            endBottom = Web2Scr(endBottom);
            startLeft = Web2Scr(startLeft);
            startTop = Web2Scr(startTop);
            startBottom = Web2Scr(startBottom);

            int[] contextMenuInfo = mWebviewCallbackListener.requestContextMenuInfo();
            int contextMenuHeight = contextMenuInfo[0];
            int contextMenuTopMargin = contextMenuInfo[1];
            int contextMenuBottomMargin = contextMenuInfo[1];
            int handlerHeight = mStartHandle.getIntrinsicHeight()/3;

            PopupData contextMenuData;
            if(contextMenuPosition.equalsIgnoreCase("START")) {
                if(startTop  - contextMenuTopMargin - contextMenuHeight < 0) { // 시작 핸들러 위 메뉴 그렸는데 화면 벗어나는 경우 - 핸들러 아래로 메뉴 그려야 함
                    contextMenuData = new PopupData(highlightID, startLeft , startBottom  + handlerHeight + contextMenuBottomMargin, BookHelper.ContextMenuType.values()[menuTypeIndex], currentPageData.getContentsPosition());
                } else {
                    contextMenuData = new PopupData(highlightID, startLeft , startTop  - contextMenuTopMargin - contextMenuHeight, BookHelper.ContextMenuType.values()[menuTypeIndex], currentPageData.getContentsPosition());
                }
            } else if (contextMenuPosition.equalsIgnoreCase("END")){
                if(endTop  - contextMenuTopMargin - contextMenuHeight < 0) { // 종료 핸들러 위 메뉴 그렸는데 화면 벗어나는 경우 - 핸들러 아래로 메뉴 그려야 함
                    contextMenuData = new PopupData(highlightID, endRight , endBottom  + handlerHeight + contextMenuBottomMargin, BookHelper.ContextMenuType.values()[menuTypeIndex], currentPageData.getContentsPosition());
                } else {
                    contextMenuData = new PopupData(highlightID, endRight ,endTop  - contextMenuTopMargin - contextMenuHeight, BookHelper.ContextMenuType.values()[menuTypeIndex], currentPageData.getContentsPosition());
                }
            } else {
                contextMenuData = new PopupData(highlightID, getWidth()/2, getHeight()/2 - contextMenuHeight/2, BookHelper.ContextMenuType.values()[menuTypeIndex], -1);
            }
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_CONTEXT_MENU_SHOW, contextMenuData));
        }

        @JavascriptInterface
        public void reportError(int errCode) {
            DebugSet.d(TAG, "reportError >>>>>>> " + errCode);
            if(errCode==2) return;
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.FIXEDLAYOUT_REPORT_ERROR, errCode));
        }
    }

    private class MyWebView extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            if( mOnPageLoad != null )
                mOnPageLoad.onPageLoadFinished(view);
            mWebviewCallbackListener.pageLoadFinished((FixedLayoutPageData.ContentsData) view.getTag());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        if(selectionRectList.size()>0){
            for(Rect rcSpan: selectionRectList) {
                Rect r = rcSpan;
                if(selectionHandler){
                    mPaint.setColor(BookHelper.textSelectionColor);
                    canvas.drawRect(r, mPaint);
                    drawHandler(canvas);
                } else{
                    mPaint.setColor(Color.parseColor(BookHelper.Colors[BookHelper.lastHighlightColor]));
                    mPaint.setAlpha(100);
                    canvas.drawRect(r, mPaint);
                }
            }
        }

        //TTS Highlight
        if( mTTSHighlightRectList.size() > 0 ) {
            Paint inner = new Paint();
            inner.setStyle(Style.FILL);
            inner.setColor(BookHelper.ttsHighlightColor);
            int i=0;
            for(Rect rc: mTTSHighlightRectList ) {
                RectF innerF = new RectF(rc.left, rc.top+i, rc.right, rc.bottom+i );
                canvas.drawRoundRect(innerF, 0, 0, inner);
            }
        }

        canvas.restore();
    }

    public Drawable mStartHandle;
    public Drawable mEndHandle;

    public void drawHandler(Canvas canvas){
        drawStartHandler(canvas);
        drawEndHandler(canvas);
    }

    public void drawStartHandler(Canvas canvas) {

        if( mStartHandle == null )
            mStartHandle = mWebviewCallbackListener.requestStartHandlerImage();

        if( !mStartHandle.isVisible() )
            mStartHandle.setVisible(true, true);

        Rect r = selectionRectList.get(0);

        int w = mStartHandle.getIntrinsicWidth();
        int h = mStartHandle.getIntrinsicHeight();

        mStartHandle.setBounds(r.left -w + (w/3), r.bottom - (h/3), r.left  + (w/3), r.bottom+h - (h/3));
        mStartHandle.draw(canvas);
    }

    public void drawEndHandler(Canvas canvas) {

        if( mEndHandle == null )
            mEndHandle = mWebviewCallbackListener.requestEndHandlerImage();

        if( !mEndHandle.isVisible() )
            mEndHandle.setVisible(true, true);

        Rect r = selectionRectList.get(selectionRectList.size()-1);

        int w = mEndHandle.getIntrinsicWidth();
        int h = mEndHandle.getIntrinsicHeight();

        mEndHandle.setBounds(r.right - (w/3), r.bottom - (h/3), r.right+w- (w/3), r.bottom+h - (h/3));
        mEndHandle.draw(canvas);
    }

    public void deleteHighlight(String id) {
        for(Highlight h: mHighlights) {
            if(h.highlightID.equals(id)) {
                deleteHighlight(h);
                break;
            }
        }
    }

    public void deleteHighlight(Highlight high) {

        JSONArray hiLite = new JSONArray();
        hiLite.put(high.get2());

        mHighlights.remove(high);
        if( BookHelper.useHistory ) {
            UserBookDataFileManager.__hlHistory.remove(high.uniqueID);
        }

        this.loadUrl("javascript:deleteHighlights(" + hiLite.toString() + ")");

        HighlightManager.setHighlightList(mHighlights);
//        SendMessage(Defines.SELECTOR_BAR_HIDE, true);     // TODO :: handler case 필요하면 정의
    }

    private void addHighlightingData(String highlightedData) {

        try {
            JSONObject jobj = new JSONObject(highlightedData);
            long uniqueId = jobj.getLong("uniqueId");
            String id = jobj.getString("highlightID");
            String startPath = jobj.getString("startElementPath");
            int startChildIndex = jobj.getInt("startChildIndex");
            int startCharOffset = jobj.getInt("startCharOffset");
            String endPath = jobj.getString("endElementPath");
            int endChildIndex = jobj.getInt("endChildIndex");
            int endCharOffset = jobj.getInt("endCharOffset");
            boolean deleted = jobj.getBoolean("isDeleted");
            boolean annotation = jobj.getBoolean("isAnnotation");
            String spanId = jobj.getString("spanId");
            String chapterId = jobj.getString("chapterId");
            int colorIndex = jobj.getInt("colorIndex");
            String text = jobj.getString("text");
            String memo = jobj.getString("memo");
            double percent = jobj.getDouble("percent");

            Highlight highlight = new Highlight();
            highlight.uniqueID = uniqueId;
            highlight.highlightID = id;
            highlight.startPath = startPath;
            highlight.endPath = endPath;
            highlight.startChild = startChildIndex;
            highlight.startChar = startCharOffset;
            highlight.endChild = endChildIndex;
            highlight.endChar = endCharOffset;
            highlight.deleted = deleted;
            highlight.annotation = annotation;
            highlight.spanId = spanId;
            highlight.chapterId = chapterId;
            highlight.text = text;
            highlight.deviceModel = DeviceInfoUtil.getDeviceModel();
            highlight.osVersion = DeviceInfoUtil.getOSVersion();

            ChapterInfo chapter = UserBookDataFileManager.getChapter().getCurrentChapter();
            highlight.chapterFile = currentPageData.getContentsFilePath();
            if (chapterId == null || chapterId.length() <= 0) {
                if (chapter != null) {
                    highlight.chapterName = UserBookDataFileManager.getChapter().getChapterInfoFromPath(currentPageData.getContentsFilePath()).getChapterName();
                }
            } else {
                chapter = UserBookDataFileManager.getChapter().getCurrentChapter();
                if (chapter != null) {
                    highlight.chapterName = chapter.getChapterName();
                }
            }

            highlight.memo = Uri.decode(memo);
            highlight.colorIndex = colorIndex;
            highlight.page = currentPageData.getContentsPage();
            highlight.percent = percent;
            highlight.percentInBook = (double) currentPageData.getContentsPage() / (double) UserBookDataFileManager.mReadingSpine.getSpineInfos().size() * 100.0d;

            mHighlights.add(highlight);

            if (BookHelper.useHistory) {
                if(!selectionHandler){
                    mWebviewCallbackListener.reportAnnotationQuick();
                }
                if (mMergedAnnotation) {
                    UserBookDataFileManager.__hlHistory.mergeAdd(highlight.uniqueID);
                    if(selectionHandler){
                        mWebviewCallbackListener.reportMergedAnnotationSelection();
                    } else {
                        mWebviewCallbackListener.reportMergedAnnotationQuick();
                    }
                    mMergedAnnotation = false;
                } else {
                    UserBookDataFileManager.__hlHistory.add(highlight.uniqueID);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        saveHighlights();
    }

    public boolean saveHighlights() {

        try {

            String fileName = UserBookDataFileManager.getFullPath(BookHelper.annotationFileName);
            if( fileName.length() <= 0 ) return false;

            DebugSet.d(TAG, "save Highlight ................ " + fileName);

            File annotationDataFile = new File(fileName);
            if( !annotationDataFile.exists()) {
                annotationDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(annotationDataFile);

            JSONObject object = new JSONObject();
            object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.ANNOTATION);
            object.put(AnnotationConst.FLK_ANNOTATION_VERSION, BookHelper.annotationVersion);

            JSONArray array = new JSONArray();
            for(Highlight hilite: mHighlights) {
                array.put(hilite.get());
            }

            object.put(AnnotationConst.FLK_ANNOTATION_LIST, array);

            DebugSet.d(TAG, "json array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();

            if(BookHelper.useHistory) {
                UserBookDataFileManager.__hlHistory.write(UserBookDataFileManager.getFullPath(BookHelper.annotationHistoryFileName));
            }

            HighlightManager.setHighlightList(mHighlights);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadHighlightData(ArrayList<Highlight> highlightData){

        mHighlights = highlightData;

        if(BookHelper.useHistory)
            UserBookDataFileManager.__hlHistory.read(UserBookDataFileManager.getFullPath(BookHelper.annotationHistoryFileName));
    }

    public void applyAllHighlight() {

        JSONArray array = new JSONArray();

        for(Highlight h : mHighlights) {
            if( UserBookDataFileManager.getChapter().getCurrentChapter() != null ) {
                if(currentPageData.getContentsFilePath().equalsIgnoreCase(h.chapterFile)) {
                    array.put(h.get2());
                }
            }
        }

        if(array.length()>0)
            loadUrl("javascript:applyHighlights(" + array.toString() + ")");
    }

    public void deleteAllHighlight(){

        JSONArray array = new JSONArray();

        for(Highlight h : mHighlights) {
            if( UserBookDataFileManager.getChapter().getCurrentChapter() != null ) {
                if(currentPageData.getContentsFilePath().equals(h.chapterFile)) {
                    array.put(h.get2());
                }
            }
        }

        if(array.length()>0)
            loadUrl("javascript:deleteAllHighlights(" + array.toString() + ")");
    }

    public void scrollToAnnotationId(String id){
        loadUrl("javascript:scrollToAnnotationID('" + id + "')");
    }

    public void scrollToSearch(){
        loadUrl("javascript:scrollToSearch()");
    }


    public void removeCommentNode() {
        loadUrl("javascript:removeCommentNode()");
    }

    public FixedLayoutPageData.ContentsData getCurrentPageData() {
        return (FixedLayoutPageData.ContentsData) getTag();
    }

    public void setCurrentPageData(FixedLayoutPageData.ContentsData currentPageData) {
        this.currentPageData = currentPageData;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && mTextSelectionMode){
            finishTextSelectionMode();
//            SendMessage(Defines.SELECTOR_BAR_HIDE, true);       // TODO :: handler case 필요하면 정의
        }
        return super.onKeyDown(keyCode, event);
    }
//	[ssin-annotation] e

    //	[ssin-tts mediaoverlay] s
    public void setJSInterface(int position, TTSDataInfoManager manager, Highlighter highlighter, MediaOverlayController mediaOverlayController){
        manager.addTTSDataInfoJSInterface(this);
        highlighter.addHighlightJSInterface(this);
        mediaOverlayController.addMediaOverlayJSInterface(position, this);
    }

    public void getIDListByPoint(int x, int y){
        int wx = Scr2Web(x);
        int wy = Scr2Web(y);
        loadUrl("javascript:getIDListByPoint(" + wx + "," + wy + ",'"+currentPageData.getContentsFilePath()+"')");
    }

    public void setTTSHighlightRect(JSONArray rectArray){

        mTTSHighlightRectList.clear();

        try {
            for(int i=0; i<rectArray.length(); i++) {
                JSONObject rectObject = rectArray.getJSONObject(i);
                Rect rc = getRectFromJsonObject(rectObject);
                mTTSHighlightRectList.add(rc);
            }
            invalidate();
        } catch(JSONException e) {
        }
    }

    public void removeTTSHighlightRect() {
        mTTSHighlightRectList.clear();
        invalidate();
    }

    public void setPreventMediaControl(boolean isPrevent) {
        loadUrl("javascript:setPreventMediaControl("+ isPrevent +")");
    }

    private Rect getRectFromJsonObject(JSONObject rect) {
        try {
            int left = this.Web2Scr(rect.getInt("left"));
            int top = this.Web2Scr(rect.getInt("top"));
            int right = left + this.Web2Scr(rect.getInt("width"));
            int bottom = top + this.Web2Scr(rect.getInt("height"));
            return new Rect(left,top,right,bottom);
        }
        catch(Exception e) {
        }
        return null;
    }
//	[ssin-tts mediaoverlay] e

    public String getCurrentUserAgent(){
        return this.getSettings().getUserAgentString();
    }

    private void mergeAnnotation(String jsonStr){

        try {
            JSONObject object = new JSONObject(jsonStr);
            String hID = object.getString("highlightID");
            String startPath = object.getString("startElementPath");
            int startChildIndex = object.getInt("startChildIndex");
            int startCharOffset = object.getInt("startCharOffset");
            String endPath = object.getString("endElementPath");
            int endChildIndex = object.getInt("endChildIndex");
            int endCharOffset = object.getInt("endCharOffset");
            int colorIndex = object.getInt("colorIndex");
            boolean isMemo = object.getBoolean("isMemo");
            String memoText = "";
            if(isMemo)
                memoText = object.getString("memo");

            String currentChapterFilePath = currentPageData.getContentsFilePath().toLowerCase();

            ArrayList<Highlight> erases = new ArrayList<>();

            for(int i=0; i<mHighlights.size(); i++) {

                Highlight h = mHighlights.get(i);

                String annotationFilePath = h.chapterFile.toLowerCase();
                if( !currentChapterFilePath.equals(annotationFilePath) )
                    continue;

                // 이너셀렉션 - 20181113 정책변경 허용
                if(startChildIndex > h.startChild || (startChildIndex == h.startChild && startCharOffset >= h.startChar )){
                    if(endChildIndex < h.endChild || (endChildIndex == h.endChild && endCharOffset <= h.endChar)){
                        DebugSet.d(TAG, "INNER");
                        erases.add(h);

                        startPath = h.startPath;
                        startChildIndex = h.startChild;
                        startCharOffset = h.startChar;
                        endPath = h.endPath;
                        endChildIndex = h.endChild;
                        endCharOffset = h.endChar;
                        continue;
                    }
                }

                //기 샐랙션이 뉴 셀렉션에 포함되는 경우
                if(startChildIndex < h.startChild || (startChildIndex == h.startChild && startCharOffset <= h.startChar )){
                    if(endChildIndex > h.endChild || (endChildIndex == h.endChild && endCharOffset >= h.endChar)){
                        erases.add(h);
                        continue;
                    }
                }

                if( startChildIndex == endChildIndex && startPath.equals(endPath)) {    // 뉴 셀렉션이 같은 문단에서 시작하고 끝나는 경우
                    // 뉴셀렉션이 기셀렉션을 감싼경우.
                    if( (startChildIndex == h.startChild && startPath.equals(h.startPath) && endChildIndex == h.endChild && endPath.equals(h.endPath))
                            && startCharOffset <= h.startChar && endCharOffset >= h.endChar ) {
                        DebugSet.d(TAG, "Include All !!!");
                        erases.add(h);
                    } else if( startChildIndex == h.startChild && startPath.equals(h.startPath) ) {   //기존 셀렉션의 시작블럭이 같을경우
                        //기셀렉션도 한 문단인 경우 : Jeong, 2013-06-28 : iOS 추가병합로직
                        if( endChildIndex == h.endChild ){
                            //뉴셀렉션이 기셀렉션 앞쪽이나 같은지점에서 시작해 기셀렉션안에서 끝난경우.
                            if( startCharOffset <= h.startChar && endCharOffset > h.startChar && endCharOffset <= h.endChar ) {
                                DebugSet.d(TAG, "PARTIAL startChild");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                            //뉴셀렉션이 기셀렉션 안쪽이나 끝나는지점에서 시작해 기셀렉션보다 뒤쪽에서 끝난경우.
                            else if( startCharOffset < h.endChar && endCharOffset >= h.endChar ) {
                                DebugSet.d(TAG, "PARTIAL endChild");
                                erases.add(h);

                                startPath = h.startPath;
                                startChildIndex = h.startChild;
                                startCharOffset = h.startChar;
                            }
                            else if( startCharOffset >= h.startChar && endCharOffset <= h.endChar ){
                                DebugSet.d(TAG, "INCLUDE 1");
                                erases.add(h);
                            }
                            //기셀렉션이 멀티인 경우 : Jeong, 2013-06-28 : iOS 추가병합로직
                        } else {
                            //셀렉션의 시작이 기셀렉션 의 시작보다 앞이고 셀렉션의 끝이 기셀렉션의 시작보다 앞인 경우
                            if (startCharOffset <= h.startChar && endCharOffset > h.startChar) {
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            } else if( startCharOffset >= h.startChar ){
                                DebugSet.d(TAG, "INCLUDE 1");
                                erases.add(h);
                            }
                        }
                    } else if( startChildIndex == h.endChild && startPath.equals(h.endPath)) {    //기존 셀렉션의 시작블럭이 더 앞일경우
                        if( startCharOffset <= h.endChar && endCharOffset <= h.endChar ) {
                            DebugSet.d(TAG, "INCLUDE 1");
                            erases.add(h);
                        } else if( startCharOffset < h.endChar ) {
                            DebugSet.d(TAG, "PARTIAL endChar");
                            erases.add(h);

                            startPath = h.startPath;
                            startChildIndex = h.startChild;
                            startCharOffset = h.startChar;
                        }
                    }
                } else {
                    // SSIN 뉴 셀렉션이 기셀렉션과 같은 문단에서 시작
                    if( startChildIndex == h.startChild && startPath.equals(h.startPath)) {
                        // SSIN 기셀렉션이 같은 문단에서 시작하고 끝난 경우
                        if( h.startChild == h.endChild ) {
                            if( startCharOffset < h.startChar ) {
                                DebugSet.d(TAG, "INCLUDE 1");
                                erases.add(h);
                            } else if( h.endChar > startCharOffset ) {
                                DebugSet.d(TAG, "PARTIAL 1");
                                erases.add(h);

                                startPath = h.startPath;
                                startChildIndex = h.startChild;
                                startCharOffset = h.startChar;
                            }
                        } else {
                            if( startCharOffset < h.startChar ) {
                                DebugSet.d(TAG, "INCLUDE 2");
                                erases.add(h);
                            } else if( h.startChar <= startCharOffset ) {
                                DebugSet.d(TAG, "PARTIAL 2");
                                erases.add(h);

                                startPath = h.startPath;
                                startChildIndex = h.startChild;
                                startCharOffset = h.startChar;
                            }
                        }
                    } else if( startChildIndex == h.endChild && startPath.equals(h.endPath)) {
                        if( h.startChild == h.endChild ) {
                            if( startCharOffset < h.startChar ) {
                                DebugSet.d(TAG, "INCLUDE 3");
                                erases.add(h);
                            } else if( startCharOffset <= h.endChar ) {
                                DebugSet.d(TAG, "PARTIAL 3");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                        } else {
                            if( startCharOffset < h.endChar ) {
                                DebugSet.d(TAG, "PARTIAL 4");
                                erases.add(h);

                                startPath = h.startPath;
                                startChildIndex = h.startChild;
                                startCharOffset = h.startChar;
                            }
                        }
                    } else {
                        DebugSet.d(TAG, "start =/= child");
                    }

                    if( endChildIndex == h.startChild && endPath.equals(h.startPath)) {
                        if( h.startChild == h.endChild ) {
                            if( endCharOffset > h.endChar ) {
                                DebugSet.d(TAG, "INCLUDE 4");
                                erases.add(h);
                            } else if( h.startChar < endCharOffset ) {
                                DebugSet.d(TAG, "PARTIAL 5");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                        } else {
                            if( endCharOffset > h.startChar ) {
                                DebugSet.d(TAG, "PARTIAL 6");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                        }
                    } else if( endChildIndex == h.endChild && endPath.equals(h.endPath)) {
                        if( endCharOffset > h.endChar ) {
                            DebugSet.d(TAG, "INCLUDE 5");
                            erases.add(h);
                        }
                        else if( endCharOffset < h.endChar ) {
                            DebugSet.d(TAG, "PARTIAL 7");
                            erases.add(h);

                            endPath = h.endPath;
                            endChildIndex = h.endChild;
                            endCharOffset = h.endChar;
                        }
                    } else {
                        DebugSet.d(TAG, "end =/= child");
                    }
                }

                if( startChildIndex < h.startChild && endChildIndex > h.endChild ) {
                    DebugSet.i(TAG, "INCLUDE !!!");
                    //        				resultArr.add(h);
                } else {
                    if( h.startChild < startChildIndex && h.endChild > startChildIndex ) {
                        DebugSet.i(TAG, "PARTIAL start !!");
                        erases.add(h);

                        startPath = h.startPath;
                        startChildIndex = h.startChild;
                        startCharOffset = h.startChar;
                    }

                    if( h.startChild < endChildIndex && h.endChild > endChildIndex ) {
                        DebugSet.i(TAG, "PARTIAL end !!");
                        erases.add(h);

                        endPath = h.endPath;
                        endChildIndex = h.endChild;
                        endCharOffset = h.endChar;
                    }
                }
            }

            Highlight prevHighlight=null;
            Highlight newHighlight = new Highlight();

            if( erases.size() > 0 ) mMergedAnnotation = true;

            for(int i = 0; i < erases.size(); i++) {
                if( erases.get(i) != prevHighlight ) {
                    if( !isMergedMemo && erases.get(i).isMemo() ) { // TODO :: 수정되는 메모는 이미 머지된 상태라
                        memoText += erases.get(i).memo;
                        if( i < erases.size()-1 )
                            memoText += "\n";
                    }
                    prevHighlight = erases.get(i);
                }

                JSONArray hiLite = new JSONArray();
                hiLite.put(erases.get(i).get2());
                mHighlights.remove(erases.get(i));
                if( BookHelper.useHistory ) {
                    UserBookDataFileManager.__hlHistory.mergeRemove(erases.get(i).uniqueID, newHighlight.uniqueID);
                }
                FixedLayoutWebview.this.loadUrl("javascript:deleteHighlights(" + hiLite.toString() + ")");
            }

            isMergedMemo = false;

            if( isMemo && memoText.length() > 0 ) {
                memoText=memoText.replace("'","\\'");
                memoText += "\n";
            }

            newHighlight.highlightID = hID;
            newHighlight.startPath = startPath;
            newHighlight.startChild = startChildIndex;
            newHighlight.startChar = startCharOffset;
            newHighlight.endPath = endPath;
            newHighlight.endChild = endChildIndex;
            newHighlight.endChar = endCharOffset;
            newHighlight.colorIndex = colorIndex;
            newHighlight.memo = memoText;
            newHighlight.deviceModel = DeviceInfoUtil.getDeviceModel();
            newHighlight.osVersion = DeviceInfoUtil.getOSVersion();

            highlightAnnotation(newHighlight);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void highlightAnnotation(Highlight highlight){  // NOTI : do after merging

        ArrayList<String> array = new ArrayList<>();

        String current = currentPageData.getContentsFilePath();
        String chapterId = "";
        String src = UserBookDataFileManager.getChapter().getCurrentChapter().getChapterFilePath();

        if( src.lastIndexOf("#") != -1 ){
            chapterId = src.substring(src.lastIndexOf("#")+1);
        }
        if( chapterId != null && current.equals(src) ) {
            array.add(chapterId);
        }

        int colorIndex = highlight.colorIndex;

        String script = (new StringBuilder())
                .append("javascript:highlightText(")
                .append("'"+highlight.uniqueID+"'").append(",")
                .append("'" + highlight.startPath + "'").append(",")
                .append("'" + highlight.endPath + "'" ).append(",")
                .append(highlight.startChar).append(",")
                .append(highlight.endChar).append(",")
                .append("'"+highlight.highlightID+"'").append(",")
                .append(colorIndex).append(",")
                .append(array.toString()).append(",'")
                .append(highlight.memo).append("'")
                .append(")").toString();

        DebugSet.d(TAG, "script >>>>>>>>>>>>> " + script);

        FixedLayoutWebview.this.loadUrl(script);

//        if( highlight.memo.length()>0 ) {
//            BookHelper.lastMemoHighlightColor = colorIndex;
//        } else {
            BookHelper.lastHighlightColor = colorIndex;
//        }

        finishTextSelectionMode();
    }

    public void startTextSelection(int x, int y){
        DebugSet.d(TAG,"startTextSelection in mTextSelectionMode : "+mTextSelectionMode);

        if(mTextSelectionMode){
            finishTextSelectionMode();
            return;
        }

        mTextSelectionMode = true;

        mWebviewCallbackListener.setTextSelectionMode(mTextSelectionMode);

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);
        loadUrl("javascript:setStartSelectionRange(" + wx + "," + wy + ")");
    }

    public void setMoveRange(int x, int y, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted, float distX, float distY){
        DebugSet.d(TAG,"setMoveRange in mTextSelectionMode : "+mTextSelectionMode);

        if(!mTextSelectionMode) {
            return;
        }

        hideAnnotationMenu();

        if(isLongPressStarted) {
            int wx = Scr2Web(x);
            int wy = Scr2Web(y);
            loadUrl("javascript:setMoveRange(" + wx + "," + wy + ")");
        } else {
            if(isStartHandlerTouched || isEndHandlerTouched) {
//                int wx = Scr2Web(x)+mWebviewCallbackListener.requestDiffX();
//                int wy = Scr2Web(y)+mWebviewCallbackListener.requestDiffY();
                int wx = Scr2Web((int) (mWebviewCallbackListener.requestStartSelectionPositionX() + distX));
                int wy = Scr2Web((int) (mWebviewCallbackListener.requestStartSelectionPositionY() + distY));
                loadUrl("javascript:setMoveRangeWithHandler(" + wx + "," + wy + "," + isStartHandlerTouched + "," + isEndHandlerTouched + ")");
            }
        }
    }

    public void setEndRange(boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted){
        DebugSet.d(TAG,"setEndRange in mTextSelectionMode : "+mTextSelectionMode);

        if(!mTextSelectionMode) {
            return;
        }

        hideAnnotationMenu();

        if(isLongPressStarted) {
            loadUrl("javascript:setEndRange(" + BookHelper.lastHighlightColor + ")");
        } else if(!isLongPressStarted && isStartHandlerTouched || isEndHandlerTouched ){
            loadUrl("javascript:setEndRangeWithHandler(" + BookHelper.lastHighlightColor + ")");
        }
    }

    public void findTagUnderPoint(int x, int y, int orgX, int orgY){
        DebugSet.d(TAG,"findTagUnderPoint in mTextSelectionMode : "+mTextSelectionMode);

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);

        boolean isTextSelectionDisabled = mWebviewCallbackListener.requestIsSelectionDisabled();
        loadUrl("javascript:findTagUnderPoint(" + wx + "," + wy + ","+ orgX +","+ orgY +"," +isTextSelectionDisabled+")");
    }

    private Handler mWebviewInnerHandler;
    private class WebviewInnerHandler extends Handler {

        private final WeakReference<FixedLayoutWebview> fixedlayoutWebView;

        public WebviewInnerHandler(FixedLayoutWebview webview) {
            fixedlayoutWebView = new WeakReference<>(webview);
        }

        @Override
        public void handleMessage(Message msg) {
            FixedLayoutWebview webview = fixedlayoutWebView.get();
            if (webview != null)
                webview.handleMessage(msg);
        }
    }

    private void handleMessage(Message msg) {

        switch(msg.what){

            case Defines.FIXEDLAYOUT_REPORT_ERROR : {
                mWebviewCallbackListener.reportError((Integer) msg.obj);
                break;
            }

            case Defines.FIXEDLAYOUT_LINK_CLICK : {
                mWebviewCallbackListener.reportLinkClick((String) msg.obj);
                break;
            }

            case Defines.FIXEDLAYOUT_MEDIAOVERLAY_PAUSE : {
                mWebviewCallbackListener.pauseMediaOverlay();
                break;
            }

            case Defines.FIXEDLAYOUT_PLAY_SELECTED_MEDIAOVERLAY : {
                mWebviewCallbackListener.playSelectedMediaOverlay((String) msg.obj);
                break;
            }

            case Defines.FIXEDLAYOUT_SET_BGM_STATE : {
                mWebviewCallbackListener.setBgmState();
                break;
            }

            case Defines.FIXEDLAYOUT_PREVENT_MEDIA_CONTROL : {
                mWebviewCallbackListener.reportMediaControl((JSONObject)msg.obj);
                break;
            }

            case Defines.FIXEDLAYOUT_REPORT_CURRENT_PAGE_INFO : {

                try {
                    String currentPageInfo = (String) msg.obj;

                    JSONObject object;
                    String elPath = "";
                    String text = "";
                    String tagName = "";
                    String type = AnnotationConst.FLK_BOOKMARK_TYPE_TEXT;

                    if (currentPageInfo != null) {
                        object = new JSONObject(currentPageInfo);
                        elPath = object.getString("elementPath");
                        text = object.getString("elementText");
                        tagName = object.getString("tagName");

                        if (tagName.isEmpty()) {
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_TEXT;
                        } else if (tagName.equalsIgnoreCase(ElementName.AUDIO)) {
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_AUDIO;
                            text = BookHelper.getFilename(text);
                        } else if (tagName.equalsIgnoreCase(ElementName.VIDEO)) {
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_VIDEO;
                            text = BookHelper.getFilename(text);
                        } else if (tagName.equalsIgnoreCase(ElementName.IMG)) {
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_IMG;
                            text = BookHelper.getFilename(text);
                        }
                    }

                    Bookmark bmd = new Bookmark();
                    bmd.path = elPath;
                    bmd.text = text;
                    bmd.type = type;
                    bmd.deviceModel = DeviceInfoUtil.getDeviceModel();
                    bmd.osVersion = DeviceInfoUtil.getOSVersion();
                    DebugSet.d(TAG, "reportCurrentPagePath bookmark :" + bmd.toString());

                    mWebviewCallbackListener.reportCurrentPageInfo(bmd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }

            case Defines.FIXEDLAYOUT_REPORT_TOUCH_POSITION: {
                int[] touchedPosition = (int[]) msg.obj;
                mWebviewCallbackListener.reportTouchPosition(touchedPosition[0], touchedPosition[1]);
                break;
            }

            case Defines.FIXEDLAYOUT_CONTEXT_MENU_SHOW : {
                PopupData pData = (PopupData)msg.obj;
                mWebviewCallbackListener.showContextMenu(pData);
                break;
            }

            case Defines.FIXEDLAYOUT_CHECK_MERGE_ANNOTATION : {
                String jsonObject = (String) msg.obj;
                mergeAnnotation(jsonObject);
                break;
            }

            case Defines.FIXEDLAYOUT_ADD_HIGHLIGHTED_DATA : {
                String highlightedData = (String) msg.obj;
                addHighlightingData(highlightedData);
                break;
            }

            case Defines.FIXEDLAYOUT_DRAW_SELECTION_RECT : {
                String currentSelectedRectList = (String)msg.obj;
                drawSelectionRect(currentSelectedRectList);
                break;
            }

            case Defines.FIXEDLAYOUT_MERGE_ALL_MEMO : {
                String annotationIdList = (String)msg.obj;
                String allMemoText = getAllMemoText(annotationIdList);
                mWebviewCallbackListener.setAllMemoText(allMemoText);
                break;
            }

            case Defines.FIXEDLAYOUT_OVERFLOW_MEMO_CONTENT : {
                mWebviewCallbackListener.reportOverflowMemoContent();
                break;
            }


            case Defines.FIXEDLAYOUT_CONTEXT_MENU_HIDE : {
                hideAnnotationMenu();
                break;
            }

            case Defines.FIXEDLAYOUT_FINISH_TEXT_SELECTION : {
                finishTextSelectionMode();
                break;
            }

            case Defines.FIXEDLAYOUT_SET_ASIDEPOPUP_STATUS : {
                mWebviewCallbackListener.reportAsidePopupStatus((Boolean)msg.obj);
                break;
            }

            case Defines.FIXEDLAYOUT_REPORT_VIDEO_INFO : {
                mWebviewCallbackListener.reportVideoInfo((String)msg.obj);
                break;
            }
        }
    }

    public boolean getSelectionMode(){
        return mTextSelectionMode;
    }

    private void drawSelectionRect(String currentSelectedRectList){
        selectionRectList.clear();
        try {
            JSONArray jsonArray = new JSONArray(currentSelectedRectList);
            for (int idx = 0; idx < jsonArray.length(); idx++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(idx);
                if(jsonObject.getInt("width") == 0)
                    continue;
                Rect currentRect = new Rect(Web2Scr(jsonObject.getInt("left")), Web2Scr(jsonObject.getInt("top"))+getScrollY(), Web2Scr(jsonObject.getInt("right")), Web2Scr(jsonObject.getInt("bottom"))+getScrollY());
                selectionRectList.add(currentRect);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FixedLayoutWebview.this.invalidate();
    }

    public void addAnnotation(){
        String script = (new StringBuilder())
                .append("javascript:addAnnotation(")
                .append(BookHelper.lastHighlightColor)
                .append(")").toString();

        loadUrl(script);
    }

    public void addAnnotationWithMemo(String memoContent, boolean modifyMerged){

        isMergedMemo = modifyMerged;

        JSONArray array = new JSONArray();
        array.put(memoContent);
        String script = (new StringBuilder())
                .append("javascript:addAnnotationWithMemo(")
                .append(BookHelper.lastHighlightColor).append(",")
                .append(array.toString())
                .append(")").toString();

        loadUrl(script);
    }

    public void deleteAnnotation(){
        loadUrl("javascript:deleteAnnotationInRange()");
        finishTextSelectionMode();
    }

    public void modifyAnnotationColorAndRange(int colorIndex){
        loadUrl("javascript:modifyAnnotationColorAndRange("+colorIndex+")");
    }

    public void  changeMemoText(String memoId, String currentMemo){
        for(int idx=0; idx<mHighlights.size(); idx++){
            Highlight targetHighlight = mHighlights.get(idx);
            if(targetHighlight.highlightID.equalsIgnoreCase(memoId)){
                targetHighlight.memo = currentMemo;
                if( BookHelper.useHistory ){ //메모 변경도 히스토리에 변경항목으로 추가(동기화를 위함)
                    long newID = System.currentTimeMillis() / 1000L;
                    UserBookDataFileManager.__hlHistory.modifyRemove(targetHighlight.uniqueID, newID);
                    UserBookDataFileManager.__hlHistory.modifyAdd(newID);
                    targetHighlight.uniqueID = newID;
                }
                break;
            }
        }

        saveHighlights();
        finishTextSelectionMode();
    }

    public void requestAllMemoText(){
        loadUrl("javascript:requestAllMemoText()");
    }

    private String getAllMemoText(String annotationIdList){

        String allMemoText = "";

        try {
            JSONArray jsonarray = new JSONArray(annotationIdList);
            String[] idArr = new String[jsonarray.length()];

            for(int idx=0; idx<jsonarray.length(); idx++){
                idArr[idx] = ((String) jsonarray.get(idx));
            }

            for(String containAnnotationId : idArr){
                for(Highlight currentHighlight : mHighlights){
                    if(currentHighlight.isMemo() && currentHighlight.highlightID.equalsIgnoreCase(containAnnotationId)){
                        allMemoText += currentHighlight.memo +"\n";
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allMemoText.trim();
    }

    public ArrayList<Rect> getSelectionRectList(){
        if(!selectionRectList.isEmpty()){
            return selectionRectList;
        }
        return null;
    }

    public void handleBackKeyEvent(){
        if(!selectionHandler){
            addAnnotation();
        } else {
            finishTextSelectionMode();
        }
    }

    public void finishTextSelectionMode(){
        hideAnnotationMenu();
        mTextSelectionMode=false;
        mWebviewCallbackListener.setTextSelectionMode(mTextSelectionMode);
        selectionRectList.clear();
        loadUrl("javascript:finishTextSelection()");
        FixedLayoutWebview.this.invalidate();
    }

    private void hideAnnotationMenu(){
        mWebviewCallbackListener.hideContextMenu();
    }

    public interface OnWebviewCallbackInterface {
        void pageLoadFinished(FixedLayoutPageData.ContentsData data);
        void reportVideoInfo(String videoSrc);
        void reportAsidePopupStatus(boolean isAsidePopopShow);
        void reportLinkClick(String href);
        void reportTouchPosition(int x, int y);
        void reportCurrentPageInfo(Bookmark bookmarkInfo);
        void reportOverflowMemoContent();
        void reportError(int errorCode);
        void setBgmState();
        void pauseMediaOverlay();
        void playSelectedMediaOverlay(String json);
        void reportMediaControl(JSONObject jsonObject);
        void setTextSelectionMode(boolean textSelectionMode);
        void setAllMemoText(String allMemoText);
        void setSeletedText(String selectedText);
        void showContextMenu(PopupData popupData);
        void hideContextMenu();
        Drawable requestStartHandlerImage();
        Drawable requestEndHandlerImage();
        boolean requestIsSelectionDisabled();
        int requestStartSelectionPositionX();
        int requestStartSelectionPositionY();
        int[] requestContextMenuInfo();
        void reportMergedAnnotationSelection();
        void reportMergedAnnotationQuick();
        void reportAnnotationQuick();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.scrollTo(l, 0);
    }
}