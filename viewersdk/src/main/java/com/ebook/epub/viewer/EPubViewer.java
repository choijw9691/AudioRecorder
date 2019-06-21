package com.ebook.epub.viewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.TextSize;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.ebook.epub.common.Defines;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.mediaoverlays.SmilSync;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.opf.XmlDCMES;
import com.ebook.epub.viewer.BookHelper.ClickArea;
import com.ebook.epub.viewer.ViewerContainer.OnAudioListener;
import com.ebook.epub.viewer.ViewerContainer.OnBGMStateListener;
import com.ebook.epub.viewer.ViewerContainer.OnBookStartEnd;
import com.ebook.epub.viewer.ViewerContainer.OnChapterChange;
import com.ebook.epub.viewer.ViewerContainer.OnCurrentPageInfo;
import com.ebook.epub.viewer.ViewerContainer.OnDecodeContent;
import com.ebook.epub.viewer.ViewerContainer.OnMediaControlListener;
import com.ebook.epub.viewer.ViewerContainer.OnMediaOverlayStateListener;
import com.ebook.epub.viewer.ViewerContainer.OnMemoSelection;
import com.ebook.epub.viewer.ViewerContainer.OnMoveToLinearNoChapterListener;
import com.ebook.epub.viewer.ViewerContainer.OnNoterefListener;
import com.ebook.epub.viewer.ViewerContainer.OnPageBookmark;
import com.ebook.epub.viewer.ViewerContainer.OnPageScroll;
import com.ebook.epub.viewer.ViewerContainer.OnReportError;
import com.ebook.epub.viewer.ViewerContainer.OnSearchResult;
import com.ebook.epub.viewer.ViewerContainer.OnTagClick;
import com.ebook.epub.viewer.ViewerContainer.OnTextSelection;
import com.ebook.epub.viewer.ViewerContainer.OnTouchEventListener;
import com.ebook.epub.viewer.ViewerContainer.OnViewerState;
import com.ebook.epub.viewer.ViewerContainer.PageDirection;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.epub.viewer.data.ReadingChapter;
import com.ebook.epub.viewer.data.ReadingOrderInfo;
import com.ebook.epub.viewer.data.ReadingSpine;
import com.ebook.media.AudioContent;
import com.ebook.media.AudioContentPlayer;
import com.ebook.media.AudioContentReader;
import com.ebook.media.OnAudioContentPlayerListener;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.tts.TTSDataInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class EPubViewer extends ViewerBase {

    String TAG = "EPubViewer";
    String __ID = "EPUBViewer";

    public int mTotalPageInChapter=0;               // 챕터 전체 페이지 수
    public int mCurrentPageIndexInChapter=0;        // 챕터 내 현재 페이지 인덱스
    public int mCurrentPageInChapter=0;             // 챕터 내 현재 페이지 수
    public double mCurrentPercentInBook= 0.0d;      // 도서 내 현재 퍼센트

    public int mWindowWidth=0;                      // WebView의 가로넓이 ( from javascript )

    boolean __chapterLoadPrior = false;             // 가로/세로 스크롤시 쳅터 이동 발생 flag
    boolean __chapterLoadNext = false;              // 가로/세로 스크롤시 쳅터 이동 발생 flag

    boolean __scrollByID = false;                   // ID값에 의한 스크롤 이동 flag
    String __scrollID="";                           // ID 값

    boolean __scrollByHighlightID = false;
    String __scrollHighlightID="";

    boolean __scrollByPATH = false;                 // PATH값에 의한 스크롤 이동 flag
    String __scrollPATH = "";                       // Path 값

    boolean __scrollByKeywordIndex = false;
    int __scrollKeywordIndex = -1;

    boolean __scrollByPage = false;                 // Page에 의한 스크롤 이동 flag
    boolean __scrollByPageFromPath = false;         // Path로 부터 구해진 page로 스크롤 이동  flag
    int     __scrollPage = 0;                       // Page 값

    boolean __scrollByOffset = false;               // Offset( 쳅터 내에서의 Page ) 값에 의한 이동 flag
    int __scrollOffset = 0;                         // Offset 값

    boolean __scrollByPosition = false;             // Position( 쳅터 인덱스 ) 값에 의한 이동 flag
    int __scrollPosition = 0;                       // Position 값

    boolean __scrollByPercent = false;              // Percent(전체) 값에 의한 이동 flag
    boolean __scrollByPercentInChapter = false;     // Percent(쳅터 내) 값에 의한 이동 flag
    double __scrollPercent = 0.0;                   // Percent 값

    boolean __moveByPercent = false;             	// 전체 Percent 값에 의한 이동 요청 flag
    double 	__movePercent = 0;                      // Percent 값

    boolean __scrollForFocusText = false;           // FocusText 에 의한 스크롤 이동

    boolean __chapterLoading = false;               // chapter 로딩중인지 여부 ( start: EPUB_CHAPTER_LOADING, end : EPUB_PAGE_READY )
    boolean __forceChapterChanging = false;         // EPUB_FORCE_CHAPTER_CHANGING 이벤트 발생시 true

//    boolean useVolumeKey = false;

    public float mScale=1f;                         // WebView의 density

    private Paint mPaint;                           // text selection Drawing

    PopupWindow mContextMenu=null;                  // selection context menu object

    FrameLayout mParent;

    final int FLAG_CLEAR = 0;
    final int CHANGE_BEFORE = 1;
    final int PAGE_READY = 4;
    boolean __reloadBook = false;
    int __reloadFlag = FLAG_CLEAR;

    private ArrayList<Highlight> mHighlights = new ArrayList<>();               // highlights of the content
    private ArrayList<Bookmark> mBookmarks = new ArrayList<>();                 // bookmarks of the content

    private HashMap<Integer, String> mChapterString = new HashMap<>();	        // 각 챕터의 Drm 해제한 챕터 스트링 보관

    private boolean mChapterStringcomplete = false;
    private boolean mChapterStringSaving = false;

    private int mDeltaY=0;
    private FrameLayout container;
    private boolean isChapterScrolling=false;

    private boolean mMergedAnnotation = false;

    private Bookmark __currentBookmark = null;      // last created bookmark

    int __orientation = -1;                         // last orientation mode

    String __searchText = null;                     // search keyword ( from Paginator )

    boolean __canFocusSearchResult = false;         // focusSearchResult Drawing flag
    SearchResult __focusSearchResult = null;        // focused Search result object

    volatile boolean __onCloseBook = false;         // Viewer 종료 시그널
    boolean __currentPageInfo = false;              // 현재 페이지의 북마크 정보 취득 flag, 현재 페이지의 정보를 취득한 후에 쳅터 로딩을 하는 것이  다르다.

    String __linkValue = "";                        // content 내의 Hyper Link object
    boolean __currentPageInfoForLinkJump = false;   // Link에 의한 점프후 현재 페이지 정보를 취득 ( 자세한 내용은 함수에서 .. )
    boolean __currentPageInfoForPageJump = false;   // Page에 의한 점프후 현재 페이지 정보를 취득
    boolean __currentPageInfoForJump = false;       // Percent에 의한 점프후 현재 페이지 정보를 취득

    boolean __currentTopPath = false;               // just get current page info not chapter loading ...

    boolean __requestPageMove = false;

    boolean isPreventPageMove = false;				//좌우 터치나 플리킹시 이동을 막고싶은 경우 true

    boolean __firstBookLoad = false;

    boolean __previewMode = false;                  // 환경 설정이나 기타 popup 윈도우로 인하여 뷰어가 일시 정지 상태가 되어야 하는 경우 true

    AnnotationHistory __bmHistory = new AnnotationHistory();    // bookmark history class
    AnnotationHistory __hlHistory = new AnnotationHistory();    // highlight history class

    int __slideRightIn;                             // slide animation
    int __slideRightOut;
    int __slideLeftIn;
    int __slideLeftOut;

    boolean __focusedScroll=false;                  // scroll 후 focus text

    boolean __forceSaveLastPosition = false;        // onScrollAfter이후 페이지 정보를 강제로 저장

    boolean __loadBook = false;

    private boolean isViewerLoadingComplete = false;

    boolean __doBookmarkShow = false;               // bookmark drawing

    Bookmark __anchorBookmark = null;               // 마지막 bookmark의 위치를 유지할 목적으로 사용

    Thread mDecodeThread = null;

    public ChromeClient myChromeClient;

    private ArrayList<Rect> mTTSHighlightRectList = new ArrayList<>();

    private ReadingSpine mReadingSpine; //spine 리스트 관리 객체
    private ReadingChapter mReadingChapter; //목차 리스트 관리 객체
    private EpubFile mEpubFile; //epub parsing data 관리 객체

    private String mDrmKey = null;

    public int mHtmlDirectionType = 0; //해당 html 파일 내 direction type (0 : ltr, 1 : rtl)
    public int mSpineDirectionType = 0; //opf 파일 내 direction type (0 : ltr, 1 : rtl)

    int bodyTopBottomMargin;	// 스크롤 모드 시 상하 버튼 영역

    public double perInchapter=0.0;	// 챕터 내 퍼센트 % - seekbar 이동 및 스크롤 모드 시 챕터 내 위치 정보

    public BookmarkManager mBookmarkManager;

    private boolean selectionHandler = false;

    private int scrollTopThreshold;
    private int scrollBottomThreshold;

    private String currentSelectedText = "";

    private int landingPage = -1;

    private boolean isMergedMemo=false;         // 신규 메모 시 false, 기존 메모 수정 시 (범위 이동으로 인한 메모도) true - new custom selection added

    private boolean isStartHandlerTouched = false;
    private boolean isEndHandlerTouched = false;

    public Drawable mStartHandle;
    public Drawable mEndHandle;

    private float mContextMenuHeight;
    private int mContextMenuTopMargin;
    private int mContextMenuBottomMargin;

    private ArrayList<Rect> selectionRectList = new ArrayList<>();
    private ArrayList<Rect> modifiedSelectionRect = new ArrayList<>();

    private int targetX;
    private int targetY;

    private BookHelper.ContextMenuType contextMenuType = null;

    private Timer autoScrollTimer = null;

    private boolean scrollToTop = false;
    private boolean scrollToBottom = false;
    private int[] touchedXY = new int[2];

    ViewerContainer.OnVideoInfoListener mOnVideoInfoListener=null;
    public void setOnVideoInfoListener(ViewerContainer.OnVideoInfoListener l) {
        mOnVideoInfoListener = l;
    }

    OnDecodeContent mOnDecodeContent = null;
    //    OnNightModeChanged mOnNightModeChanged = null;
    OnTouchEventListener mOnTouchEventListener = null;
    OnChapterChange mOnChapterChange = null;
    ViewerContainer.OnContextMenu mOnContextMenu = null;
    OnPageScroll    mOnPageScroll = null;
    OnTagClick      mOnTagClick = null;
    OnBookStartEnd  mOnBookStartEnd = null;
    OnPageBookmark  mOnPageBookmark = null;
    OnSearchResult  mOnSearchResult = null;
    OnCurrentPageInfo mOnCurrentPageInfo = null;
    OnReportError mOnReportError = null;
    OnMemoSelection mOnMemoSelection = null;
    OnTextSelection mOnTextSelection = null;
    OnViewerState mOnViewerState = null;
    ViewerContainer.OnAnalyticsListener mOnAnalyticsListener = null;

    OnMoveToLinearNoChapterListener mMoveToLinearNoChapter = null;
    public void setMoveToLinearNoChapter(OnMoveToLinearNoChapterListener listener){
        mMoveToLinearNoChapter = listener;
    }

    OnMediaControlListener mOnMediaControlListener = null;
    public void setOnMediaControlListener(OnMediaControlListener listener) {
        mOnMediaControlListener = listener;
    }

    public AudioContentPlayer audioContentPlayer;
    public OnAudioListener audioListener;
    public void setOnAudioListener(OnAudioListener listener){
        audioListener = listener;
    }

    private ClickArea touchedPositionDuringPlaying;
    private MediaOverlayController mediaOverlayController;
    private OnMediaOverlayStateListener mOnMediaOverlayStateListener = null;
    public void setOnMediaOverlayStateListener(OnMediaOverlayStateListener listener){
        mOnMediaOverlayStateListener = listener;
        mediaOverlayController.setOnMediaOverlayStateListener(listener);
    }

    public boolean asidePopupStatus = false; 	// aside popup status -> touch event flag
    OnNoterefListener mOnNoterefListener = null;
    public void setOnNoterefListener(OnNoterefListener listener){
        mOnNoterefListener= listener;
    }

    /**
     * 	 Drm을 사용할 경우 호출되는 이벤트
     *   @param l : OnDecodeContent 리스너 객체
     */
    public void setOnDecodeContent(OnDecodeContent l) {
        mOnDecodeContent = l;
    }

    /**
     * 	 뷰어의 상태에 따라 발생  start, end
     *   @param l : OnViewerState 리스너 객체
     */
    public void setOnViewerState(OnViewerState l) {
        mOnViewerState = l;
    }

    /**
     * 	 text selection start, end
     *   @param l : OnTextSelection 리스너 객체
     */
    public void setOnTextSelection(OnTextSelection l) {
        mOnTextSelection = l;
    }

    /**
     * 	 뷰어에서 배경색 변경시 야간모드 여부를 전달받을 리스너를 등록하는 메소드
     *   @param l : OnNightModeChanged 리스너 객체
     */
//    public void setOnNightModeChanged(OnNightModeChanged l) {
//        mOnNightModeChanged = l;
//    }

    public void setOnMemoSelection(OnMemoSelection l) {
        mOnMemoSelection = l;
    }

    /**
     * 	 뷰어 에러 발생 시 전달받을 리스너를 등록하는 메소드
     *   @param l : OnReportError 리스너 객체
     */
    public void setOnReportError(OnReportError l) {
        mOnReportError = l;
    }

    /**
     * 	 뷰어에서 현재 페이지 정보를 전달받을 리스너를 등록하는 메소드
     *   @param l : OnCurrentPageInfo 리스너 객체
     */
    public void setOnCurrentPageInfo(OnCurrentPageInfo l) {
        mOnCurrentPageInfo = l;
    }

    /**
     * 	 뷰어에서 검색 결과를 전달받을 리스너를 등록하는 메소드
     *   @param l : OnSearchResult 리스너 객체
     */
    public void setOnSearchResult(OnSearchResult l) {
        mOnSearchResult = l;
    }

    /**
     * 	 뷰어에서 북마크에 대한 내용을 전달받을 리스너를 등록하는 메소드
     *   @param l : OnPageBookmark 리스너 객체
     */
    public void setOnPageBookmark(OnPageBookmark l ) {
        mOnPageBookmark = l;
    }

    /**
     * 	 뷰어에서 터치 이벤트에 대한 내용을 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnTouchEventListener 리스너 객체
     */
    public void setOnTouchEventListener(OnTouchEventListener listener) {
        mOnTouchEventListener = listener;
    }

    /**
     * 	 뷰어에서 챕터 로딩에 대한 내용을 전달받을 리스너를 등록하는 메소드
     *   @param l : OnChapterChange 리스너 객체
     */
    public void setOnChapterChange(OnChapterChange l) {
        mOnChapterChange = l;
    }

    /**
     * 	 뷰어에서 메뉴 활성 여부를 전달받을 리스너를 등록하는 메소드
     *   @param l : OnSelectionMenu 리스너 객체
     */
    public void setOnSelectionMenu(ViewerContainer.OnContextMenu l) {
        mOnContextMenu = l;
    }

    /**
     * 	 페이지 스크롤이 발생할때.
     *   @param l : OnPageScroll 리스너 객체
     */
    public void setOnPageScroll(OnPageScroll l) {
        mOnPageScroll= l;
    }

    /**
     * 	 이미지/Link 등을 선택할 때
     *   @param l : OnTagClick 리스너 객체
     */
    public void setOnTagClick(OnTagClick l) {
        mOnTagClick = l;
    }

    /**
     * 	 컨텐츠의 시작/끝 통지
     *   @param l : OnBookStartEnd 리스너 객체
     */
    public void setOnBookStartEnd(OnBookStartEnd l) {
        mOnBookStartEnd = l;
    }

    public void setOnAnalyticsListener(ViewerContainer.OnAnalyticsListener listener){
        mOnAnalyticsListener = listener;
    }

    //****************************************************************************

    //Error Code
    private static final int EPUB_SETUP_CHAPTER_ERROR = 0;
    private static final int EPUB_TEXT_SELECTION_ERROR = 1;
    private static final int EPUB_BOOKMARK_ERROR = 2;
    private static final int EPUB_LINK_ERROR = 4;
    private static final int EPUB_HIGHLIGHTING_ERROR = 5;

    /**
     * MyJavaScriptObject class - javascript interface용 클래스
     */
    class MyJavaScriptObject {

        @JavascriptInterface
        public void setCurrentInnerChapter(String chapterId){
            if(chapterId.isEmpty()){
                mReadingChapter.setCurrentChapter(mReadingSpine.getCurrentSpineInfo().getSpinePath());
            } else {
                mReadingChapter.setCurrentChapter(mReadingSpine.getCurrentSpineInfo().getSpinePath()+'#'+chapterId);
            }
        }

        @JavascriptInterface
        public void overflowedMemoContent(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_OVERFLOW_MEMO_CONTENT));
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
        public void overflowedTextSelection(int overflowType){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_OVERFLOW_TEXT_SELECTION, overflowType));
        }

        @JavascriptInterface
        public void invalidateSelectionDraw(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_VIEWER_REFRESH));
        }

        @JavascriptInterface
        public void setLandingPage(int landingPage){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SET_LANDING_PAGE, landingPage));
        }

        @JavascriptInterface
        public void mergeAllMemoText(String ids){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_MERGE_ALL_MEMO, ids));
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
                                __hlHistory.remove(high.uniqueID);
                            }
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
        public void finishTextSelectionMode(){
            currentSelectedText = "";
            selectionRectList.clear();
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CONTEXT_MENU_HIDE));
        }

        @JavascriptInterface
        public void setSelectedText(String selectedText){
            currentSelectedText = selectedText;
        }

        @JavascriptInterface
        public void drawSelectionRect(String rectList, boolean isExistHandler) {
            selectionHandler = isExistHandler;
            if(selectionHandler)
                mTextSelectionMode = true;

            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_DRAW_SELECTION_RECT, rectList));
        }

        /**
         * javascript log 출력 메소드
         * @param _s
         */
        @JavascriptInterface
        public void print(String _s) {
            DebugSet.d(TAG, _s);
        }

        /**
         * WebView에 설정된 dir 값을 전달
         * @param type 0 : ltr, 1 : trl
         */
        @JavascriptInterface
        public void reportDirectionType(int type) {
            mHtmlDirectionType = type;
        }

        /**
         * 에러가 발생한 경우
         * text selection 에 대한 모든 값들을 초기화 한다.
         *
         *	@param errCode
         *   - 0 : setupChapter failed
         *   - 1 : getElementOffset failed
         *   - 2 : scroll failed ( bad page number or greater then pageCount
         */
        @JavascriptInterface
        public void reportError(int errCode) {
            if(errCode==2) return;

            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_REPORT_ERROR, errCode));
        }

        @JavascriptInterface
        public void turnOnNightModeDone(boolean isNightMode) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_NIGHT_MODE,isNightMode));
        }

        /**
         * 비디오 태그 클릭 시 fullScreen으로 보는 경우 사용하는 메소드
         * @param url
         */
        @JavascriptInterface
        public void playVideo(final String url){
            DebugSet.w(TAG, "playVideo ========================== "  + url);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    showFullScreenVideo(url);
                }

            }).start();
        }

        @JavascriptInterface
        public void videocontrol( String src){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_VIDEO_CONTROL, src));
        }

        /**
         *
         * HitTestResult
         *      - image/link/video/audio 가 아닐경우 단순 click 이벤트를 처리 한다.
         * @param url
         * @param type      : image/link
         * @param x
         * @param y
         * @param singleTap : click or double click
         */
        @JavascriptInterface
        public void HitTestResult(final String url, final int type, final int x, final int y, final boolean singleTap, final boolean isExceptionalTagOrAttr) {
            DebugSet.w(TAG, "HittestResult ========================== "  + type + "| " + url);

            if(asidePopupStatus)
                return;

            Thread linkThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    if( BookHelper.allowTagSelect && url != null && url.length() > 0 ) {
                        switch(type) {
                            case 1: {
                                //image일 경우에는 src를 받아온다 2015.04.01
                                if( !singleTap  ){
                                    String fileName = url.replaceAll(" ", "+").replaceAll("/", "%2F");
                                    Uri uri = Uri.parse(URLDecoder.decode(fileName));
                                    String filePath = URLDecoder.decode(fileName).replaceAll("file://", "");
                                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_IMAGE_TAG_CLICK, filePath));
                                    return;
                                }
                                break;
                            }
                            case 2: {
                                String fileName = url.replaceAll(" ", "+").replaceAll("/", "%2F");
                                String hrefValue = URLDecoder.decode(fileName);
                                if( hrefValue.length() > 0 && singleTap ) {
                                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_LINK_TAG_CLICK, hrefValue));
                                    return;
                                }
                                break;
                            }
                            case 4: {
                                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_VIDEO_TAG_CLICK, url));
                                return;
                            }
                            default : {
                                return;
                            }
                        }
                    }
                    {
//                        MotionEvent event = EPubViewerInputListener.touchEvent;
                        if(!isExceptionalTagOrAttr) {
                            MotionEvent event = ViewerActionListener.touchEvent;
                            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_CLICK, event));
                        }
                    }
                }
            });
            linkThread.start();
        }

        /**
         *
         * updatePosition
         *      - 스크롤이 발생한 후에 호출 된다.
         *
         * @param currentPage   : 현재 페이지
         * @param position      : 현재 페이지 * innerWidth
         */
        @JavascriptInterface
        public void updatePosition(int currentPage, int position) {

            mCurrentPageInChapter = currentPage;
            mCurrentPageIndexInChapter = currentPage;

            if( __moveByPercent ){
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PERCENT,__movePercent));
            } else{
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PAGE_SCROLL_AFTER));
            }
        }

        /**
         *
         * pageReady
         *      - setupChapter(...) 를 수행한 후에 호출 된다.
         *      - 이후 다음 작업들에 대한 flag를 설정하므로써 셋업과 동시에 scroll같은 처리를 할수 있다.
         *
         * @param pageCount     : pagination 이후의 페이지 수
         * @param windowWidth   : inner-width
         */
        @JavascriptInterface
        public void pageReady(float pageCount, int windowWidth) {
            DebugSet.d(TAG, "pageReady >>>>>>>>>>>>>> " + pageCount );
            __reloadFlag = PAGE_READY;

            mTotalPageInChapter = (int)Math.ceil(pageCount);
            mWindowWidth = windowWidth;

            if(BookHelper.animationType==3)
                mTotalPageInChapter=1;

            if( mWebviewInnerHandler != null ) {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PAGE_READY));
            }

            int currentChapter = mReadingSpine.getCurrentSpineIndex();
            if( __chapterLoadPrior && currentChapter >= 0 ) {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_LOAD_PRIOR_CHAPTER));
            }  else if( __chapterLoadNext && currentChapter >= 0 ) {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_LOAD_NEXT_CHAPTER));
            } else {
                if( __scrollByID ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_ID, __scrollID));
                }
                else if( __scrollByHighlightID ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_HIGHLIGHT_ID, __scrollHighlightID));
                }
                else if( __scrollByPATH ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PATH, __scrollPATH));

                }
                else if( __scrollByPage ) {
                    if( __scrollByPageFromPath ) {
                        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PATH, __scrollPATH));

                    } else {
                        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PAGE, __scrollPage));
                    }
                }
                else if( __scrollByOffset ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_OFFSET, __scrollByOffset));
                }
                else if( __scrollByPosition ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_POSITION, __scrollPosition));
                }
                else if( __scrollForFocusText ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_FOCUS, __scrollPage));
                }
                else if( __scrollByPercentInChapter ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PERCENT_IN_CHAPTER, __scrollPercent));
                }
                else if( __scrollByKeywordIndex ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_KEYWORD_INDEX, __scrollKeywordIndex));
                }
                else {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PAGE_SCROLL,null));
                }
            }
        }

        /**
         *
         * reportBookmarkForPage
         *      - 페이지 스크롤시 isBookmark() 를 통해 현재 페이지에 북마크가 존재하고 있는지의 여부를 알려준다.
         */
        @JavascriptInterface
        public void reportBookmarkForPage(boolean isBookmarkVisible) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_CHECK,isBookmarkVisible));
        }

        /**
         *
         * reportBookmarkPath
         *      - getCurrentPageInfo() 등의 함수를 통해 현재 페이지의 정보를 취득하려는 경우.
         *      - 경우에 따라 북마크의 path정보를 취득하지 못하는 경우 쳅터내의 page를 percent로 환산하여 저장.
         *
         * @param path      : 취득한 북마크의 path
         * @param bShown    : show/hide 여부
         */
        @JavascriptInterface
        public void reportBookmarkPath(String path, boolean bShown) {

            try {
                Bookmark bmd = null;
                if( bShown ) {
                    for(Bookmark bm: mBookmarks ) {
                        if( bm.path.equals(path) ) {
                            bmd = bm;
                            break;
                        }
                    }
                    if( bmd==null ) {
                        throw new Exception();
                    }
                } else {

                    JSONObject object;
                    String chapterId = "";
                    int page = 0;
                    String elPath = "";
                    String text = "";
                    String tagName="";
                    String type = AnnotationConst.FLK_BOOKMARK_TYPE_TEXT;

                    if( path != null ) {
                        object = new JSONObject(path);
                        chapterId = object.getString("id");         // chapterId
                        page = object.getInt("page");
                        elPath = object.getString("elementPath");
                        text = object.getString("elementText");
                        tagName = object.getString("tagName");
                        if(tagName.isEmpty()){
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_TEXT;
                        } else if(tagName.equalsIgnoreCase(ElementName.AUDIO)){
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_AUDIO;
                            text = BookHelper.getFilename(text);
                        } else if(tagName.equalsIgnoreCase(ElementName.VIDEO)){
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_VIDEO;
                            text = BookHelper.getFilename(text);
                        } else if(tagName.equalsIgnoreCase(ElementName.IMG)){
                            type = AnnotationConst.FLK_BOOKMARK_TYPE_IMG;
                            text = BookHelper.getFilename(text);
                        }
                    } else {
                        page = mCurrentPageInChapter;
                    }

                    int pageInChapter = page;
                    bmd = new Bookmark(chapterId, page, elPath);
                    if( __currentBookmark != null ) {
                        bmd.uniqueID = __currentBookmark.uniqueID;
                    }
                    bmd.text = text;
                    bmd.type = type;
                    bmd.chapterFile = mReadingSpine.getCurrentSpineInfo().getSpinePath();

                    if(BookHelper.animationType!=3)
                        bmd.percent = ((double)pageInChapter / (double)mTotalPageInChapter) * 100;
                    else
                        bmd.percent = perInchapter;

                    double startPercent = mReadingSpine.getCurrentSpineInfo().getSpineStartPercentage();
                    double havePercent = mReadingSpine.getCurrentSpineInfo().getSpinePercentage();
                    bmd.percentInBook = startPercent + havePercent * ((double)bmd.percent/100);
                    bmd.deviceModel = DeviceInfoUtil.getDeviceModel();
                    bmd.osVersion = DeviceInfoUtil.getOSVersion();
                }

                // 북마크 취득후에 행해질 특수 기능들
                if( __onCloseBook ) {
                    // Viewer 종료 프르세스
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_VIEWER_CLOSE, bmd));
                }
                else if( __currentPageInfo ) {
                    // 현재 페이지 정보 취득
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CURRENT_PAGE_INFO, bmd));
                }
                else if( __currentPageInfoForPageJump || __currentPageInfoForLinkJump || __currentPageInfoForJump ) {
                    // 현재 페이지 정보 취득 & onGet 이벤트 발생
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_NOTIFY_CURRENT_PAGE_INFO, bmd));
                }
                else if( __forceSaveLastPosition ) {
                    // 현재 페이지 정보 취득 후 마지막 읽은 위치 저장
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SAVE_LAST_POSITION,bmd));
                }
                else if( __doBookmarkShow ){
                    // 현재 페이지 정보 취득후 북마크 show/hide
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_SHOW, bmd));
                }
                else if( __currentTopPath ) {
                    // 현재 페이지 정보 취득
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_GET_CURRENT_TOP_PATH, bmd));
                }
            } catch(Exception e) {
                if( __onCloseBook ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_VIEWER_CLOSE, null));
                }
                else if( __currentPageInfo ) {  // TODO :: 정확한 용도 알아보기
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CURRENT_PAGE_INFO,null));
                }
                else if( __currentPageInfoForPageJump || __currentPageInfoForLinkJump || __currentPageInfoForJump ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_NOTIFY_CURRENT_PAGE_INFO, null));
                }
                else if( __forceSaveLastPosition ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SAVE_LAST_POSITION,null));
                }
                else if( __doBookmarkShow ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_SHOW,null));
                }
                else if( __currentTopPath ) {
                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_GET_CURRENT_TOP_PATH, null));
                }
            }
        }

        /**
         * showContextMenu
         *      - selection context menu
         *      - 재활성화 시 highlight id 보내 메모 판단 가능하도록 함
         *      - id가 없는 경우 text selection start
         * @param highlightID
         * @param menuTypeIndex 0 : 신규 / 1 : 신규+페이지넘김 / 2 : 수정 / 3 : 수정+페이지넘김 / 4 : 형광펜이어긋기
         * @param contextMenuPosition 컨텍스트 그릴 기준 핸들러 포지션 ( START : 시작핸들러 / END : 종료핸들러 / CENTER : 시작,종료 다 안보이는 경우 )
         */
        @JavascriptInterface
        public void showContextMenu(String highlightID, int menuTypeIndex, String contextMenuPosition,
                                    int endRight, int endTop, int endBottom, int startLeft, int startTop, int startBottom) {
            if(__forceChapterChanging){
                return;
            }

            mTextSelectionMode = true;

            endRight = Web2Scr(endRight) + EPubViewer.this.getScrollX();
            endTop = Web2Scr(endTop) + EPubViewer.this.getScrollY();
            endBottom = Web2Scr(endBottom) + EPubViewer.this.getScrollY();
            startLeft = Web2Scr(startLeft) + EPubViewer.this.getScrollX();
            startTop = Web2Scr(startTop) + EPubViewer.this.getScrollY();
            startBottom = Web2Scr(startBottom) + EPubViewer.this.getScrollY();

            // #1. handler가 보이는지 체크
            // #2. context menu draw 가능 여부 체크
            // #3. 위치 보정 및 전달
            if(endRight - getScrollX() >  getWidth() || endTop -  getScrollY() > getHeight()){
                contextMenuPosition = "START";
                if(startLeft - getScrollX() < 0 || startTop - getScrollY() <  0){
                    contextMenuPosition = "CENTER";
                }
            }

            int handlerHeight = mStartHandle.getIntrinsicHeight()/3;

            PopupData contextMenuData;
            if(contextMenuPosition.equalsIgnoreCase("START")) {
                if(startTop - getScrollY() - mContextMenuTopMargin - mContextMenuHeight < 0) { // 시작 핸들러 위 메뉴 그렸는데 화면 벗어나는 경우 - 핸들러 아래로 메뉴 그려야 함
                    contextMenuData = new PopupData(highlightID, startLeft - getScrollX(), startBottom - getScrollY() + handlerHeight + mContextMenuBottomMargin, BookHelper.ContextMenuType.values()[menuTypeIndex]);
                } else {
                    contextMenuData = new PopupData(highlightID, startLeft - getScrollX(), (int) (startTop - getScrollY() - mContextMenuTopMargin - mContextMenuHeight), BookHelper.ContextMenuType.values()[menuTypeIndex]);
                }
            } else if (contextMenuPosition.equalsIgnoreCase("END")){
                if(endTop - getScrollY() - mContextMenuTopMargin - mContextMenuHeight < 0) { // 종료 핸들러 위 메뉴 그렸는데 화면 벗어나는 경우 - 핸들러 아래로 메뉴 그려야 함
                    contextMenuData = new PopupData(highlightID, endRight - getScrollX(), endBottom - getScrollY() + handlerHeight + mContextMenuBottomMargin, BookHelper.ContextMenuType.values()[menuTypeIndex]);
                } else {
                    contextMenuData = new PopupData(highlightID, endRight - getScrollX(), (int) (endTop - getScrollY() - mContextMenuTopMargin - mContextMenuHeight), BookHelper.ContextMenuType.values()[menuTypeIndex]);
                }
            } else {
                contextMenuData = new PopupData(highlightID, getWidth()/2, (int) (getHeight()/2 - mContextMenuHeight/2), BookHelper.ContextMenuType.values()[menuTypeIndex]);
            }
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CONTEXT_MENU_SHOW, contextMenuData));
        }

        /**
         * 하이라이트 색상이 변경된 경우 처리하는 메소드
         * @param highlightID
         * @param clrIndex
         */
        @JavascriptInterface
        public void changeHighlightColor(String highlightID, int clrIndex, double percent) {    // TODO :: new custom selection - deleted

            for(Highlight h: mHighlights) {
                if( h.highlightID.equals(highlightID) ) {
                    h.colorIndex = clrIndex;
                    h.percent = percent;	// 앱개편 이전 데이터 수정 시 percent 갱신을 위해   // TODO :: new custom selection check

                    if( BookHelper.useHistory ){ //색상 변경도 히스토리에 변경항목으로 추가(동기화를 위함)
                        long newID = System.currentTimeMillis() / 1000L;
                        __hlHistory.modifyRemove(h.uniqueID, newID);
                        __hlHistory.modifyAdd(newID);
                        h.uniqueID = newID;
                    }
                    break;
                }
            }
        }

        /**
         *
         * finishedApplyingHighlight
         *      - Highlight 종료시
         *
         * @param isAnnotation
         */
        @JavascriptInterface
        public void finishedApplyingHighlight(boolean isAnnotation) {
            DebugSet.d(TAG, "finishedApplyingHighlight >>>> " + isAnnotation);
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CONTEXT_MENU_HIDE));
        }

        /**
         * addAndUpdateRange
         *      - highlightFromSelection -> checkMergeAnnotation -> highlightText 의 순으로 highlighting이 진행됨
         *      - 이 함수에서는 highlight의 병합 및 삭제처리를 하게 된다.
         */
        @JavascriptInterface
        public void checkMergeAnnotation(String jsonString) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHECK_MERGE_ANNOTATION, jsonString));
        }

        /**
         *
         * saveHighlight
         *      - highlightText 에서 호출.
         *      - highlight 프로세스가 완료된 후에 마지막으로 highlight 정보를 저장하기 위해 호출.
         *
         * @return void
         * @param json
         */
        @JavascriptInterface
        public void saveHighlight(String json) {
            DebugSet.d(TAG, "saveHighlight >>>>>> " + " [" + mHighlights.size() + "] " + json );
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_ADD_HIGHLIGHTED_DATA,json));
        }

        @JavascriptInterface
        public void reportFocusRect(String json) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_REPORT_FOCUS_RECT,json));
        }

        @JavascriptInterface
        public void imgResizingDone(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_IMG_RESIZING_DONE));
        }

        @JavascriptInterface
        public void stopMediaOverlay(){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_MEDIAOVERLAY_PAUSE));
        }

        @JavascriptInterface
        public void createAudioContents(String json) {

            HashMap<String, AudioContent> audioContents = new HashMap<String, AudioContent>();

            try{
                JSONArray jsArray = new JSONArray(json);

                for(int i = 0; i < jsArray.length(); i++){
                    JSONObject jsObj = jsArray.getJSONObject(i);

                    AudioContent audioContent = new AudioContent();

                    if(!jsObj.isNull("xpath"))
                        audioContent.setXPath(jsObj.getString("xpath"));
                    if(!jsObj.isNull("source"))
                        audioContent.setSource(jsObj.getString("source"));
                    if(!jsObj.isNull("loop"))
                        audioContent.setLoop(true);
                    if(!jsObj.isNull("autoplay"))
                        audioContent.setAutoplay(true);
                    if(!jsObj.isNull("controls"))
                        audioContent.setControls(true);
                    if(!jsObj.isNull("muted"))
                        audioContent.setMuted(true);
                    if(!jsObj.isNull("preload"))
                        audioContent.setPreload(jsObj.getString("preload"));
                    // duraion은 metadata info에서 뽑아온다
                    audioContent.setDuration(getDuration(jsObj.getString("source")));

                    audioContents.put(jsObj.getString("xpath"), audioContent);
                }

                if(audioContentPlayer.audioContentReader!=null)
                    audioContentPlayer.audioContentReader.audioContents = audioContents;

                audioListener.finishAudioList(audioContents);
            } catch (JSONException e) {
                Log.d("DEBUG","createAudioContents() JSONException");
            }
        }

        @JavascriptInterface
        public void didPlayAudio(String xPath, double startTime) {
            audioListener.didPlayAudio(xPath, startTime);
        }

        @JavascriptInterface
        public void didPauseAudio(String xPath, double currentTime) {
            if(currentTime>0)
                audioListener.didPauseAudio(xPath, currentTime);
            else
                audioListener.didStopAudio(xPath);
        }

        @JavascriptInterface
        public void didFinishAudio(String xPath) {
            audioListener.didFinishAudio(xPath);
        }

        @JavascriptInterface
        public void updateCurrentPlayingPosition(String xPath, double currentTime) {
            audioListener.updateCurrentPlayingPosition(xPath, currentTime);
        }

        @JavascriptInterface
        public void didPlayPreventMedia(String id, String mediaType) {
            mOnMediaControlListener.didPlayPreventMedia(id, mediaType);
        }

        @JavascriptInterface
        public void stopScrolling(double scrollY) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_STOP_SCROLLING, scrollY));
        }

        @JavascriptInterface
        public void finishScrollToBottom(double scrollY) {
            perInchapter = scrollY;
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_START_SCROLL_ANIMATION));
        }

        @JavascriptInterface
        public void setIdListByPoint(final String json){
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PLAY_SELECTED_MEDIAOVERLAY, json));
        }

        @JavascriptInterface
        public void setIDListOnCurrentPage(String json) {
            boolean hasMediaOverlayOnPage=false;
            try {
                JSONArray jsonArr = new JSONArray(json);
                for(int index=0; index<jsonArr.length(); index++){

                    LinkedHashMap<String, SmilSync> smilSync = mediaOverlayController.getSmilSyncs();

                    if(smilSync!=null && smilSync.get(jsonArr.get(index))!=null){
                        hasMediaOverlayOnPage = true;
                        break;
                    }
                }
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_HAS_MEDIAOVERLAY, hasMediaOverlayOnPage));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void scrollNextPage() {  // 미디어 오버레이 중 다음 페이지 이동 ( 챕터 이동도 가능해야 함 )
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_PAGE_OR_CHAPTER, 1));
        }

        @JavascriptInterface
        public void setAsidePopupStatus(boolean status){
            asidePopupStatus = status;
            if(mOnNoterefListener!=null){
                if( asidePopupStatus){
                    mOnNoterefListener.didShowNoterefPopup();
                } else{
                    mOnNoterefListener.didHideNoterefPopup();
                }
            }
        }

        @JavascriptInterface
        public String getMemoIconPath(){
            return BookHelper.memoIconPath;
        }

        @JavascriptInterface
        public void setPercentInChapter(double percent){
            perInchapter = percent;
        }

        @JavascriptInterface
        public boolean getTextSelectionMode(){
            return mTextSelectionMode;
        }
    }

    public EPubViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public EPubViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EPubViewer(Context context) {
        super(context);
        init(context);
    }

    public EPubViewer(Context context, String instName) {
        super(context);
        init(context);
        __ID = instName;
    }

    /**
     * Spine 정보 세팅
     * @param readingSpine
     */
    public void setSpineInfo(ReadingSpine readingSpine) {
        this.mReadingSpine = readingSpine;
    }

    /**
     * 목차 정보 세팅
     * @param readingChapter
     */
    public void setChapterInfo(ReadingChapter readingChapter) {
        this.mReadingChapter = readingChapter;
    }

    /**
     * epub 파싱 정보 세팅
     * @param epubFile
     */
    public void setEpubFileInfo(EpubFile epubFile) {
        this.mEpubFile = epubFile;
    }

    private Rect getRectFromJsonObject(JSONObject rect) {
        try {
            int left = this.Web2Scr(rect.getInt("left"));
            int top = this.Web2Scr(rect.getInt("top"));
            int right = left + this.Web2Scr(rect.getInt("width"));
            int bottom = top + this.Web2Scr(rect.getInt("height"));
            return new Rect(left,top,right,bottom);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // WebView의 density
    public float getScaled(int value) {
        return Math.round( value * mScale );
    }

    // WebView ==> Screen 좌표변환
    public int Web2Scr(int value) {
        return Math.round( value * mScale );
    }

    // Screen ==> WebView 좌표변환
    public int Scr2Web(int value) {
        return Math.round( value / mScale );
    }

    /**
     * 	 선택된 텍스트를 요청하는 메소드
     *   @return String : 텍스트 String을 리턴
     */
    public String getSelectedText() {
        return currentSelectedText;
    }

    boolean setDisplayMode() {
        if( BookHelper.pageNumOnLandscape == 2 && BookHelper.animationType!=3) {
            if( BookHelper.getOrientation(mContext) == Configuration.ORIENTATION_LANDSCAPE ) {
                BookHelper.twoPageMode = 1;
            } else {
                BookHelper.twoPageMode = 0;
            }
        } else {
            BookHelper.twoPageMode = 0;
        }
        __orientation = BookHelper.getOrientation(mContext);
        return true;
    }

    /**
     * 	 텍스트를 선택 시 표시해주는 이미지 설정 메소드
     *   @param start : 셀렉션 컨트롤 시작 이미지
     *   @param end : 셀렉션 컨트롤 끝 이미지
     */
    public void setSelectionIcon(Drawable start, Drawable end) {
        mStartHandle = start;
        mEndHandle = end;
    }

    /**
     * 	 셀렉션 관련 컨텍스트 메뉴 UI 정보 설정 메소드
     *   @param height : 메뉴 높이
     *   @param topMargin : 상위로 그릴 경우 기준 여백
     *   @param bottomMargin : 하위로 그릴 경우 기준 여백
     */
    public void setContextMenuSize(float height, int topMargin, int bottomMargin) {
        mContextMenuHeight = convertDpToPixels((int) height);
        mContextMenuTopMargin = convertDpToPixels(topMargin);               // 글자 기준 dp -> px
        mContextMenuBottomMargin = convertDpToPixels(bottomMargin);         // 핸들러 기준 dp -> px
    }

    /**
     * 셀렉션 해제 처리 메소드
     */
    public void removeSelection(){     // TODO :: new custom selection - modified
//        SendMessage(EPUB_SELECTION_END, null, 50);
        finishTextSelectionMode();
    }

    /**
     * 	 설정 미리보기 모드가 on인지 off 인지 여부를 설정하는 메소드 (미리보기를 도서 내용에서 바로 사용할 경우에 설정 진입 시 on 해주어야 함)
     *   @param onoff : 설정 미리보기 on/off 여부
     */
    public void setPreviewMode(boolean onoff) {
        __previewMode = onoff;
    }

    public void preventPageMove(boolean isPrevent) {
        isPreventPageMove = isPrevent;
    }

//    public void setHandMode(int handmode){
//        if(handmode == 0){
//            rightHandMode = true;
//        }else{
//            rightHandMode = false;
//        }
//    }

    /**
     * 볼륨키로 페이지 이동 여부 설정 메소드
     */
//    public void setUseVolumeKey(boolean bAble){
//        useVolumeKey = bAble;
//    }

//    private Drawable backgroundMag;
//    backgroundMag = getResources().getDrawable(R.drawable.comment_bg);

    void init(Context context) {
        mContext = context;

        myChromeClient = new ChromeClient(mContext);

        setWebChromeClient(myChromeClient);
        setWebViewClient(new ViewerClient(mContext));

        addJavascriptInterface( new MyJavaScriptObject(), "selection" );
        setLongClickable(false);
        setHorizontalScrollBarEnabled(false);

        if(BookHelper.animationType!=3)
            setVerticalScrollBarEnabled(false);

        getSettings().setJavaScriptEnabled(true);
        getSettings().setNeedInitialFocus(false);
        getSettings().setAppCacheEnabled(true);
        getSettings().setTextSize(TextSize.NORMAL);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        getSettings().setRenderPriority(RenderPriority.HIGH);
        getSettings().setDefaultTextEncodingName("UTF-8");
        getSettings().setPluginState(PluginState.ON);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setUseWideViewPort(true);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        BookHelper.deviceType = BookHelper.getDevice(mContext);

        setDisplayMode();

        mBookmarkManager = new BookmarkManager(EPubViewer.this, __bmHistory);

        mScale = getScale();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(BookHelper.textSelectionColor);

        mWebviewInnerHandler = new WebviewInnerHandler(this);


        if( mOnViewerState != null ) {
            mOnViewerState.onStart();
        }

        audioContentPlayer = new AudioContentPlayer();
        audioContentPlayer.setWebView(this);
        audioContentPlayer.addAudioPlayerJSInterface();
        audioContentPlayer.setOnAudioContentPlayerListener(new OnAudioContentPlayerListener() {

            @Override
            public void existAudioContentsOncurrentPage(ArrayList<String> audioXPathList) {
                audioListener.existAudioContentsOncurrentPage(audioXPathList);
            }

            @Override
            public void didFailAudio(String xPath) {
                Log.d("DEBUG","didFailAudio invalid playing xPath : "+xPath);
            }
        });
    }

    /**
     * 사용자 정보 데이터를 모두 저장하는 메소드
     */
    public void saveAllViewerData() {

        if( !__loadBook ) return;

        if(BookHelper.animationType!=3)
            saveLastPosition( null );

        saveHighlights();
        saveBookmarks();
        saveOption();
    }

    void versionInspect() {
        BookHelper.deviceType = BookHelper.getDevice(mContext);
    }


    /**
     * 	 도서 로딩을 시작하는 메소드-epub의 분석이 끝나고 모든 정보를 로드한후에 수행한다.
     */
    public void showBook() {
        __firstBookLoad = true;
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));
    }

    /**
     *
     * loadBook
     *      - loadBook은 epub 객체를 할당하고 분석한 후, 기타 정보의 로드를 모두 완료한다.
     */
    /**
     * 	 해당 Path에 있는 도서 정보 및 사용자 데이터를 확인하여 정상 로드 가능 여부를 전달하는 메소드
     *   @param path : 해당 도서의 RootPath
     *   @return boolean 성공 여부
     */
    public boolean loadBook(String path) {

        try {

            isViewerLoadingComplete = false;

            loadOption();

            restoreBookmarks();
            restoreHighlights();

            mParent = (FrameLayout)getParent();

            __loadBook = true;
            __firstBookLoad = true;

        }  catch(Exception e) {
            e.printStackTrace();
            __loadBook = false;
        }
        return __loadBook;

    }

    // html 로딩 후 컬러마이징 처리 및 모든 뷰어 스타일을 세팅을 요청하는 메소드
    public void setupChapter() {

        int width = getWidth();
        int height = getHeight();

        float density = this.getResources().getDisplayMetrics().density;

        JSONArray array = new JSONArray();
        ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();
        for(Highlight h : mHighlights) {
            if( spine != null ) {
                String hFile = h.chapterFile.toLowerCase();
                String chapterFile = spine.getSpinePath().toLowerCase();
                if(hFile.equals(chapterFile)) {
                    array.put(h.get2());
                }
            }
        }

        StringBuilder script = new StringBuilder().append("javascript:setupChapter(");
        script.append(array.toString()).append(",");
        script.append(BookHelper.deviceType).append(",");
        script.append(BookHelper.twoPageMode).append(",");
        script.append(BookHelper.nightMode).append(",");
        script.append(width).append(",");
        script.append(height).append(",");
        script.append(BookHelper.leftMargin).append(",");
        script.append(BookHelper.topMargin).append(",");
        script.append(BookHelper.rightMargin).append(",");
        script.append(BookHelper.bottomMargin).append(",");
        script.append(BookHelper.paraSpace).append(",");
        script.append(density).append(",");
        script.append(android.os.Build.VERSION.SDK_INT).append(",");
        script.append("").append(BookHelper.animationType).append(",");
        if(BookHelper.backgroundColor!=null){
            String clr = String.format("#%06X", BookHelper.backgroundColor);
            script.append("'").append(clr).append("',");
        } else {
            script.append("").append((String)null).append(",");
        }
        script.append("").append(BookHelper.indent).append(",");
        script.append("'").append(BookHelper.paraSpace + "%").append("',");
        script.append("'").append(BookHelper.lineSpace + "%").append("',");
        script.append("'").append(BookHelper.faceName).append("',");
        script.append(BookHelper.maxSelectionLength).append(",");
        script.append("").append(bodyTopBottomMargin).append(")");

        DebugSet.d(TAG, "setupChapter >>>>>>>>>>> " + script.toString());

        loadUrl(script.toString());
    }

    /**
     * loadPriorChapter - 이전 쳅터를 로드하고 쳅터의 마지막 페이지로 이동한다.
     */
    boolean loadPriorChapter() {
        int numChapter = mReadingSpine.getCurrentSpineIndex() - 1;

        if( numChapter >= 0 ) {

            forceChapterChanging();

            __chapterLoadPrior = true;

            mCurrentPageInChapter--;
            mReadingSpine.setCurrentSpineIndex(numChapter);

            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));

            return true;
        }
        else {
            if( mOnBookStartEnd != null ) {
                mOnBookStartEnd.onStart();
                if(BookHelper.animationType==3){
                    isChapterScrolling = false;
                    container.setVisibility(View.GONE);
                }
            }
            return false;
        }
    }

    /**
     * loadNextChapter - 다음 쳅터를 로드하고 쳅터의 처음으로 이동한다.
     */
    boolean loadNextChapter() {

        int numChapter = mReadingSpine.getCurrentSpineIndex() + 1;

        int size = mReadingSpine.getSpineInfos().size();

        if( numChapter < size ) {

            forceChapterChanging();

            __chapterLoadNext = true;

            mCurrentPageInChapter++;
            mReadingSpine.setCurrentSpineIndex(numChapter);

            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));

            mCurrentPageIndexInChapter = 0;

            return true;
        } else {
            if( mOnBookStartEnd != null ) {
                mOnBookStartEnd.onEnd();
                if(BookHelper.animationType==3){
                    isChapterScrolling = false;
                    container.setVisibility(View.GONE);
                }
            }
            return false;
        }

    }

    /**
     * scrollPrior - 이전 페이지로 이동 ( 쳅터간 이동이 가능하다 )
     */
    public void scrollPrior() {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_PAGE_OR_CHAPTER,-1));
    }

    /**
     * scrollNext - 다음 페이지로 이동 ( 쳅터간 이동이 가능하다 )
     */
    public void scrollNext() {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_PAGE_OR_CHAPTER,1));
    }

    /**
     * scrollPage - nPage 수 만큼 이동 ( 쳅터 이동이 가능하다 )
     * @param nPage     : - 값이면 이전 페이지로, + 값이면 다음 페이지로 이동
     */
    public void scrollPage(int nPage) {

        if(mOnPageScroll != null) {
            removeSearchHighlight();
            mOnPageScroll.onScrollBefore(mCurrentPageInChapter);
        }

        DebugSet.d(TAG, "scrollPage ----------- current : " + mCurrentPageIndexInChapter +  ", nPage : " + nPage);

        int pageNumber = mCurrentPageIndexInChapter + nPage;
        boolean chapterChanged=false;
        if( pageNumber < 0 ) {
            chapterChanged = loadPriorChapter();
        } else if( pageNumber > mTotalPageInChapter -1 ) {
            chapterChanged = loadNextChapter();
        }

        if( !chapterChanged ) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PAGE_SCROLL,pageNumber));
        }
    }

    /**
     * 	 pageNumber로 scroll ( 현재 쳅터만 해당 )
     *   @param pageNumber : 이동할 페이지 번호
     */
    public void scroll(int pageNumber) {

        if( pageNumber < 0 ) {
            pageNumber = 0;
        } else if( pageNumber > mTotalPageInChapter -1 ) {
            pageNumber = mTotalPageInChapter;
        }

//        __requestPageMove = true;
        boolean updatePageInfo = true;
        String script = (new StringBuilder())
                .append("javascript:scrollPage(")
                .append(pageNumber).append(",")
                .append(BookHelper.twoPageMode).append(")").toString();

        loadUrl(script);
    }

    /**
     *
     * goPageByLink
     *      - hyper link에 의한 이동
     *
     * @param fileName
     * @param id
     */
    public void goPageByLink(String fileName, String id) {

        int currentSpineIndex = mReadingSpine.getCurrentSpineIndex();

        if( fileName.endsWith("/") )
            fileName = fileName.substring(0, fileName.length()-1);
        String targetFilePath = fileName.toLowerCase();

        mReadingSpine.setCurrentSpineIndex(targetFilePath);

        if( currentSpineIndex != mReadingSpine.getCurrentSpineIndex()) {

            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {

                goPageByChapter();

                if( id!=null && id.length() > 0 ) {
                    __scrollByID = true;
                    __scrollID = id;
                }
            }
        } else {
            __scrollByID=true;
            __scrollID=id;
            goPageByID(id);
        }
    }

    /**
     * goPageByJump()
     *      - percent 방식에서 seekbar 이동시 호출
     *      - history 저장용
     */
    public void goPageByJump() {
        __currentPageInfoForJump = true;
        getCurrentPageInfo(false);
    }

    /**
     * goPage
     *      - highlight 선택에 의한 페이지 이동 함수
     * @return void
     * @param high
     */
    public void goPage(Highlight high) {

        __scrollByHighlightID = true;
        __scrollHighlightID = high.highlightID;

        String hFile = high.chapterFile.toLowerCase();
        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();

        if( !hFile.equals(current) ) {

            mReadingSpine.setCurrentSpineIndex(high.chapterFile);

            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {
                goPageByChapter();
            }
        }
        else {
            goPageByHighlightID(__scrollHighlightID);
        }
    }

    public int getNextChapterIndex(){

        int numChapter = mReadingSpine.getCurrentSpineIndex() + 1;
        int size = mReadingSpine.getSpineInfos().size();

        if( numChapter < size ) {
            mReadingSpine.setCurrentSpineIndex(numChapter);
            mReadingChapter.setCurrentChapter(mReadingSpine.getCurrentSpineInfo().getSpinePath());
            return numChapter;
        } else {
            if(mOnBookStartEnd != null ) {
                mOnBookStartEnd.onEnd();
                if(BookHelper.animationType==3){
                    isChapterScrolling = false;
                    container.setVisibility(View.GONE);
                }
            }
            return -1;
        }
    }

    public void moveByTTSData(TTSDataInfo ttsDataInfo){
        __scrollByPATH=true;
        __scrollPATH=ttsDataInfo.getXPath();
        showBookByPosition(mReadingSpine.getCurrentSpineIndex());
    }


    /**
     *
     * goPageMig
     *      - old version의 북마크 데이타인 경우 별도의 루틴에서 처리
     *
     * @return void
     * @param bmd
     */
    private void goPageMig(Bookmark bmd) {
        double percent = bmd.percent;

        int spineNum = 0;
        UnModifiableArrayList<ReadingOrderInfo> spines = null;

        try {
            spineNum = Integer.parseInt(bmd.extra1.trim());

            spines = mReadingSpine.getSpineInfos();
            if( spines != null && spines.size() <= spineNum ) {
                spineNum = spines.size()-1;
            }
        } catch(NumberFormatException e) {
            spineNum = 0;
        }

        String selectedFile = null;
        if( spines != null ) {
            selectedFile = spines.get(spineNum).getSpinePath().toLowerCase();
        }

        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();

        __scrollByPercentInChapter=true;
        __scrollPercent=percent;
        __currentBookmark = bmd;

        if( selectedFile == null ) {
            mReadingSpine.setCurrentSpineIndex(0);
            goPageByChapter();
        }
        else if( !selectedFile.equals(current) ) {
            mReadingSpine.setCurrentSpineIndex(selectedFile);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {
                goPageByChapter();
            }
        } else {
            goPageByPercentInChapter(percent);
        }
    }

    /**
     *
     * goPage
     *      - 북마크 선택에 의한 페이지 이동함수
     *
     * @return void
     * @param bmd
     */
    public void goPage(Bookmark bmd) {

        if( bmd.chapterFile.trim().equals("") ) {
            goPageMig(bmd);
            return;
        }

        if( bmd.path.trim().length() == 0 ) {
            __scrollByPercentInChapter=true;
            __scrollPercent=bmd.percent;
        }
        else {
            __scrollByPATH=true;
            __scrollPATH=bmd.path;
        }

        String bFile = bmd.chapterFile.toLowerCase();
        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();

        if( !bFile.equals(current) ) {
            mReadingSpine.setCurrentSpineIndex(bmd.chapterFile);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {
                goPageByChapter();
            }
        }
        else {
            if( __scrollByPATH )
                goPageByPath(bmd.path);
            else if( __scrollByPercentInChapter )
                goPageByPercentInChapter(bmd.percent);
        }
    }

    /**
     *
     * goPage
     *      - 검색 결과 선택에 의한 페이지 이동 함수
     *
     * @return void
     * @param sr
     */
    public void goPage(SearchResult sr) {

        __scrollByPATH=true;
        __scrollPATH=sr.path;

        __canFocusSearchResult = true;
        __focusSearchResult = sr;
        __focusedScroll = true;

        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();
        String file = sr.chapterFile.toLowerCase();

        if( !file.equals(current) ) {

            mReadingSpine.setCurrentSpineIndex(file);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {
                goPageByChapter();
            }
        } else {
            goPageByPath(sr.path);
        }

    }

    /**
     *
     * goPage
     *      - 쳅터 선택에 의한 페이지 이동 함수
     *
     * @return void
     * @param chapter
     */
    public void goPage(ChapterInfo chapter) {

        String targetFilePath = chapter.getChapterFilePath();
        String id = "";

        if(mEpubFile.hasLinearNo(targetFilePath.replace(mEpubFile.getPublicationPath(), ""))){
            mMoveToLinearNoChapter.moveToLinearNoChapter(targetFilePath);
            return;
        }

        if( targetFilePath.lastIndexOf("#") != -1 ){
            id = targetFilePath.substring(targetFilePath.lastIndexOf("#")+1);
            targetFilePath = targetFilePath.substring(0, targetFilePath.lastIndexOf("#"));
            __scrollByID=true;
            __scrollID=id;
        }

        String current = mReadingChapter.getCurrentChapter().getChapterFilePath().toLowerCase();
//        String chapterFile = chapter.getChapterFilePath().toLowerCase();
        if(current.lastIndexOf("#")!=-1){
            current = current.substring(0,current.lastIndexOf("#"));
        }

        //챕터 파일 네임이 없는 경우 이동처리 안함.
        if( targetFilePath.length() <= 0 )
            return;

        if(!targetFilePath.equalsIgnoreCase(current)){
            mReadingChapter.setCurrentChapter(chapter.getChapterFilePath());
            mReadingSpine.setCurrentSpineIndex(targetFilePath);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ){
                goPageByChapter();
            }
        } else {
            if(id.isEmpty()) {  // 같은 챕터에 id 없는 경우 이동 시
                goPageByChapter();
            } else {
                goPageByID(id);
            }
        }
    }

    /**
     *
     * goPage
     *      - position 값에 의한 페이지 이동함수
     *      - 여기서 position의 의미는 chapter index를 의미한다.
     *
     * @return void
     * @param position
     */
    public void goPage(int position) {

        if( __forceChapterChanging ) return;

        if( position < 0 || mReadingSpine.getSpineInfos().size() <= position )
            return;

        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath();
        String next = mReadingSpine.getSpineInfos().get(position).getSpinePath();

        if( !current.equals(next) ) {

            mReadingSpine.setCurrentSpineIndex(next);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ){
                goPageByChapter();
            }
        }
    }

    /**
     *
     * goPage
     *      - percent 값에 의한 페이지 이동
     *
     * 작성자    : YongWoon
     * 작성일    : 2013. 4. 23. 오후 5:17:52
     *
     * @return void
     * @param percent
     */
    public void goPage(double percent) {

        __movePercent = percent;
        __moveByPercent = true;

        int spineIndex = getSpineIndexFromPercent( __movePercent );

        if( spineIndex == mReadingSpine.getCurrentSpineIndex() ){

            perInchapter = ( __movePercent - getChapterStartPercent( spineIndex ) ) / getChapterHavePercent( spineIndex ) * 100;

            if(BookHelper.animationType!=3){
                int page = ( int ) ( mTotalPageInChapter * perInchapter/100 );
                scroll( page );
            } else if(BookHelper.animationType==3){
//				[ssin-scroll] s 스크롤 모드인 경우 챕터내에서 퍼센트로 이동하도록
                __requestPageMove = true;
                String script = (new StringBuilder())
                        .append("javascript:scrollByPercent('")
                        .append(perInchapter).append("',")
                        .append(BookHelper.twoPageMode).append(")").toString();
                loadUrl(script);
//				[ssin-scroll] e
            }
            __moveByPercent = false;
            __movePercent = -1.0d;
        } else {
            goPage(spineIndex);
        }
    }

    // goPage(... ) 함수류에서 호출되는 내부 함수이며  lastChapter에 정의된 쳅터 index에 따라 쳅터를 로딩한다.
    private void goPageByChapter() {

        forceChapterChanging();

        mCurrentPageIndexInChapter = 0;

        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));
    }

    //goPage(...) 류의 함수 내부에서 호출
    private void goPageByID(String id) {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_ID, id));
    }

    private void goPageByHighlightID(String id) {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_HIGHLIGHT_ID, id));
    }

    private void goPageByPath(String path) {
        if( path == null ) return;
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PATH, path));
    }

    private void goPageByPercentInChapter(double percent) {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PERCENT_IN_CHAPTER, percent));
    }

    /**
     *
     * loadBookContent
     *      - epub을 분석한 데이타를 토대로 컨텐츠를 webview에 로드한다.
     *      - Viewer 실행시 설정해 두었던 ( 혹은 기본값) 들에 의해 style과 각종 표현 icon등을 재설정한다.
     *
     * @return void
     * @param baseUrl   : 로드되는 컨텐츠의 파일위치
     * @param headSrc   : 파일의 head source
     * @param bodySrc   : 파일의 body source
     * @throws Exception
     */
    void loadBookContent(String baseUrl, String headSrc, String bodySrc, String htmlAttr, String bodyAttr, String docType) throws Exception {

        float scale = EPubViewer.this.getScale();
        int windowInnerWidth= Math.round(EPubViewer.this.getWidth()/scale);
        int windowInnerHeight= Math.round(EPubViewer.this.getHeight()/scale);
//        int windowInnerWidth= (int)Math.ceil(EPubViewer.this.getWidth()/scale);
//        int windowInnerHeight= (int)Math.ceil(EPubViewer.this.getHeight()/scale);
        int windowNumColumns=1;
        if(BookHelper.twoPageMode==1){
            windowNumColumns=2;
        }

        try {
            String baseStyle = BookHelper.getBaseStyle();
            if(BookHelper.animationType==3){
                bodyTopBottomMargin = convertDpToPixels(20);
                baseStyle=baseStyle.replaceAll("%feelingk_booktableheight_value%", "auto");
                baseStyle=baseStyle.replaceAll("%feelingk_booktablecolumnwidth_value%", "");
                baseStyle=baseStyle.replaceAll("%feelingk_body_width_value%", windowInnerWidth+"px !important");
                baseStyle=baseStyle.replaceAll("%feelingk_bodymargintop_value%", ""+bodyTopBottomMargin);
                baseStyle=baseStyle.replaceAll("%feelingk_bodymarginbottom_value%", ""+bodyTopBottomMargin);
                baseStyle=baseStyle.replaceAll("%feelingk_body_touch_action%", "");
            } else {
                baseStyle=baseStyle.replaceAll("%feelingk_booktableheight_value%", ""+ (windowInnerHeight-(BookHelper.topMargin+BookHelper.bottomMargin))+"px");
                baseStyle=baseStyle.replaceAll("%feelingk_booktablecolumnwidth_value%", " -webkit-column-width : "+(windowInnerWidth/windowNumColumns)+"px !important;" );
                baseStyle=baseStyle.replaceAll("%feelingk_body_width_value%", windowInnerWidth+"px !important");
                baseStyle=baseStyle.replaceAll("%feelingk_bodymargintop_value%", ""+0);
                baseStyle=baseStyle.replaceAll("%feelingk_bodymarginbottom_value%", ""+0);
                baseStyle=baseStyle.replaceAll("%feelingk_body_touch_action%", "touch-action: none;");
            }

            String fontStyle = "";
            if( BookHelper.faceName!=null && BookHelper.faceName!=""){
                fontStyle = BookHelper.getFontStyle(BookHelper.faceName, BookHelper.fontPath);
            }

            String style = new StringBuilder("\n<style type='text/css'>")
                    .append(baseStyle)
                    .append("\n</style>")
                    .append(fontStyle).toString();

            headSrc = headSrc.replaceAll("<title/>", "");

            String head = new StringBuilder("\n<head>")
                    .append(headSrc)
                    .append(style)
                    .append("\n</head>\n\n").toString();


            String body = new StringBuilder("\n"+ bodyAttr +"\n<div id='feelingk_booktable'>\n<div id='feelingk_bookcontent'>").append(bodySrc).append("\n</div>\n</div>\n</body>\n").toString();
            String html = new StringBuilder(docType+"\n\n"+htmlAttr).append(head).append(body).append("\n</html>\n").toString();

            String mimeType = "";
            if(baseUrl.indexOf(".xhtml")!=-1){
                mimeType = "application/xhtml+xml";
            } else {
                mimeType = "text/html";
            }

            loadDataWithBaseURL("file://" + replaceString(baseUrl), html, mimeType, "UTF-8", null);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private int convertDpToPixels(int value){
        float px = 0;
        try {
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mContext.getResources().getDisplayMetrics());
        } catch (Exception e) { e.printStackTrace(); }
        return (int) px;
    }

    //특정문자 제거 하기
    public static String replaceString(String str){
        try {
            str = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        str = str.replaceAll("\\+", " ");
        str = str.replaceAll("%2F", "/");
        return str;
    }

    void showBookByPosition(int pos) {  // 쳅터의 인덱스로 부터 load/show

        if( mReadingSpine.getSpineInfos().size() == 0 )
            return;

        if( pos < 0 )
            pos = 0;

        if( mReadingSpine.getSpineInfos().size()-1 < pos )
            pos = mReadingSpine.getSpineInfos().size() - 1;

        try {
            ReadingOrderInfo loadSpine = mReadingSpine.getSpineInfos().get(pos);
            String baseUrl = loadSpine.getSpinePath();
            String drmKey = getDrmKey();
            String decodeStr = getDecodeStr(drmKey, loadSpine.getSpinePath());
            if( decodeStr == null || decodeStr.trim().length() == 0 ){
                throw new Exception();
            }

            String bodySrc = "";
            String bodyAttr = "<body>";
            String htmlAttr = "<html>";
            String headSrc = "";
            String docType = "";	// [ssin] add : content docType
            if( loadSpine.getSpinePath().toLowerCase().endsWith(".svg") ){
                bodySrc = makeSVGContentsBody(decodeStr);
                headSrc = BookHelper.getHeadText(mContext) + makeSVGContentsHead(decodeStr);
            } else {
                bodySrc = BookHelper.getHtmlBody(decodeStr);
                bodyAttr = BookHelper.getBodyAttribute(decodeStr);
                bodyAttr = BookHelper.getBodyDetail(bodyAttr);	// [ssin] add : content body id,dir,style
                htmlAttr = BookHelper.getHtmlAttribute(decodeStr);
                docType = BookHelper.getDoctType(decodeStr);	// [ssin] add : content docType
                String head = BookHelper.getHtmlHead(decodeStr);
//                String headTemplate = getHtmlHeadWithCustomJs(head, mEpubFile.getPublicationPath());
                headSrc = head + "\n" + BookHelper.getHeadText(mContext);
            }
            loadBookContent(baseUrl, headSrc, bodySrc, htmlAttr, bodyAttr, docType);
        } catch(OutOfMemoryError e) {
            e.printStackTrace();
            mOnReportError.onError(0);
        } catch(Exception e) {
            e.printStackTrace();
            mOnReportError.onError(0);
        }
        System.gc();
    }

//    TODO :: font size 변환 건 원복 3.053
//    private String getHtmlHeadWithCustomJs(String headSrc, String epubFilePath) {
//        String linkReg = "<link[^>]+href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
//        Pattern pattern = Pattern.compile(linkReg);
//        Matcher matcher = pattern.matcher(headSrc);
//        while (matcher.find()) {
//            String cssPath = matcher.group(1).toString();
//            if( cssPath.toLowerCase().endsWith(".css")) {
//                headSrc = headSrc.replace(matcher.group(1), changeStyleSheetStr(matcher.group(1), cssPath, epubFilePath));
//            }
//        }
//        return headSrc;
//    }
//
//    private String changeStyleSheetStr(String targetLinkStr, String cssPath, String epubFilePath){
//
//        cssPath = cssPath.replaceAll("\\../", "");
//
//        String customFileDir = cssPath.substring(0, cssPath.lastIndexOf("/")+1);
//        String customFileName = BookHelper.getOnlyFilename(cssPath)+"_flk.css";
//        String customFilePath = customFileDir+customFileName;
//
//        FileInfo orgStyleSheetFileInfo = EpubFileUtil.getResourceFile(epubFilePath,cssPath);
//
//        if(orgStyleSheetFileInfo == null)
//            return targetLinkStr;
//
//        FileInfo customStyleSheetFullPath = EpubFileUtil.getResourceFile(epubFilePath, customFilePath);
//
//        if(customStyleSheetFullPath==null){
//
//            String orgStyleSheetStr = EpubFileUtil.readFile(orgStyleSheetFileInfo.filePath);
//
//            if(orgStyleSheetStr.isEmpty()) return orgStyleSheetFileInfo.filePath;
//
//            orgStyleSheetStr = replaceFixedUnit("font-size", ";", orgStyleSheetStr, 0);
//            orgStyleSheetStr = replaceFixedUnit("line-height", ";", orgStyleSheetStr, 0);
//
//            try {
//                String customFileFullPath;
//                if(epubFilePath.endsWith("/"))
//                    customFileFullPath = epubFilePath+customFilePath;
//                else
//                    customFileFullPath = epubFilePath+"/"+customFilePath;
//                File customStyleSheetFile = new File(customFileFullPath);
//                if( !customStyleSheetFile.exists() )
//                    customStyleSheetFile.createNewFile();
//                FileOutputStream outputStream = new FileOutputStream( customStyleSheetFile );
//                outputStream.write( orgStyleSheetStr.getBytes() );
//                outputStream.close();
//                return customFileFullPath;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            return customStyleSheetFullPath.filePath;
//        }
//        return orgStyleSheetFileInfo.filePath;
//    }
//
//    private String replaceFixedUnit(String startStr, String endStr, String targetStr, int startIndex){
//        int nextStartIndex = targetStr.indexOf(startStr, startIndex);
//        int nextEndIndex = targetStr.indexOf(endStr, nextStartIndex);
//        if(nextStartIndex!=-1 && nextEndIndex!=1){
//            String targetReplaceStr = targetStr.substring(nextStartIndex, nextEndIndex);
//            targetStr=targetStr.replace(targetReplaceStr, convertFontSize(targetReplaceStr));
//            return replaceFixedUnit(startStr, endStr, targetStr, nextEndIndex);
//        }
//        return targetStr;
//    }
//
//    private String convertFontSize(String targetReplaceStr) {
//
//        String[] sizeWithUnit = targetReplaceStr.split(":");
//
//        if(sizeWithUnit.length==0 || sizeWithUnit[1].contains("%"))
//            return targetReplaceStr;
//
//        if(sizeWithUnit[1].contains("px")){
//            double orgFontSize = Double.parseDouble(sizeWithUnit[1].replace("px",""));
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], String.format("%s%%",String.valueOf(orgFontSize * 6.25)));
//        } else if(sizeWithUnit[1].contains("pt")){
//            double orgFontSize = Double.parseDouble(sizeWithUnit[1].replace("pt",""));
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], String.format("%s%%",String.valueOf(orgFontSize *8.3)));
//        } else if(sizeWithUnit[1].contains("xx-small")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "50%");
//        } else if(sizeWithUnit[1].contains("x-small")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "62.50%");
//        } else if(sizeWithUnit[1].contains("small")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "80%");
//        } else if(sizeWithUnit[1].contains("medium")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "100%");
//        } else if(sizeWithUnit[1].contains("large")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "112.5%");
//        }  else if(sizeWithUnit[1].contains("x-large")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "150%");
//        }  else if(sizeWithUnit[1].contains("xx-large")){
//            targetReplaceStr = targetReplaceStr.replace(sizeWithUnit[1], "250%");
//        }
//        return targetReplaceStr;
//    }

    private String makeSVGContentsHead(String fileName){
        StringBuilder svgHead = new StringBuilder();

        int styleTagStart = fileName.indexOf("<?xml-stylesheet");
        int styleTagEnd = fileName.indexOf(">", styleTagStart) + 1;
        int styleLastTagEnd = fileName.lastIndexOf("?>") + 2;

        if( styleTagStart != -1 ){
            svgHead.append(fileName.substring(styleTagStart, styleLastTagEnd));
            svgHead = new StringBuilder(svgHead.toString().replace("<?xml-stylesheet", "<link rel=\"stylesheet\" ").replace("?>", "/>"));
        }

        //    	svgBody.append("<img src=\"" + fileName + "\" alt=\"svg image\"/>");
        //    	svgBody.append("<embed src=\"" + fileName + "\"/>");
        //    	svgBody.append("<object data=\"" + fileName/*.substring(fileName.lastIndexOf("/")+1)*/ + "\" id=\"flkSvgContent\"/>");

        DebugSet.d(TAG, "makeSVGContentsBody : " + svgHead.toString());

        return svgHead.toString();
    }

    private String makeSVGContentsBody(String fileName){
        StringBuilder svgBody = new StringBuilder();

        int svgTagStart = fileName.indexOf("<svg");
        int svgTagEnd = fileName.indexOf(">", svgTagStart) + 1;

        if( svgTagStart != -1 ){
            //    		svgBody.append("<svg>");
            svgBody.append(fileName.substring(svgTagStart, fileName.indexOf("</svg>")+6));
        }

        //    	svgBody.append("<img src=\"" + fileName + "\" alt=\"svg image\"/>");
        //    	svgBody.append("<embed src=\"" + fileName + "\"/>");
        //    	svgBody.append("<object data=\"" + fileName/*.substring(fileName.lastIndexOf("/")+1)*/ + "\" id=\"flkSvgContent\"/>");

        DebugSet.d(TAG, "makeSVGContentsBody : " + svgBody.toString());

        return svgBody.toString();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if( !isViewerLoadingComplete ){
            DebugSet.d(TAG, "onScrollChanged pageLoading... ");
            return;
        }

        DebugSet.d(TAG, "onScrollChanged l : " + l + ". t : " + t + ", oldl : " + oldl + ", width : " + this.getWidth() + ", requestMove : " + __requestPageMove);

        if(BookHelper.animationType==3 && mTextSelectionMode) {
            if( scrollToTop || scrollToBottom){
                if(isStartHandlerTouched || isEndHandlerTouched) {
                    loadUrl("javascript:setMoveRangeWithHandler(" + touchedXY[0] + "," + touchedXY[1] +","+isStartHandlerTouched+","+isEndHandlerTouched+")");
                } else {
                    loadUrl("javascript:setMoveRange(" + touchedXY[0] + "," + touchedXY[1] +")");
                }
            } else {
                if(isStartHandlerTouched || isEndHandlerTouched){
                    loadUrl("javascript:setMoveRangeWithHandler(" + touchedXY[0] + "," + touchedXY[1] +","+isStartHandlerTouched+","+isEndHandlerTouched+")");
                } else {
                    loadUrl("javascript:setMoveRange(" + touchedXY[0] + "," + touchedXY[1] +")");
                }
            }
        }

        if( mHtmlDirectionType == 1 )
            l = (mTotalPageInChapter * this.getWidth()) - l - this.getWidth();

        int currentPage = Math.round(l / this.getWidth());

        if( __requestPageMove ){
            __requestPageMove = false;
            mCurrentPageInChapter = currentPage;
            mCurrentPageIndexInChapter = currentPage;
            if( __moveByPercent ) {
                goPage(__movePercent);
            } else {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_PAGE_SCROLL_AFTER));
            }
        }
    }

    private void reloadSVG(){
        loadUrl("javascript:getSvgTag()");
    }

    /**
     * forceChapterChanging
     *      - 쳅터 이동간의 화면 조작을 위해 뷰어에서 사용.
     *      - 또는 뷰어에게 강제로 화면 전환을 알리기 위한 용도로도 사용될 수 있다.
     */
    public void forceChapterChanging() {
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_FORCE_CHAPTER_CHANGING));
    }

    /**
     * 	 text selection 시 사용될 popup window를 등록하는 메소드
     *   @param pw : 셀렉션 메뉴를 표시할 PopupWindow 객체
     */
    public void registSelectionMenu(PopupWindow pw) {
        mContextMenu = pw;
    }

    public void applyChapterAllHighlight(){
        JSONArray array = new JSONArray();
        ReadingOrderInfo chapter = mReadingSpine.getCurrentSpineInfo();
        for(Highlight h : mHighlights) {
            if( chapter != null ) {
                String hFile = h.chapterFile.toLowerCase();
                String chapterFile = chapter.getSpinePath().toLowerCase();
                if(hFile.equals(chapterFile)) {
                    array.put(h.get2());
                }
            }
        }
        loadUrl("javascript:applyHighlights(" + array.toString() + ")");
    }

    public void deleteChapterAllHighlight(){
        JSONArray array = new JSONArray();
        ReadingOrderInfo chapter = mReadingSpine.getCurrentSpineInfo();
        for(Highlight h : mHighlights) {
            if( chapter != null ) {
                String hFile = h.chapterFile.toLowerCase();
                String chapterFile = chapter.getSpinePath().toLowerCase();
                if(hFile.equals(chapterFile)) {
                    array.put(h.get2());
                }
            }
        }
        loadUrl("javascript:deleteAllHighlights(" + array.toString() + ")");
    }

    /**
     *
     * deleteHighlight - 형광펜 삭제
     * @return void
     * @param id        : highlight ID
     */
    public void deleteHighlight(String id) {
        for(Highlight h: mHighlights) {
            if(h.highlightID.equals(id)) {
                deleteHighlight(h);
                break;
            }
        }
    }

    /**
     * 	 해당 하이라이트를 삭제 요청하는 메소드
     *   @param high : 삭제할 하이라이트 객체
     */
    public void deleteHighlight(Highlight high) {
        JSONArray hiLite = new JSONArray();
        hiLite.put(high.get2());
        mHighlights.remove(high);
        if( BookHelper.useHistory ) {
            __hlHistory.remove(high.uniqueID);
        }
        loadUrl("javascript:deleteHighlights(" + hiLite.toString() + ")");
        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CONTEXT_MENU_HIDE));
    }

    public void searchResultFocusRect(String json) {

        if( __canFocusSearchResult ) return;

        try {
            if( json.trim().length() > 0 ) {
                JSONObject object = new JSONObject(json);
                int page = object.getInt("page");
                String text = object.getString("text");

                __focusSearchResult = new SearchResult(mReadingSpine.getCurrentSpineInfo().getSpinePath(), text, page);
                __focusSearchResult.chapterFile = mReadingSpine.getCurrentSpineInfo().getSpinePath();
                __focusSearchResult.clear();

                JSONArray jsArray = object.getJSONArray("bounds");
                for(int i=0; i<jsArray.length(); i++) {
                    JSONObject rectObject = jsArray.getJSONObject(i);
                    Rect rc = getRectFromJsonObject(rectObject);
                    __focusSearchResult.rects.add(rc);
                }

                __canFocusSearchResult = true;
                if( __focusSearchResult.rects.size() <= 0 ) {
                    __canFocusSearchResult = false;
                }
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PAGE,page));
            } else {
                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_PAGE,0));
            }
        } catch(Exception e) {
            e.printStackTrace();
            __canFocusSearchResult = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);

        if( !__ID.equals("EPUBViewer") ) return;

        if( w == ow ) return;

        if( mTextSelectionMode ) {
            finishTextSelectionMode();
        }

        if( __reloadBook ) {
            reLoadBook();
        } else {
            __reloadBook = true;
        }
    }

    /**
     * 	 화면 회전등으로 인하여 리로딩이 필요한 경우 사용.
     */
    private boolean isReload = false;
    public void reLoadBook() {
        DebugSet.d(TAG, "########### RE-LOAD BOOK #################");

        isViewerLoadingComplete = false;
        ViewerActionListener.preventGestureEvent=false;

        asidePopupStatus = false;

        isReload = true;

        isChapterScrolling = false;
        if(container!=null && container.getVisibility()==View.VISIBLE)
            container.setVisibility(View.GONE);

        forceChapterChanging();

        if(mContextMenu!=null && mContextMenu.isShowing() || mTextSelectionMode) {
            finishTextSelectionMode();
        }

        if( __anchorBookmark != null ) {
            if( __anchorBookmark.path.trim().length() > 0 ) {
                __scrollByPATH = true;
                __scrollPATH = __anchorBookmark.path;
            } else {
                __scrollByPercentInChapter = true;
                __scrollPercent = __anchorBookmark.percent;
            }
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));
        } else {
            __currentPageInfo = true;
            getCurrentPageInfo(false);
        }

        __canFocusSearchResult = false;
        __focusSearchResult = null;
    }

    /**
     * 	 설정 진입 시 보고 있던 페이지 정보를 저장하기 위한 메소드
     */
    public void getCurrentTopPath() {
        __currentTopPath = true;
        getCurrentPageInfo(false);
    }

    /**
     *   현재 페이지에 북마크 유무를 확인하는 메소드
     */
    public void hasBookmark() {

        if( mBookmarks.size() <= 0 ) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_CHECK,false));
            return;
        }

        // 저장한 bookmark 목록에서 현재 chapter와 일치 하는 북마크 만 저장.
        // script 호출하여 보이는지 여부에 따라 이벤트 발생.
        ChapterInfo currentChapter = mReadingChapter.getCurrentChapter();
        ReadingOrderInfo currentSpine = mReadingSpine.getCurrentSpineInfo();
        if( currentSpine == null ) return;

        String chapterFile = currentSpine.getSpinePath().toLowerCase();

        ArrayList<String> array = new ArrayList<>();

        for(Bookmark bm: mBookmarks) {

            if( bm.path.trim().equals("") ) {
                // 이전 데이타 존재시
                String chName = bm.chapterName.toLowerCase().trim();
                String chapterName = currentChapter.getChapterName().toLowerCase().trim();

                if( chapterName.equals(chName) ) {
                    int page = (int)(((double)(mTotalPageInChapter-1) * bm.percent)/100);
                    if( page == mCurrentPageIndexInChapter ) {
                        mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_CHECK,true));
                        return;
                    }
                }
            } else {
                String fileName = bm.chapterFile.toLowerCase();
                if( chapterFile.equals(fileName) )
                    array.add(bm.path);
            }
        }

        if( array.size() <= 0 ) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_BOOKMARK_CHECK,false));
            return;
        }

        JSONArray jarr = new JSONArray(array);
        loadUrl("javascript:getBookmarkForPage(" + jarr.toString() +  ")" );
    }

    /**
     * 	 해당 페이지에 북마크 추가/삭제 요청하는 메소드(없으면 추가, 있으면 삭제)
     */
    public void doBookmark() {

        String currentFile = mReadingSpine.getCurrentSpineInfo().getSpinePath();

        // gathering id_tag information
        ArrayList<String> array = new ArrayList<>();
        for (int i = 0; i < mReadingChapter.getChapters().size(); i++) {
            ChapterInfo ch = mReadingChapter.getChapters().get(i);
            String chapterId = "";
            String src = ch.getChapterFilePath();
            if (src.lastIndexOf("#") != -1) {
                chapterId = src.substring(src.lastIndexOf("#") + 1);
            }
            if (chapterId != null && src.contains(currentFile)) {
                array.add(chapterId);
            }
        }
        JSONArray carr = new JSONArray(array);
        array.clear();

        for( Bookmark bm: mBookmarks ) {
            String file = bm.chapterFile.toLowerCase();
            if( file.equals( currentFile ) ) {
                array.add( bm.path );
            }
        }
        JSONArray barr = new JSONArray(array);

        __doBookmarkShow = true;

        loadUrl("javascript:getBookmarkPath(" + carr.toString() + "," + barr.toString() + "," + BookHelper.twoPageMode + ")" );
    }

    /**
     * 	 현재 페이지 정보를 요청하는 메소드
     */
    public void getCurrentPageInfo(boolean isClose) {
        String currentFile = mReadingSpine.getCurrentSpineInfo().getSpinePath();
//        String currentChapterPath = mReadingChapter.getCurrentChapter().getChapterFilePath().toLowerCase();
        ArrayList<String> array = new ArrayList<String>();
        for (int i = 0; i < mReadingChapter.getChapters().size(); i++) {
            ChapterInfo ch = mReadingChapter.getChapters().get(i);
            String chapterId = "";
            String src = ch.getChapterFilePath();

            if (src.lastIndexOf("#") != -1) {
                chapterId = src.substring(src.lastIndexOf("#") + 1);
            }
            if (!chapterId.isEmpty() && src.contains(currentFile)) {
                array.add(chapterId);
            }
        }
        JSONArray carr = new JSONArray(array);

        array.clear();

        if(!isClose){
            for( Bookmark bm: mBookmarks ) {
                String file = bm.chapterFile.toLowerCase();

                if( file.equals( currentFile ) ) {
                    array.add( bm.path );
                }
            }
        }
        JSONArray barr = new JSONArray(array);

//        loadUrl("javascript:getBookmarkPath(" + carr.toString() + "," + barr.toString() + "," + BookHelper.twoPageMode + ")" );
        loadUrl("javascript:getCurrentInnerChapterId("+ carr.toString() + "," + barr.toString() + "," + BookHelper.twoPageMode + ")" );
    }

    ChapterInfo getChapterById(String file, String id) {

        for (int i = 0; i < mReadingChapter.getChapters().size(); i++) {
            ChapterInfo ch = mReadingChapter.getChapters().get(i);
            String filePath = ch.getChapterFilePath();
            String chapterId = "";

            if( filePath.lastIndexOf("#") != -1 ){
                chapterId = filePath.substring(filePath.lastIndexOf("#")+1);
                filePath = filePath.substring(0, filePath.lastIndexOf("#"));
            }

            if( !chapterId.equals("") ) {

                String src = file.toLowerCase();
                String fileName = filePath.toLowerCase();

                if( src.equals(fileName) && chapterId.equals(id) ) {
                    return ch;
                }
            }
        }
        return null;
    }

    /**
     * 추가된 북마트를 저장하는 메소드
     * @param path
     * @return
     */
    boolean addBookmarkingData(Bookmark path) {

        if( path == null ) return false;

        boolean bSuccess = false;

        String pFile = path.chapterFile.toLowerCase();

        for(Bookmark bm: mBookmarks) {
            String bFile = bm.chapterFile.toLowerCase();

            if( !(path.path.trim().length()==0) && bm.path.equals(path.path) && bFile.equals(pFile) ) {
                mBookmarks.remove(bm);
                if(BookHelper.useHistory)
                    __bmHistory.remove(bm.uniqueID);
                return false;
            }

            int pageSrc = (int)Math.round(((double)(mTotalPageInChapter) * bm.percent)/100);
            int pageDst = (int)Math.round(((double)(mTotalPageInChapter) * path.percent)/100);
            if( bFile.equals(pFile) && pageSrc == pageDst && BookHelper.animationType!=3) {
                mBookmarks.remove(bm);
                if(BookHelper.useHistory)
                    __bmHistory.remove(bm.uniqueID);
                return false;
            }
        }

        // id tag가 없는 경우
        if( path.chapterId==null || path.chapterId.length() <= 0 ) {
            ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();
            if( spine != null ) {
                path.chapterName = mReadingChapter.getChapterInfoFromPath(spine.getSpinePath()).getChapterName();
                bSuccess = true;
            }
        } else {
            ChapterInfo chapter = getChapterById(path.chapterFile, path.chapterId);
            if( chapter != null ) {
                path.chapterName = chapter.getChapterName();
            }
            bSuccess = true;
        }

        if( bSuccess ) {
            mBookmarks.add(path);
            if(BookHelper.useHistory)
                __bmHistory.add(path.uniqueID);
        }
        return bSuccess;
    }

    public ArrayList<Bookmark> getBookMarks() {
        return mBookmarks;
    }

    /**
     *
     * deleteBookmark
     *      - 북마크를 삭제한 후에 현재 보이는 페이지를 갱신해 줘야 하므로
     *      - front에서 hasBookMark() 호출해서 화면을 갱신할 필요가 있음.
     *
     * 작성자    : syw
     * 작성일    : 2012. 10. 17. 오후 4:08:02
     *
     * @return void
     * @param src
     */
    public void deleteBookmark(Bookmark src) {

        String srcFile = src.chapterFile.toLowerCase();

        boolean bRemoved = false;
        for(Bookmark bm: mBookmarks ) {
            String bFile = bm.chapterFile.toLowerCase();

            if( srcFile.trim().length() <= 0 || src.path.trim().length() <= 0 ) {
                if( bm.uniqueID == src.uniqueID ) {
                    if( BookHelper.useHistory) {
                        __bmHistory.remove(bm.uniqueID);
                    }
                    mBookmarks.remove(bm);
                    bRemoved = true;
                    break;
                }
            }
            else if( bm.path.equals(src.path) && bFile.equals(srcFile) ) {
                if(BookHelper.useHistory) {
                    __bmHistory.remove(bm.uniqueID);
                }
                mBookmarks.remove(bm);
                bRemoved = true;
                break;
            }
        }

        if( !bRemoved ) {
            if( BookHelper.useHistory)
                __bmHistory.remove(src.uniqueID);
            mBookmarks.remove(src);
        }

        if( mBookmarks.size() <= 0 ) {
            JSONArray jarr = new JSONArray();
            loadUrl("javascript:getBookmarkForPage(" + jarr.toString() +  ")" );
        }
    }

    /**
     * 마지막 읽은 위치를 저장하는 메소드
     * @param bm
     * @return
     */
    boolean saveLastPosition(Bookmark bm) {

        if( bm == null || (bm!=null && (bm.path.length()==0)) ) {
            try {
                bm = new Bookmark();
                ReadingOrderInfo currentSpine = mReadingSpine.getCurrentSpineInfo();
                if( currentSpine != null )
                    bm.chapterFile = currentSpine.getSpinePath().toLowerCase();

                int pageInChapter = mCurrentPageIndexInChapter+1;

                double startPercent = mReadingSpine.getCurrentSpineInfo().getSpineStartPercentage();
                double havePercent = mReadingSpine.getCurrentSpineInfo().getSpinePercentage();

                if(BookHelper.animationType!=3){
                    bm.percent = ((double)pageInChapter / (double)mTotalPageInChapter) * 100;
                } else{
                    bm.percent  = startPercent + havePercent * perInchapter;
                }

                if(Double.isInfinite(bm.percent) )
                    bm.percent = 0.0;

                bm.percentInBook = startPercent + havePercent * ((double)bm.percent/100);

                DebugSet.d(TAG, "saveLastPosition::percent >> " + bm.percent);
                DebugSet.d(TAG, "saveLastPosition::perInchapter >> " + bm.percentInBook);

            } catch(Exception e) {
                return false;
            }
        }

        boolean bSuccess = false;

        try {
            String filePath = getFullPath(BookHelper.readPositionFileName);
            DebugSet.d(TAG, "save last .................... " + bm.path + " | " + filePath);

            File bookmarkDataFile = new File(filePath);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

            JSONObject object = new JSONObject();
            //2013.10.31 DeviceModel, OSVersion, FileVersion, FileType 추가
            object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.READPOSITION);
            object.put(AnnotationConst.FLK_READPOSITION_VERSION, "2.0");
            object.put(AnnotationConst.FLK_READPOSITION_MODEL, DeviceInfoUtil.getDeviceModel());
            object.put(AnnotationConst.FLK_READPOSITION_OS_VERSION, DeviceInfoUtil.getOSVersion());
            object.put(AnnotationConst.FLK_READPOSITION_TIME, System.currentTimeMillis()/1000L);
            object.put(AnnotationConst.FLK_READPOSITION_PATH, bm.path);
            object.put(AnnotationConst.FLK_READPOSITION_FILE, BookHelper.getRelFilename(bm.chapterFile) );
            object.put(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT, bm.percent);
            object.put(AnnotationConst.FLK_READPOSITION_TOTAL_PERCENT, bm.percentInBook);		// [ssin] add total percent
            DebugSet.d(TAG, "json array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();
            bSuccess = true;
        } catch( Exception e ) {
            e.printStackTrace();
            bSuccess = false;
        }
        return bSuccess;
    }


    void restoreLastPosition() {

        String filePath = getFullPath(BookHelper.readPositionFileName);
        DebugSet.d(TAG, "restore last ........................ " + filePath);

        JSONObject object = EpubFileUtil.getJSONObjectFromFile(filePath);

        try {
            if( object == null ) {
                throw new Exception();
            }

            DebugSet.d(TAG, "restore last file........................ " + object.toString(1));

            String path = "";
            String file = "";
            if( object.isNull(AnnotationConst.FLK_READPOSITION_PATH) ||
                    object.isNull(AnnotationConst.FLK_READPOSITION_FILE) ) {
            } else {
                path = object.getString(AnnotationConst.FLK_READPOSITION_PATH);
                file = object.getString(AnnotationConst.FLK_READPOSITION_FILE);
            }

            if( file.trim().length() <= 0 ) {
                // oldVersion data
                if( object.isNull(AnnotationConst.FLK_READPOSITION_CHAPTER_INDEX) ||
                        object.isNull(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT) ) {

                    if( !object.isNull(AnnotationConst.FLK_READPOSITION_PERCENT) ) {
                        // goPage by Percent when paging done
                        __scrollByPercent = true;
                        __scrollPercent = object.getDouble(AnnotationConst.FLK_READPOSITION_PERCENT);
                    } else {
                        //첫 번째 챕터부터 fileName이 없을 수 있기 때문에 네임 체크 후 챕터 번호를 부여한다.
                        ReadingOrderInfo first = mReadingSpine.getSpineInfos().get(0);
                        mReadingSpine.setCurrentSpineIndex(0);
                        mReadingChapter.setCurrentChapter(first.getSpinePath());
                    }
                } else {
                    int index = object.getInt(AnnotationConst.FLK_READPOSITION_CHAPTER_INDEX);
                    double ch_percent = object.getDouble(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT);
                    ReadingOrderInfo spine = mReadingSpine.getSpineInfos().get(index);
                    mReadingSpine.setCurrentSpineIndex(index);
                    mReadingChapter.setCurrentChapter(spine.getSpinePath());
                    __scrollByPercentInChapter = true;
                    __scrollPercent = ch_percent;
                }
            } else {
                // new version data
                if( file.indexOf('\\') != -1 ) {
                    file = file.replace('\\', '/');
                }

                mReadingChapter.setCurrentChapter(file);

                if(file.indexOf("#")!=-1){
                    file = file.substring(0,file.indexOf("#"));
                }
                mReadingSpine.setCurrentSpineIndex(getFullPath(file));

                if( path.trim().length() <= 0 ) {
                    double ch_percent = object.getDouble(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT);
                    __scrollByPercentInChapter = true;
                    __scrollPercent = ch_percent;
                } else {
                    if(!object.isNull(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT)){
                        perInchapter = object.getDouble(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT);
                    }
                    __scrollByPATH = true;
                    __scrollPATH = path;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            // first load시 exception 강제 발생
            mReadingSpine.setCurrentSpineIndex(0);
            mCurrentPageInChapter = 0;
            mTotalPageInChapter = 0;
            mCurrentPageIndexInChapter = 0;
        }
    }

    /**
     * 	 북마크 파일을 데이터 형태로 불러오기를 요청하는 메소드
     */
    public void restoreBookmarks() {

        String fileName = getFullPath(BookHelper.bookmarkFileName);
        JSONObject object = EpubFileUtil.getJSONObjectFromFile(fileName);
        if( object == null ) return;

        mBookmarks.clear();

        try {
            DebugSet.d(TAG, "restore Bookmark ................ " + object.toString(1));

            JSONArray array = object.getJSONArray(AnnotationConst.FLK_BOOKMARK_LIST );

            for(int i=0; i<array.length(); i++) {

                JSONObject item = array.getJSONObject(i);

                long uniqueID;
                if( item.isNull(AnnotationConst.FLK_BOOKMARK_ID) ) {
                    uniqueID = System.currentTimeMillis() / 1000L;
                } else {
                    uniqueID = item.getLong(AnnotationConst.FLK_BOOKMARK_ID);
                }

                String creationTime = item.getString(AnnotationConst.FLK_BOOKMARK_CREATION_TIME);
                String file = item.getString(AnnotationConst.FLK_BOOKMARK_FILE);
                String path = item.getString(AnnotationConst.FLK_BOOKMARK_PATH);

                // for bookmark migration data
                if( file.trim().length() == 0 ) {
                } else {
                    file = getFullPath(file);
                }

                String model = item.has(AnnotationConst.FLK_BOOKMARK_MODEL) ? item.optString(AnnotationConst.FLK_BOOKMARK_MODEL) : DeviceInfoUtil.getDeviceModel();
                String osVersion = item.has(AnnotationConst.FLK_BOOKMARK_OS_VERSION) ? item.optString(AnnotationConst.FLK_BOOKMARK_OS_VERSION) : DeviceInfoUtil.getOSVersion();

                double percent = item.getDouble(AnnotationConst.FLK_BOOKMARK_PERCENT);
                String chapterName = item.getString(AnnotationConst.FLK_BOOKMARK_CHAPTER_NAME);
                int color = item.getInt(AnnotationConst.FLK_BOOKMARK_COLOR);
                String type = item.getString(AnnotationConst.FLK_BOOKMARK_TYPE);

                String text="";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_TEXT) ) {
                    text = item.getString(AnnotationConst.FLK_BOOKMARK_TEXT);
                }

                String extra1="";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA1) )
                    extra1 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA1);

                String extra2 = "";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA2) )
                    extra2 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA2);

                String extra3 = "";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA3))
                    extra3 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA3);


                if( file.indexOf('\\') != -1 ) {
                    file = file.replace('\\', '/');
                }

                int spineIndex = getSpineIndex(file);
                double startPercent = getChapterStartPercent(spineIndex);
                double havePercent = getChapterHavePercent(spineIndex);
                double currentPercent = startPercent + havePercent * ((double)percent/100);

                Bookmark bm = new Bookmark("", 0, path);
                bm.chapterFile = file.toLowerCase();
                bm.chapterName = chapterName;
                bm.uniqueID = uniqueID;
                bm.text = text;
                bm.percent = percent;
                bm.percentInBook = currentPercent;
                bm.creationTime = creationTime;
                bm.color = color;
                bm.type = type;
                bm.extra1 = extra1;
                bm.extra2 = extra2;
                bm.extra3 = extra3;
                bm.deviceModel = model;
                bm.osVersion = osVersion;
                mBookmarks.add(bm);
            }

            if(BookHelper.useHistory)
                __bmHistory.read(getFullPath(BookHelper.bookmarkHistoryFileName));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBookmarks(ArrayList<Bookmark> bookmarkList) {
        mBookmarks = bookmarkList;
        saveBookmarks();
    }
    /**
     *
     * 	 북마크 데이터를 파일로 저장 요청하는 메소드
     *   @return boolean : 성공 시 true 리턴
     */
    public boolean saveBookmarks() {

        try {

            String fileName = getFullPath(BookHelper.bookmarkFileName);
            if( fileName.length() <= 0 ) return false;

            DebugSet.d(TAG, "save Bookmark ................ " + fileName);

            File bookmarkDataFile = new File(fileName);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

            JSONObject object = new JSONObject();
            object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.BOOKMARK);
            object.put(AnnotationConst.FLK_BOOKMARK_VERSION, BookHelper.bookmarkVersion);

            JSONArray array = new JSONArray();
            for(Bookmark bm: mBookmarks) {
                array.put(bm.get());
            }
            object.put(AnnotationConst.FLK_BOOKMARK_LIST, array);

            DebugSet.d(TAG, "save array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();

            if(BookHelper.useHistory) {
                __bmHistory.write(getFullPath(BookHelper.bookmarkHistoryFileName));
            }

            return true;

        } catch( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 	 하이라이트 데이터를 파일로 저장 요청하는 메소드
     *  @return boolean : 성공 시 true 리턴
     */
    public boolean saveHighlights() {

        try {

            String fileName = getFullPath(BookHelper.annotationFileName);
            if( fileName.length() <= 0 ) return false;

            DebugSet.d(TAG, "save Highlight ................ " + fileName);

            File bookmarkDataFile = new File(fileName);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

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
                __hlHistory.write(getFullPath(BookHelper.annotationHistoryFileName));
            }
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 	 하이라이트 파일을 데이터 형태로 불러오기를 요청하는 메소드
     */
    public void restoreHighlights() {

        String fileName = getFullPath(BookHelper.annotationFileName);
        JSONObject object = EpubFileUtil.getJSONObjectFromFile(fileName);
        if( object == null ) return;

        mHighlights.clear();

        try {

            DebugSet.d(TAG, "restore highlight ................ " + object.toString(1));

            JSONArray array = object.getJSONArray(AnnotationConst.FLK_ANNOTATION_LIST);

            for(int i=0; i<array.length(); i++) {

                JSONObject jobj = array.getJSONObject(i);

                long uniqueID;
                if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_ID) ) {
                    uniqueID = System.currentTimeMillis() / 1000L;
                } else {
                    uniqueID = Long.parseLong(jobj.getString(AnnotationConst.FLK_ANNOTATION_ID));
                }

                String creationTime = jobj.getString(AnnotationConst.FLK_ANNOTATION_CREATION_TIME);
                String file = jobj.getString(AnnotationConst.FLK_ANNOTATION_FILE);
                file = getFullPath(file);

                String startPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_START_ELEMENT_PATH);
                int startChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHILD_INDEX);
                int startCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHAR_OFFSET);
                String endPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_END_ELEMENT_PATH);
                int endChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHILD_INDEX);
                int endCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHAR_OFFSET);
                double percent = jobj.getDouble(AnnotationConst.FLK_ANNOTATION_PERCENT);
                String chapterName = jobj.getString(AnnotationConst.FLK_ANNOTATION_CHAPTER_NAME);
                String type = jobj.getString(AnnotationConst.FLK_ANNOTATION_TYPE);
                String text = jobj.getString(AnnotationConst.FLK_ANNOTATION_TEXT);
                String memo = jobj.getString(AnnotationConst.FLK_ANNOTATION_MEMO);
                String extra1 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA1);
                String extra2 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA2);
                String extra3 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA3);

                String model = jobj.has(AnnotationConst.FLK_ANNOTATION_MODEL) ? jobj.optString(AnnotationConst.FLK_ANNOTATION_MODEL) : DeviceInfoUtil.getDeviceModel();
                String osVersion = jobj.has(AnnotationConst.FLK_ANNOTATION_OS_VERSION) ? jobj.optString(AnnotationConst.FLK_ANNOTATION_OS_VERSION) : DeviceInfoUtil.getOSVersion();

                int colorIndex;
                if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_COLOR) ) {
                    colorIndex = BookHelper.lastHighlightColor;
                }
