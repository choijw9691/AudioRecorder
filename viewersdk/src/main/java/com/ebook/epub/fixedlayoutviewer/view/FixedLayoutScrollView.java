package com.ebook.epub.fixedlayoutviewer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.ebook.bgm.BGMMediaPlayer;
import com.ebook.bgm.BGMPlayer;
import com.ebook.epub.common.Defines;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.fixedlayoutviewer.manager.BookmarkManager;
import com.ebook.epub.fixedlayoutviewer.manager.HighlightManager;
import com.ebook.epub.fixedlayoutviewer.manager.HtmlContentsManager;
import com.ebook.epub.fixedlayoutviewer.manager.SearchManager;
import com.ebook.epub.fixedlayoutviewer.manager.UserBookDataFileManager;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlDCMES;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Bookmark;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.PopupData;
import com.ebook.epub.viewer.SearchResult;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.epub.viewer.data.ReadingChapter;
import com.ebook.epub.viewer.data.ReadingOrderInfo;
import com.ebook.epub.viewer.data.ReadingSpine;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.tts.Highlighter;
import com.ebook.tts.TTSDataInfoManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 @class FixedLayoutScrollView
 @brief FixedLayout content 최상위 class ( ViewPager )
 */
public class FixedLayoutScrollView extends ViewPager implements Runnable, FixedLayoutZoomView.TouchAreaChecker {

    private String TAG = "FixedLayoutScrollView";

    private Context mContext;

    public enum PageMode { OnePage, TwoPage }
    public PageMode mPageMode = PageMode.OnePage;

    private enum CurrentMode { Bookmark, LastPosition, SearchFocus, ViewerOpen, ViewerClose }
    private CurrentMode mCurrentMode;

    private EpubFile mEpubFile;

    private ReadingSpine mReadingSpine;
    private ReadingChapter mReadingChapter;

    private ViewerContainer.PageDirection mPageDirection = ViewerContainer.PageDirection.LTR;

    private Thread mDecodeThread = null;

    private HtmlContentsManager mContentsManager;

    private BookmarkManager mBookmarkManager;
    private SearchManager mSearchManager;
    private UserBookDataFileManager mUserBookDataFileManager;
    private HighlightManager mHighlightManager;

    private TTSDataInfoManager ttsDataInfoManager;              // javascriptInterface target class
    private Highlighter ttsHighlighter;                         // javascriptInterface target class
    private MediaOverlayController mediaOverlayController;      // javascriptInterface target class

    private ArrayList<FixedLayoutPageData> mPageDataList;

    private PagerAdapterClass mPagerAdapter;

    private Handler mViewerHandler;

    private GestureDetector mGestureDetector;

    private boolean isViewerLoadingComplete = false;    //    boolean viewerLoadComplate = false;
    private boolean isViewerClosed = false;
    private boolean isIgnoreDrm = true;
    public boolean pagerAnimation=true;
    private boolean isPageScrolling=false;
    private boolean isTextSelectionMode = false;
//    private boolean useVolumeKey = false;
    private boolean isPreventPageMove = false;
    private boolean asidePopupStatus = false;

    private boolean isLongPressStarted = false;
    private boolean isLeftWebviewLoadFinished=false;
    private boolean isRightWebviewLoadFinished=false;

    private BGMPlayer bgmPlayer;

    private PopupWindow mContextMenu=null;  // 사용X

    private SearchResult mSearchResult;
    private int mSearchPageIndex;

    private String selectedAnnotationId="";

    private BookHelper.ClickArea touchedPositionDuringPlaying;

    private int mCurrentPageIndex=-1;                   // viewPager index
    private int mCurrentPage = 0;                       // content page

    private int targetX;
    private int targetY;

    private boolean isMoveByFling=false;
    private boolean isFromRight = false;
    private boolean isFromLeft = false;

    private boolean isStartHandlerTouched = false;
    private boolean isEndHandlerTouched = false;

    /********************************************************************** s : listener variable and setter */
    private ViewerContainer.OnDecodeContent mOnDecodeContent;
    private ViewerContainer.OnPageScroll mOnPageScroll;
    private ViewerContainer.OnChapterChange mOnChapterChange;
    private ViewerContainer.OnPageBookmark mOnPageBookmark;
    private ViewerContainer.OnSearchResult mOnSearchResult;
    private ViewerContainer.OnViewerState mOnViewerState;
    private ViewerContainer.OnTouchEventListener mOnTouchEventListener;
    private ViewerContainer.OnTagClick mOnTagClick;
    private ViewerContainer.OnCurrentPageInfo mOnCurrentPageInfo;
    private ViewerContainer.OnBookStartEnd mOnBookStartEnd;
    private ViewerContainer.OnContextMenu mOnContextMenu = null;
    public ViewerContainer.OnBGMStateListener mOnBgmStateListener;
    private ViewerContainer.OnNoterefListener mOnNoterefListener =null;
    private ViewerContainer.OnMoveToLinearNoChapterListener mMoveToLinearNoChapter = null;
    private ViewerContainer.OnVideoInfoListener mOnVideoInfoListener =null;
    private ViewerContainer.OnTextSelection mOnTextSelection = null;
    private ViewerContainer.OnMemoSelection mOnMemoSelection = null;
    private ViewerContainer.OnReportError mOnReportError = null;
    private ViewerContainer.OnMediaControlListener mOnMediaControlListener = null;
    private ViewerContainer.OnMediaOverlayStateListener mOnMediaOverlayStateListener;
    private ViewerContainer.OnAnalyticsListener mOnAnalyticsListener;

    public void setOnDecodeContent(ViewerContainer.OnDecodeContent l){
        mOnDecodeContent = l;
    }

    public void setOnPageScroll(ViewerContainer.OnPageScroll l){
        mOnPageScroll = l;
    }

    public void setOnChapterChange(ViewerContainer.OnChapterChange l){
        mOnChapterChange = l;
    }

    public void setOnPageBookmark(ViewerContainer.OnPageBookmark l){
        mOnPageBookmark = l;
    }

    public void setOnSearchResult(ViewerContainer.OnSearchResult l){
        mOnSearchResult = l;
    }

    public void setOnViewerState(ViewerContainer.OnViewerState l){
        mOnViewerState = l;
    }

    public void setOnTagClick(ViewerContainer.OnTagClick l) {
        mOnTagClick = l;
    }

    public void setOnCurrentPageInfo(ViewerContainer.OnCurrentPageInfo l){
        mOnCurrentPageInfo = l;
    }

    public void setOnTouchEventListener(ViewerContainer.OnTouchEventListener listener) {
        mOnTouchEventListener = listener;
    }

    public void setOnSelectionMenu(ViewerContainer.OnContextMenu l) {
        mOnContextMenu = l;
    }

    public void setOnBookStartEnd(ViewerContainer.OnBookStartEnd l) {
        mOnBookStartEnd = l;
    }

    public void setOnBGMStateListener(ViewerContainer.OnBGMStateListener listener){
        mOnBgmStateListener = listener;
    }

    public void setOnNoterefListener(ViewerContainer.OnNoterefListener listener){
        mOnNoterefListener= listener;
    }

    public void setMoveToLinearNochapter(ViewerContainer.OnMoveToLinearNoChapterListener listener){
        mMoveToLinearNoChapter = listener;
    }

    public void setOnVideoInfoListener(ViewerContainer.OnVideoInfoListener listener){
        mOnVideoInfoListener= listener;
    }

    public void setOnTextSelection(ViewerContainer.OnTextSelection l) {
        mOnTextSelection = l;
    }

    public void setOnMemoSelection(ViewerContainer.OnMemoSelection l) {
        mOnMemoSelection = l;
    }

    public void setOnReportError(ViewerContainer.OnReportError l) {
        mOnReportError = l;
    }

    public void setOnMediaControlListener(ViewerContainer.OnMediaControlListener listener) {
        mOnMediaControlListener = listener;
    }

    public void setOnMediaOverlayStateListener(ViewerContainer.OnMediaOverlayStateListener listener) {
        mOnMediaOverlayStateListener = listener;
        mediaOverlayController.setOnMediaOverlayStateListener(mOnMediaOverlayStateListener);
    }

