package com.ebook.epub.fixedlayoutviewer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.ebook.bgm.BGMPlayer;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData.ContentsData;
import com.ebook.epub.fixedlayoutviewer.manager.HtmlContentsManager;
import com.ebook.epub.fixedlayoutviewer.view.FixedLayoutScrollView.PageMode;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.tts.Highlighter;
import com.ebook.tts.TTSDataInfoManager;

import org.json.JSONArray;

import java.util.ArrayList;

public class FixedLayoutZoomView extends LinearLayout {

    private String TAG = "FixedLayoutZoomView";

    private FixedLayoutContainerView mFixedLayoutContainerView=null;

    private boolean isTwoPageMode = false;

    private int mDisplayWidth = 0;
    private int mDisplayHeight = 0;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private float mLastTouchX;
    private float mLastTouchY;

    private final float MAXSCALE = 2.5f;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;

    private Context mContext;
    private FixedLayoutPageData mPageData;
    private PageMode mPageMode;
    private int pageCount = 0;

    private HtmlContentsManager mContentsManager;

    private boolean mIsLoadEmpty = true;

    private ViewerContainer.PageDirection mPageDirection;

    private TouchAreaChecker mTouchAreaChecker;

    private boolean isFromLeft = false;
    private boolean isFromRight = false;
    private boolean isLeftEnd = false;
    private boolean isRightEnd = false;

//    private static final int SWIPE_MIN_DISTANCE = 120;
//    private static final int SWIPE_MAX_OFF_PATH = 350;
    private static final int SWIPE_THRESHOLD_VELOCITY = 5000;

    public interface TouchAreaChecker {
        void onLeftTouched(float x, float y);
        void onRightTouched(float x, float y);
        void onMiddleTouched(float x, float y);
        void onLeftCornerTouched();
    }

    public FixedLayoutZoomView(Context context, FixedLayoutPageData pageData, PageMode pageMode, ViewerContainer.PageDirection pageDirection, boolean isLoadEmpty){     // org
        super(context);

        mIsLoadEmpty = isLoadEmpty;
        mContext = context;
        mPageData = pageData;
        mPageMode = pageMode;
        mPageDirection = pageDirection;
        mContentsManager = new HtmlContentsManager();
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        initData();

        initView();
    }

    private void initData(){

        if( mPageMode == PageMode.OnePage ){
            isTwoPageMode = false;
            pageCount = 1;
        }
        else {
            isTwoPageMode = true;
            pageCount = 2;
        }

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        float fontScale = mContext.getResources().getConfiguration().fontScale;
        float density = mContext.getResources().getDisplayMetrics().density;
        float densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
        float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;

        float widthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        float heightPixels = mContext.getResources().getDisplayMetrics().heightPixels;

        mDisplayWidth = (int) (widthPixels/ pageCount);
        mDisplayHeight = (int) heightPixels;

        //기 세팅된 경우 계산하지 않음.
        if( mPageData.getContentsDataList().get(0).getContentsWidth() != -1 ){
            for( int i=0; i<mPageData.getContentsCount(); i++ ){
                ContentsData contents = mPageData.getContentsDataList().get(i);

                viewWidth += contents.getContentsWidth();
                viewHeight = Math.max(viewHeight, contents.getContentsHeight());
            }
        } else {
            for( int i = 0 ; i < mPageData.getContentsCount(); i++ ){
                String viewport = getViewPortSize(mPageData.getContentsDataList().get(i).getContentsFilePath(), mPageData.getContentsDataList().get(i).getContentsString());	// decode된 컨텐츠 string 으로 param 수정
                int contentWidth = Integer.parseInt(viewport.split(",")[0]);
                int contentHeight = Integer.parseInt(viewport.split(",")[1]);

                //크기의 차이가 큰 쪽 비율을 맞춘다.
                float widthScale = (float)mDisplayWidth/(float)contentWidth;
                float heightScale = (float)mDisplayHeight/(float)contentHeight;
//				if( (contentWidth - mDisplayWidth) > (contentHeight - mDisplayHeight) ){
                if(widthScale <= heightScale) {
                    int webViewHeight = (int) Math.ceil((double)(mDisplayWidth) * contentHeight / contentWidth);
                    mPageData.getContentsDataList().get(i).setContentsWidth(mDisplayWidth);
                    mPageData.getContentsDataList().get(i).setContentsHeight(webViewHeight);
                    mPageData.getContentsDataList().get(i).setContentsInitalScale(widthScale);
                } else {
                    int webViewWidth = (int) Math.ceil((double)(mDisplayHeight) * contentWidth / contentHeight);
                    mPageData.getContentsDataList().get(i).setContentsWidth(webViewWidth);
                    mPageData.getContentsDataList().get(i).setContentsHeight(mDisplayHeight);
                    mPageData.getContentsDataList().get(i).setContentsInitalScale(heightScale);
                }
            }

            if( isTwoPageMode ){
                if( mPageData.getContentsDataList().get(0).getContentsFilePath().equals("about:blank") ){
                    mPageData.getContentsDataList().get(0).setContentsWidth(mPageData.getContentsDataList().get(1).getContentsWidth());
                    mPageData.getContentsDataList().get(0).setContentsHeight(mPageData.getContentsDataList().get(1).getContentsHeight());
                } else if( mPageData.getContentsDataList().get(1).getContentsFilePath().equals("about:blank") ) {
                    mPageData.getContentsDataList().get(1).setContentsWidth(mPageData.getContentsDataList().get(0).getContentsWidth());
                    mPageData.getContentsDataList().get(1).setContentsHeight(mPageData.getContentsDataList().get(0).getContentsHeight());
                }
            }

            for( int i=0; i<mPageData.getContentsCount(); i++ ){
                ContentsData contents = mPageData.getContentsDataList().get(i);
                viewWidth += contents.getContentsWidth();
                viewHeight = Math.max(viewHeight, contents.getContentsHeight());
            }
        }
    }