//                else if(jobj.getInt(AnnotationConst.FLK_ANNOTATION_COLOR) == 5){
//                    colorIndex = BookHelper.lastMemoHighlightColor;
//                    uniqueID = addColorIndexHistory(uniqueID, i);
//                }
                else {
                    colorIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_COLOR);
                }

                int spineIndex = getSpineIndex(file);
                double startPercent = getChapterStartPercent(spineIndex);
                double havePercent = getChapterHavePercent(spineIndex);
                double currentPercent = startPercent + havePercent * ((double)percent/100);

                Highlight highlight = new Highlight();
                highlight.highlightID = generateUniqueID();
                highlight.startPath = startPath;
                highlight.endPath = endPath;
                highlight.startChild = startChildIndex;
                highlight.startChar = startCharOffset;
                highlight.endChild = endChildIndex;
                highlight.endChar = endCharOffset;
                highlight.deleted = false;
                highlight.annotation = memo.trim().length() > 0 ? true : false;
                highlight.chapterFile = file;
                highlight.text = text;
                highlight.memo = memo;
                highlight.spanId = highlight.highlightID + "_-_2";
                highlight.uniqueID = uniqueID;
                highlight.colorIndex = colorIndex;
                highlight.page = -1;
                highlight.percent = percent;
                highlight.percentInBook = currentPercent;
                highlight.creationTime = creationTime;
                highlight.chapterName = chapterName;
                highlight.type = type;
                highlight.extra1 = extra1;
                highlight.extra2 = extra2;
                highlight.extra3 = extra3;
                highlight.deviceModel = model;
                highlight.osVersion = osVersion;

                mHighlights.add(highlight);
            }

            if(BookHelper.useHistory)
                __hlHistory.read(getFullPath(BookHelper.annotationHistoryFileName));

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void loadOption() {

        BookHelper.initReadingStyle();

        String fileName = getFullPath(BookHelper.optionFileName);

        JSONObject object = EpubFileUtil.getJSONObjectFromFile(fileName);

        if( object == null ) {
            return;
        }

        try {

            if( !BookHelper.handleFront ) {
                if( !object.isNull( "fontSize" ) )
                    BookHelper.fontSize = object.getInt("fontSize");

                String path="";
                if(!object.isNull("fontPath")) {
                    path = object.getString("fontPath");
                }

                String replace ="";
                boolean exist = false;

                //2013.06.25
                //기본 asset 폴더에 내장하는 폰트의 경우 파일 체크 하지 않음.
                if( path.contains("android_asset") ){
                    exist = true;
                } else {
                    replace = path.replaceAll("file:", "");
                    File font = new File(replace);
                    exist = font.exists();
                }


                if( object.isNull("fontName") || !exist ) {
                    BookHelper.fontName = null;
                    BookHelper.faceName = null;
                    BookHelper.fontPath = null;
                } else {
                    BookHelper.fontName = object.getString("fontName");
                    BookHelper.faceName = object.getString("faceName");
                    BookHelper.fontPath = object.getString("fontPath");
                }
                if( object.isNull("lineSpace") ) {
                    BookHelper.lineSpace = null;
                } else {
                    BookHelper.lineSpace = object.getInt("lineSpace");
                }

                if( object.isNull("paraSpace") ) {
                    BookHelper.paraSpace = null;
                }
                else
                    BookHelper.paraSpace = object.getInt("paraSpace");

                if( !object.isNull("marginLeft" ) ) {
                    BookHelper.leftMargin = object.getInt("marginLeft");
                    BookHelper.topMargin = object.getInt("marginTop");
                    BookHelper.rightMargin = object.getInt("marginRight");
                    BookHelper.bottomMargin = object.getInt("marginBottom");
                }

                if( object.isNull("indent") )
                    BookHelper.indent = null;	//[ssin] add : 원본스타일 null
                else{
                    if(object.getString("indent").equalsIgnoreCase("false")){
                        BookHelper.indent = 0;
                    } else if(object.getString("indent").equalsIgnoreCase("true")){
                        BookHelper.indent = 1;
                    } else{
                        BookHelper.indent = object.getInt("indent");
                    }
                }


                if( object.isNull("listStyle") )
                    BookHelper.listStyle = "";
                else
                    BookHelper.listStyle = object.getString("listStyle");

                if( object.isNull("fontStyle") )
                    BookHelper.fontStyle = "";
                else
                    BookHelper.fontStyle = object.getString("fontStyle");

                if( object.isNull("fontWeight") )
                    BookHelper.fontWeight = "";
                else
                    BookHelper.fontWeight = object.getString("fontWeight");

                if( object.isNull("textEmphasis") )
                    BookHelper.textEmphasis = "";
                else
                    BookHelper.textEmphasis = object.getString("textEmphasis");

            }

            if( !object.isNull( "lastViewMode" ) )
                BookHelper.lastViewMode = object.getInt("lastViewMode");

            if( !object.isNull( "nightMode" ) )
                BookHelper.nightMode = object.getInt("nightMode");

            if( object.isNull("themeName") ) {
                BookHelper.themeName = "";
            } else
                BookHelper.themeName = object.getString("themeName");

            if( object.isNull("themeDefault") )
                BookHelper.themeDefault = "";
            else
                BookHelper.themeDefault = object.getString("themeDefault");

            if( object.isNull("themeSpecial") )
                BookHelper.themeSpecial = "";
            else
                BookHelper.themeSpecial = object.getString("themeSpecial");


            if( object.isNull("highlightColor") )
                BookHelper.lastHighlightColor = 2;
            else
                BookHelper.lastHighlightColor = object.getInt("highlightColor");

            if( object.isNull("viewStyleType") && BookHelper.getViewStyleType()==BookHelper.VIEW_STYLE_ORIGINAL){
                BookHelper.setViewStyleType(BookHelper.VIEW_STYLE_ORIGINAL);
            }else{
                BookHelper.setViewStyleType(object.getInt("viewStyleType"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.d("DEBUG","[EPUBVIEWER] loadOption() exception");
        }

        DebugSet.d(TAG, "load Option ..................... " + object.toString());
    }

    public void saveOption() {

        String fileName = getFullPath(BookHelper.optionFileName);
        if( fileName.length() <= 0 ) return;

        try {

            DebugSet.d(TAG, "save Option ................ " + fileName);

            File bookmarkDataFile = new File(fileName);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

            JSONObject object = new JSONObject();
            object.put("fontSize", BookHelper.fontSize);
            object.put("lastViewMode", __orientation);
            object.put("nightMode", BookHelper.nightMode);
            object.put("use3DCurling", false);
            object.put("landPerPage", BookHelper.landPages);
            object.put("landTotalPage", BookHelper.landTotalPage);
            object.put("portPerPage", BookHelper.portPages);
            object.put("portTotalPage", BookHelper.portTotalPage);
            object.put("fontName", BookHelper.fontName);
            object.put("faceName", BookHelper.faceName);
            object.put("fontPath", BookHelper.fontPath);
            object.put("backgroundColor", BookHelper.backgroundColor);
            object.put("themeName", BookHelper.themeName);
            object.put("themeDefault", BookHelper.themeDefault);
            object.put("themeSpecial", BookHelper.themeSpecial);
            object.put("lineSpace", BookHelper.lineSpace);
            object.put("paraSpace", BookHelper.paraSpace);
            object.put("marginLeft", BookHelper.leftMargin);
            object.put("marginTop", BookHelper.topMargin);
            object.put("marginRight", BookHelper.rightMargin);
            object.put("marginBottom", BookHelper.bottomMargin);
            object.put("indent", BookHelper.indent);

            //EPUB 3.0 설정 값 추가
            object.put("listStyle", BookHelper.listStyle);
            object.put("fontStyle", BookHelper.fontStyle);
            object.put("fontWeight", BookHelper.fontWeight);
            object.put("textEmphasis", BookHelper.textEmphasis);
            /////////////////////
            object.put("animationType", BookHelper.animationType);
            object.put("useDefault", BookHelper.useDefault);
            object.put("highlightColor", BookHelper.lastHighlightColor);
            object.put("viewStyleType", BookHelper.getViewStyleType());

            DebugSet.d(TAG, "json array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();
        }
        catch( Exception e ) {
            DebugSet.e( TAG, e.getMessage() );
        }
    }

    /**
     * 	 해당 검색어로 검색을 요청하는 메소드
     *   @param keyword : 검색어
     */
    @SuppressWarnings("unchecked")
    public void searchText(String keyword) {
        removeSearchHighlight();
        SearchTask st = new SearchTask(keyword);
        st.execute();
    }

    @SuppressWarnings("rawtypes")
    class SearchTask extends AsyncTask {

        String keyword;

        public SearchTask(String keyword) {
            this.keyword = keyword;
            __searchText = keyword;
        }

        @Override
        protected Object doInBackground(Object... arg0) {

            UnModifiableArrayList<ReadingOrderInfo> spineInfoList = mReadingSpine.getSpineInfos();

            for(int i=0; i< spineInfoList.size(); i++) {

                ReadingOrderInfo ch = spineInfoList.get(i);

                String drmKey = getDrmKey();
                String decodeStr =  getDecodeStr(drmKey, ch.getSpinePath());
                String bodyText = Html.fromHtml( BookHelper.getHtmlBody(decodeStr) ).toString();

                int keyPos = 0;

                int index = bodyText.indexOf(keyword, 0);
                while(index>=0) {

                    int textLen = bodyText.length();

                    int startPos = index-20;
                    int endPos = index + keyword.length() + 30;

                    int leftStartLength = 0;
                    int leftEndLength = 0;

                    if(startPos < 0 ){
                        leftStartLength = Math.abs(index-20);
                        startPos = 0;
                    }

                    if(endPos>textLen){
                        leftEndLength = Math.abs(endPos-textLen);
                        endPos = textLen;
                    }

                    if(leftStartLength > 0 && endPos+leftStartLength<=textLen){
                        endPos = endPos+leftStartLength;
                    } else if(leftEndLength > 0 && startPos-leftEndLength>=0){
                        startPos =  startPos-leftEndLength;
                    }

                    String snippet = bodyText.substring(startPos, endPos);
                    DebugSet.d(TAG, "found keyword : " + keyword + "[" + keyPos + "] : " + snippet);

                    int searchKeywordIndex = index - startPos;
                    if(searchKeywordIndex >= snippet.length())
                        searchKeywordIndex = endPos - snippet.length();

                    if(searchKeywordIndex < 0)
                        searchKeywordIndex = 0;

                    //개행문제 제거
//					snippet = snippet.replaceAll("\r\n", "");
//					snippet = snippet.replaceAll("\n", "");

                    double startPercent = ch.getSpineStartPercentage();
                    double havePercent = ch.getSpinePercentage();

                    double currentPercent = startPercent + havePercent * ((double)index/(double)textLen);

                    ChapterInfo chapter = mReadingChapter.getChapterInfoFromPath(ch.getSpinePath());

                    String chapterId = "";
                    String src = chapter.getChapterFilePath();

                    if (src.lastIndexOf("#") != -1) {
                        src = src.substring(0, src.lastIndexOf("#"));
                        chapterId = src.substring(src.lastIndexOf("#") + 1);
                    }

                    SearchResult sr = new SearchResult();
                    sr.chapterName = chapter.getChapterName();
                    sr.chapterId = chapterId;
                    sr.chapterFile = src;
                    sr.keyword = keyword;
                    sr.text = snippet;
                    sr.pageOffset = keyPos;
                    sr.percent = currentPercent;
                    sr.spineIndex = i;
                    sr.currentKeywordIndex = searchKeywordIndex;

                    mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SEARCH_RESULT, sr));

                    keyPos ++;
                    index = bodyText.indexOf(keyword, index+keyword.length());
                };
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SEARCH_END));
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SEARCH_START));
            super.onPreExecute();
        }

    };

    private int getSpineIndex(String filename){
        int spineIdx = 0;
        UnModifiableArrayList<ReadingOrderInfo> spines = mReadingSpine.getSpineInfos();
        for(int idx=0; idx < spines.size() ; idx++){
            if(spines.get(idx).getSpinePath().toLowerCase().equals(filename.toLowerCase())){
                spineIdx = idx;
                break;
            }
        }
        return spineIdx;
    }

    /**
     * 	 해당검색어 하이라이트 처리 요청 메소드
     *   @param sr : 검색결과 데이터
     */
    public void focusText(SearchResult sr) {

        __focusSearchResult = sr;

        __scrollByKeywordIndex = true;
        __scrollKeywordIndex = sr.pageOffset;

        __focusedScroll = true;

        String hFile = sr.chapterFile.toLowerCase();
        String current = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();

        if( !hFile.equals(current) ) {

            mReadingSpine.setCurrentSpineIndex(sr.chapterFile);
            if( mReadingSpine.getCurrentSpineIndex() >= 0 ) {
                goPageByChapter();
            }
        }
        else {
            __canFocusSearchResult = false;
            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_BY_KEYWORD_INDEX,__scrollKeywordIndex));
        }
    }

    public void removeSearchHighlight(){
        loadUrl("javascript:removeSearchHighlight()");
        __canFocusSearchResult = false;
        __focusedScroll = false;
        __focusSearchResult = null;
    }

    /**
     * 	 Viewer의 종료 프로세스 시작
     */
    public void onClose() {
        DebugSet.d(TAG, "EpubViewer.onClose()");

        finishTextSelectionMode();

        stopAllMedia();

        __onCloseBook = true;

        getCurrentPageInfo(true);

        if( mDecodeThread != null ){
            mDecodeThread.interrupt();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mChapterString.clear();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        finishTextSelectionMode();
    }

    float mStartX = 0;
    float mStartY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        DebugSet.d(TAG, "onTouchEvent ------ " + ev.getAction() + ", selectionMode : " + mTextSelectionMode);

        if(isChapterScrolling)
            return true;

        if( ev.getPointerCount() > 1 )
            return true;

        if( ev.getAction() == MotionEvent.ACTION_DOWN ){
            mStartX = ev.getX();
            mStartY = ev.getY();
        }
        else if(ev.getAction() == MotionEvent.ACTION_MOVE){
            ev.setLocation(mStartX, ev.getY());
            if(isPreventPageMove)
                return (ev.getAction() == MotionEvent.ACTION_MOVE);	// 페이지 이동 방지 때는 MOVE 액션 무시
            else
                return super.onTouchEvent(ev);
        } else if( ev.getAction() == MotionEvent.ACTION_UP ) {
            ev.setLocation(mStartX, ev.getY());
            return super.onTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        if(selectionRectList.size()>0){
            for(Rect rcSpan: modifiedSelectionRect) {
                Rect r = rcSpan;
                if(selectionHandler){
                    mPaint.setColor(BookHelper.textSelectionColor);
                    mPaint.setAlpha(70);
                    canvas.drawRect(r, mPaint);
                    drawHandler(canvas);
                } else{
                    mPaint.setColor(Color.parseColor(BookHelper.Colors[BookHelper.lastHighlightColor]));
                    mPaint.setAlpha(100);
                    canvas.drawRect(r, mPaint);
                }
            }
        }

        // focus search result
        if( __focusSearchResult != null ) {
            ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();
            String chapterName = spine.getSpinePath().toLowerCase();
            String focusFile = __focusSearchResult.chapterFile.toLowerCase();

            if( chapterName.equals(focusFile) ) {

                Paint inner = new Paint();
                inner.setStyle(Style.FILL);
                inner.setColor(BookHelper.focusTextColor);

                for(Rect rc: __focusSearchResult.rects ) {
                    RectF innerF = null;
                    if( mHtmlDirectionType == 1 )
                        innerF = new RectF(((mTotalPageInChapter * this.getWidth()) + rc.left)-3, rc.top-3, ((mTotalPageInChapter * this.getWidth()) + rc.right)+3, rc.bottom+3 );
                    else
                        innerF = new RectF((rc.left)-3, rc.top-3, (rc.right)+3, rc.bottom+3 );
                    canvas.drawRoundRect(innerF, 5, 5, inner);
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
        setDrawingCacheEnabled(true);
    }

    /**
     * 1. MethodName : getHighlights
     * 4. 작성자    : syw
     * 5. 작성일    : 2012. 9. 27. 오후 5:12:54
     *   @return ArrayList<Highlight>
     */
    public ArrayList<Highlight> getHighlights() {
        return mHighlights;
    }

    /**
     * 	 폰트 종류 변경을 요청하는 메소드
     *   @param fontName : 화면에 보여줄 폰트명
     *   @param faceName : 폰트 family에 정의된 face 명
     *   @param fontPath : 폰트 face에 정의된 path
     */
    public void changeFontDirect(String fontName, String faceName, String fontPath) {
        loadUrl("javascript:changeFontDirect('" + faceName + "','" + fontPath + "')");
    }

//    public void changeFontStyle(String styleValue) {
//        loadUrl("javascript:setFontStyle('" + styleValue + "')");
//    }

//    public void changeFontWeight(String weightValue) {
//        loadUrl("javascript:setFontWeight('" + weightValue + "')");
//    }

//    public void changeListStyle(String styleValue) {
//        loadUrl("javascript:setListStyleType('" + styleValue + "')");
//    }

//    public void changeTextEmphasis(String styleValue, String colorValue) {
//        loadUrl("javascript:setTextEmphasis('" + styleValue + "','" + colorValue +"')");
//    }

    /**
     * 	 폰트 사이즈 변경을 요청하는 메소드
     *   @param value : 변경할 폰트 사이즈 값
     */
    public void changeFontSizeDirect(String value) {
        loadUrl("javascript:changeFontSizeDirect('" + value + "')");
    }

    /**
     * 	 줄간격 변경을 요청하는 메소드
     *   @param value : 변경할 줄간격 값
     */
    public void changeLineHeightDirect(String value) {
        loadUrl("javascript:changeLineHeightDirect('" + value + "')");

    }
    /**
     * 	 뷰어 배경색 변경을 요청하는 메소드
     *   @param color :  알파 값 포함된 16진수 컬러 값
     *   @param nightMode : 야간 모드 유무
     */
    public void changeBackgroundColorDirect(int color, boolean nightMode) {
        String strClr = String.format("#%06X", color);
        loadUrl("javascript:changeBackgroundColorDirect('" + strClr + "'," + nightMode + ")");
    }
    /**
     * 	 문단 간격 변경을 요청하는 메소드
     *   @param value : 변경할 문단 간격 값
     */
    public void changeParaHeightDirect(String value ) {
        loadUrl("javascript:changeParaHeightDirect('" + value + "')");
    }
    /**
     * 	 상하좌우 여백 변경을 요청하는 메소드
     *   @param left : 좌측 마진 값
     *   @param top : 상단 마진 값
     *   @param right : 우측 마진 값
     *   @param bottom : 하단 마진 값
     */
    public void changeMarginDirect(int left, int top, int right, int bottom) {
        loadUrl("javascript:changeMarginDirect("+left+","+top+","+right+","+bottom+")");
    }
    /**
     *   들여쓰기 On/Off 요청하는 메소드
     *   @param value : 들여쓰기 여부
     */
    public void changeIndentDirect(boolean value) {
        loadUrl("javascript:changeIndentDirect(" + value + ")");
    }

    private boolean checkScrollPageExist(int page) {
        int pageNumber = mCurrentPageIndexInChapter + page;
        if( pageNumber < 0 ) {
            int numChapter = mReadingSpine.getCurrentSpineIndex() - 1;
            if( numChapter >= 0 ) {
                return true;
            } else {
                if( mOnBookStartEnd != null ) {
                    mOnBookStartEnd.onStart();
                }
                return false;
            }
        } else if( pageNumber > mTotalPageInChapter -1 ) {
            int numChapter = mReadingSpine.getCurrentSpineIndex() + 1;
            int size = mReadingSpine.getSpineInfos().size();
            if( numChapter < size ) {
                return true;
            } else {
                if( mOnBookStartEnd != null ) {
                    mOnBookStartEnd.onEnd();
                }
                return false;
            }
        }
        return true;
    }

    /**
     *
     * getCurrentPageInfoByJump
     *
     *      getCurrentPageInfo() 함수의 파라메타 중에 link Jump에 해당하는 기능을 수행하기 위해서 사용.
     *      이 함수의 호출후에
     *      1) getCurrentPageInfo() 함수 수행..
     *      2) onGet 이벤트 발생.
     *
     * 작성자    : syw
     * 작성일    : 2012. 10. 24. 오후 2:11:41
     *
     * @return void
     */
    public void getCurrentPageInfoByJump() {
        __currentPageInfoForLinkJump = true;
    }

    /**
     *
     * setIgnoreDrm
     *      true - disable Drm Decode Callback function
     *      false - enable Drm decode callback function
     *
     * 작성자    : syw
     * 작성일    : 2012. 10. 24. 오후 2:41:28
     *
     * @return void
     * @param isIgnore
     */
    public void setIgnoreDrm(boolean isIgnore) {
        __isIgnoreDrm = isIgnore;
    }

    private String generateUniqueID() {
        String randID = "kyoboId" + (int)Math.floor(Math.random()*10001);
        for(Highlight h: mHighlights) {
            if( h.highlightID.equals(randID) ) {
                randID = "kyoboId" + (int)Math.floor(Math.random()*10001);
            }
        }
        return randID;
    }

    /**
     * 	 slide 구현을 위한 anim 정의 파일 등록
     *   @param bLeft : 좌측 반향 애니메이션 인지 아닌지데 대한 여부 값
     *   @param inID : in 애니메이션 리소스 id
     *   @param outID : out 애니메이션 리소스 id
     */
    public void setSlideResource(boolean bLeft, int inID, int outID) {
        if( bLeft ) {
            __slideLeftIn = inID;
            __slideLeftOut = outID;
        } else {
            __slideRightIn = inID;
            __slideRightOut = outID;
        }
    }

    /**
     * 	 설정을 초기화 하는 메소드
     */
    public void setDefaultReadingStyle(ReadingStyle readingStyle){
        BookHelper.defaultReadingStyle = readingStyle;
    }

    public void setDefaultReadingStyle(){
        BookHelper.paraSpace = BookHelper.defaultReadingStyle.paragraphSpace;
        BookHelper.lineSpace =  BookHelper.defaultReadingStyle.lineSpace;
        BookHelper.fontSize = BookHelper.defaultReadingStyle.fontSize;
        BookHelper.faceName = BookHelper.defaultReadingStyle.fontFace;
        BookHelper.fontPath = BookHelper.defaultReadingStyle.fontPath;
        BookHelper.indent = BookHelper.defaultReadingStyle.textIndent;
        BookHelper.topMargin = BookHelper.defaultReadingStyle.topMargin;
        BookHelper.bottomMargin = BookHelper.defaultReadingStyle.bottomMargin;
        BookHelper.leftMargin = BookHelper.defaultReadingStyle.leftMargin;
        BookHelper.rightMargin = BookHelper.defaultReadingStyle.rightMargin;
//		reLoadBook();	// front에서 reloadBook 해주고 있어서 주석처리
    }

    public void clearBookmarkHistory() {
        __bmHistory.clear();
    }
    public void clearHighlightHistory() {
        __hlHistory.clear();
    }
    public AnnotationHistory getAnnotationHistory() {
        if( __hlHistory == null )
            __hlHistory = new AnnotationHistory();

        return __hlHistory;
    }
    public AnnotationHistory getBookmarkHistory() {
        if( __bmHistory == null )
            __bmHistory = new AnnotationHistory();

        return __bmHistory;
    }

    /**
     * 스타일 설정 전체를 파일로 저장한다.
     *
     * @param stylePath String : 저장할 파일 경로
     * @param fontSize int : 폰트 사이즈 % 값
     * @param fontName String : 화면에 보여지는 폰트명
     * @param faceName String : 폰트 family에 정의된 face 명
     * @param fontPath String : 폰트 face에 정의된 path
     * @param lineSpace int : 줄간격 설정 값
     * @param paraSpace int : 문단 간격 설정 값
     * @param indent boolean : 들여쓰기 여부
     * @param marginLeft int : 좌측 여백
     * @param marginTop int : 상단 여백
     * @param marginRight int : 우측 여백
     * @param marginBottom int : 하단 여백
     */
    public void saveAllStyle(String stylePath, Integer fontSize, String fontName, String faceName, String fontPath,
                             Integer lineSpace, Integer paraSpace, Integer indent, Integer marginLeft, Integer marginTop, Integer marginRight, Integer marginBottom ){

        BookSettings bs = new BookSettings();
        bs.saveAllStyle(mContext, stylePath, fontSize, fontName, faceName, fontPath, lineSpace, paraSpace, indent, marginLeft, marginTop, marginRight, marginBottom);
    }

    /**
     * 	 해당 챕터의 시작 퍼센테이지 값을 전달한다.
     *   @param index : 시작 퍼센테이지 값을 얻고자 하는 Spine index 값
     *   @return double : 해당 챕터의 시작 퍼센테이지 값
     */
    public double getChapterStartPercent(int index){
        if( mReadingSpine.getSpineInfos().size() <= 0 )
            return -1.0;

        if( index < 0 )
            return 0.0;
        else if( index > mReadingSpine.getSpineInfos().size() - 1 )
            return 100.0;
        else
            return mReadingSpine.getSpineInfos().get(index).getSpineStartPercentage();
    }
    /**
     * 	 해당 챕터가 차지하고 있는 퍼센테이지 값을 전달한다.
     *   @param index : 차지하고 있는 퍼센테이지 값을 얻고자 하는 Spine index 값
     *   @return double : 해당 챕터가 차지하고 있는 퍼센테이지 값
     */
    public double getChapterHavePercent(int index){
        if( mReadingSpine.getSpineInfos().size() <= 0 )
            return -1.0;

        return mReadingSpine.getSpineInfos().get(index).getSpinePercentage();
    }
    /**
     * 	 퍼센테이지에 해당하는 Spine Index를 전달한다.
     *   @param percent
     *   @return
     */
    public int getSpineIndexFromPercent(double percent){

        UnModifiableArrayList<ReadingOrderInfo> spines = mReadingSpine.getSpineInfos();

        for( int i = 0; i < spines.size(); i++ ) {
            if( spines.get( i ).getSpineStartPercentage() > percent )
                return i - 1;
        }
        return spines.size()-1;
    }
    /**
     * 	 해당 Spine Index의 Chapter 객체를 전달한다.
     *   @param index
     *   @return
     */
    public ChapterInfo getChapterFromSpineIndex(int index){
        UnModifiableArrayList<ChapterInfo> chapters = mReadingChapter.getChapters();
        for( int i = 0; i < chapters.size(); i++ ) {
            ChapterInfo cp = chapters.get(i);
            if( cp.getChapterFilePath().contains(mReadingSpine.getSpineInfos().get( index ).getSpinePath()) ) {
                return cp;
            }
        }
        return new ChapterInfo(mReadingSpine.getSpineInfos().get(index).getSpinePath(), "", 0, "");
    }

    public void startChapterStringGet(){
        if(!mChapterStringcomplete && !mChapterStringSaving){
            mDecodeThread = new Thread(new ChapterStringGet());
            mDecodeThread.start();
        }
    }

    /**
     * 백드라운드에서 챕터파일을 저장처리함.
     * 검색이나 로딩시 활용하기위한 저장.
     *
     */
    class ChapterStringGet implements Runnable {

        public ChapterStringGet() {
        }

        @Override
        public void run() {
            mChapterStringSaving = true;

            UnModifiableArrayList<ReadingOrderInfo> spines = mReadingSpine.getSpineInfos();
            try {

                for(int i=0; i<spines.size() ; i++){
                    DebugSet.d( TAG, "DRM decoder run spine :" + i + ", onClose : " + __onCloseBook);

                    if( __onCloseBook )
                        break;


                    if (mDecodeThread.isInterrupted()) {
                        //					if (__onCloseBook) {
                        DebugSet.d( TAG, "Thread.currentThread().isInterrupted() : " + i );
                        throw new InterruptedException();
                    }
                    //					Thread.sleep(100);
                    ReadingOrderInfo spine = spines.get(i);
                    String decodeStr = "";

                    if( __isIgnoreDrm ) {
                        decodeStr = BookHelper.file2String(spine.getSpinePath());
                    } else {
                        if( mOnDecodeContent != null ) {
                            try {
                                DebugSet.d( TAG, "decode listener is not null : " );
                                decodeStr = mOnDecodeContent.onDecode(getDrmKey(), spine.getSpinePath());

                                if(decodeStr==null)
                                    mDecodeThread.interrupt();

                            } catch (Exception e) {
                                e.printStackTrace();
                                DebugSet.e( TAG, "ChapterStringGet.run() Exception : " + e.getMessage() );
                            }
                        } else {
                            DebugSet.e( TAG, "DRM decoder not defined!" );
                        }
                    }

                    if (mDecodeThread.isInterrupted()) {
                        DebugSet.d( TAG, "Thread.currentThread().isInterrupted() : " + i );
                        throw new InterruptedException();
                    }
                    mChapterString.put(i, decodeStr);
                }
                DebugSet.printMemory("chapter string finish");



            } catch (InterruptedException e) {
                e.printStackTrace();
                DebugSet.e( TAG, "InterruptedException, DecodeThread close" );
                mDecodeThread.interrupt();
            }

            mChapterStringcomplete = true;
            mChapterStringSaving = false;

        }
    }

    /**
     * 챕터파일의 디코드 메소드
     *
     * @param drmKey
     * @param fileName
     * @return
     */
    private synchronized String getDecodeStr(String drmKey, String fileName){
        DebugSet.d( TAG, "getDecodeStr drmKey :" + drmKey + ", fileName : " + fileName );
        String decodeStr = "";

        int spineIndex = getSpineIndex(fileName);

        if (mChapterStringcomplete){
            String chapterStr = mChapterString.get(spineIndex);
            if( !chapterStr.equals("") )
                return mChapterString.get(spineIndex);
        }

        if( __isIgnoreDrm ) {
            decodeStr = BookHelper.file2String(fileName);
        } else {
            if( mOnDecodeContent != null ) {
                decodeStr = mOnDecodeContent.onDecode(drmKey, fileName);
            } else {
                DebugSet.e( TAG, "getDecodeStr DRM decoder not defined!" );
            }
        }

        if(decodeStr != null)
            mChapterString.put(spineIndex, decodeStr);

        return decodeStr;
    }

    public void setUseEPUB3Viewer(boolean use){
        BookHelper.setUseEPUB3Viewer(use);
    }

    public void stopAllMedia(){

        if(mediaOverlayController!=null)
            mediaOverlayController.stop(false);

        loadUrl("javascript:stopAllMedia()");
    }

    private void checkVideoTag(){
        this.loadUrl("javascript:getVideoTag()");
    }

    private void showFullScreenVideo(String url){
        DebugSet.d(TAG, "showFullScreenVideo url : "+url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/*");
        ((Activity)mContext).startActivity(intent);
    }

    private String getDrmKey()  {

        if( mDrmKey != null )
            return mDrmKey;

        Iterator<XmlDCMES> drm = null;

        drm = mEpubFile.getDublinCoreDrms();

        while (drm.hasNext()) {
            XmlDCMES xmlDublinCore = (XmlDCMES) drm.next();

            mDrmKey = xmlDublinCore.getValue();
            break;
        }
        if( mDrmKey == null )
            mDrmKey = "";


        return mDrmKey;
    }

    private String getFullPath(String fileName) {

        String fullName="";
        String file =fileName;

        String path = mEpubFile.getEpubPath();

        if( MyZip.mIsUnzip ) {
            BookHelper.epubFilePath = path;
            if( !MyZip.mUnzipDir.endsWith("/") )
                BookHelper.epubFilePath = path + "/";
        }
        else {
            BookHelper.epubFilePath = "";
        }

        fullName = BookHelper.epubFilePath + file;

        return fullName;
    }

    public void setPageDirection(PageDirection dir) {
        if( dir == PageDirection.RTL ) {
            mSpineDirectionType = 1;
        } else if( dir == PageDirection.LTR ) {
            mSpineDirectionType = 0;
        }
    }

    public void setTTSHighlightRect(JSONArray rectArray) {
        mTTSHighlightRectList.clear();
        try {
            for(int i=0; i<rectArray.length(); i++) {
                JSONObject rectObject = rectArray.getJSONObject(i);
                Rect rc = getRectFromJsonObject(rectObject);
                mTTSHighlightRectList.add(rc);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeTTSHighlightRect() {
        mTTSHighlightRectList.clear();
    }

    public void setPreventMediaControl(boolean isPrevent){
        loadUrl("javascript:setPreventMediaControl("+ isPrevent +")");
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY){
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

        if(BookHelper.animationType!=3 || isPreventPageMove || asidePopupStatus)
            return;

        if(clampedY){
            if(mDeltaY<-20){
                mOnPageScroll.onScrollInScrollMode(-1);
            } else if(mDeltaY>20){
                mOnPageScroll.onScrollInScrollMode(1);
            }
        } else{
            mOnPageScroll.onScrollInScrollMode(0);
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        mDeltaY = deltaY;
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public Bitmap createBitmap() {
        setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    @SuppressWarnings("deprecation")
    public void startCaptureScrollAnimation(int startPosition, int endPosition, final boolean isSlideToNext){
        TranslateAnimation slideCapture = new TranslateAnimation(0, 0, startPosition, endPosition);
        slideCapture.setDuration(1000);
        slideCapture.setFillAfter(false);
        slideCapture.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if(isSlideToNext)
                    startScrollAnimation(mParent.getHeight(), 0, isSlideToNext);
                else
                    startScrollAnimation(-1*mParent.getHeight(), 0, isSlideToNext);
            }

            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                container.setVisibility(View.GONE);
            }
        });
        container.startAnimation(slideCapture);
    }

    private void startScrollAnimation(int startPosition, int endPosition, final boolean isSlideToNext){
        TranslateAnimation slideViewer = new TranslateAnimation(0, 0, startPosition, endPosition);
        slideViewer.setDuration(1000);
        slideViewer.setFillAfter(false);
        slideViewer.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isChapterScrolling = false;
            }
        });
        EPubViewer.this.startAnimation(slideViewer);
    }

    public void moveAudioPlayingPosition(String xPath, double movingUnit){
        audioContentPlayer.moveAudioPlayingPosition(xPath, movingUnit);
    }

    private double getDuration(String filePath){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mEpubFile.getPublicationPath()+filePath.replace("..", ""));
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Double.parseDouble(duration)/1000;
    }

    public void setMediaOverlayController(MediaOverlayController mediaOverlayController) {
        this.mediaOverlayController = mediaOverlayController;
    }

    private OnBGMStateListener mOnBgmStateListener;
    public void setOnBGMStateListener(OnBGMStateListener listener){
        mOnBgmStateListener = listener;
    }

    public String getCurrentChapterFile(){
        return mReadingChapter.getCurrentChapter().getChapterFilePath();
    }

    public void existMediaOverlayOnPage(){
        loadUrl("javascript:getIDListOnCurrentPage('"+mReadingSpine.getCurrentSpineInfo().getSpinePath()+"')");
    }

    public void unpluggedHeadSet(boolean isUnplugged){
        if(isUnplugged){
            stopAllMedia();
            mediaOverlayController.pause();
        }
    }

    public boolean isNoterefEnabled(){
        return asidePopupStatus;
    }

    public void hideNoteref(){
        loadUrl("javascript:resetBodyStatus()");
    }

    public void setPreventNoteref(boolean isPrevent){
        loadUrl("javascript:setPreventNoteref("+isPrevent+")");
    }

    public String getCurrentHtmlString(){
        return mChapterString.get(mReadingSpine.getCurrentSpineIndex());
    }

    public long addColorIndexHistory(long highlightID, int i) { // TODO :: 언제필요한거지?
        if( BookHelper.useHistory ){
            long newID = System.currentTimeMillis() / 1000L +i;
            __hlHistory.modifyRemove(highlightID, newID);
            __hlHistory.modifyAdd(newID);
            return newID;
        }
        return highlightID;
    }

    public void mergeBookmarkData(String json){
        mBookmarkManager.mergeBookmark(json);
    }

    public String getCurrentUserAgent(){
        return this.getSettings().getUserAgentString();
    }

    public Bitmap loadBitmapFromView() {

        Bitmap bitmap = null;

        // width measure spec
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), View.MeasureSpec.AT_MOST);
        // height measure spec
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), View.MeasureSpec.AT_MOST);

        // measure the view
        measure(widthSpec, heightSpec);

        // set the layout sizes
        int left = getLeft();
        int top = getTop();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int scrollX = getScrollX();
        int scrollY = getScrollY();

        layout(left, top, width + left, height + top);

        // create the bitmap
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        // create a canvas used to get the view's image and draw it on the
        // bitmap

        Canvas c = new Canvas(bitmap);
        // position the image inside the canvas
        c.translate(-getScrollX(), -getScrollY());
        // get the canvas
        draw(c);

        return bitmap;
    }

    /******************************************************************** s : do annotation continue */
    private boolean isContinueSelection=false;
    private boolean isContinueHighlight=false;
    public void selectionContinue(boolean isHighlight){

        isContinueSelection=false;
        isContinueHighlight=false;

        if( mOnContextMenu != null ) {
            mOnContextMenu.onHideContextMenu();
        }

        if(isHighlight)
            isContinueHighlight = true;
        else
            isContinueSelection = true;

        loadUrl("javascript:getSelectionLandingPage("+isHighlight+","+BookHelper.lastHighlightColor+")");
    }
    /******************************************************************** e : do annotation continue */

    /******************************************************************** s : merge */
    private void mergeAnnotation(String jsonObject){
        try {
            JSONObject object = new JSONObject(jsonObject);
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
                memoText = object.getString("memo").replace("'","\\'").trim();

            String currentChapterFilePath = mReadingSpine.getCurrentSpineInfo().getSpinePath().toLowerCase();

            ArrayList<Highlight> erases = new ArrayList<>();

            for(int i=0; i<mHighlights.size(); i++) {

                Highlight h = mHighlights.get(i);

                String annotationFilePath = h.chapterFile.toLowerCase();
                if(!currentChapterFilePath.equals(annotationFilePath))
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
                            if( startCharOffset <= h.startChar && endCharOffset > h.startChar && endCharOffset <= h.endChar ) {    // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
                                DebugSet.d(TAG, "PARTIAL startChild");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                            //뉴셀렉션이 기셀렉션 안쪽이나 끝나는지점에서 시작해 기셀렉션보다 뒤쪽에서 끝난경우.
                            else if( startCharOffset < h.endChar && endCharOffset >= h.endChar ) {  // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                            if (startCharOffset <= h.startChar && endCharOffset > h.startChar) {   // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                        } else if( startCharOffset < h.endChar ) { // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                            } else if( h.endChar > startCharOffset ) { // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                            if( startCharOffset < h.endChar ) {    // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                            } else if( h.startChar < endCharOffset ) { // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
                                DebugSet.d(TAG, "PARTIAL 5");
                                erases.add(h);

                                endPath = h.endPath;
                                endChildIndex = h.endChild;
                                endCharOffset = h.endChar;
                            }
                        } else {
                            if( endCharOffset > h.startChar ) { // TODO :: 0129 수정건 - 0314 재수정 (인접제외)
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
                    if( !isMergedMemo && erases.get(i).isMemo() ) {     // TODO :: 수정되는 메모는 이미 머지된 상태라
                        if(!memoText.isEmpty()) {
                            memoText += "\n";
                        }
                        memoText += erases.get(i).memo;
                    }
                    prevHighlight = erases.get(i);
                }

                JSONArray hiLite = new JSONArray();
                hiLite.put(erases.get(i).get2());
                mHighlights.remove(erases.get(i));
                if( BookHelper.useHistory ) {
                    __hlHistory.mergeRemove(erases.get(i).uniqueID, newHighlight.uniqueID);
                }
                EPubViewer.this.loadUrl("javascript:deleteHighlights(" + hiLite.toString() + ")");  // 머지로 지워지는 주석
            }

            isMergedMemo = false;

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
    /******************************************************************** e : merge */

    /******************************************************************** s : add annotation */
    public void addAnnotation(){

//        hideAnnotationMenu();

        String script = (new StringBuilder())
                .append("javascript:addAnnotation(")
                .append(BookHelper.lastHighlightColor)
                .append(")").toString();

        EPubViewer.this.loadUrl(script);
    }

    public void addAnnotationWithMemo(String memoContent, boolean memoMerged){

//        hideAnnotationMenu();

        isMergedMemo = memoMerged;

        JSONArray array = new JSONArray();
        array.put(memoContent);

        String script = (new StringBuilder())
                .append("javascript:addAnnotationWithMemo(")
                .append(BookHelper.lastHighlightColor).append(",")
                .append(array.toString())
                .append(")").toString();

        EPubViewer.this.loadUrl(script);
    }

    public void requestAllMemoText(){
        loadUrl("javascript:requestAllMemoText()");
    }

    private void highlightAnnotation(Highlight highlight){      // NOTI : do after merging

        ArrayList<String> array = new ArrayList<>();

        for(int i=0 ; i<  mReadingChapter.getChapters().size(); i++){
            ChapterInfo ch = mReadingChapter.getChapters().get(i);
            String current = mReadingChapter.getCurrentChapter().getChapterFilePath().toLowerCase();
            String id = "";
            String src = ch.getChapterFilePath();

            if( src.lastIndexOf("#") != -1 ){
                id = src.substring(src.lastIndexOf("#")+1);
            }
            if( id != null && current.equals(src) ) {
                array.add(id);
            }
        }

        int colorIndex = highlight.colorIndex;

        JSONArray memoArr = new JSONArray();
        memoArr.put(highlight.memo);

        String script = (new StringBuilder())
                .append("javascript:highlightText(")
                .append("'"+highlight.uniqueID+"'").append(",")
                .append("'" + highlight.startPath + "'").append(",")
                .append("'" + highlight.endPath + "'" ).append(",")
                .append(highlight.startChar).append(",")
                .append(highlight.endChar).append(",")
                .append("'"+highlight.highlightID+"'").append(",")
                .append(colorIndex).append(",")
                .append(array.toString()).append(",")
//                .append(Uri.encode(highlight.memo)).append("'")
                .append(memoArr.toString())
                .append(")").toString();

        DebugSet.d(TAG, "script >>>>>>>>>>>>> " + script);

        EPubViewer.this.loadUrl(script);

//        if( highlight.memo.length()>0 ) {
//            BookHelper.lastMemoHighlightColor = colorIndex;
//        } else {
        BookHelper.lastHighlightColor = colorIndex;
//        }

        finishTextSelectionMode();
    }
    /******************************************************************** e : add annotation */

    /******************************************************************** s : delete annotation */
    public void deleteAnnotation(){
        loadUrl("javascript:deleteAnnotationInRange()");
        finishTextSelectionMode();
    }
    /******************************************************************** e : delete annotation */

    /******************************************************************** s : modify annotation */
    public void  modifyAnnotationColorAndRange(int colorIndex){
        loadUrl("javascript:modifyAnnotationColorAndRange("+colorIndex+")");
    }

    public void  changeMemoText(String memoId, String currentMemo){
        for(int idx=0; idx<mHighlights.size(); idx++){
            Highlight targetHighlight = mHighlights.get(idx);
            if(targetHighlight.highlightID.equalsIgnoreCase(memoId)){
                targetHighlight.memo = currentMemo.trim();
                if( BookHelper.useHistory ){ //메모 변경도 히스토리에 변경항목으로 추가(동기화를 위함)
                    long newID = System.currentTimeMillis() / 1000L;
                    __hlHistory.modifyRemove(targetHighlight.uniqueID, newID);
                    __hlHistory.modifyAdd(newID);
                    targetHighlight.uniqueID = newID;
                }
                break;
            }
        }

        saveHighlights();
        finishTextSelectionMode();
    }
    /******************************************************************** e : modify annotation */

    /******************************************************************** s : context menu */
    private void hideAnnotationMenu(){
        if( mOnContextMenu != null ) {
            mOnContextMenu.onHideContextMenu();
        }
    }
    /******************************************************************** e : context menu */

    /******************************************************************** s : common */
    public void handleBackKeyEvent(){
        if(mTextSelectionMode){
            if(!selectionHandler){
                addAnnotation();
            } else {
                finishTextSelectionMode();
            }
        }
    }

    public void finishTextSelectionMode(){

        ViewerActionListener.preventGestureEvent = true;

        hideAnnotationMenu();

        mTextSelectionMode = false;

        selectionRectList.clear();
        EPubViewer.this.invalidate();

        contextMenuType = null;

        mStartHandle.setVisible(false, true);
        mEndHandle.setVisible(false, true);

        landingPage=-1;
        isContinueHighlight = false;
        isContinueSelection = false;

        scrollToBottom = false;
        scrollToTop = false;

        if(autoScrollTimer!=null) {
            autoScrollTimer.cancel();
            autoScrollTimer = null;
        }

        currentSelectedText = "";

        loadUrl("javascript:finishTextSelection()");

        if(mOnTextSelection!=null){
            mOnTextSelection.onEndTextSelection();
        }
    }

    public void drawHandler(Canvas canvas){
        drawStartHandler(canvas);
        drawEndHandler(canvas);
    }

    public void drawStartHandler(Canvas canvas) {

        if( mStartHandle == null ) return;

        if( !mStartHandle.isVisible() )
            mStartHandle.setVisible(true, true);

        Rect r = selectionRectList.get(0);

        int w = mStartHandle.getIntrinsicWidth();
        int h = mStartHandle.getIntrinsicHeight();

        mStartHandle.setBounds(r.left -w + (w/3), r.bottom - (h/3), r.left  + (w/3), r.bottom+h - (h/3));
        mStartHandle.draw(canvas);
    }

    public void drawEndHandler(Canvas canvas) {

        if( mEndHandle == null ) return;

        if( !mEndHandle.isVisible() )
            mEndHandle.setVisible(true, true);

        Rect r  = selectionRectList.get(selectionRectList.size()-1);

        int w = mEndHandle.getIntrinsicWidth();
        int h = mEndHandle.getIntrinsicHeight();

        mEndHandle.setBounds(r.right - (w/3), r.bottom - (h/3), r.right+w- (w/3), r.bottom+h - (h/3));
        mEndHandle.draw(canvas);
    }
    /******************************************************************** e : common */

    /******************************************************************** s : user action */
    public void onFling(MotionEvent e, float dx, float dy, int direction) {
        DebugSet.d(TAG, "onFling direction : " + direction);

        if(asidePopupStatus || isPreventPageMove || BookHelper.animationType==3){
            return;
        }

        if(mOnTouchEventListener!=null)
            mOnTouchEventListener.onFling();

        switch (direction) {
            case ViewerActionListener.FLING_LEFT:
                if( mSpineDirectionType == 0 )
                    scrollPrior();
                else
                    scrollNext();
                break;
            case ViewerActionListener.FLING_RIGHT:
                if( mSpineDirectionType == 0 )
                    scrollNext();
                else
                    scrollPrior();
                break;
            default:
                break;
        }
    }

    public void onTouchDown(int x, int y) {
        DebugSet.d(TAG, "onTouchDown in x : "+x +" / y : "+y);

        isStartHandlerTouched = false;
        isEndHandlerTouched = false;

        if(isChapterScrolling || __forceChapterChanging)
            return;

        hideAnnotationMenu();

        if( mOnTouchEventListener != null ) {
            mOnTouchEventListener.onDown(x, y);
        }

        Rect handlerStartRegion = mStartHandle.getBounds();
        Rect handlerEndRegion = mEndHandle.getBounds();

        if(mStartHandle.isVisible() && handlerStartRegion.contains(x+getScrollX(),y+getScrollY())){
            isStartHandlerTouched=true;
            mTextSelectionMode = true;
            targetX = selectionRectList.get(0).left - getScrollX();
            targetY = selectionRectList.get(0).top - getScrollY() + (selectionRectList.get(0).height()/2);
        }

        if(mEndHandle.isVisible() && handlerEndRegion.contains(x+getScrollX(),y+getScrollY())){
            isEndHandlerTouched=true;
            mTextSelectionMode = true;
            targetX = selectionRectList.get(selectionRectList.size()-1).right - getScrollX();
            targetY = selectionRectList.get(selectionRectList.size()-1).top - getScrollY() + (selectionRectList.get(selectionRectList.size()-1).height()/2);
        }
    }

    public void onSingleTap(int x, int y) {
        DebugSet.d(TAG, "onSingleTap in x : "+x +" / y : "+y);

        if(!isStartHandlerTouched && !isEndHandlerTouched && mTextSelectionMode){
            // 1.셀렉션 가능 외 터치
            // 2.형광펜 이어긋기 케이스에서 그냥 터치 시
            if(contextMenuType!=null && contextMenuType== BookHelper.ContextMenuType.TYPE_CONTINUE && !isContinueHighlight && !isContinueSelection){
                addAnnotation();
                contextMenuType = null;
            } else {
                finishTextSelectionMode();
            }
            return;
        }

        if(mTextSelectionMode)
            return;

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);

        if( !__forceChapterChanging ) {
            loadUrl("javascript:findTagUnderPoint(" + wx + "," + wy + "," + true + ","+isTextSelectionDisabled+")");
        }
    }

    public void onTouchMove(int x, int y, float moveDistX, float moveDistY) { // 롱프레스 후 움직이면 실행 X
        DebugSet.d(TAG, "onTouchMove in x : "+x +" / y : "+y);
        DebugSet.d(TAG, "onTouchMove in moveDistX : "+moveDistX +" / moveDistY : "+moveDistY);

        if(isChapterScrolling  || !mTextSelectionMode ) {     //|| isPreventPageMove
            if(autoScrollTimer!=null) {
                autoScrollTimer.cancel();
                autoScrollTimer = null;
            }
            return;
        }

        int wx;
        int wy;

//        if(!isStartHandlerTouched && !isEndHandlerTouched) {
//            finishTextSelectionMode();
//            return;
//        }

        hideAnnotationMenu();

        scrollToTop = false;
        scrollToBottom = false;

        if(isStartHandlerTouched || isEndHandlerTouched){  // 핸들러 터치 후 이동 시

            if(BookHelper.animationType==3){    // 스크롤 모드 시 좌표가  자동 스크롤 영역 내부인지 판단
                if(scrollTopThreshold>y) {
                    scrollToTop = true;
                    scrollToBottom = false;
                } else if(scrollBottomThreshold<y) {
                    scrollToTop = false;
                    scrollToBottom = true;
                }
            }

            wx = Scr2Web((int) (targetX + moveDistX));
            wy = Scr2Web((int) (targetY + moveDistY));

            if(scrollToTop || scrollToBottom){  // 자동 스크롤 실행

                touchedXY[0]=wx;
                touchedXY[1]=wy;

                if(autoScrollTimer==null){
                    autoScrollTimer = new Timer();
                    autoScrollTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_AUTO));
                        }
                    }, 0, 100);
                }
            } else { // 자동 스크롤 중지
                if(autoScrollTimer!=null) {
                    autoScrollTimer.cancel();
                    autoScrollTimer = null;
                }
                loadUrl("javascript:setMoveRangeWithHandler(" + wx + "," + wy +","+isStartHandlerTouched+","+isEndHandlerTouched+")");
            }
        }
    }

    public void onLongPress(int x, int y) {
        DebugSet.d(TAG, "onLongPress in x : "+x +" / y : "+y);

        if(asidePopupStatus) {
            hideNoteref();
            return;
        }

        if(isTextSelectionDisabled)
            return;

        if(mTextSelectionMode){
            if(isStartHandlerTouched || isEndHandlerTouched)
                return;
            finishTextSelectionMode();
//            return;
        }

        mTextSelectionMode = true;

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);

        if(mOnTextSelection!=null)
            mOnTextSelection.onStartTextSelection();

        loadUrl("javascript:setStartSelectionRange(" + wx + "," + wy + ")");

//        selectionLayout.setVisibility(View.VISIBLE);
    }

    public void onTouchMoveAfterLongPress(int x, int y, float moveDistX, float moveDistY) {
        DebugSet.d(TAG, "onTouchMoveAfterLongPress in x : "+x +" / y : "+y);
        DebugSet.d(TAG, "onTouchMoveAfterLongPress in moveDistX : "+moveDistX +" / moveDistY : "+moveDistY);

        if(!mTextSelectionMode) {
            if(autoScrollTimer!=null) {
                autoScrollTimer.cancel();
                autoScrollTimer = null;
            }
            return;
        }

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);

        hideAnnotationMenu();

        scrollToBottom = false;
        scrollToTop = false;

        if(!isStartHandlerTouched && !isEndHandlerTouched) {

            if(BookHelper.animationType==3){    // 스크롤 모드 시 좌표가  자동 스크롤 영역 내부인지 판단
                if(scrollTopThreshold>y) {
                    scrollToTop = true;
                    scrollToBottom = false;
                } else if(scrollBottomThreshold<y) {
                    scrollToTop = false;
                    scrollToBottom = true;
                }
            }

            if(scrollToTop || scrollToBottom){  // 자동 스크롤 실행

                touchedXY[0]=wx;
                touchedXY[1]=wy;

                if(autoScrollTimer==null){
                    autoScrollTimer = new Timer();
                    autoScrollTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_SCROLL_AUTO));
                        }
                    }, 0, 100);
                }
            } else { // 자동 스크롤 중지
                if(autoScrollTimer!=null) {
                    autoScrollTimer.cancel();
                    autoScrollTimer = null;
                }
                loadUrl("javascript:setMoveRange(" + wx + "," + wy +")");
            }
        } else {
            onTouchMove(x,y, moveDistX , moveDistY);
        }
    }

    public void onTouchUp(int x, int y) {    // 단순 탭 시 실행 X
        DebugSet.d(TAG, "onTouchUp in x : "+x +" / y : "+y);

        if(!mTextSelectionMode)
            return;

        int wx = Scr2Web(x);
        int wy = Scr2Web(y);

        if(autoScrollTimer!=null) {
            autoScrollTimer.cancel();
            autoScrollTimer = null;
            scrollToBottom = false;
            scrollToTop = false;
        }


        if(ViewerActionListener.isLongPressed){
            loadUrl("javascript:setEndRange(" + wx + "," + wy + "," + BookHelper.lastHighlightColor + ","+false+")");
        } else {
            if(isStartHandlerTouched || isEndHandlerTouched){
                loadUrl("javascript:setEndRangeWithHandler(" + wx + "," + wy + "," + BookHelper.lastHighlightColor + ")");
            } else {
                if(contextMenuType== BookHelper.ContextMenuType.TYPE_CONTINUE){
                    loadUrl("javascript:setEndRange(" + wx + "," + wy + "," + BookHelper.lastHighlightColor + ","+true+")");
                } else {
                    finishTextSelectionMode();
                }
            }
        }
//        selectionLayout.setVisibility(View.GONE);
    }

    public void onDoubleTap(int x, int y) {
        DebugSet.d(TAG, "onDoubleTap in x : "+x +" / y : "+y);

        if(mTextSelectionMode)
            finishTextSelectionMode();

        if( !__forceChapterChanging ) {
            int wx = Scr2Web(x);
            int wy = Scr2Web(y);
            loadUrl("javascript:findTagUnderPoint(" + wx + "," + wy + "," + false + ","+isTextSelectionDisabled+")");
        }
    }

    public void onTwoFingerMove(int moveDirection){ //	changeSize ->  -1 : smaller , 1 : bigger
        if(__chapterLoading || isPreventPageMove || mTextSelectionMode)
            return;
        mOnTouchEventListener.onTwoFingerMove(moveDirection);
    }
    /******************************************************************** e : user action */

    /******************************************************************** s : handler */
    private Handler mWebviewInnerHandler;
    private class WebviewInnerHandler extends Handler {

        private final WeakReference<EPubViewer> reflowableWebView;

        public WebviewInnerHandler(EPubViewer webview) {
            reflowableWebView = new WeakReference<>(webview);
        }

        @Override
        public void handleMessage(Message msg) {
            EPubViewer webview = reflowableWebView.get();
            if (webview != null)
                webview.handleMessage(msg);
        }
    }

    private void handleMessage(Message msg) {

        switch(msg.what){

            /**** s : load */
            case Defines.REF_IMG_RESIZING_DONE :
                DebugSet.d(TAG, "REF_IMG_RESIZING_DONE");
                loadUrl("javascript:getPageCount(" + BookHelper.twoPageMode + ")");
                break;

            case Defines.REF_PAGE_READY :
                DebugSet.d(TAG, "REF_PAGE_READY");
                setPageReady();
                break;

            case Defines.REF_CHAPTER_LOADING :
                DebugSet.d(TAG, "REF_CHAPTER_LOADING");
                setChapterLoading();
                break;

            case Defines.REF_FORCE_CHAPTER_CHANGING :
                DebugSet.d(TAG, "REF_FORCE_CHAPTER_CHANGING");
                notifyForceChapterChanging();
                break;

            /**** s : selection */
            case Defines.REF_CHECK_MERGE_ANNOTATION :
                DebugSet.d(TAG, "REF_CHECK_MERGE_ANNOTATION");
                String currentSelectedRangeInfo = (String) msg.obj;
                mergeAnnotation(currentSelectedRangeInfo);
                break;

            case Defines.REF_DRAW_SELECTION_RECT :
                String currentSelectedRectList = (String)msg.obj;
                drawSelectionRect(currentSelectedRectList);
                break;

            case Defines.REF_MERGE_ALL_MEMO :
                DebugSet.d(TAG, "REF_MERGE_ALL_MEMO");
                String annotationIdList = (String)msg.obj;
                String allMemoText = getAllMemoText(annotationIdList);
                if( mOnMemoSelection!=null ) {
                    mOnMemoSelection.onGetAllMemoText(allMemoText);
                }
                break;

            case Defines.REF_SET_LANDING_PAGE :
                DebugSet.d(TAG, "REF_SET_LANDING_PAGE");
                landingPage = (Integer)msg.obj;
                if(landingPage == mCurrentPageIndexInChapter){
                    landingPage =-1;
                    isContinueHighlight = false;
                    isContinueSelection = false;
                } else if(landingPage!=mCurrentPageIndexInChapter){
                    scrollNext();
                }
                break;

            case Defines.REF_OVERFLOW_TEXT_SELECTION :
                DebugSet.d(TAG, "REF_OVERFLOW_TEXT_SELECTION");
                int overflowType = (Integer)msg.obj;
                if(mOnTextSelection != null){
                    mOnTextSelection.onOverflowTextSelection(BookHelper.SelectionErrorType.values()[overflowType]);
                }
                break;

            case Defines.REF_OVERFLOW_MEMO_CONTENT :
                DebugSet.d(TAG, "REF_OVERFLOW_MEMO_CONTENT");
                if(mOnTextSelection != null){
                    mOnTextSelection.onOverflowMemoContent();
                }
                break;

            case Defines.REF_SCROLL_AUTO :
                DebugSet.d(TAG, "REF_SCROLL_AUTO");
                if(autoScrollTimer==null || selectionRectList.isEmpty())
                    return;
                loadUrl("javascript:autoScroll("+scrollToTop+","+scrollToBottom+")");
                break;

            case Defines.REF_ADD_HIGHLIGHTED_DATA :
                DebugSet.d(TAG, "REF_ADD_HIGHLIGHTED_DATA");
                String highlightedData = (String)msg.obj;
                addHighlightingData(highlightedData);
                break;

            case Defines.REF_CONTEXT_MENU_SHOW :
                DebugSet.d(TAG, "REF_CONTEXT_MENU_SHOW");

                PopupData contextMenuData = (PopupData)msg.obj;

                if(selectionRectList.isEmpty())
                    return;

                if( mOnContextMenu != null ) {
                    mOnContextMenu.onShowContextMenu(contextMenuData.highlightId, contextMenuData.menuType, contextMenuData.x, contextMenuData.y);
                }

                if(contextMenuData.menuType== BookHelper.ContextMenuType.TYPE_CONTINUE){
                    contextMenuType = contextMenuData.menuType;
                }
                break;

            case Defines.REF_CONTEXT_MENU_HIDE :
                DebugSet.d(TAG, "REF_CONTEXT_MENU_HIDE");
                finishTextSelectionMode();
                break;

            /**** s : bookmark */
            case Defines.REF_BOOKMARK_SHOW :
                DebugSet.d(TAG, "REF_BOOKMARK_SHOW");

                Bookmark bookmark = (Bookmark)msg.obj;
                DebugSet.d(TAG, "REF_BOOKMARK_SHOW "+bookmark.toString());

                setBookmarkVisibility(bookmark);
                break;

            case Defines.REF_BOOKMARK_CHECK :
                boolean bookmarkVisibility = ((Boolean)msg.obj).booleanValue();
                if(mOnPageBookmark != null ) {
                    mOnPageBookmark.onMark(bookmarkVisibility);
                }
                break;

            /**** s : read position */
            case Defines.REF_SAVE_LAST_POSITION :
                DebugSet.d(TAG, "REF_SAVE_LAST_POSITION");

                Bookmark lastPosition = (Bookmark)msg.obj;
                DebugSet.d(TAG, "REF_SAVE_LAST_POSITION "+lastPosition.toString());

                saveLastPosition(lastPosition);

                __forceSaveLastPosition = false;
                break;

            /**** paging */
            case Defines.REF_SCROLL_PAGE_OR_CHAPTER :
                DebugSet.d(TAG, "REF_SCROLL_PAGE_OR_CHAPTER");
                int scrollDirection = (Integer) msg.obj;
                pageScrollByAnimationType(scrollDirection);
                break;

            case Defines.REF_STOP_SCROLLING :
                DebugSet.d(TAG, "REF_STOP_SCROLLING");

                getCurrentTopPath();

                perInchapter = (Double) msg.obj;

                int currentSpineIndex = mReadingSpine.getCurrentSpineIndex();
                double currentScrollPercent = getChapterStartPercent(currentSpineIndex) + getChapterHavePercent(currentSpineIndex)*perInchapter/100;
                mOnPageScroll.onScrollAfter(mCurrentPageIndexInChapter, mCurrentPageInChapter, mTotalPageInChapter, currentScrollPercent);

                if(audioContentPlayer!=null) {
                    audioContentPlayer.findAudioContentOnCurrentPage();
                }
                break;

            case Defines.REF_START_SCROLL_ANIMATION :
                DebugSet.d(TAG, "REF_START_SCROLL_ANIMATION");
                startCaptureScrollAnimation(0, mParent.getHeight(), false);
                existMediaOverlayOnPage();
                break;

            case Defines.REF_LOAD_PRIOR_CHAPTER :
                DebugSet.d(TAG, "REF_LOAD_PRIOR_CHAPTER");
                mCurrentPageIndexInChapter = mTotalPageInChapter-1;
                scroll(mCurrentPageIndexInChapter);
                break;

            case Defines.REF_LOAD_NEXT_CHAPTER :
                DebugSet.d(TAG, "REF_LOAD_NEXT_CHAPTER");
                scroll(mCurrentPageIndexInChapter);  // TODO :: currentPage로?? 확인하자
                break;

            case Defines.REF_PAGE_SCROLL :  // 챕터 내 이동
                DebugSet.d(TAG, "REF_PAGE_SCROLL");
                int page = msg.obj != null ? (Integer)msg.obj : mCurrentPageIndexInChapter;
                scroll(page);
                break;

            case Defines.REF_SCROLL_BY_PERCENT :
                DebugSet.d(TAG, "REF_SCROLL_BY_PERCENT");
                double percent = (Double)msg.obj ;
                goPage( percent );
                break;

            case Defines.REF_SCROLL_BY_PERCENT_IN_CHAPTER :
                DebugSet.d(TAG, "REF_SCROLL_BY_PERCENT_IN_CHAPTER");

                double percentInChapter = (Double)msg.obj;
                page = (int)(((double)(mTotalPageInChapter-1) * percentInChapter)/100);
                scroll(page);

                __scrollByPercentInChapter = false;
                __scrollPercent = 0;
                break;

            case Defines.REF_SCROLL_BY_FOCUS :
                DebugSet.d(TAG, "REF_SCROLL_BY_FOCUS");

                page = (Integer)msg.obj;
                DebugSet.d(TAG, "REF_SCROLL_BY_FOCUS " + page);
                scroll(page);

                SearchResult sr = __focusSearchResult;
                if( sr != null ) {
                    loadUrl("javascript:focusText('" + sr.path + "','" + sr.keyword + "'," + sr.charOffset + ")");
                }
                __scrollForFocusText = false;
                __scrollPage = 0;
                break;

            case Defines.REF_SCROLL_BY_POSITION :
                DebugSet.d(TAG, "REF_SCROLL_BY_POSITION");

                int position = (Integer)msg.obj;
                DebugSet.d(TAG, "REF_SCROLL_BY_POSITION " + position);

                __canFocusSearchResult = true;

                scroll(position / Web2Scr(mWindowWidth));

                __scrollByPosition = false;
                __scrollPosition = 0;
                break;

            case Defines.REF_SCROLL_BY_OFFSET :
                DebugSet.d(TAG, "REF_SCROLL_BY_OFFSET "+ mCurrentPageIndexInChapter);

                int offset = (Integer)msg.obj;
                if( offset < 0 ) {
                    mCurrentPageIndexInChapter = mTotalPageInChapter-1;
                    scroll(mCurrentPageIndexInChapter);
                } else {
                    mCurrentPageIndexInChapter = offset;
                    scroll(mCurrentPageIndexInChapter);
                }
                __scrollByOffset = false;
                __scrollOffset = 0;
                break;

            case Defines.REF_SCROLL_BY_KEYWORD_INDEX :
                DebugSet.d(TAG, "REF_SCROLL_BY_KEYWORD_INDEX ");

                Integer keywordIndex = (Integer)msg.obj;
                DebugSet.d(TAG, "REF_SCROLL_BY_KEYWORD_INDEX "+keywordIndex);

                JSONArray array = new JSONArray();
                ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();

                for(Highlight h : mHighlights) {
                    if( spine != null ) {
                        String hFile = h.chapterFile.toLowerCase();
                        String currentFile = spine.getSpinePath().toLowerCase();
                        if(hFile.equals(currentFile)) {
                            array.put(h.get2());
                        }
                    }
                }

                __scrollByKeywordIndex = false;
                __scrollKeywordIndex = -1;

                loadUrl("javascript:searchTextByKeywordIndex("+array.toString()+",'" + __searchText + "','" + keywordIndex + "'," + BookHelper.twoPageMode + ")");
                break;

            case Defines.REF_SCROLL_BY_PAGE :
                DebugSet.d(TAG, "REF_SCROLL_BY_PAGE ");
                page = (Integer)msg.obj;
                scroll(page);

                __scrollByPage = false;
                __scrollPage = 0;
                break;

            case Defines.REF_SCROLL_BY_ID :
                DebugSet.d(TAG, "REF_SCROLL_BY_ID");

                String id = (String)msg.obj;
                DebugSet.d(TAG, "REF_SCROLL_BY_ID " + id);

                __requestPageMove = true;

                String script = "javascript:gotoID('" + id + "'," + BookHelper.twoPageMode + ")";
                loadUrl(script);

                __scrollByID = false;
                __scrollID = null;
                break;

            case Defines.REF_SCROLL_BY_HIGHLIGHT_ID :
                DebugSet.d(TAG, "REF_SCROLL_BY_HIGHLIGHT_ID");

                id = (String)msg.obj;
                DebugSet.d(TAG, "EPUB_SCROLL_BY_HIGHLIGHT_ID @@@@@@@@@@@@@@@@ " + id);

                __requestPageMove = true;

                script = "javascript:gotoHighlight('" + id + "'," + BookHelper.twoPageMode + ")";
                loadUrl(script);

                __scrollByHighlightID = false;
                __scrollHighlightID = null;
                break;

            case Defines.REF_SCROLL_BY_PATH:
                DebugSet.d(TAG, "REF_SCROLL_BY_PATH");

                __requestPageMove = true;

                String path = (String)msg.obj;
                script = "javascript:gotoPATH('" + path + "'," + isReload +","+ BookHelper.twoPageMode + ")";
                loadUrl(script);

                __scrollByPATH = false;
                __scrollPATH = null;
                __scrollByPageFromPath = false;
                isReload = false;	// 스크롤보기 화면처리를 위한 플래그
                break;

            case Defines.REF_SCROLL_BY_CLICK :  // TODO :: REF_HANDLE_TOUCH_EVENT
                DebugSet.d(TAG, "REF_SCROLL_BY_CLICK");

                MotionEvent event = (MotionEvent)msg.obj;
                if(mediaOverlayController.isMediaOverlayPlaying()){
                    int x = Scr2Web((int)event.getX());
                    int y = Scr2Web((int)event.getY());
                    touchedPositionDuringPlaying=  BookHelper.getClickArea(EPubViewer.this, event.getX(), event.getY());
                    loadUrl("javascript:getIDListByPoint(" + x + "," + y + ",'"+mReadingChapter.getCurrentChapter().getChapterFilePath()+"')");
                    return;
                }

                ClickArea ca = BookHelper.getClickArea(EPubViewer.this, event.getX(), event.getY());
                if( isPreventPageMove == false && BookHelper.animationType!=3)  {
                    if( ca == ClickArea.Left ) {
                        if( mSpineDirectionType == 0 ){
                            scrollPrior();
                        } else {
                            scrollNext();
                        }
                    } else if( ca == ClickArea.Right ) {
                        if( mSpineDirectionType == 0 ) {
                            scrollNext();
                        } else {
                            scrollPrior();
                        }
                    }
                }
                if( mOnTouchEventListener != null ) {
                    mOnTouchEventListener.onUp(ca);
                }
                break;

            case Defines.REF_PAGE_SCROLL_AFTER :
                DebugSet.d(TAG, "REF_PAGE_SCROLL_AFTER");
                pageScrollAfter();
                break;

            /**** media overlay */
            case Defines.REF_HAS_MEDIAOVERLAY :
                DebugSet.d(TAG, "REF_HAS_MEDIAOVERLAY");
                if(mOnMediaOverlayStateListener!=null)
                    mOnMediaOverlayStateListener.existMediaOverlay((Boolean)msg.obj);
                break;

            case Defines.REF_MEDIAOVERLAY_PAUSE :
                DebugSet.d(TAG, "REF_MEDIAOVERLAY_PAUSE");
                mediaOverlayController.pause();
                break;

            case Defines.REF_PLAY_SELECTED_MEDIAOVERLAY :
                DebugSet.d(TAG, "REF_PLAY_SELECTED_MEDIAOVERLAY");

                boolean playing =false;

                if(msg.obj!=null){
                    try {
                        JSONArray jsonArr = new JSONArray((String) msg.obj);
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
//                    else{
//                        playing=false;
//                    }
                }
                break;

            /**** search */
            case Defines.REF_SEARCH_START :
                DebugSet.d(TAG, "REF_SEARCH_START");
                if( mOnSearchResult != null ) {
                    mOnSearchResult.onStart();
                }
                break;

            case Defines.REF_SEARCH_END :
                DebugSet.d(TAG, "REF_SEARCH_END");
                if( mOnSearchResult != null ) {
                    mOnSearchResult.onEnd();
                }
                break;

            case Defines.REF_SEARCH_RESULT:
                DebugSet.d(TAG, "REF_SEARCH_RESULT");
                SearchResult searchResult = (SearchResult)msg.obj;
                if( mOnSearchResult != null ) {
                    mOnSearchResult.onFound(searchResult);
                }
                break;

            case Defines.REF_REPORT_FOCUS_RECT :
                DebugSet.d(TAG, "REF_REPORT_FOCUS_RECT");

                String focusRectInfo = (String) msg.obj;
                DebugSet.d(TAG, "REF_REPORT_FOCUS_RECT "+focusRectInfo);

                searchResultFocusRect((String) msg.obj);
                break;

            /**** settings */
            case Defines.REF_NIGHT_MODE:
                boolean isNightMode = (Boolean)msg.obj;
                BookHelper.nightMode = isNightMode ? 1 : 0;
                break;

            /**** etc */
            case Defines.REF_CURRENT_PAGE_INFO :   // TODO :: EPUB_GET_CURRENT_PAGE_INFO랑 무슨차이?
                DebugSet.d(TAG, "REF_CURRENT_PAGE_INFO");

                bookmark = (Bookmark)msg.obj;
                if( bookmark != null ) {
                    if( bookmark.path.trim().length() == 0 ) {
                        __scrollByPercentInChapter = true;
                        __scrollPercent = bookmark.percent;
                    } else {
                        __scrollByPATH = true;
                        __scrollPATH = bookmark.path;
                    }
                } else {
                    __scrollByPage = true;
                    __scrollPage = mCurrentPageIndexInChapter;
                }
                __currentPageInfo = false;
                __anchorBookmark = bookmark;

                mWebviewInnerHandler.sendMessage(mWebviewInnerHandler.obtainMessage(Defines.REF_CHAPTER_LOADING));

                saveLastPosition(bookmark);
                break;

            case Defines.REF_NOTIFY_CURRENT_PAGE_INFO :
                DebugSet.d(TAG, "REF_NOTIFY_CURRENT_PAGE_INFO");

                bookmark = (Bookmark)msg.obj;
                if( mOnCurrentPageInfo != null ) {
                    mOnCurrentPageInfo.onGet(bookmark);
                }

                if( __currentPageInfoForLinkJump ) {
                    if(mOnTagClick!=null && __linkValue!=null) {
                        mOnTagClick.onLink(__linkValue);
                    }
                }

                __currentPageInfoForPageJump = __currentPageInfoForLinkJump = false;
                __currentPageInfoForJump = false;

                saveLastPosition(bookmark);
                break;

            case Defines.REF_GET_CURRENT_TOP_PATH :
                DebugSet.d(TAG, "REF_NOTIFY_CURRENT_PAGE_INFO");

                bookmark = (Bookmark)msg.obj;
                DebugSet.d(TAG, "REF_NOTIFY_CURRENT_PAGE_INFO "+ bookmark.toString());

                __currentTopPath = false;
                __anchorBookmark = bookmark;

                saveLastPosition(bookmark);
                break;

            case Defines.REF_IMAGE_TAG_CLICK :
                DebugSet.d(TAG, "REF_IMAGE_TAG_CLICK");
                String src = (String)msg.obj;
                if(mOnTagClick!=null) {
                    mOnTagClick.onImage(src);
                }
                break;

            case Defines.REF_LINK_TAG_CLICK :
                DebugSet.d(TAG, "REF_LINK_TAG_CLICK");
                String link = (String)msg.obj;
                __linkValue = link;
                __currentPageInfoForLinkJump = true;

                getCurrentPageInfo(false);

                __canFocusSearchResult = false;
                __focusSearchResult = null;
                break;

            case Defines.REF_VIDEO_TAG_CLICK :  // TODO :: 필요없으면 지우자 ?
                break;

            case Defines.REF_VIDEO_CONTROL :    // TODO :: 필요없으면 지우자 ?
                if(mOnVideoInfoListener!=null){
                    mOnVideoInfoListener.videoInfo((String)msg.obj);
                }
                break;

            case Defines.REF_REPORT_ERROR :
                DebugSet.d(TAG, "REF_REPORT_ERROR");

                int code = (Integer)msg.obj;

                if( mOnReportError != null ) {
                    mOnReportError.onError(code);
                }

                finishTextSelectionMode();
                break;

            case Defines.REF_VIEWER_CLOSE :
                if( __loadBook ) {
                    bookmark = (Bookmark)msg.obj;
                    saveLastPosition(bookmark);
                    saveHighlights();
                    saveBookmarks();
                    saveOption();
                    __loadBook = false;
                }

                mBookmarks.clear();
                mHighlights.clear();

                if( mOnViewerState != null ) {
                    mOnViewerState.onEnd();
                }
                break;

            case Defines.REF_VIEWER_REFRESH :
                EPubViewer.this.invalidate();
                break;
        }
    }
    /******************************************************************** s : handler */

    private void setPageReady(){
        audioContentPlayer.setAudioContentReader(new AudioContentReader(EPubViewer.this));

        if(mOnBgmStateListener!=null)
            mOnBgmStateListener.setCurrentBGMState();

        if( mOnChapterChange != null) {
            mOnChapterChange.onPageReady(mTotalPageInChapter);
        }

        isViewerLoadingComplete = true;
        __chapterLoading = false;
        __forceChapterChanging = false;
    }

    private void setChapterLoading(){
        if (__firstBookLoad) { // 책 처음 로드시에 한번만 호출

            __firstBookLoad = false;

            restoreLastPosition();  // TODO :: 왜 여기서하냐..

            container = new FrameLayout(mContext);  // 스크롤모드 시 챕터 변경되는 경우 필요한 레이아웃
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.setLayoutParams(params);
            container.setClickable(false);
            container.setVisibility(View.GONE);
            mParent.addView(container, params);
        }

        scrollTopThreshold = Math.round(getHeight() / 10);
        scrollBottomThreshold = getHeight() - Math.round(getHeight() / 10);

        // 쳅터가 바뀌면 search focus가 안되게 설정
        __canFocusSearchResult = false;
        __chapterLoading = true;

        setDisplayMode();

        showBookByPosition(mReadingSpine.getCurrentSpineIndex());

//        mReadingChapter.setCurrentChapter(mReadingSpine.getCurrentSpineInfo().getSpinePath());
    }

    private void notifyForceChapterChanging(){
        __forceChapterChanging = true;
        if( mOnChapterChange != null ) {
            if(BookHelper.animationType==3 && mTTSHighlightRectList.size()>0){
                removeTTSHighlightRect();
            }

            if(mTextSelectionMode)
                finishTextSelectionMode();

            mOnChapterChange.onChangeBefore();
        }
        __reloadFlag = CHANGE_BEFORE;
    }

    private void drawSelectionRect(String currentSelectedRectList){
        selectionRectList.clear();
        modifiedSelectionRect.clear();

        mStartHandle.setVisible(false, true);
        mEndHandle.setVisible(false, true);

        try {
            JSONArray jsonArray = new JSONArray(currentSelectedRectList);
            for(int idx=0; idx<jsonArray.length(); idx++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(idx);
                if(jsonObject.getInt("width") == 0)
                    continue;
                Rect currentRect = new Rect(Web2Scr(jsonObject.getInt("left")), Web2Scr(jsonObject.getInt("top")), Web2Scr(jsonObject.getInt("right")), Web2Scr(jsonObject.getInt("bottom")));
                selectionRectList.add(currentRect);
            }

            if(BookHelper.animationType!=3) {
                for (Rect tempRect : selectionRectList) {
                    tempRect.left += EPubViewer.this.getScrollX();
                    tempRect.right += EPubViewer.this.getScrollX();
                    modifiedSelectionRect.add(tempRect);
                }
            } else {
                for (Rect tempRect : selectionRectList) {
                    tempRect.top += EPubViewer.this.getScrollY();
                    tempRect.bottom += EPubViewer.this.getScrollY();
                    modifiedSelectionRect.add(tempRect);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EPubViewer.this.invalidate();
    }

    private String getAllMemoText(String annotationIdList){

        String allMemoText = "";

        try{
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

    private void addHighlightingData(String highlightedData){
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

            ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();
            ChapterInfo chapter = mReadingChapter.getCurrentChapter();
            highlight.chapterFile = spine.getSpinePath();
            if( chapterId==null || chapterId.length() <= 0 ) {
                if( chapter != null ) {
                    highlight.chapterName = mReadingChapter.getChapterInfoFromPath(spine.getSpinePath()).getChapterName();
                }
            } else {
                chapter = getChapterById(highlight.chapterFile, chapterId);
                if( chapter != null ) {
                    highlight.chapterName = chapter.getChapterName();
                }
            }

            highlight.memo = Uri.decode(memo);
            highlight.colorIndex = colorIndex;
            highlight.page = -1;
            highlight.percent = percent;

            double startPercent = mReadingSpine.getCurrentSpineInfo().getSpineStartPercentage();
            double havePercent = mReadingSpine.getCurrentSpineInfo().getSpinePercentage();
            highlight.percentInBook = startPercent + havePercent * (highlight.percent/100);

            mHighlights.add(highlight);

            if(BookHelper.useHistory) {
                if(mOnAnalyticsListener!=null){
                    if(!selectionHandler){
                        mOnAnalyticsListener.onAnnotationQuick();
                    }
                }
                if( mMergedAnnotation ){
                    __hlHistory.mergeAdd(highlight.uniqueID);
                    if(mOnAnalyticsListener!=null){
                        if(selectionHandler){
                            mOnAnalyticsListener.onAnnotationMergeSelection();
                        } else {
                            mOnAnalyticsListener.onAnnotationMergeQuick();
                        }
                    }
                    mMergedAnnotation = false;
                } else {
                    __hlHistory.add(highlight.uniqueID);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveHighlights();
    }

    private void setBookmarkVisibility(Bookmark path){

        boolean bShow = addBookmarkingData(path);

        if( bShow && __currentBookmark != null ) {
            mBookmarks.remove(__currentBookmark);
            if( BookHelper.useHistory ) {
                __bmHistory.remove(__currentBookmark.uniqueID);
            }
            __currentBookmark = null;
        }

        if(mOnPageBookmark != null ) {
            mOnPageBookmark.onAdd(path, bShow);
        }

        __doBookmarkShow = false;

        saveBookmarks();
    }

    private void pageScrollByAnimationType(int scrollDirection){   // 현재 보기 모드에 따라 페이지 처리

        if( __forceChapterChanging ) return;

        if( BookHelper.animationType == 1 ) {
            if( !checkScrollPageExist(scrollDirection) ){
                return;
            }

            clearAnimation();

            if( scrollDirection == -1 ) {
                try {
                    Animation prevPage = AnimationUtils.loadAnimation( mContext, __slideRightOut );
                    if( mSpineDirectionType == 1 )
                        prevPage = AnimationUtils.loadAnimation( mContext, __slideLeftOut );
                    setAnimation(prevPage);
                    prevPage.setAnimationListener( new AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            scrollPage(-1);
                            if( mSpineDirectionType == 1 ) {
                                setAnimation(AnimationUtils.loadAnimation(mContext, __slideLeftIn));
                            } else {
                                setAnimation(AnimationUtils.loadAnimation(mContext, __slideRightIn));
                            }
                            getAnimation().setDuration( BookHelper.animationDuration );
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationStart(Animation animation) {}
                    });
                    getAnimation().setDuration( BookHelper.animationDuration );
                    startAnimation( getAnimation() );
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Animation nextPage = AnimationUtils.loadAnimation( mContext, __slideLeftOut );
                    if( mSpineDirectionType == 1 )
                        nextPage = AnimationUtils.loadAnimation( mContext, __slideRightOut );
                    setAnimation(nextPage);
                    nextPage.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            scrollPage(1);
                            if( mSpineDirectionType == 1 ) {
                                setAnimation(AnimationUtils.loadAnimation(mContext, __slideRightIn));
                            } else {
                                setAnimation(AnimationUtils.loadAnimation(mContext, __slideLeftIn));
                            }
                            getAnimation().setDuration( BookHelper.animationDuration );
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationStart(Animation animation) {}
                    });
                    getAnimation().setDuration( BookHelper.animationDuration );
                    startAnimation( getAnimation() );
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else if( BookHelper.animationType==3 ) {
            isChapterScrolling = true;
            container.setBackgroundDrawable(new BitmapDrawable(createBitmap()));
            container.setVisibility(View.VISIBLE);
            scrollPage(scrollDirection);
        } else {
            scrollPage(scrollDirection);
        }
    }

    private void pageScrollAfter(){

        if( __onCloseBook ) {
            DebugSet.d(TAG, "pageScrollAfter __onCloseBook is true");
            return;
        }

        if( __canFocusSearchResult ) {
            EPubViewer.this.invalidate();
        }

        __requestPageMove = false;

        reloadSVG();

        checkVideoTag();

        if(__reloadFlag == CHANGE_BEFORE || __reloadFlag == PAGE_READY){
            if( !__chapterLoading && mOnChapterChange != null) {
                mOnChapterChange.onChangeAfter(mTotalPageInChapter);
                __reloadFlag = FLAG_CLEAR;
                if(BookHelper.animationType==3 && __chapterLoadPrior ){
                    EPubViewer.this.loadUrl("javascript:scrollToBottom()");
                } else if(BookHelper.animationType==3 && __chapterLoadNext ){
                    startCaptureScrollAnimation(0, -1*mParent.getHeight(), true);	//TODO 테스트 isPrevent관련
                }
            }
        }

        if( mOnPageScroll != null ) {
            double startPercent = mReadingSpine.getCurrentSpineInfo().getSpineStartPercentage();
            double havePercent = mReadingSpine.getCurrentSpineInfo().getSpinePercentage();

            if(BookHelper.animationType!=3){
                mCurrentPercentInBook = startPercent + havePercent * ((double)mCurrentPageInChapter/(double)mTotalPageInChapter);
                if(mReadingSpine.getSpineInfos().size()-1 == mReadingSpine.getCurrentSpineIndex()){
                    if(mTotalPageInChapter == mCurrentPageInChapter+1){
                        mCurrentPercentInBook = 100;
                    }
                }
            } else{
                mCurrentPercentInBook = startPercent + havePercent * perInchapter/100;
            }
            mOnPageScroll.onScrollAfter(mCurrentPageIndexInChapter, mCurrentPageInChapter+1, mTotalPageInChapter, mCurrentPercentInBook);
            audioContentPlayer.findAudioContentOnCurrentPage();
        }
        __chapterLoadPrior=false;
        __chapterLoadNext=false;
        __forceChapterChanging = false;

        if( __focusedScroll ) {
            __focusedScroll = false;
        } else {
            __focusSearchResult = null;
        }

        if( __currentBookmark != null ) {
            __doBookmarkShow = true;    // 마이그레션된 데이타가 페이지에 걸리는 경우에 새로운 북마크 획득
            getCurrentPageInfo(false);
        }

        __currentTopPath = true;
        getCurrentPageInfo(false);

        startChapterStringGet(); // 백그라운드에서 챕터파일 데이터 저장

        if(landingPage!=-1) {
            if (landingPage == mCurrentPageIndexInChapter) {
                if (isContinueSelection || isContinueHighlight) {
                    final String script = (new StringBuilder())
                            .append("javascript:selectionContinue(")
                            .append(isContinueHighlight).append(",")
                            .append(BookHelper.lastHighlightColor)
                            .append(")").toString();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EPubViewer.this.loadUrl(script);
                        }
                    }, 200);
                }
                isContinueSelection = false;
                isContinueHighlight = false;
                landingPage =-1;
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollNext();
                    }
                }, 500);
            }
        }
    }

    private boolean isTextSelectionDisabled = false;
    public void setSelectionDisabled(boolean isTextSelectionDisabled){
        this.isTextSelectionDisabled = isTextSelectionDisabled;
    }
}