    public void setOnAnalyticsListener(ViewerContainer.OnAnalyticsListener listener){
        mOnAnalyticsListener = listener;
    }
    /********************************************************************** e : event listener variable and setter */

    public FixedLayoutScrollView(Context context) {
        super(context);
    }

    public FixedLayoutScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedLayoutScrollView(Context context, ViewerContainer.PageDirection pageDirection, EpubFile epubFile) throws XmlPackageException, XmlContainerException, EpubFileSystemException {
        super(context);

        mContext = context;

        setBackgroundColor(Color.WHITE);

        mEpubFile = epubFile;
        mContentsManager = new HtmlContentsManager();
        mPageDirection = pageDirection;

        initViewPageMode(mEpubFile.getRenditionSpread());

        if(BookHelper.isPreload) {
            setOffscreenPageLimit(2);
        } else {
            setOffscreenPageLimit(1);
        }

        addOnPageChangeListener(viewPageChangeListener);

        mViewerHandler = new ViewerHandler(this);
        mGestureDetector = new GestureDetector(context, new SimpleGestureListener());
    }

    /********************************************************************** s : before loading */
    private void initViewPageMode(String page){
        if( page == null || page.equals("none") || page == "") {
            mPageMode = PageMode.OnePage;
        } else {
            mPageMode = PageMode.TwoPage;
        }
    }

    public void setChapterInfo(ReadingChapter readingChapter) {
        mReadingChapter = readingChapter;
    }

    public void setSpineInfo(ReadingSpine chapterReader) {

        mReadingSpine = chapterReader;

        setPageData();

        mBookmarkManager = new BookmarkManager(mReadingSpine, mReadingChapter);
        mSearchManager = new SearchManager(mViewerHandler, mReadingSpine);
        mUserBookDataFileManager = new UserBookDataFileManager(mReadingSpine, mReadingChapter);
        mHighlightManager = new HighlightManager();
        mUserBookDataFileManager.setEpubPath(mEpubFile.getEpubPath());
    }

    public void setPageData() {

        UnModifiableArrayList<ReadingOrderInfo> spines = mReadingSpine.getSpineInfos();
        if( spines.size() <= 0 )
            return;

        mPageDataList = new ArrayList<>();

        int page = 0;
        if( mPageMode == PageMode.OnePage ) {
            page = 1;
        } else {
            page = 2;
        }

        int pageCount = (int)Math.ceil((double)(spines.size() - 1) / page) + 1;
        int start = -1;             //리스트 추가 시작점
        int end = -1;               // 리스트 추가 끝 점
        int n = 0;                  //리스트 추가 방향 ( 1 : 순차, -1: 역순 )
        int position = -1;          //2페이지 일 경우 왼쪽부터 추가할지 오른쪽부터 추가할지에 대한 값 (left : 0, right : 1)

        int spineStart = 0;
        int spineEnd = spines.size();

        //pageDirection에 따른 시작,끝 점 초기화
        if( mPageDirection == ViewerContainer.PageDirection.LTR ){
            start = 0;
            end = pageCount;
            n = 1;
            position = 0;
        } else if( mPageDirection == ViewerContainer.PageDirection.RTL ){
            start = pageCount - 1;
            end = -1;
            n = -1;
            position = 1;
        }

        int i = start;

        if( mPageMode == PageMode.OnePage ) {
            while(i != end){
                FixedLayoutPageData pageData = new FixedLayoutPageData();
                pageData.addContentsList(spines.get(i).getSpinePath(), -1, -1, 0, i+1, i);
                mPageDataList.add(pageData);
                i += n;
            }
        } else {
            //미리 size만큼 셋팅 (RTL을 위함)
            for( int k = 0; k < pageCount; k++ ){
                mPageDataList.add(new FixedLayoutPageData());
            }
            int j = spineStart;
            while(i != end){
                FixedLayoutPageData pageData = new FixedLayoutPageData();
                if( i == start ) {
                    pageData.addContentsList("about:blank", -1, -1, position, -1, i);
                    pageData.addContentsList(spines.get(j).getSpinePath(), -1, -1, position + n, j+1, i);
                    mPageDataList.remove(i);
                    mPageDataList.add(i, pageData);
                    j++;
                } else {
                    for( int k = 0; k < 2 ; k++ ){
                        int po = 0;
                        if( k == 0 )
                            po = position;
                        else
                            po = position + n;

                        if(j < spineEnd)
                            pageData.addContentsList(spines.get(j).getSpinePath(), -1, -1, po, j+1, i);
                        if( j == spineEnd )
                            pageData.addContentsList("about:blank", -1, -1, po, j+1, i);
                        j++;
                    }
                    mPageDataList.remove(i);
                    mPageDataList.add(i, pageData);
                }
                i += n;
            }
        }
    }
    /********************************************************************** e : before loading */

    /********************************************************************** s : start loading */
    public boolean loadBook(){
        try{
//            mParent = (FrameLayout)this.getParent();
            mCurrentMode = CurrentMode.ViewerOpen;

            boolean isLoadBookDataFinished = loadUserBookData();
            if(isLoadBookDataFinished){
                mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_FINISH_LOAD_DATA,null));
            }
            return isLoadBookDataFinished;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadUserBookData(){
        boolean isLoadBookDataFinished = false;
        try{

            loadLastReadPosition();
            loadHighlightData();
            loadBookmarkData();

            isLoadBookDataFinished = true;
        } catch( Exception e ){
            e.printStackTrace();
            return isLoadBookDataFinished;
        }
        return isLoadBookDataFinished;
    }