    public void initView(){
        mFixedLayoutContainerView = new FixedLayoutContainerView(mContext, viewWidth, viewHeight, mPageData, isTwoPageMode, mPageDirection, mIsLoadEmpty);
        addView(mFixedLayoutContainerView);
        scrollTo(-(mDisplayWidth*pageCount-viewWidth)/2, -(mDisplayHeight - viewHeight)/2);
    }

    public void loadContent(FixedLayoutPageData pageData){
        mFixedLayoutContainerView.loadContent(pageData);
    }

    public void loadEmptyPage(){
        mFixedLayoutContainerView.loadEmptyPage();
    }

    public void setTouchAreaCheckInterface(TouchAreaChecker checker) {
        mTouchAreaChecker = checker;
    }

    public void setWebviewCallbackListener(FixedLayoutWebview.OnWebviewCallbackInterface listener){
        mFixedLayoutContainerView.setWebviewCallbackListener(listener);
    }

    public void getCurrentPath(){
        mFixedLayoutContainerView.getCurrentPath();
    }

    public void focusSearchText(String keyword, int index, int pagePosition){
        mFixedLayoutContainerView.focusSearchText(keyword, index, pagePosition );
    }

    public void removeSearchHighlight(){
        mFixedLayoutContainerView.removeSearchHighlight();
    }

    public void stopAllMedia(){
        mFixedLayoutContainerView.stopAllMedia();
    }

    private String getViewPortSize(String filePath, String decodeStr){
        if( filePath.equals("about:blank") )
            return mDisplayWidth + ",1";

        String viewPort = mContentsManager.getViewPort(decodeStr);
        int width;
        int height;
        if( viewPort == null ){
            width = mDisplayWidth;
            height = mDisplayHeight;
        } else {
            String[] viewPortValue = viewPort.split(",");
            width = Integer.parseInt(viewPortValue[0].split("=")[1].trim());
            height = Integer.parseInt(viewPortValue[1].split("=")[1].trim());
        }

        return width+","+height;
    }

    public float getCurrentScale(){
        return mScaleFactor;
    }

    public boolean getIsFromLeft() {
        return isFromLeft;
    }

    public void setIsFromLeft(boolean isFromLeft) {
        this.isFromLeft = isFromLeft;
    }

    public boolean getIsFromRight() {
        return isFromRight;
    }

    public void setIsFromRight(boolean isFromRight) {
        this.isFromRight = isFromRight;
    }

