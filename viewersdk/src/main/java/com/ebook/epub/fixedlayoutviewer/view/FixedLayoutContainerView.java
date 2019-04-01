package com.ebook.epub.fixedlayoutviewer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ebook.bgm.BGMPlayer;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.tts.Highlighter;
import com.ebook.tts.TTSDataInfoManager;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;

public class FixedLayoutContainerView extends LinearLayout {

    private String TAG = "FixedLayoutContainerView";

    private ArrayList<FixedLayoutWebview>	mWebviewList;
    private ArrayList<ProgressBar> 			mProgressBarList;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private FixedLayoutPageData mPageData;

    private ViewerContainer.PageDirection mPageDirection;

    private boolean mIsLoadEmpty;

    private boolean isTwoPageMode = false;


    public FixedLayoutContainerView(Context context, int width, int height, FixedLayoutPageData pageData, boolean isTwoPageMode, ViewerContainer.PageDirection pageDirection, boolean isLoadEmpty) {    // org
        super(context);

        viewWidth = width;
        viewHeight = height;

        mPageData = pageData;

        mWebviewList = new ArrayList<>();
        mProgressBarList = new ArrayList<>();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewWidth, viewHeight);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
        setBackgroundColor(Color.WHITE);

        this.isTwoPageMode = isTwoPageMode;

        mPageDirection = pageDirection;

        mIsLoadEmpty = isLoadEmpty;