    public void finishLoadBookData(){
        try{
            isViewerLoadingComplete = true;

            mPagerAdapter = new PagerAdapterClass(mContext, mPageDataList);
            setAdapter(mPagerAdapter);

            mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_LOAD_BOOK,null));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showBook(){
        int lastReadPage=mReadingSpine.getCurrentSpineIndex();
        goPage(lastReadPage);

        if(lastReadPage==0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewPageChangeListener.onPageSelected(0);	    // noti ::: 0 페이지로 로드 시 onPageSelected callback 동작 X
                }
            }, 100);
        }
    }

    public void reloadBook(){
        for( int i=0; i<mPageDataList.size(); i++ ){
            FixedLayoutPageData pageData = mPageDataList.get(i);
            for(int j=0; j<pageData.getContentsCount(); j++){
                FixedLayoutPageData.ContentsData data = pageData.getContentsDataList().get(j);
                data.setContentsWidth(-1);
                data.setContentsHeight(-1);
            }
        }

        finishLoadBookData();
    }
    /********************************************************************** e : start loading */

    /********************************************************************** s : event listener */
    OnPageChangeListener viewPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if( !isViewerLoadingComplete )
                return;

            if(isTextSelectionMode)
                finishTextSelectionMode();
        }

        @Override
        public void onPageSelected(int position) {
            loadCurrentPage(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if( state == ViewPager.SCROLL_STATE_DRAGGING){
                isPageScrolling = true;
            } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                isPageScrolling=false;
                isMoveByFling=false;
            }
        }
    };
    /********************************************************************** e : event listener */

    /********************************************************************** s : viewpager adapter */
    private class PagerAdapterClass extends PagerAdapter {

        private Context mContext;

        private ArrayList<FixedLayoutPageData> mPageDataArrayList;

        private HashMap<Integer, FixedLayoutZoomView> mLoadedViewMap ;

        private FixedLayoutZoomView mCurrentView;

        public PagerAdapterClass(Context context, ArrayList<FixedLayoutPageData> pageDataArrayList){
            super();
            mContext = context;
            mPageDataArrayList = pageDataArrayList;
            mLoadedViewMap = new HashMap<>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            checkPageData(mPageDataArrayList.get(position));

            FixedLayoutZoomView fixedLayoutZoomView = null;
            if(BookHelper.isPreload){
                fixedLayoutZoomView = new FixedLayoutZoomView(mContext, mPageDataArrayList.get(position), mPageMode, mPageDirection, false);
                fixedLayoutZoomView.setWebviewCallbackListener(listener);
                fixedLayoutZoomView.setTouchAreaCheckInterface(FixedLayoutScrollView.this);
                fixedLayoutZoomView.setJSInterface(ttsDataInfoManager, ttsHighlighter, mediaOverlayController);
                fixedLayoutZoomView.setSelectionDisabled(isTextSelectionDisabled);
            } else{
                fixedLayoutZoomView = new FixedLayoutZoomView(mContext, mPageDataArrayList.get(position), mPageMode, mPageDirection, true);
                fixedLayoutZoomView.setWebviewCallbackListener(listener);
                fixedLayoutZoomView.setTouchAreaCheckInterface(FixedLayoutScrollView.this);
                fixedLayoutZoomView.setSelectionDisabled(isTextSelectionDisabled);
            }
            container.addView(fixedLayoutZoomView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mLoadedViewMap.put(position,fixedLayoutZoomView);
            return fixedLayoutZoomView;
        }

        private void checkPageData(FixedLayoutPageData pageData){
            for( int i = 0 ; i < pageData.getContentsCount(); i++ ){
                FixedLayoutPageData.ContentsData data = pageData.getContentsDataList().get(i);
                if( data.getContentsString() == null ){
                    String decode = mContentsManager.getDecodeContentsString(data.getContentsFilePath(), isIgnoreDrm, getDrmKey(), mOnDecodeContent, isViewerClosed);
                    data.setContentsString(mContentsManager.makeHtmlTemplate(mContext, data.getContentsFilePath(), decode));
                } else if(data.getContentsString() != null && !data.getContentsString().contains("feelingk_booktable")){    // 이미 template 만든 경우
                    data.setContentsString(mContentsManager.makeHtmlTemplate(mContext, data.getContentsFilePath(), data.getContentsString()));
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FixedLayoutZoomView) object);
            if( mLoadedViewMap != null && mLoadedViewMap.size() > 0){
                mLoadedViewMap.remove(position);
            }
            System.gc();
        }

        @Override
        public int getCount() {
            if(mPageDataArrayList!=null)
                return mPageDataArrayList.size();
            else
                return 0;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentView = (FixedLayoutZoomView)object;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public FixedLayoutZoomView getCurrentView(){
            return mCurrentView;
        }

        public Set<Integer> getVisibleViewIndexList() {
            if( mLoadedViewMap != null && mLoadedViewMap.size() > 0 )
                return mLoadedViewMap.keySet();
            return null;
        }

        public HashMap<Integer, FixedLayoutZoomView> getLoadedViewMap() {
            return mLoadedViewMap;
        }
    }
    /********************************************************************** e : viewpager adapter */

    /********************************************************************** s : handler */
    private void handleMessage(Message msg) {

        switch(msg.what){

            case Defines.FIXEDLAYOUT_LOAD_BOOK:
                showBook();
                FixedLayoutScrollView.this.setVisibility(View.VISIBLE);
                break;

            case Defines.FIXEDLAYOUT_FINISH_LOAD_DATA:
                finishLoadBookData();
                break;

            case Defines.FIXEDLAYOUT_SCROLL_PAGE:
                int scrollDirection = (Integer) msg.obj;
                ttsHighlighter.remove();
                checkPageStatus(scrollDirection);
//                setCurrentItem(mCurrentPageIndex+scrollDirection, pagerAnimation);
                break;


            case Defines.FIXEDLAYOUT_SEARCH_RESULT:
                SearchResult sr = (SearchResult)msg.obj;
                if( mOnSearchResult != null ) {
                    mOnSearchResult.onFound(sr);
                }
                break;

            case Defines.FIXEDLAYOUT_SEARCH_END:
                if( mOnSearchResult != null ) {
                    mOnSearchResult.onEnd();
                }
                break;
        }
    }

    private class ViewerHandler extends Handler {

        private final WeakReference<FixedLayoutScrollView> mViewPager;

        public ViewerHandler(FixedLayoutScrollView pager) {
            mViewPager = new WeakReference<>(pager);
        }

        @Override
        public void handleMessage(Message msg) {
            FixedLayoutScrollView pager = mViewPager.get();
            if (pager != null)
                pager.handleMessage(msg);
        }
    }
    /********************************************************************** e : handler */

    /********************************************************************** s : decode thread */
    @Override
    public void run() {
        try {
            for (int i = 0; i < mPageDataList.size(); i++) {
                FixedLayoutPageData pagedata = mPageDataList.get(i);
                for (int j = 0; j < pagedata.getContentsCount(); j++) {
                    FixedLayoutPageData.ContentsData contents = pagedata.getContentsDataList().get(j);

                    if (contents.getContentsFilePath().equals("about:blank"))
                        continue;

                    if (mDecodeThread.isInterrupted()) {        // TODO :: 아래 interrupted 중복인데??
                        throw new InterruptedException();
                    }

                    Thread.sleep(100);       // TODO :: ??

                    String decodeStr = mContentsManager.getDecodeContentsString(contents.getContentsFilePath(), isIgnoreDrm, getDrmKey(), mOnDecodeContent, isViewerClosed);

                    if (decodeStr == null)
                        mDecodeThread.interrupt();

                    if (mDecodeThread.isInterrupted()) {
                        throw new InterruptedException();
                    }

                    contents.setContentsString(mContentsManager.makeHtmlTemplate(mContext, contents.getContentsFilePath(), decodeStr));	// org
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            DebugSet.e(TAG, "InterruptedException, DecodeThread close");
            mDecodeThread.interrupt();
        }
    }
    /********************************************************************** e : decode thread */

    /********************************************************************** s : user action callback */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            mGestureDetector.onTouchEvent(ev);
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if(mPagerAdapter==null || mPagerAdapter.getCurrentView()==null || asidePopupStatus || isPreventPageMove)
            return false;

        if(!pagerAnimation || isTextSelectionMode || mPagerAdapter.getCurrentView().getCurrentScale() > 1){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            isStartHandlerTouched = false;
            isEndHandlerTouched = false;
            isLongPressStarted = false;

            if( mOnTouchEventListener != null ) {
                mOnTouchEventListener.onDown((int)e.getX(), (int)e.getY());
            }

            if(!isTextSelectionDisabled){
                int[] webviewPosition = mPagerAdapter.getCurrentView().getConvertWebviewPosition((int)e.getX(), (int)e.getY());

                if(webviewPosition==null)
                    return super.onDown(e);

                Rect handlerStartRegion = mStartHandle.getBounds();
                Rect handlerEndRegion = mEndHandle.getBounds();
                ArrayList<Rect> selectionRectList = mPagerAdapter.getCurrentView().getSelectionRectList();
                if(selectionRectList!=null && isTextSelectionMode ){

                    if( mStartHandle.isVisible() && handlerStartRegion.contains(webviewPosition[0],webviewPosition[1])){
                        isStartHandlerTouched=true;
                        isTextSelectionMode = true;
                        targetX = selectionRectList.get(0).left;
                        targetY = selectionRectList.get(0).top+ (selectionRectList.get(0).height()/2);
                    }

                    if( mEndHandle.isVisible() && handlerEndRegion.contains(webviewPosition[0],webviewPosition[1])){
                        isEndHandlerTouched=true;
                        isTextSelectionMode = true;
                        targetX = selectionRectList.get(selectionRectList.size()-1).right;
                        targetY = selectionRectList.get(selectionRectList.size()-1).top + (selectionRectList.get(selectionRectList.size()-1).height()/2);
                    }

                    mPagerAdapter.getCurrentView().setCurrentTouchCondition(isLongPressStarted, isStartHandlerTouched, isEndHandlerTouched, isTextSelectionMode);
                }
            }
            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(isTextSelectionMode)
                finishTextSelectionMode();
            mPagerAdapter.getCurrentView().onDoubleTab(e);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            DebugSet.d(TAG,"onSingleTapConfirmed in isTextSelectionMode : "+isTextSelectionMode);
            if(isTextSelectionMode && !isEndHandlerTouched && !isStartHandlerTouched){
                finishTextSelectionMode();
            } else if(!isTextSelectionMode){
                mPagerAdapter.getCurrentView().findTagUnderPoint(e.getX(), e.getY());
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            DebugSet.d(TAG,"onLongPress in isTextSelectionMode : "+isTextSelectionMode);
            super.onLongPress(e);

            if(asidePopupStatus){
                mPagerAdapter.getCurrentView().hideNoteref();
                return;
            }

            if(isTextSelectionDisabled || mPagerAdapter.getCurrentView().getCurrentScale() > 1){
                return;
            }

            if(isTextSelectionMode){
                int[] webviewPosition = mPagerAdapter.getCurrentView().getConvertWebviewPosition((int)e.getX(), (int)e.getY());

                if(webviewPosition==null)
                    return;

                Rect handlerStartRegion = mStartHandle.getBounds();
                Rect handlerEndRegion = mEndHandle.getBounds();

                if(handlerStartRegion.contains(webviewPosition[0],webviewPosition[1])){
                    isStartHandlerTouched=true;
                    isTextSelectionMode = true;
                }

                if(handlerEndRegion.contains(webviewPosition[0],webviewPosition[1])){
                    isEndHandlerTouched=true;
                    isTextSelectionMode = true;
                }

                if(!isStartHandlerTouched && !isEndHandlerTouched){
                    finishTextSelectionMode();
                }
            }

            isTextSelectionMode=true;
            isLongPressStarted = true;
            mPagerAdapter.getCurrentView().setCurrentTouchCondition(isLongPressStarted, isStartHandlerTouched,isEndHandlerTouched,isTextSelectionMode);
            mPagerAdapter.getCurrentView().onLongPress(e.getX(), e.getY());

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(asidePopupStatus){
                mPagerAdapter.getCurrentView().hideNoteref();
                return true;
            }

            isMoveByFling=true;

            if(isTextSelectionMode && !isStartHandlerTouched && !isEndHandlerTouched)
                finishTextSelectionMode();

            if(pagerAnimation==true){
                if(mPagerAdapter.getCurrentView().getCurrentScale() != 1f){
                    mPagerAdapter.getCurrentView().onFling(e1, e2, velocityX, velocityY);
                } else {
                    if(velocityX<0){
                        if(mPageDirection == ViewerContainer.PageDirection.LTR){
                            if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                                mOnBookStartEnd.onEnd();
                                return false;
                            }
                        } else {
                            if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                                mOnBookStartEnd.onStart();
                                return false;
                            }
                        }
                    } else if(velocityX>0){

                        if(mPageDirection == ViewerContainer.PageDirection.LTR){
                            if ( getCurrentItem()-1 < 0) {
                                mOnBookStartEnd.onStart();
                                return false;
                            }
                        } else {
                            if ( getCurrentItem()-1 < 0) {
                                mOnBookStartEnd.onEnd();
                                return false;
                            }
                        }
                    }
                }
            } else {
                if(mPagerAdapter.getCurrentView().getCurrentScale() != 1f){
                    mPagerAdapter.getCurrentView().onFling(e1, e2, velocityX, velocityY);
                } else{

                    float x = e1.getX();
                    float y = e1.getY();

                    if(Math.abs(velocityX) > BookHelper.swipeThreshold && velocityX>0){
                        isMoveByFling=true;
                        onLeftTouched(x, y);
                    } else if( Math.abs(velocityX) > BookHelper.swipeThreshold && velocityX<0 ) {
                        isMoveByFling=true;
                        onRightTouched(x, y);
                    }
                }
            }
            return true;
        }
    }

    @Override
    public void onLeftTouched(float x, float y) {
        if(asidePopupStatus){
            mPagerAdapter.getCurrentView().hideNoteref();
            return;
        }

        if(isPreventPageMove)
            return;

        if(mPagerAdapter.getCurrentView().getCurrentScale() != 1f) {
            return;
        }

        if(mPageDirection == ViewerContainer.PageDirection.LTR){
            int numChapter = getCurrentItem() - 1;
            if( numChapter < 0 ) {
                mOnBookStartEnd.onStart();
                return;
            }
        } else {
            int numChapter = getCurrentItem() + 1;
            if( numChapter == mPagerAdapter.getCount() ) {
                mOnBookStartEnd.onEnd();
                return;
            }
        }

        isFromRight = mPagerAdapter.getCurrentView().getIsFromRight();

        setCurrentItem(mCurrentPageIndex-1, pagerAnimation);

        if( mOnTouchEventListener != null  && !isMoveByFling){
            mOnTouchEventListener.onUp(BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x, y));
        }
    }

    @Override
    public void onRightTouched(float x, float y) {
        if(asidePopupStatus){
            mPagerAdapter.getCurrentView().hideNoteref();
            return;
        }

        if(isPreventPageMove)
            return;

        if(mPagerAdapter.getCurrentView().getCurrentScale() != 1f) {
            return;
        }

        if(mPageDirection == ViewerContainer.PageDirection.LTR){
            int numChapter = getCurrentItem() + 1;
            if( numChapter == mPagerAdapter.getCount() ) {
                mOnBookStartEnd.onEnd();
                return;
            }
        } else{
            int numChapter = getCurrentItem() - 1;
            if( numChapter < 0 ) {
                mOnBookStartEnd.onStart();
                return;
            }
        }

        isFromLeft = mPagerAdapter.getCurrentView().getIsFromLeft();

        setCurrentItem(mCurrentPageIndex+1, pagerAnimation);

        if( mOnTouchEventListener != null  && !isMoveByFling){
            mOnTouchEventListener.onUp(BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x, y));    // TODO :: 프론트가 필요한 콜백인가?
        }
    }

    @Override
    public void onMiddleTouched(float x, float y) {
        if(asidePopupStatus){
            mPagerAdapter.getCurrentView().hideNoteref();
            return;
        }

        if( mOnTouchEventListener != null ){
            mOnTouchEventListener.onUp(BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x, y));
        }
    }

    @Override
    public void onLeftCornerTouched() {
        if( mOnTouchEventListener != null){
            mOnTouchEventListener.onUp(BookHelper.ClickArea.Left_Corner);
        }
    }
    /********************************************************************** e : user action callback */

    /********************************************************************** s : webview callback */
    FixedLayoutWebview.OnWebviewCallbackInterface listener = new FixedLayoutWebview.OnWebviewCallbackInterface() {

        @Override
        public void pageLoadFinished(FixedLayoutPageData.ContentsData data) {

            if(mCurrentMode == CurrentMode.ViewerClose)
                return;

            int scroll = data.getContentsScrollIndex();     // 실제 컨텐츠 기준 스크롤 인덱스 ( page -1 )
            int page = data.getContentsPage();              // 실제 컨텐츠 기준 페이지
            int position = data.getContentsPosition();      // 두면보기 좌/우 여부

            DebugSet.d(TAG,"SSIN FIXEDLAYOUT_PAGE_FINISHED page : "+page);

            if( mCurrentMode == CurrentMode.ViewerOpen && mCurrentPage == page){
                mDecodeThread = new Thread(FixedLayoutScrollView.this);
                mDecodeThread.start();
                mCurrentMode = null;
            }

            if( mCurrentMode == CurrentMode.SearchFocus && mCurrentPage == page && mSearchPageIndex == page ){
                focusText(mSearchResult);
                mCurrentMode = null;
            }

            if( mCurrentPageIndex == scroll ){     // RTL,LTR 체크 필요 없음 무조건 콘텐츠 스크롤인덱스 기준임

                if( mOnChapterChange != null ){
                    mOnChapterChange.onPageReady(mPagerAdapter.getCount());     // TODO :: 프론트가 해당 콜백 받고 하는 기능 있는지 확인하기
                }

                if(mPageMode == PageMode.OnePage){
                    mOnChapterChange.onChangeAfter(mPagerAdapter.getCount());

                    if( mOnPageScroll != null ){
                        mOnPageScroll.onScrollAfter(data.getContentsPage(), 0, mReadingSpine.getSpineInfos().size(), (double) data.getContentsPage() / (double)mReadingSpine.getSpineInfos().size() * 100.0);
                    }
                } else if(mPageMode == PageMode.TwoPage) {
                    if(position==0){
                        isLeftWebviewLoadFinished = true;
                    } else if(position==1){
                        isRightWebviewLoadFinished = true;
                    }

                    if(isLeftWebviewLoadFinished && isRightWebviewLoadFinished){
                        mOnChapterChange.onChangeAfter(mPagerAdapter.getCount());
                        if( mOnPageScroll != null ){
                            int currentPage = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(0).getContentsPage();
                            if(currentPage==-1)
                                currentPage = 1;
                            mOnPageScroll.onScrollAfter(currentPage, 0, mReadingSpine.getSpineInfos().size(), (double) currentPage / (double)mReadingSpine.getSpineInfos().size() * 100.0);
                        }
                        isLeftWebviewLoadFinished=false;
                        isRightWebviewLoadFinished=false;
                    }
                }

                mPagerAdapter.getLoadedViewMap().get(mCurrentPageIndex).applyCurrentChapterHighlight(position);

                if(!selectedAnnotationId.isEmpty()){
                    mPagerAdapter.getLoadedViewMap().get(mCurrentPageIndex).scrollToAnnotationId(selectedAnnotationId);
                    selectedAnnotationId="";
                }

                mPagerAdapter.getLoadedViewMap().get(mCurrentPageIndex).removeCommentNode(position);

                saveLastPosition();
            }
        }

        @Override
        public void reportVideoInfo(String videoSrc) {
            if(mOnVideoInfoListener!=null){
                mOnVideoInfoListener.videoInfo(videoSrc);
            }
        }

        @Override
        public void reportAsidePopupStatus(boolean isAsidePopopShow) {
            asidePopupStatus = isAsidePopopShow;
            if(mOnNoterefListener!=null){
                if( asidePopupStatus){
                    mOnNoterefListener.didShowNoterefPopup();
                } else{
                    mOnNoterefListener.didHideNoterefPopup();
                }
            }
        }

        @Override
        public void reportLinkClick(String link) {
            getCurrentPageInfo();
            if(mOnTagClick!=null && link!=null && !link.isEmpty())
                mOnTagClick.onLink(link);
        }

        @Override
        public void reportTouchPosition(int x, int y) {

//            if(isPageScrolling){
//                isPageScrolling = false;
//                return;
//            }

//            if(isMoveByFling){
//                isMoveByFling=false;
//                return;
//            }

//            if(asidePopupStatus)
//                return;

            if(mediaOverlayController.isMediaOverlayPlaying()){
                touchedPositionDuringPlaying = BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x, y);   // 미디어오버레이 아닌 영역 터치 시 필요
                mPagerAdapter.getCurrentView().getIDListByPoint(x,y);
                return;
            }

            if( mOnTouchEventListener != null ){

                if(mPagerAdapter.getCurrentView().getCurrentScale() == 1f) {

                    BookHelper.ClickArea clickArea = BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x,y);

                    if(isPreventPageMove){
                        if(clickArea == BookHelper.ClickArea.Middle && !isMoveByFling){
                            mOnTouchEventListener.onUp(clickArea);
                        }
                        return;
                    }

                    if(clickArea== BookHelper.ClickArea.Left){
                        if(mPageDirection == ViewerContainer.PageDirection.LTR){
                            if ( getCurrentItem()-1 < 0) {
                                mOnBookStartEnd.onStart();
                                return;
                            }
                        } else {
                            if ( getCurrentItem()-1 < 0) {
                                mOnBookStartEnd.onEnd();
                                return;
                            }
                        }
                        setCurrentItem(mCurrentPageIndex-1, pagerAnimation);
                    } else if(clickArea== BookHelper.ClickArea.Right){

                        if(mPageDirection == ViewerContainer.PageDirection.LTR){
                            if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                                mOnBookStartEnd.onEnd();
                                return;
                            }
                        } else {
                            if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                                mOnBookStartEnd.onStart();
                                return;
                            }
                        }
                        setCurrentItem(mCurrentPageIndex+1, pagerAnimation);
                    } else if(clickArea== BookHelper.ClickArea.Middle){
                        mOnTouchEventListener.onUp(clickArea);
                    }
                } else {
                    BookHelper.ClickArea clickArea = BookHelper.getClickArea(mPagerAdapter.getCurrentView(), x,y);
                    if(clickArea == BookHelper.ClickArea.Middle){
                        mOnTouchEventListener.onUp(clickArea);
                    }
                }
            }
        }

        @Override
        public void reportCurrentPageInfo(Bookmark bookmarkInfo) {
            bookmarkInfo.chapterFile = mReadingSpine.getCurrentSpineInfo().getSpinePath();

            FixedLayoutPageData pageData = mPageDataList.get(getCurrentItem());

            switch (mCurrentMode) {
                case Bookmark:
                    mBookmarkManager.setBookmarkData(bookmarkInfo, pageData);
                    mBookmarkManager.addBookmark(bookmarkInfo);
                    if( mOnPageBookmark != null )
                        mOnPageBookmark.onAdd(bookmarkInfo, true); //추가처리

                    mUserBookDataFileManager.__bmHistory.add(bookmarkInfo.uniqueID);

                    saveBookmarks();

                    mUserBookDataFileManager.saveBookmarkHistory();
                    break;
                default:
                    break;
            }
            mCurrentMode = null;
        }

        @Override
        public void reportOverflowMemoContent() {
            if(mOnTextSelection != null){
                mOnTextSelection.onOverflowMemoContent();
            }
        }

        @Override
        public void reportError(int errorCode) {
            if( mOnReportError != null ) {
                mOnReportError.onError(errorCode);
            }
        }

        @Override
        public void setBgmState() {
            if(bgmPlayer!=null)
                bgmPlayer.setBGMState();
        }

        @Override
        public void pauseMediaOverlay() {
            if(mediaOverlayController!=null)
                mediaOverlayController.pause();
        }

        @Override
        public void playSelectedMediaOverlay(String json) {

            boolean playing =false;

            if(json!=null){
                try {
                    JSONArray jsonArr = new JSONArray(json);
                    for(int index=0; index<jsonArr.length(); index++){
                        if(mediaOverlayController.getSmilSyncs().get(jsonArr.getString(index))!=null){
                            playing = true;
                            mediaOverlayController.setPlayId(jsonArr.getString(index), true);
                            mOnMediaOverlayStateListener.selectedMediaOverlayPlaying();
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(!playing && touchedPositionDuringPlaying!=null){
                    mOnTouchEventListener.onUp(touchedPositionDuringPlaying);
                    touchedPositionDuringPlaying=null;
                }
            }
        }

        @Override
        public void reportMediaControl(JSONObject jsonObject) {
            try {
                mOnMediaControlListener.didPlayPreventMedia(jsonObject.getString("id"), jsonObject.getString("mediaType"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setTextSelectionMode(boolean textSelectionMode) {
            isTextSelectionMode = textSelectionMode;
            if(isTextSelectionMode && mOnTextSelection!=null) {
                mOnTextSelection.onStartTextSelection();
            } else if(!isTextSelectionMode) {
                currentSelectedText = "";
                if(mOnTextSelection!=null){
                    mOnTextSelection.onEndTextSelection();
                }
            }
        }

        @Override
        public void setAllMemoText(String allMemoText) {
            if( mOnMemoSelection!=null ) {
                mOnMemoSelection.onGetAllMemoText(allMemoText);
            }
        }

        @Override
        public void setSeletedText(String selectedText) {
            currentSelectedText=selectedText;
        }

        @Override
        public void showContextMenu(PopupData popupData) {

            if(popupData.contentsPosition!=-1){
                int[] containerViewLocation = getContextMenuTargetViewPosition(popupData.contentsPosition);
                popupData.x = popupData.x + containerViewLocation[0];
                popupData.y = popupData.y + containerViewLocation[1];
            }

            if( mOnContextMenu != null ) {
                mOnContextMenu.onShowContextMenu(popupData.highlightId, popupData.menuType, popupData.x, popupData.y);
            }

            isTextSelectionMode = true;
        }

        @Override
        public void hideContextMenu() {
            if( mOnContextMenu != null ) {
                mOnContextMenu.onHideContextMenu();
            }
        }

        @Override
        public Drawable requestStartHandlerImage() {
            return mStartHandle;
        }

        @Override
        public Drawable requestEndHandlerImage() {
            return mEndHandle;
        }

        @Override
        public boolean requestIsSelectionDisabled() {
            return isTextSelectionDisabled;
        }

        @Override
        public int requestStartSelectionPositionX() {
            return targetX;
        }

        @Override
        public int requestStartSelectionPositionY() {
            return targetY;
        }

        @Override
        public int[] requestContextMenuInfo() {
            int[] contextMenuInfo = new int[3];
            contextMenuInfo[0] = (int) mContextMenuHeight;
            contextMenuInfo[1] = mContextMenuTopMargin;
            contextMenuInfo[2] = mContextMenuBottomMargin;
            return contextMenuInfo;
        }

        @Override
        public void reportMergedAnnotationSelection() {
            if(mOnAnalyticsListener!=null){
                mOnAnalyticsListener.onAnnotationMergeSelection();
            }
        }

        @Override
        public void reportMergedAnnotationQuick() {
            if(mOnAnalyticsListener!=null){
                mOnAnalyticsListener.onAnnotationMergeQuick();
            }
        }

        @Override
        public void reportAnnotationQuick() {
            if(mOnAnalyticsListener!=null){
                mOnAnalyticsListener.onAnnotationQuick();
            }
        }
    };
    /********************************************************************** e : webview callback */

    /********************************************************************** s : common */
    private String mDrmKey = "";
    private String getDrmKey()  {

        if( !mDrmKey.isEmpty() )
            return mDrmKey;

        Iterator<XmlDCMES> drm = mEpubFile.getDublinCoreDrms();
        while (drm.hasNext()) {
            XmlDCMES xmlDublinCore = drm.next();
            mDrmKey = xmlDublinCore.getValue();
            break;
        }

        return mDrmKey;
    }

    public void registSelectionMenu(PopupWindow pw) {
        mContextMenu = pw;
    }

    public void setBgmPlayer(BGMPlayer bgmPlayer){
        this.bgmPlayer = bgmPlayer;
    }

    public void setIgnoreDrm(boolean isIgnore){
        isIgnoreDrm = isIgnore;
    }

    public void setTTSDataInfoManager(TTSDataInfoManager ttsDataInfoManager) {
        this.ttsDataInfoManager = ttsDataInfoManager;
    }

    public void setTTSHighlighter(Highlighter ttsHighlighter) {
        this.ttsHighlighter = ttsHighlighter;
    }

    public Highlighter getTTSHighlighter() {
        return ttsHighlighter;
    }

    public void setTTSHighlightRect(JSONArray rectArray, String filePath) {
        mPagerAdapter.getCurrentView().setTTSHighlightRect(rectArray, filePath);
    }

//    ViewerContainer.OnTTSStateChangeListener mOnTTSStateChangeListener = null;
//    public void setOnTTSStateChangeListener(ViewerContainer.OnTTSStateChangeListener l) {
//        mOnTTSStateChangeListener = l;
//    }

    public void removeTTSHighlightRect() {
        mPagerAdapter.getCurrentView().removeTTSHighlightRect();
    }

    public void setMediaOverlayController(MediaOverlayController mediaOverlayController) {
        this.mediaOverlayController = mediaOverlayController;
    }

//    public void setUseVolumeKey(boolean bAble){
//        useVolumeKey = bAble;
//    }

    public void onClose(){

        isViewerClosed = true;

        mCurrentMode = CurrentMode.ViewerClose;

        stopAllMedia();
        saveLastPosition();

        if( mDecodeThread != null )
            mDecodeThread.interrupt();

        if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null)
            mPagerAdapter.getCurrentView().onClose();

        if( mOnViewerState != null ){
            mOnViewerState.onEnd();
        }

        System.gc();
    }

    public ChapterInfo getChapterFromSpineIndex(int index){
        for (int i = 0; i < mReadingChapter.getChapters().size(); i++) {
            ChapterInfo cp = mReadingChapter.getChapters().get(i);
            if( mReadingSpine.getSpineInfos().get( index ).getSpinePath().toLowerCase().equals( cp.getChapterFilePath().toLowerCase() ) )
                return cp;
        }
        return new ChapterInfo(mReadingSpine.getSpineInfos().get(index).getSpinePath(),"", 0, "");
    }

    public int getSpineIndexFromPercent(double percent){
        for( int i = 0; i < mReadingSpine.getSpineInfos().size(); i++ ) {
            if( mReadingSpine.getSpineInfos().get(i).getSpineStartPercentage() > percent )
                return i - 1;
        }
        return mReadingSpine.getSpineInfos().size()-1;
    }

    private int getPageIndexFromFilePath(String filePath){
        int pageNum = 0;
        for( int i = 0; i<mPageDataList.size(); i++ ){
            FixedLayoutPageData pageData = mPageDataList.get(i);
            for( int j = 0 ; j< pageData.getContentsCount(); j++ ){
                FixedLayoutPageData.ContentsData contents = pageData.getContentsDataList().get(j);
                String contentsFile = contents.getContentsFilePath().toLowerCase().trim();
                String paramFile = filePath.toLowerCase().trim();
                if( contentsFile.equals(paramFile) ) {
                    pageNum = i;
                    return pageNum;
                }
            }
        }
        return pageNum;
    }

    public void getCurrentPageInfo() {

        Bookmark bm = new Bookmark();
        FixedLayoutPageData pageData = mPageDataList.get(getCurrentItem());
        FixedLayoutPageData.ContentsData contents;
        if( mPageDirection == ViewerContainer.PageDirection.LTR ){
            contents = pageData.getContentsDataList().get(0);
            if( contents.getContentsFilePath().equals("about:blank") )
                contents = pageData.getContentsDataList().get(1);
        } else {
            if( mPageMode == PageMode.OnePage )
                contents = pageData.getContentsDataList().get(0);
            else
                contents = pageData.getContentsDataList().get(1);

            if( contents.getContentsFilePath().equals("about:blank") )
                contents = pageData.getContentsDataList().get(0);
        }

        bm.chapterFile = contents.getContentsFilePath();
        bm.percent = (double)contents.getContentsPage() / (double)mReadingSpine.getSpineInfos().size() * 100.0d;
        bm.path = "";

        if( mOnCurrentPageInfo != null ){
            mOnCurrentPageInfo.onGet(bm);
        }
    }

    public void stopAllMedia(){

        if(bgmPlayer!=null){
            bgmPlayer.isClose = true;
            bgmPlayer.clearMediaPlayer();
        }

        if(mediaOverlayController!=null){
            mediaOverlayController.clear();
            mediaOverlayController.stop(false);
        }

        if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null)
            mPagerAdapter.getCurrentView().stopAllMedia();
    }

    public void removeSelection(){
        finishTextSelectionMode();
    }

    private Drawable mStartHandle;
    private Drawable mEndHandle;
    public void setSelectionIcon(Drawable start, Drawable end) {
        mStartHandle = start;
        mEndHandle = end;
    }

    private float mContextMenuHeight;
    private int mContextMenuTopMargin;
    private int mContextMenuBottomMargin;
    public void setContextMenuSize(float height, int topMargin, int bottomMargin) {
        mContextMenuHeight = convertDpToPixels((int) height);
        mContextMenuTopMargin = convertDpToPixels(topMargin);               // 글자 기준 dp -> px
        mContextMenuBottomMargin = convertDpToPixels(bottomMargin);         // 핸들러 기준 dp -> px
    }

    private int convertDpToPixels(int value){
        float px = 0;
        try {
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mContext.getResources().getDisplayMetrics());
        } catch (Exception e) { e.printStackTrace(); }
        return (int) px;
    }

    private String currentSelectedText="";
    public String getSelectedText(){
        return currentSelectedText;
    }

    public void preventPageMove(boolean isPrevent) {
        isPreventPageMove = isPrevent;
    }

    public void setPreventMediaControl(boolean isPrevent){
        mPagerAdapter.getCurrentView().setPreventMediaControl(isPrevent);
    }

    public FixedLayoutWebview getCurrentWebView(String filePath){
        return mPagerAdapter.getCurrentView().getCurrentWebView(filePath);
    }

    public FixedLayoutWebview getLeftWebView(){
        return mPagerAdapter.getCurrentView().getLeftWebView();
    }

    public FixedLayoutWebview getRightWebView(){
        return mPagerAdapter.getCurrentView().getRightWebView();
    }

    public void unpluggedHeadSet(boolean isUnplugged){
        if(isUnplugged){

            if(BGMMediaPlayer.getBgmMediaPlayerClass().isPlaying){
                bgmPlayer.pause();
            }

            if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null){
                mPagerAdapter.getCurrentView().stopAllMedia();
            }

            if(mediaOverlayController!=null){
                mediaOverlayController.pause();
            }
        }
    }

    public boolean isNoterefEnabled(){
        return asidePopupStatus;
    }

    public void hideNoteref(){
        if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null)
            mPagerAdapter.getCurrentView().hideNoteref();
    }

    public void setPreventNoteref(boolean isPrevent){
        if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null)
            mPagerAdapter.getCurrentView().setPreventNoteref(isPrevent);
    }

    public FixedLayoutPageData getPageData(boolean isBackground){
        if(isBackground) {
            int numChapter = mReadingSpine.getCurrentSpineIndex() + 1;
            if (numChapter == mReadingSpine.getSpineInfos().size()) {
                mOnBookStartEnd.onEnd();
                return null;
            } else {
                mCurrentPageIndex = numChapter;
            }
        }
        return mPageDataList.get(mCurrentPageIndex);
    }

    public String getCurrentUserAgent() {
        if(mPagerAdapter!=null && mPagerAdapter.getCurrentView()!=null)
            return mPagerAdapter.getCurrentView().getCurrentUserAgent();
        return "";
    }

    public void handleBackKeyEvent(){
        if(isTextSelectionMode){
            mPagerAdapter.getCurrentView().handleBackKeyEvent();
        }
    }

    public void finishTextSelectionMode(){
        isTextSelectionMode = false;
        mPagerAdapter.getCurrentView().finishTextSelectionMode();
    }

    public int getTouchedWebviewPosition(){
        return mPagerAdapter.getCurrentView().getTouchedWebviewPosition();
    }

    public int[] getContextMenuTargetViewPosition(int currentContentsPosition){
        return mPagerAdapter.getCurrentView().getContextMenuTargetViewPosition(currentContentsPosition);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        DebugSet.d(TAG, "onSizeChanged w : " + w + ", h :" + h );
        if( !isViewerLoadingComplete || isViewerClosed)
            return;

        if( oldw != w && oldh != h){
            setVisibility(View.INVISIBLE);
            reloadBook();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }
    /********************************************************************** e : common */

    /********************************************************************** s : annotation bookmark lastposition */
    public void loadLastReadPosition() {
        mUserBookDataFileManager.restoreLastPosition();
    }

    public void loadHighlightData(){
        mHighlightManager.setHighlightList(mUserBookDataFileManager.restoreHighlights());	// TODO : 임시 data class로 빼기
    }

    public void loadBookmarkData() {
        mBookmarkManager.setBookmarkList(mUserBookDataFileManager.restoreBookmarks());
    }

    public ArrayList<Bookmark> getBookmarkList(){
        return mBookmarkManager.getBookmarkList();
    }

    public void hasBookmark(){
        FixedLayoutPageData pageData = mPageDataList.get(getCurrentItem());

        ArrayList<Bookmark> bookmarkList = mBookmarkManager.checkBookmarkInCurrentPage(pageData);
        if( mOnPageBookmark != null ){

            if(bookmarkList.size()>0){
                mOnPageBookmark.onMark(true);
            } else{
                mOnPageBookmark.onMark(false);
            }
        }
    }

    public void doBookmark(){
        DebugSet.d(TAG, "doBookmark");
        FixedLayoutPageData pageData = mPageDataList.get(getCurrentItem());

        ArrayList<Bookmark> bookmarkList = mBookmarkManager.checkBookmarkInCurrentPage(pageData);

        if(!bookmarkList.isEmpty()){
            for(int idx=0; idx<bookmarkList.size(); idx++){
                Bookmark bm = bookmarkList.get(idx);
                deleteBookmark(bm);
                if( mOnPageBookmark != null )
                    mOnPageBookmark.onAdd(bm, false);// 삭제처리
            }
        } else {
            FixedLayoutZoomView currentView = mPagerAdapter.getCurrentView();
            if( currentView != null ){
                mCurrentMode = CurrentMode.Bookmark;
                currentView.getCurrentPath();
            }
        }
    }

    public void deleteBookmark(Bookmark bm){
        mBookmarkManager.deleteBookmark(bm);
        if(mUserBookDataFileManager!=null){
            if(mUserBookDataFileManager.__bmHistory!=null){
                mUserBookDataFileManager.__bmHistory.remove(bm.uniqueID);
                mUserBookDataFileManager.saveBookmarkHistory();
            }
            mUserBookDataFileManager.saveBookmarks(mBookmarkManager.getBookmarkList());
        }
    }

    public ArrayList<Highlight> getHighlights() {
        return mHighlightManager.getHighlightList();
    }

    public boolean saveHighlights() {
        return mPagerAdapter.getCurrentView().saveHighlights();
    }

    public void saveLastPosition(){
        Bookmark bm = new Bookmark();
        FixedLayoutPageData pageData = mPageDataList.get(getCurrentItem());
        FixedLayoutPageData.ContentsData contents;
        if( mPageDirection == ViewerContainer.PageDirection.LTR ){
            contents = pageData.getContentsDataList().get(0);
            if( contents.getContentsFilePath().equals("about:blank") )
                contents = pageData.getContentsDataList().get(1);
        } else {
            if( mPageMode == PageMode.OnePage )
                contents = pageData.getContentsDataList().get(0);
            else
                contents = pageData.getContentsDataList().get(1);

            if( contents.getContentsFilePath().equals("about:blank") )
                contents = pageData.getContentsDataList().get(0);
        }
        bm.chapterFile = contents.getContentsFilePath();
        bm.percent = (double)contents.getContentsPage() / (double)mReadingSpine.getSpineInfos().size() * 100.0d;
        bm.path = "";
        mUserBookDataFileManager.saveLastPosition(bm);
    }

    public boolean saveBookmarks(){
        ArrayList<Bookmark> bookmarks = mBookmarkManager.getBookmarkList();
        if( bookmarks != null && bookmarks.size() > 0 )
            return mUserBookDataFileManager.saveBookmarks(bookmarks);

        return false;
    }

    public void saveUserBookData(){
        saveBookmarks();
        saveLastPosition();
        saveHighlights();
    }
    /********************************************************************** e : annotation bookmark lastposition */

    /********************************************************************** s : search */
    public void searchText(String keyword) {
        if( mOnSearchResult != null )
            mOnSearchResult.onStart();

        removeSearchHighlight();

        mSearchManager.searchText(keyword, mPageDataList);
    }

    public void focusText(SearchResult sr){
        DebugSet.d(TAG, "focusText keyword : "+ sr.keyword + ", index : " + sr.pageOffset);

        int searchPageIndex = mSearchManager.getIndexBySearchResult(sr, mPageDataList);
        boolean isCurrentPage = false;
        int pagePosition = 0;
        int pagerIndex = 0;

//		//현재 로드되어있는 뷰 리스트에서 체크
        Set<Integer> loadedViedIndexSet = mPagerAdapter.getVisibleViewIndexList();

        for (Integer index : loadedViedIndexSet) {
            FixedLayoutPageData pageData = mPageDataList.get(index);
            for( int i=0; i<pageData.getContentsCount(); i++ ){

                FixedLayoutPageData.ContentsData contents = pageData.getContentsDataList().get(i);
                if( contents.getContentsPage() == searchPageIndex ){
                    isCurrentPage = true;
                    pagePosition = i;
                    break;
                }
            }

            if( isCurrentPage ){
                pagerIndex = index;
                break;
            }
        }

        if( mCurrentPageIndex == pagerIndex ) {
            setCurrentItem(pagerIndex, pagerAnimation);
            mPagerAdapter.getLoadedViewMap().get(pagerIndex).focusSearchText(sr.keyword, sr.pageOffset, pagePosition);
        }
        else {
            mCurrentMode = CurrentMode.SearchFocus;
            mSearchPageIndex = searchPageIndex;
            mSearchResult = sr;
            goPage(sr);

        }
    }

    public void removeSearchHighlight(){
        FixedLayoutZoomView zoomView = mPagerAdapter.getLoadedViewMap().get(mCurrentPageIndex);
        if( zoomView != null ){
            zoomView.removeSearchHighlight();
        }
    }
    /********************************************************************** e : search */

    /********************************************************************** s : page */
    public void scrollNext(){
        mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_SCROLL_PAGE, 1));
    }

    public void scrollPrior(){
        mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_SCROLL_PAGE, -1));
    }

    public void goPage(int spineIndex){
        if( spineIndex >= 0 && spineIndex < mReadingSpine.getSpineInfos().size() ){
            int pageNum = getPageIndexFromFilePath(mReadingSpine.getSpineInfos().get(spineIndex).getSpinePath());
            setCurrentItem(pageNum, pagerAnimation);
        }
    }

    public void goPage(Highlight highlight){
        if(mReadingChapter.getCurrentChapter().getChapterFilePath().equals(highlight.chapterFile)){
            mPagerAdapter.getCurrentView().scrollToAnnotationId(highlight.highlightID);
        } else{
            selectedAnnotationId = highlight.highlightID;
            goPage(highlight.chapterFile);
        }
    }

    public void goPage(ChapterInfo ch){
        if( ch != null ){
            if(mEpubFile.hasLinearNo(ch.getChapterFilePath().replace(mEpubFile.getPublicationPath(), ""))){
                mMoveToLinearNoChapter.moveToLinearNoChapter(ch.getChapterFilePath());
                return;
            }
            int pageNum = getPageIndexFromFilePath(ch.getChapterFilePath());
            setCurrentItem(pageNum, pagerAnimation);
        }
    }

    public void goPage(Bookmark bm){
        if( bm != null ){
            int pageNum = getPageIndexFromFilePath(bm.chapterFile);
            setCurrentItem(pageNum, pagerAnimation);
        }
    }

    public void goPage(SearchResult sr){
        if( sr != null ){
            int pageNum = getPageIndexFromFilePath(sr.chapterFile);
            setCurrentItem(pageNum, pagerAnimation);
        }
    }

    public void goPage(String fileName){
        if( fileName.length() > 0 ){
            int pageNum = getPageIndexFromFilePath(fileName);
            setCurrentItem(pageNum, pagerAnimation);
        }
    }

    public void goPage(double percent) {
        int spineIndex = getSpineIndexFromPercent( percent );
        goPage(spineIndex);
    }

    public void goPageByJump(){
        getCurrentPageInfo();
    }

    public void goPageByPage(int page){     // TODO :: FRONT 안쓰면 지우기
        for( int i = 0; i<mPageDataList.size(); i++ ){
            FixedLayoutPageData pageData = mPageDataList.get(i);

            for( int j = 0 ; j< pageData.getContentsCount(); j++ ){
                FixedLayoutPageData.ContentsData contents = pageData.getContentsDataList().get(j);

                if( contents.getContentsPage() == page ){
                    setCurrentItem(i, pagerAnimation);
                    return;
                }
            }
        }
    }

    private void loadCurrentPage(int position) {

        mCurrentPageIndex = position;
        if(mPageDirection == ViewerContainer.PageDirection.LTR){
            mCurrentPage = mCurrentPageIndex+1;
        } else if(mPageDirection == ViewerContainer.PageDirection.RTL){
            mCurrentPage = mPagerAdapter.getCount()-mCurrentPageIndex;
        }

        FixedLayoutPageData.ContentsData contents;
        if( mPageDirection == ViewerContainer.PageDirection.LTR ){
            contents = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(0);
            if( contents.getContentsFilePath().equals("about:blank") )
                contents = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(1);
        } else {
            if( mPageMode == PageMode.OnePage )
                contents = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(0);
            else
                contents = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(1);

            if( contents.getContentsFilePath().equals("about:blank") )
                contents = mPageDataList.get(mCurrentPageIndex).getContentsDataList().get(0);
        }

        String currentFile =  contents.getContentsFilePath();
        mReadingChapter.setCurrentChapter(currentFile);
        mReadingSpine.setCurrentSpineIndex(currentFile);

        if(!BookHelper.isPreload){
            mPagerAdapter.getLoadedViewMap().get(position).loadContent(mPageDataList.get(mCurrentPageIndex));
            mPagerAdapter.getLoadedViewMap().get(position).setJSInterface(ttsDataInfoManager, ttsHighlighter, mediaOverlayController);
            mPagerAdapter.getLoadedViewMap().get(position).setCurrentWebView(bgmPlayer);
            Iterator<Integer> iterator =  mPagerAdapter.getVisibleViewIndexList().iterator();
            while (iterator.hasNext()) {
                int targetIndex = iterator.next();
                if(position!=targetIndex) {
                    mPagerAdapter.getLoadedViewMap().get(targetIndex).loadEmptyPage();
                }
            }
        }

        isPageScrolling = false;

        System.gc();
    }
    /********************************************************************** e : page */

    /******************************************************************** s : add annotation */
    public void addAnnotation(){
        mPagerAdapter.getCurrentView().addAnnotation();
    }

    public void addAnnotationWithMemo(String memoContent, boolean modifyMerged){
        mPagerAdapter.getCurrentView().addAnnotationWithMemo(memoContent, modifyMerged);
    }

    public void requestAllMemoText(){
        mPagerAdapter.getCurrentView().requestAllMemoText();
    }
    /******************************************************************** e : add annotation */

    /******************************************************************** s : delete annotation */
    public void deleteAnnotation(){
        mPagerAdapter.getCurrentView().deleteAnnotation();
    }
    /******************************************************************** e : delete annotation */

    /******************************************************************** s : modify annotation */
    public void  modifyAnnotationColorAndRange(int colorIndex){
        mPagerAdapter.getCurrentView().modifyAnnotationColorAndRange(colorIndex);
    }

    public void  changeMemoText(String memoId, String currentMemo){
        mPagerAdapter.getCurrentView().changeMemoText(memoId, currentMemo);
    }
    /******************************************************************** e : modify annotation */

    private boolean isTextSelectionDisabled = false;
    public void setSelectionDisabled(boolean isTextSelectionDisabled){
        this.isTextSelectionDisabled = isTextSelectionDisabled;
    }

    private void checkPageStatus(int scrollDirection){

        if(asidePopupStatus){
            mPagerAdapter.getCurrentView().hideNoteref();
            return;
        }

        if(mPagerAdapter.getCurrentView().getCurrentScale() != 1f)
            return;

        if(isTextSelectionMode)
            finishTextSelectionMode();

        if(scrollDirection==1){
            if(mPageDirection == ViewerContainer.PageDirection.LTR){
                if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                    mOnBookStartEnd.onEnd();
                    return;
                }
            } else {
                if ( getCurrentItem()+1 == mPagerAdapter.getCount() ) {
                    mOnBookStartEnd.onStart();
                    return;
                }
            }
        } else if(scrollDirection==-1){
            if(mPageDirection == ViewerContainer.PageDirection.LTR){
                if ( getCurrentItem()-1 < 0) {
                    mOnBookStartEnd.onStart();
                    return;
                }
            } else {
                if ( getCurrentItem()-1 < 0) {
                    mOnBookStartEnd.onEnd();
                    return;
                }
            }
        }
        setCurrentItem(mCurrentPageIndex+scrollDirection, pagerAnimation);
    }
}