    public void onDoubleTab(MotionEvent e){

        float scrollX = 0;
        float scrollY = getHeight() / 2;

        if( mScaleFactor > 1.0f ){
            mScaleFactor = 1.0f;
        } else {
            if( isTwoPageMode ){
                int width = 0;
                int height = 0;
                if(e!=null){
                    if( e.getX() < mDisplayWidth ){
                        width = mPageData.getContentsDataList().get(0).getContentsWidth();
                        height = mPageData.getContentsDataList().get(0).getContentsHeight();
                    } else {
                        width = mPageData.getContentsDataList().get(1).getContentsWidth();
                        height = mPageData.getContentsDataList().get(1).getContentsHeight();
                    }
                    if( (mDisplayWidth*2 - width) < (mDisplayHeight - height) ){
                        mScaleFactor = (float)(mDisplayWidth*2) / (float)width;
                    } else {
                        mScaleFactor = (float)mDisplayHeight / (float)height;
                    }

                    if( e.getX() > mDisplayWidth )
                        scrollX = mDisplayWidth * pageCount;

                } else{
                    if(isFromLeft){
                        isFromLeft = false;
                        width = mPageData.getContentsDataList().get(0).getContentsWidth();
                        height = mPageData.getContentsDataList().get(0).getContentsHeight();
                    } else if(isFromRight){
                        isFromRight = false;
                        width = mPageData.getContentsDataList().get(1).getContentsWidth();
                        height = mPageData.getContentsDataList().get(1).getContentsHeight();
                        scrollX = mDisplayWidth * pageCount;
                    }
                    if( (mDisplayWidth*2 - width) < (mDisplayHeight - height) ){
                        mScaleFactor = (float)(mDisplayWidth*2) / (float)width;
                    } else {
                        mScaleFactor = (float)mDisplayHeight / (float)height;
                    }
                }
            } else {
                scrollX = e.getX();
                scrollY = e.getY();
                mScaleFactor = MAXSCALE;
            }
        }
        mFixedLayoutContainerView.setPivotX(getScrollX() + scrollX);
        mFixedLayoutContainerView.setPivotY(getScrollY() + scrollY);
        mFixedLayoutContainerView.setScaleX(mScaleFactor);
        mFixedLayoutContainerView.setScaleY(mScaleFactor);
    }