        init(context);
    }

    private void init(Context context) {

        LayoutParams params;

        int start = 0;
        int end = mPageData.getContentsCount();
        int n = 1;

        if( isTwoPageMode && mPageData.getContentsDataList().get(0).getContentsPosition() == 1 ){
            start = mPageData.getContentsCount() - 1;
            end = -1;
            n = -1;
        }

        for( int i = start ; i != end; i+=n ) {

            FixedLayoutPageData.ContentsData data = mPageData.getContentsDataList().get(i);
            params = new LayoutParams(data.getContentsWidth(), data.getContentsHeight());
            params.gravity = Gravity.CENTER;
            params.weight = 1;

            FrameLayout layout = new FrameLayout(getContext());
            layout.setLayoutParams(params);
            layout.setBackgroundColor(Color.WHITE);

            String fileName = "file://" + replaceString(data.getContentsFilePath());
            if( data.getContentsFilePath().equals("about:blank") )
                fileName = data.getContentsFilePath();

            FixedLayoutWebview webview = new FixedLayoutWebview(context);
            params.gravity = Gravity.CENTER;
            webview.setLayoutParams(params);
            webview.setTag(data);
            webview.setInitialScale((int)(data.getContentsInitalScale()*100));
            webview.setScale(data.getContentsInitalScale());
            webview.setVisibility(View.INVISIBLE);
            webview.setCurrentPageData(data);

            String mimeType = "";
            if(data.getContentsFilePath().indexOf(".xhtml")!=-1){
                mimeType = "application/xhtml+xml";
            } else {
                mimeType = "text/html";
            }

            if(!mIsLoadEmpty){
                webview.loadDataWithBaseURL(fileName, data.getContentsString(), mimeType, "UTF-8", null);
            }

            webview.setOnPageLoadListener(new FixedLayoutWebview.OnPageLoad() {

                @Override
                public void onPageLoadFinished(WebView view) {
                    int position = ((FixedLayoutPageData.ContentsData)view.getTag()).getContentsPosition();
                    view.setVisibility(View.VISIBLE);
                    mProgressBarList.get(position).setVisibility(View.INVISIBLE);
                }
            });

            mWebviewList.add(webview);

            ProgressBar progress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
            FrameLayout.LayoutParams Fparams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            Fparams.gravity = Gravity.CENTER;
            progress.setLayoutParams(Fparams);
            progress.setVisibility(View.VISIBLE);
            mProgressBarList.add(progress);

            layout.addView(webview);
            layout.addView(progress);
            addView(layout);
        }
    }

    public void loadEmptyPage(){
        for( int i = 0; i < mWebviewList.size(); i++ ){
            mWebviewList.get(i).loadDataWithBaseURL("about:blank", "", "text/html", "UTF-8", null);
        }
    }

    public void loadContent(FixedLayoutPageData pageData){
        for(int idx=0; idx<mWebviewList.size(); idx++){
            FixedLayoutPageData.ContentsData data = pageData.getContentsDataList().get(idx);
            String fileName = "file://" + replaceString(data.getContentsFilePath());
            String mimeType = "";
            if(data.getContentsFilePath().indexOf(".xhtml")!=-1){
                mimeType = "application/xhtml+xml";
            } else {
                mimeType = "text/html";
            }
            mWebviewList.get(idx).setCurrentPageData(data);
            mWebviewList.get(idx).loadDataWithBaseURL(fileName, data.getContentsString(), mimeType, "UTF-8", null);
        }
    }

    //특정문자 제거 하기
    private String replaceString(String str){
        //    	str = str.replaceAll("[/% ]",""); // "/", "%", " "을 제거한 문자열 전달
        str = URLEncoder.encode(str);

        str = str.replaceAll("\\+", " ");
        str = str.replaceAll("%2F", "/");
        return str;
    }

    public void setWebviewCallbackListener(FixedLayoutWebview.OnWebviewCallbackInterface listener){
        for( int i = 0; i < mWebviewList.size(); i++ ){
            mWebviewList.get(i).setWebviewCallbackListener(listener);
        }
    }


    public void getCurrentPath(){   //	[ssin-bookmark] s : 1page<->2page 정책 적용
        int position = 0;

        if (isTwoPageMode) {
            if(mPageDirection == ViewerContainer.PageDirection.RTL){
                if(mPageData.getContentsDataList().get(1).getContentsFilePath().equalsIgnoreCase("about:blank")){
                    position = 0;
                } else{
                    position = 1;
                }

            } else if(mPageDirection == ViewerContainer.PageDirection.LTR){
                if(mPageData.getContentsDataList().get(0).getContentsFilePath().equalsIgnoreCase("about:blank")){
                    position = 1;
                } else {
                    position = 0;
                }
            }
        } else {
            position = 0;
        }

        mWebviewList.get(position).loadUrl("javascript:getCurrentPagePath()");
    }

    public void focusSearchText(String keyword, int index, int pagePosition){
        mWebviewList.get(pagePosition).loadUrl("javascript:searchTextByKeywordIndex('"+ keyword + "', " + index + ")");
        mWebviewList.get(pagePosition).scrollToSearch();
    }

    public void removeSearchHighlight(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.loadUrl("javascript:removeSearchHighlight()");
        }
    }

    public void stopAllMedia(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.loadUrl("javascript:stopAllMedia()");
        }
    }

    public void deleteHighlight(Highlight hign) {
        for(FixedLayoutWebview webview : mWebviewList){
            webview.deleteHighlight(hign);
        }
    }

    public boolean saveHighlights() {
        return mWebviewList.get(getCurrentWebviewPosition()).saveHighlights();
    }

    public void applyAllHighlight(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.applyAllHighlight();
        }
    }
    public void applyCurrentChapterHighlight(int position){
        mWebviewList.get(position).applyAllHighlight();
    }

    public void deleteAllHighlight(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.deleteAllHighlight();
        }
    }

    public void scrollToAnnotationId(String id){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.scrollToAnnotationId(id);
        }
    }

    public void removeCommentNode(int position){
        mWebviewList.get(position).removeCommentNode();
    }

    private int getCurrentWebviewPosition(){
        int position = 0;
        if( isTwoPageMode && mPageData.getContentsDataList().get(0).getContentsPosition() == 1 ){
            if( mPageData.getContentsDataList().get(1).getContentsFilePath().equals("about:blank") ){
                position = 1;
            }else{
                position = 0;
            }
        } else {
            if( mPageData.getContentsDataList().get(0).getContentsFilePath().equals("about:blank") ){
                position = 1;
            }else{
                position = 0;
            }
        }
        return position;
    }

    public FixedLayoutWebview getCurrentWebView(String filePath){
        for(FixedLayoutWebview webview : mWebviewList){
            if(filePath.equalsIgnoreCase(webview.getCurrentPageData().getContentsFilePath())){
                return webview;
            }
        }
        return mWebviewList.get(0);
    }

    public FixedLayoutWebview getLeftWebView(){
        return mWebviewList.get(0);
    }

    public FixedLayoutWebview getRightWebView(){
        return isTwoPageMode == true ? mWebviewList.get(1) : null;
    }

    public void setJSInterface(TTSDataInfoManager manager, Highlighter highlighter, MediaOverlayController mediaoverlayController){
        for(int idx=0; idx<mWebviewList.size(); idx++){
            mWebviewList.get(idx).setJSInterface(idx, manager, highlighter, mediaoverlayController);
        }
    }

    public void setTTSHighlightRect(JSONArray rectArray, String filePath) {
        removeTTSHighlightRect();
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getCurrentPageData().getContentsFilePath().equalsIgnoreCase(filePath)){
                webview.setTTSHighlightRect(rectArray);
            }
        }
    }
    public void removeTTSHighlightRect() {
        for(FixedLayoutWebview webview : mWebviewList){
            webview.removeTTSHighlightRect();
        }
    }

    public void setPreventMediaControl(boolean isPrevent) {
        for(FixedLayoutWebview webview : mWebviewList){
            webview.setPreventMediaControl(isPrevent);
        }
    }

    public void getIDListByPoint(int x, int y){
        for(FixedLayoutWebview webview : mWebviewList){
            if(isTouchInView(webview,x,y)){
                int[] touchedPosition = convertWebviewPosition(x, y, (FixedLayoutZoomView)getParent(), webview);
                webview.getIDListByPoint(touchedPosition[0],touchedPosition[1]);
            }
        }
    }

    public void setCurrentWebView(BGMPlayer bgmPlayer){
        if(isTwoPageMode){
            bgmPlayer.setCurrentWebView(mWebviewList.get(0), mWebviewList.get(1));
        } else{
            bgmPlayer.setCurrentWebView(mWebviewList.get(0), null);
        }
    }

    public void hideNoteref(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.loadUrl("javascript:hideNoteref()");
        }
    }

    public void setPreventNoteref(boolean isPrevent){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.loadUrl("javascript:setPreventNoteref("+isPrevent+")");
        }
    }

    public void onClose(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.loadDataWithBaseURL("about:blank", "", "text/html", "UTF-8", null);
        }
    }

    public String getCurrentUserAgent() {
        if(mWebviewList!=null && mWebviewList.get(0)!=null)
            return mWebviewList.get(0).getCurrentUserAgent();
        return "";
    }

    private boolean isTouchInView(View view, int x, int y) {
        Rect hitBox = new Rect();
        view.getGlobalVisibleRect(hitBox);
        return hitBox.contains( x, y);
    }

    public void setMoveRange(int x, int y, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted, float distX, float distY){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(isTouchInView(webview,x,y)){
                int[] touchedPosition = convertWebviewPosition(x,y,(FixedLayoutZoomView)getParent(), webview);
                webview.setMoveRange(touchedPosition[0],touchedPosition[1], isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted, distX, distY);
            }
        }
    }

    public void setEndRange(int x, int y, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(webview.getSelectionMode()) { //isTouchInView(webview,x,y)
                int[] touchedPosition = convertWebviewPosition(x,y,(FixedLayoutZoomView)getParent(), webview);
                webview.setEndRange(touchedPosition[0],touchedPosition[1], isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted);
            }
        }
    }

    public void showLastContextMenu(){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(webview.getSelectionMode()){
                webview.showLastContextMenu();
            }
        }
    }

    public void startTextSelection(int x, int y){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(isTouchInView(webview,x,y)){
                int[] touchedPosition = convertWebviewPosition(x, y, (FixedLayoutZoomView)getParent(), webview);
                webview.startTextSelection(touchedPosition[0],touchedPosition[1]);
            }
        }
    }

    public void findTagUnderPoint(int x, int y){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(isTouchInView(webview,x,y)){
                int[] touchedPosition = convertWebviewPosition(x, y,(FixedLayoutZoomView)getParent(), webview);
                webview.findTagUnderPoint(touchedPosition[0],touchedPosition[1],x,y);
            }
        }
    }

    private int[] convertWebviewPosition(int x, int y, FixedLayoutZoomView parentView, FixedLayoutWebview targetWebView){
        int[] locationParent = new int[2];
        int[] locationWebview = new int[2];
        parentView.getLocationOnScreen(locationParent);
        targetWebView.getLocationOnScreen(locationWebview);
        int newX = x +(locationParent[0]-locationWebview[0]);
        int newY = y +(locationParent[1]-locationWebview[1]);
        int[] touchedPosition = new int[3];
        touchedPosition[0] = newX;
        touchedPosition[1] = newY;
        touchedPosition[2] = targetWebView.getCurrentPageData().getContentsPosition();
        return touchedPosition;
    }

    public void revertWebviewPosition(int x, int y, FixedLayoutZoomView parentView, FixedLayoutWebview targetWebView){
        int[] locationParent = new int[2];
        int[] locationWebview = new int[2];
        parentView.getLocationOnScreen(locationParent);
        targetWebView.getLocationOnScreen(locationWebview);
        int orgX = x +(locationWebview[0]-locationParent[0]);
        int orgY = y +(locationWebview[1]-locationParent[1]);
        int[] touchedPosition = new int[2];
        touchedPosition[0] = orgX;
        touchedPosition[1] = orgY;
    }

    public int[] getConvertWebviewPosition(int x, int y){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(isTouchInView(webview,x,y)){
                return convertWebviewPosition(x, y,(FixedLayoutZoomView)getParent(), webview);
            }
        }
        return null;
    }

    public void addAnnotation(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                webview.addAnnotation();
            }
        }
    }

    public void addAnnotationWithMemo(String memoContent, boolean modifyMerged){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                webview.addAnnotationWithMemo(memoContent,modifyMerged);
            }
        }
    }

    public void requestAllMemoText(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                webview.requestAllMemoText();
            }
        }
    }

    public void deleteAnnotation(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                webview.deleteAnnotation();
            }
        }
    }

    public void modifyAnnotationColorAndRange(int colorIndex){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                webview.modifyAnnotationColorAndRange(colorIndex);
            }
        }
    }

    public void  changeMemoText(String memoId, String currentMemo){
        for(FixedLayoutWebview webview : mWebviewList){

            if(webview.getCurrentPageData().getContentsString()==null)
                continue;

            if(webview.getSelectionMode()){
                webview.changeMemoText(memoId, currentMemo);
            }
        }
    }

    public ArrayList<Rect> getSelectionRectList(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                return webview.getSelectionRectList();
            }
        }
        return null;
    }

    public void handleBackKeyEvent(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()) {
                webview.handleBackKeyEvent();
            }
        }
    }

    public void finishTextSelectionMode(){
        for(FixedLayoutWebview webview : mWebviewList){
            webview.finishTextSelectionMode();
        }
    }

    public int getTouchedWebviewPosition(){
        for(FixedLayoutWebview webview : mWebviewList){
            if(webview.getSelectionMode()){
                int currentWebviewPosition = ((FixedLayoutPageData.ContentsData)webview.getTag()).getContentsPosition();
                return currentWebviewPosition;
            }
        }
        return -1;
    }

    public int[] getContextMenuTargetViewPosition(int currentContentsPosition){
        int[] locationParent = new int[2];
        int[] locationWebview = new int[2];
        ((FixedLayoutZoomView)getParent()).getLocationOnScreen(locationParent);
        (mWebviewList.get(currentContentsPosition)).getLocationOnScreen(locationWebview);
        int leftOffset = (locationWebview[0] - locationParent[0]);
        int topOffset = (locationWebview[1] - locationParent[1]);
        int[] currentWebviewOffset = new int[2];
        currentWebviewOffset[0] = leftOffset;
        currentWebviewOffset[1] = topOffset;
        return currentWebviewOffset;
    }
}