    private boolean isTextSelectionMode = false;
    private boolean isStartHandlerTouched = false;
    private boolean isEndHandlerTouched = false;
    private boolean isLongPressStarted=false;
    public void setCurrentTouchCondition(boolean isLongPressStarted, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isTextSelectionMode){
        this.isLongPressStarted = isLongPressStarted;
        this.isTextSelectionMode = isTextSelectionMode;
        this.isStartHandlerTouched = isStartHandlerTouched;
        this.isEndHandlerTouched = isEndHandlerTouched;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try{
            if(!isTextSelectionMode) {
                mScaleDetector.onTouchEvent(ev);
            }

            switch (ev.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    mLastTouchX = ev.getX();
                    mLastTouchY = ev.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(isTextSelectionMode) {
                        int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();    // TODO :: 텍스트 아주 작은 콘텐츠에서 slop 조정 필요
                        boolean userMoveConfirmed = Math.abs(mLastTouchX - ev.getX()) > slop || Math.abs(mLastTouchY -  ev.getY()) > slop;
                        if(userMoveConfirmed){
                            float moveDistx = ev.getX() - mLastTouchX;
                            float moveDisty = ev.getY() - mLastTouchY;
                            setMoveRange((int) ev.getX(), (int) ev.getY(), isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted, moveDistx, moveDisty);
                        }
                    } else {
                        float dx = mLastTouchX - ev.getX();
                        float dy = mLastTouchY - ev.getY();

                        mLastTouchX = ev.getX();
                        mLastTouchY = ev.getY();

                        float px = mFixedLayoutContainerView.getPivotX();
                        float py = mFixedLayoutContainerView.getPivotY();

                        if( ev.getPointerCount() < 2 && !mScaleDetector.isInProgress() ){
                            px = mFixedLayoutContainerView.getPivotX() + dx;
                            py = mFixedLayoutContainerView.getPivotY() + dy;
                        }

                        if( px < getScrollX()){
                            px = getScrollX();
                            isLeftEnd=true;
                            isRightEnd=false;
                        } else if( px > getScrollX() + getWidth()){
                            px = getScrollX() + getWidth();
                            isRightEnd=true;
                            isLeftEnd=false;
                        }

                        if( py < getScrollY()){
                            py = getScrollY();
                        } else if( py > getScrollY() + getHeight()){
                            py = getScrollY() + getHeight();
                        }
                        mFixedLayoutContainerView.setPivotX(px);
                        mFixedLayoutContainerView.setPivotY(py);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if(isTextSelectionMode) {
                        isTextSelectionMode = false;
                        setEndRange(isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted);
                    }
                    return true;

                case MotionEvent.ACTION_POINTER_DOWN:
                    PointF p = new PointF();
                    midPoint(p, ev);
                    mFixedLayoutContainerView.setPivotX(getScrollX() + p.x);
                    mFixedLayoutContainerView.setPivotY(getScrollY() + p.y);
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
            return true;
        } catch( IllegalArgumentException e ){
            DebugSet.d(TAG, "onTouchEvent IllegalArgumentException : " + e.getMessage());
            return true;
        }
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();

            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, MAXSCALE));

            mFixedLayoutContainerView.setScaleX(mScaleFactor);
            mFixedLayoutContainerView.setScaleY(mScaleFactor);

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean saveHighlights() {
        return mFixedLayoutContainerView.saveHighlights();
    }

    public void applyAllHighlight(){
        mFixedLayoutContainerView.applyAllHighlight();
    }

    public void applyCurrentChapterHighlight(int position){
        mFixedLayoutContainerView.applyCurrentChapterHighlight(position);
    }

    public void deleteAllHighlight(){
        mFixedLayoutContainerView.deleteAllHighlight();
    }

    public void scrollToAnnotationId(String id){
        mFixedLayoutContainerView.scrollToAnnotationId(id);
    }

    public void removeCommentNode(int position){
        mFixedLayoutContainerView.removeCommentNode(position);
    }

    public FixedLayoutWebview getCurrentWebView(String filePath){
        return mFixedLayoutContainerView.getCurrentWebView(filePath);
    }

    public FixedLayoutWebview getLeftWebView(){
        return mFixedLayoutContainerView.getLeftWebView();
    }

    public FixedLayoutWebview getRightWebView(){
        return mFixedLayoutContainerView.getRightWebView();
    }

    public void setJSInterface(TTSDataInfoManager manager, Highlighter highlighter, MediaOverlayController mediaoverlayController){
        mFixedLayoutContainerView.setJSInterface(manager, highlighter, mediaoverlayController);
    }

    public void setTTSHighlightRect(JSONArray rectArray, String filePath) {
        mFixedLayoutContainerView.setTTSHighlightRect(rectArray, filePath);
    }

    public void removeTTSHighlightRect() {
        mFixedLayoutContainerView.removeTTSHighlightRect();
    }

    public void getIDListByPoint(int x, int y){
        mFixedLayoutContainerView.getIDListByPoint(x, y);
    }

    public void setPreventMediaControl(boolean isPrevent) {
        mFixedLayoutContainerView.setPreventMediaControl(isPrevent);
    }

    public void setCurrentWebView(BGMPlayer bgmPlayer){
        mFixedLayoutContainerView.setCurrentWebView(bgmPlayer);
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        final float x = e1.getX();
        final float y = e1.getY();

        if ( mScaleFactor != 1f && mTouchAreaChecker!=null){

            if(isLeftEnd && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && velocityX>0){
                isLeftEnd=false;
                isRightEnd=false;
                isFromRight = true;

                mFixedLayoutContainerView.setScaleX(1);
                mFixedLayoutContainerView.setScaleY(1);

                mScaleFactor = 1.0f;
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mTouchAreaChecker.onLeftTouched(x, y);
                    }
                }, 1000);
                return true;
            }

            if(isRightEnd && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && velocityX<0){
                isRightEnd=false;
                isLeftEnd=false;
                isFromLeft=true;

                mFixedLayoutContainerView.setScaleX(1);
                mFixedLayoutContainerView.setScaleY(1);

                mScaleFactor = 1.0f;
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mTouchAreaChecker.onRightTouched(x, y);
                    }
                }, 1000);
                return true;
            }
        }
        return false;
    }

    public void hideNoteref(){
        mFixedLayoutContainerView.hideNoteref();
    }

    public void setPreventNoteref(boolean isPrevent){
        mFixedLayoutContainerView.setPreventNoteref(isPrevent);
    }

    public void onClose(){
        mFixedLayoutContainerView.onClose();
    }

    public String getCurrentUserAgent() {
        return mFixedLayoutContainerView.getCurrentUserAgent();
    }

    private boolean isTouchInView(View view, int x, int y) {
        Rect hitBox = new Rect();
        view.getGlobalVisibleRect(hitBox);
        return hitBox.contains( x, y);
    }

    public void onLongPress(float x, float y){
        if(isTouchInView(mFixedLayoutContainerView,(int)x,(int)y)){
            mFixedLayoutContainerView.startTextSelection((int)x,(int)y);
        }
    }

    public void setMoveRange(int x, int y, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted, float distX, float distY){
//    public void setMoveRange(int x, int y, boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted){
        if(isTouchInView(mFixedLayoutContainerView,x,y)){
            mFixedLayoutContainerView.setMoveRange(x, y, isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted, distX, distY);
        }
    }

    public void setEndRange(boolean isStartHandlerTouched, boolean isEndHandlerTouched, boolean isLongPressStarted){
        mFixedLayoutContainerView.setEndRange(isStartHandlerTouched, isEndHandlerTouched, isLongPressStarted);
    }

    public void findTagUnderPoint(float x, float y){
        if(isTouchInView(mFixedLayoutContainerView,(int)x,(int)y)){
            mFixedLayoutContainerView.findTagUnderPoint((int)x,(int)y);
        } else {
            BookHelper.ClickArea clickArea = BookHelper.getClickArea(this, x,y);
            if(clickArea== BookHelper.ClickArea.Left){
                mTouchAreaChecker.onLeftTouched(x, y);
            } else if(clickArea== BookHelper.ClickArea.Right){
                mTouchAreaChecker.onRightTouched(x, y);
            } else if(clickArea== BookHelper.ClickArea.Middle){
                mTouchAreaChecker.onMiddleTouched(x, y);
            } else if(clickArea== BookHelper.ClickArea.Left_Corner){
                mTouchAreaChecker.onLeftCornerTouched();
            }
        }
    }

    public int[] getConvertWebviewPosition(int x, int y){
        if(isTouchInView(mFixedLayoutContainerView,x,y)){
            return mFixedLayoutContainerView.getConvertWebviewPosition(x,y);
        } else {
            return null;
        }
    }

    public void addAnnotation(){
        mFixedLayoutContainerView.addAnnotation();
    }

    public void addAnnotationWithMemo(String memoContent, boolean modifyMerged){
        mFixedLayoutContainerView.addAnnotationWithMemo(memoContent,modifyMerged);
    }

    public void requestAllMemoText(){
        mFixedLayoutContainerView.requestAllMemoText();
    }

    public void deleteAnnotation(){
        mFixedLayoutContainerView.deleteAnnotation();
    }

    public void modifyAnnotationColorAndRange(int colorIndex){
        mFixedLayoutContainerView.modifyAnnotationColorAndRange(colorIndex);
    }

    public void changeMemoText(String memoId, String currentMemo){
        mFixedLayoutContainerView.changeMemoText(memoId,currentMemo);
    }

    public ArrayList<Rect> getSelectionRectList(){
        return mFixedLayoutContainerView.getSelectionRectList();
    }

    public void handleBackKeyEvent(){
        mFixedLayoutContainerView.handleBackKeyEvent();
    }

    public void finishTextSelectionMode(){
        isTextSelectionMode = false;
        isStartHandlerTouched = false;
        isEndHandlerTouched = false;
        isLongPressStarted=false;
        mFixedLayoutContainerView.finishTextSelectionMode();
    }

    public int getTouchedWebviewPosition(){
        return mFixedLayoutContainerView.getTouchedWebviewPosition();
    }

    public int[] getContextMenuTargetViewPosition(int currentContentsPosition){
        return mFixedLayoutContainerView.getContextMenuTargetViewPosition(currentContentsPosition);
    }
}