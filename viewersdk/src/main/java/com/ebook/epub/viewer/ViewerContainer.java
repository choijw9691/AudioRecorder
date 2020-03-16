package com.ebook.epub.viewer;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.ebook.bgm.BGMMediaPlayer;
import com.ebook.bgm.BGMPlayer;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.fixedlayoutviewer.view.FixedLayoutScrollView;
import com.ebook.epub.parser.common.PackageVersion;
import com.ebook.epub.parser.common.PageDirectionType;
import com.ebook.epub.parser.common.RenditionLayoutType;
import com.ebook.epub.parser.common.RenditionOrientationType;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.mediaoverlays.SmilContentProvider;
import com.ebook.epub.parser.mediaoverlays.SmilDocumentReader;
import com.ebook.epub.parser.mediaoverlays.SmilSync;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ocf.EpubFileSystem;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlDCMES;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.parser.ops.XmlNavigationException;
import com.ebook.epub.viewer.data.AudioNavigationInfo;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.epub.viewer.data.ImageReader;
import com.ebook.epub.viewer.data.ReadingAudioNavigation;
import com.ebook.epub.viewer.data.ReadingChapter;
import com.ebook.epub.viewer.data.ReadingOrderInfo;
import com.ebook.epub.viewer.data.ReadingSpine;
import com.ebook.media.AudioContent;
import com.ebook.mediaoverlay.MediaOverlayController;
import com.ebook.mediaoverlay.OnMediaOverlayListener;
import com.ebook.tts.Highlighter;
import com.ebook.tts.OnHighlighterListener;
import com.ebook.tts.OnTTSDataInfoListener;
import com.ebook.tts.TTSDataInfo;
import com.ebook.tts.TTSDataInfoManager;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 @class ViewerContainer
 @brief Front interface class
 */
public class ViewerContainer extends FrameLayout implements Highlighter.OnHighlightRectInfoListener {

    public enum LayoutMode { Reflowable, FixedLayout }
    public enum Orientation { Landscape, Portrait, Auto }
    public enum PageDirection { LTR, RTL }

    private EpubFile mEpubFile;
    private ReadingSpine mSpineInfo;
    private ReadingChapter mChapterInfo;
    private ImageReader mImageReader;
    private LayoutMode mLayoutMode;
    private PageDirection mPageDirection = PageDirection.LTR;
    private Orientation mOrientation = Orientation.Auto;
    public EPubViewer mEPubViewer;
    public FixedLayoutScrollView mFixedLayoutView;
    private Context mContext;
    private AttributeSet mAttrs;
    private String mInstName;
    private int mDefStyle = -1;
    private boolean mIgnoreDrm = false;
    private OnDecodeContent mOnDecodeContent;
    private TTSDataInfoManager ttsDataInfoManager;
    private Highlighter ttsHighlighter;
    private MediaOverlayController mediaOverlayController;
    private RequestStringOfFileListener mRequestStringOfFile=null;
    private BGMPlayer bgmPlayer;
    private ReadingAudioNavigation mReadingAudioNavigation;

    public interface OnDecodeContent {
        /**
         * onDecode - 쳅터간 이동이나 정보 파일 등을 로드할때 호출.
         * @return String       : decode된 파일 내용
         * @param drmKey        : drm 항목에 해당하는 값
         * @param decodeFile    : drm이 적용된 파일 이름 ( full path )
         */
        String onDecode(String drmKey, String decodeFile);
    }

    public interface OnChapterChange {
        /**
         * onChangeBefore - 쳅터(파일)가 바뀌기 전에 발생
         */
        void onChangeBefore();
        /**
         * onChangeAfter - 쳅터 (파일)가 바뀐후에 발생
         * @param pageCount : 쳅터 page count
         */
        void onChangeAfter(int pageCount);
        /**
         * onPageReady - 쳅터의 로딩이 완료 됐지만 아직 위치 이동은 이루어지지 않았다.
         * @param totalPageInChapter : 쳅터 page count
         */
        void onPageReady(int totalPageInChapter);
    }

    public interface OnPageScroll {
        /**
         * onScrollBefore - 스크롤 이벤트 발생 이전에 발생
         * @param currentPageInChapter   : 현재 읽은 페이지 수
         */
        void onScrollBefore(int currentPageInChapter);
        /**
         * onScrollAfter - 스크롤 이벤트 발생 이후에 발생, 모든 스크롤 이벤트의 마지막 이벤트
         * @param currentPageIndexInChapter     : 챕터 내 현재 페이지 인덱스
         * @param currentPageInChapter          : 챕터 내 현재 페이지 수
         * @param totalPageInChapter            : 챕터 내 전체 페이지 수
         */
        void onScrollAfter(int currentPageIndexInChapter, int currentPageInChapter, int totalPageInChapter, double currentPercentInBook);
        /**
         * onScrollInScrollMode - 스크롤 모드 시 스크롤 이벤트 발생 이후에 발생, 모든 스크롤 이벤트의 마지막 이벤트
         * @param moveChapterNum     : 이동할 챕터 인덱스
         */
        void onScrollInScrollMode(int moveChapterNum);
    }

    public interface OnBookStartEnd {
        /**
         * onStart - contents의 맨 처음 위치 에서  -1 일때 발생
         */
        void onStart();
        /**
         * onEnd - contents의 맨 끝 위치에서 +1 일때 발생
         */
        void onEnd();
    }

    public interface OnContextMenu {
        /**
         * onShowContextMenu - 텍스트 셀렉션 후 context 메뉴 활성화 요청
         * @param highlightId       : 기존 하이라이트 재활성화 시 해당 id ( 셀렉션 범위 수정 시 null로 오게 되어짐 )
         * @param contextMenuType   : 메뉴 타입 ( 0 : 신규 메뉴 / 1 : 신규 및 이어긋기 메뉴 / 2 : 수정 메뉴 / 3 : 수정 및 페이지넘김 메뉴 / 4 : 이어긋기 메뉴 )
         * @param x                 : 터치 업 x 좌표
         * @param y                 : 터치 업 y 좌표
         */
        void onShowContextMenu(String highlightId, BookHelper.ContextMenuType contextMenuType, int x, int y);   // TODO :: new custom selection - added
        /**
         * onHideContextMenu - 텍스트 셀렉션 종료 시 context 메뉴 비활성화 요청
         */
        void onHideContextMenu();                                                                               // TODO :: new custom selection - added
    }

    public interface OnPageBookmark {
        /**
         * onAdd - 새로운 북마크가 추가되거나 삭제된 경우
         * @param bm        : 새로 생성되거나 삭제된 북마크 객체
         * @param bShow     : true - 생성, false - 삭제
         */
        void onAdd(Bookmark bm, boolean bShow);
        /**
         * onMark - 화면에 bookmark 모양을 drawing
         * @param bShow : true - show, false - hide
         */
        void onMark(boolean bShow);
    }

    public interface OnMemoSelection {
        /**
         * onGetAllMemoText - 셀렉션 영역 내 모든 메모 주석 내용 전달
         * @param allMemoText        : 셀렉션 영역 내 모든 메모 주석 내용
         */
        void onGetAllMemoText (String allMemoText);
    }

    public interface OnTouchEventListener {
        /**
         * onDown - down 이벤트
         * @param x
         * @param y
         */
        void onDown(int x, int y);
        /**
         * onUp - up 이벤트
         * @param ca    :  up 위치에 따라  Left, Right, Top, Bottom, Left_Corner 등
         */
        void onUp(BookHelper.ClickArea ca);
        /**
         * onFling - fling 이벤트
         */
        void onFling();                 // TODO : 프론트에서 안쓰면 없애자
        /**
         * onTwoFingerMove - 투핑거로 폰트 조절 시 발생하는 이벤트
         * @param moveDirection   : -1 : smaller / 1 : bigger
         */
        void onTwoFingerMove(int moveDirection);
    }

    public interface OnTagClick {
        /**
         * onImage - 사용자가 이미지태그 터치 시
         * @param url   : 이미지의 url ( full path )
         */
        void onImage(String url);
        /**
         * onLink - 사용자가 hyper link 터치 시
         * @param url   : url 주소
         */
        void onLink(String url);
        void onNoteref(String title, String value, Rect position);
    }

    public interface OnSearchResult {
        /**
         * onStart - search 프로세스 시작
         */
        void onStart();
        /**
         * onFound - 모든 쳅터를 탐색하여 검색된 결과가 있을경우 검색 건수에 따라 발생
         * @param sr    : 검색 결과 객체
         */
        void onFound(SearchResult sr);
        /**
         * onEnd - search 프로세스 종료
         */
        void onEnd();
    }

    public interface OnCurrentPageInfo {
        /**
         * onGet - 페이지 점프 동작시 히스토리 관리를 목적으로 작성. pageJump, linkjump, seekbarJump 등에서 사용됨
         * @param bm    : 이동하기 전의 북마크 객체
         */
        void onGet(Bookmark bm);
    }

    public interface OnMoveToLinearNoChapterListener {
        void moveToLinearNoChapter(String chapterFilePath);
    }

    public interface RequestStringOfFileListener {
        String requestStringOfFile(String filePath, String drmKey);
    }

    public interface OnVideoInfoListener {
        void videoInfo(String onVideoInfo);
    }

    public interface OnMediaControlListener {
        void didPlayPreventMedia(String id, String mediaType);
    }

    public interface OnReportError {
        /**
         * onError
         *      - 통합 에러 이벤트 ( native, javascript ... )
         * @param errCode
         *      - 0: setupChapter error -> drmdecodeString is null or setup error
         *      - 1: text selection error
         *      - 2: bookmark error
         *      - 3: too large text error
         *      - 4: 컨텐츠 링크 error
         *      - 5: text highlighting error
         */
        void onError(int errCode);
    }

    public interface OnViewerState {    // TODO : 프론트에서 안쓰면 없애자
        /**
         * onStart - 뷰어 생성시 발생 ( 내부 함수인 init() 함수 호출 시 )
         */
        void onStart();
        /**
         * onEnd - onCloseBook 이벤트시 발생
         */
        void onEnd();
    }

    public interface OnTextSelection {      // TODO : 메모 시 웹뷰 위로 올리는 기능 하면 커스텀셀렉션 위치 틀어짐
        /**
         * onStart- text selection start
         */
        void onStartTextSelection();
        /**
         * onEnd - text selection end
         */
        void onEndTextSelection();
        void onOverflowTextSelection(BookHelper.SelectionErrorType selectionErrorType);
        void onOverflowMemoContent();
    }

    public interface OnAnalyticsListener{
        void onAnnotationQuick();
        void onAnnotationMergeSelection();
        void onAnnotationMergeQuick();
    }

    public ViewerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        mDefStyle = defStyle;

    }

    public ViewerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        mAttrs = attrs;
    }

    public ViewerContainer(Context context) {
        super(context);
        mContext = context;
    }

    public ViewerContainer(Context context, String instName) {
        super(context);
        mContext = context;
        mInstName = instName;
    }

    /**
     @breif 뷰어 초기화
     */
    public void initView() throws XmlPackageException, XmlContainerException, EpubFileSystemException{

        if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView = new FixedLayoutScrollView(mContext, mPageDirection, mEpubFile);
            mFixedLayoutView.setChapterInfo(mChapterInfo);
            mFixedLayoutView.setSpineInfo(mSpineInfo);
            addView(mFixedLayoutView);

            ttsHighlighter = new Highlighter();
            ttsDataInfoManager = new TTSDataInfoManager();
            mFixedLayoutView.setTTSDataInfoManager(ttsDataInfoManager);
            mFixedLayoutView.setTTSHighlighter(ttsHighlighter);
            mFixedLayoutView.getTTSHighlighter().setOnHighlightRectInfoListener(this);

            mediaOverlayController = new MediaOverlayController();
            mFixedLayoutView.setMediaOverlayController(mediaOverlayController);

            bgmPlayer = new BGMPlayer(mEpubFile);
            mFixedLayoutView.setBgmPlayer(bgmPlayer);

        } else if( mLayoutMode == LayoutMode.Reflowable ){

            if( mDefStyle != -1 ){
                mEPubViewer = new EPubViewer(mContext, mAttrs, mDefStyle);
                mEPubViewer.setChapterInfo(mChapterInfo);
                mEPubViewer.setSpineInfo(mSpineInfo);
                mEPubViewer.setEpubFileInfo(mEpubFile);
                addView(mEPubViewer);
            } else {
                if( mAttrs != null ){
                    mEPubViewer = new EPubViewer(mContext, mAttrs);
                    mEPubViewer.setChapterInfo(mChapterInfo);
                    mEPubViewer.setSpineInfo(mSpineInfo);
                    mEPubViewer.setEpubFileInfo(mEpubFile);
                    addView(mEPubViewer);
                } else {
                    if( mInstName != null ){
                        mEPubViewer = new EPubViewer(mContext, mInstName);
                        mEPubViewer.setChapterInfo(mChapterInfo);
                        mEPubViewer.setSpineInfo(mSpineInfo);
                        mEPubViewer.setEpubFileInfo(mEpubFile);
                        addView(mEPubViewer);
                    } else {
                        mEPubViewer = new EPubViewer(mContext);
                        mEPubViewer.setChapterInfo(mChapterInfo);
                        mEPubViewer.setSpineInfo(mSpineInfo);
                        mEPubViewer.setEpubFileInfo(mEpubFile);
                        addView(mEPubViewer);
                    }
                }
            }

            mEPubViewer.setPageDirection(mPageDirection);

            ttsDataInfoManager = new TTSDataInfoManager();
            ttsDataInfoManager.addTTSDataInfoJSInterface(mEPubViewer);

            ttsHighlighter = new Highlighter();
            ttsHighlighter.addHighlightJSInterface(mEPubViewer);
            ttsHighlighter.setOnHighlightRectInfoListener(this);

            mediaOverlayController = new MediaOverlayController();
            mEPubViewer.setMediaOverlayController(mediaOverlayController);
            mediaOverlayController.addMediaOverlayJSInterface(0, mEPubViewer);

            bgmPlayer = new BGMPlayer(mEpubFile);
            bgmPlayer.setCurrentWebView(mEPubViewer, null);
        }

        setOnMediaOverlayListener(new OnMediaOverlayListener() {

            @Override
            public void addMediaOverlayHighlighter(String currentFilePath, String id) {
                addMediaOverlayHighlight(currentFilePath, id);
            }

            @Override
            public void removeMediaOverlayHighlighter() {
                removeMediaOverlayHighlight();
            }
        });

        setIgnoreDrm(mIgnoreDrm);
        setOnDecodeContent(mOnDecodeContent);
    }

    /**
     @breif Epub parsing
     */
    public void startEpubParse(String path) throws XmlPackageException, XmlContainerException, EpubFileSystemException, XmlNavigationException{

        MyZip.mIsUnzip = true;
        MyZip.mUnzipDir = path;

        System.gc();

        mRequestStringOfFile = new RequestStringOfFileListener() {

            @Override
            public String requestStringOfFile(String filePath, String drmKey) {
                if( mIgnoreDrm ) {
                    return BookHelper.file2String(filePath);
                } else {
                    if( mOnDecodeContent != null && filePath!=null) {
                        String decodeStr = mOnDecodeContent.onDecode(drmKey, filePath);
                        if(decodeStr!=null)
                            return decodeStr;
                    }
                }
                return "";
            }
        };

        mEpubFile = new EpubFile(path, mRequestStringOfFile);
        mSpineInfo = new ReadingSpine(mEpubFile);
        mChapterInfo = new ReadingChapter(mEpubFile);
        mReadingAudioNavigation = new ReadingAudioNavigation(mEpubFile);
        mImageReader = new ImageReader(mEpubFile);
        mLayoutMode = LayoutMode.Reflowable;
        mPageDirection = PageDirection.LTR;
        mOrientation = Orientation.Auto;

        String pageDirection = mEpubFile.getGlovalTextDirection();
        if( pageDirection.equals("")  || pageDirection.equals(PageDirectionType.LTR) ) {
            mPageDirection = PageDirection.LTR;
        } else {
            mPageDirection = PageDirection.RTL;
        }

        if( mEpubFile.getVersion().equals(PackageVersion.EPUB3) ) {
            String layoutMode = mEpubFile.getRenditionLayout();
            if( layoutMode.equals("") || layoutMode.equals(RenditionLayoutType.REFLOWABLE) ) {
                mLayoutMode = LayoutMode.Reflowable;
            } else {
                mLayoutMode = LayoutMode.FixedLayout;
            }

            String orientation = mEpubFile.getRenditionOrientation();
            if( orientation == null || orientation.equals(RenditionOrientationType.AUTO) ) {
                mOrientation = Orientation.Auto;
            } else if( orientation.equals(RenditionOrientationType.PORTRAIT) ) {
                mOrientation = Orientation.Portrait;
            } else if( mOrientation.equals(RenditionOrientationType.LANDSCAPE) ) {
                mOrientation = Orientation.Landscape;
            }
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnTouchListener(l);
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnKeyListener(l);
    }

    public void destroy(){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.destroy();

        mEpubFile = null;
        mSpineInfo = null;
        mChapterInfo = null;
        mImageReader = null;
        mEPubViewer = null;
        mFixedLayoutView = null;
    }

    /**
     * 해당 도서가 일반 도서(Reflowable)인지 FixedLayout인지 전달
     * @return
     */
    public LayoutMode getViewerLayoutMode() {
        return mLayoutMode;
    }

    /**
     * 해당 도서가 우철인지 좌철인지 전달
     * @return RTL : 좌철, LTR : 우철
     */
    public PageDirection getViewerPageDirection(){  // TODO : 스펠링 틀려서 변경함 기존 getViewerPageDerection() 였음
        return mPageDirection;
    }

    public UnModifiableArrayList<ReadingOrderInfo> getSpineInfoList() {

        if( mSpineInfo != null )
            return mSpineInfo.getSpineInfos();

        return new UnModifiableArrayList<>();
    }

    /**
     @breif Linear 속성 값이 no인 항목 리스트 가져오기
     @return Linear 속성 값이 no인 항목 리스트
     */
    public UnModifiableArrayList<ReadingOrderInfo> getNonLinearSpineInfoList() {

        if( mSpineInfo != null )
            return mSpineInfo.getNonLinearSpineInfos();

        return new UnModifiableArrayList<>();
    }

    /**
     @breif 현재 spine 정보 가져오기
     @return 현재 spine 정보가 담긴 ReadingOrderInfo 객체
     */
    public ReadingOrderInfo getCurrentSpineInfo() {

        if( mSpineInfo != null )
            return mSpineInfo.getCurrentSpineInfo();

        return new ReadingOrderInfo("", 0.0, 0.0, false);
    }

    public int getCurrentSpneIndex() {
        return mSpineInfo.getCurrentSpineIndex();
    }

    public UnModifiableArrayList<ChapterInfo> getChapterInfoList() {

        if( mChapterInfo != null )
            return mChapterInfo.getChapters();

        return new UnModifiableArrayList<>();
    }

    public ChapterInfo getCurrentChapterInfo() {

        if( mChapterInfo != null )
            return mChapterInfo.getCurrentChapter();

        return new ChapterInfo("", "", 0, "");
    }

    public String getBookTitle() {

        if(mEpubFile!=null){
            Set<XmlDCMES> dcTitles = mEpubFile.getDublinCoreTitles().keySet();

            if( dcTitles.size() > 0 )
                return dcTitles.iterator().next().getValue();
        }
        return "";
    }

    public String getBookCreator() {

        if(mEpubFile!=null){
            Set<XmlDCMES> dcAuthors = mEpubFile.getDublinCoreCreators().keySet();

            if( dcAuthors.size() > 0 )
                return dcAuthors.iterator().next().getValue();
        }
        return "";
    }

    /**
     * 	 Drm을 사용할 경우 호출되는 이벤트
     *   @param listener : OnDecodeContent 리스너 객체
     */
    public void setOnDecodeContent(OnDecodeContent listener) {
        mOnDecodeContent = listener;
        if (mLayoutMode == LayoutMode.Reflowable) {
            mEPubViewer.setOnDecodeContent(listener);
        } else if (mLayoutMode == LayoutMode.FixedLayout) {
            mFixedLayoutView.setOnDecodeContent(listener);
        }
    }

    /**
     * 	 뷰어의 상태에 따라 발생  start, end
     *   @param listener : OnViewerState 리스너 객체
     */
    public void setOnViewerState(OnViewerState listener) {  // TODO : 프론트에서 안쓰면 없애자
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnViewerState(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnViewerState(listener);
        }
    }

    /**
     * 	 text selection start, end
     *   @param listener : OnTextSelection 리스너 객체
     */
    public void setOnTextSelection(OnTextSelection listener) {  // TODO : 프론트에서 안쓰면 없애자
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnTextSelection(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnTextSelection(listener);
        }
    }

    public void setOnMemoSelection(OnMemoSelection listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnMemoSelection(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnMemoSelection(listener);
    }

    /**
     * 	 뷰어 에러 발생 시 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnReportError 리스너 객체
     */
    public void setOnReportError(OnReportError listener) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnReportError(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnReportError(listener);
        }
    }

    /**
     * 	 뷰어에서 현재 페이지 정보를 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnCurrentPageInfo 리스너 객체
     */
    public void setOnCurrentPageInfo(OnCurrentPageInfo listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnCurrentPageInfo(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnCurrentPageInfo(listener);
    }

    /**
     * 	 뷰어에서 검색 결과를 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnSearchResult 리스너 객체
     */
    public void setOnSearchResult(OnSearchResult listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnSearchResult(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnSearchResult(listener);
    }

    /**
     * 	 뷰어에서 북마크에 대한 내용을 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnPageBookmark 리스너 객체
     */
    public void setOnPageBookmark(OnPageBookmark listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnPageBookmark(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnPageBookmark(listener);
    }

    /**
     * 	 뷰어에서 터치 이벤트에 대한 내용을 전달받을 리스너를 등록하는 메소드
     */
    public void setOnTouchEventListener(OnTouchEventListener listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnTouchEventListener(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnTouchEventListener(listener);
    }

    /**
     * 	 뷰어에서 챕터 로딩에 대한 내용을 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnChapterChange 리스너 객체
     */
    public void setOnChapterChange(OnChapterChange listener) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnChapterChange(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnChapterChange(listener);
    }

    /**
     * 	 뷰어에서 메뉴 활성 여부를 전달받을 리스너를 등록하는 메소드
     *   @param listener : OnSelectionMenu 리스너 객체
     */
    public void setOnSelectionMenu(OnContextMenu listener) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnSelectionMenu(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnSelectionMenu(listener);
        }
    }

    /**
     * 	 뷰어에서 페이지 스크롤 발생 시 전달받을 리스너 등록하는 메소드
     *   @param listener : OnPageScroll 리스너 객체
     */
    public void setOnPageScroll(OnPageScroll listener) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnPageScroll(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnPageScroll(listener);
        }
    }

    /**
     * 	 뷰어에서 이미지/링크 등 선택 시 전달받을 리스너 등록하는 메소드
     *   @param listener : OnTagClick 리스너 객체
     */
    public void setOnTagClick(OnTagClick listener) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnTagClick(listener);
        } else if(mLayoutMode == LayoutMode.FixedLayout) {
            mFixedLayoutView.setOnTagClick(listener);
        }
    }

    /**
     * 	 뷰어에서 컨텐츠의 시작/끝 전달받을 리스너 등록하는 메소드
     *   @param listener : OnBookStartEnd 리스너 객체
     */
    public void setOnBookStartEnd(OnBookStartEnd listener) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnBookStartEnd(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnBookStartEnd(listener);
        }
    }

//    public void forceChapterChanging() {        // TODO : 프론트에서 안쓰면 없애자 deleted
//        if( mLayoutMode == LayoutMode.Reflowable )
//            mEPubViewer.forceChapterChanging();
//    }

    public String getSelectedText() {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            return mEPubViewer.getSelectedText();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            return mFixedLayoutView.getSelectedText();
        }
        return "";
    }

    public void setSelectionIcon(Drawable start, Drawable end) {    // TODO :: new custom selection - modified
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setSelectionIcon(start, end);
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.setSelectionIcon(start, end);
        }
    }

    public void setContextMenuSize(float height, int topMargin, int bottomMargin) {    // TODO : new
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setContextMenuSize(height, topMargin, bottomMargin);
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.setContextMenuSize(height, topMargin, bottomMargin);
        }
    }

    public void setSelectionMaxLength(int maxSelectionLength){  // TODO : new
        BookHelper.maxSelectionLength = maxSelectionLength;
    }

    public void setTextSelectionColor(int color){   // TODO :: new custom selection - modified
        BookHelper.textSelectionColor = color;
    }

    public void setSlideResource(boolean bLeft, int inId, int outId) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setSlideResource(bLeft, inId, outId);
    }

    public void setIgnoreDrm(boolean isIgnore){
        mIgnoreDrm = isIgnore;
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setIgnoreDrm(isIgnore);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setIgnoreDrm(isIgnore);
        }
    }

    public void setUseEPUB3Viewer(boolean use){
        BookHelper.setUseEPUB3Viewer(use);
    }

    public boolean loadBook(String path) {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            return mEPubViewer.loadBook(path);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            return mFixedLayoutView.loadBook();
        }
        return false;
    }

    public void showBook() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.showBook();
    }

    public void reLoadBook(){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.reLoadBook();
    }

    public void scrollPrior(){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.scrollPrior();
        else if( mLayoutMode == LayoutMode.FixedLayout)
            mFixedLayoutView.scrollPrior();
    }

    public void scrollNext(){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.scrollNext();
        } else if( mLayoutMode == LayoutMode.FixedLayout){
            mFixedLayoutView.scrollNext();
        }
    }

    public void goPageByJump() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPageByJump();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPageByJump();
    }

    public void goPageByLink(String fileName, String id){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPageByLink(fileName, id);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(fileName);
    }

    public void goPage(Highlight high){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.goPage(high);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.goPage(high);
        }
    }

    public void goPage(Bookmark bmd){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPage(bmd);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(bmd);
    }

    public void goPage(SearchResult sr){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPage(sr);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(sr);
    }

    public void goPage(ChapterInfo chapter){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPage(chapter);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(chapter);
    }

    public void goPage(int chapterIndex){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPage(chapterIndex);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(chapterIndex);
    }

    public void goPage(double percent){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.goPage(percent);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.goPage(percent);
    }

    public void searchText(String keyword){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.searchText(keyword);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.searchText(keyword);
    }

    public void focusText(SearchResult sr){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.focusText(sr);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.focusText(sr);
    }

    public void removeSearchHighlight(){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.removeSearchHighlight();
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.removeSearchHighlight();
        }
    }

    public void registSelectionMenu(PopupWindow pw){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.registSelectionMenu(pw);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.registSelectionMenu(pw);
        }
    }

    public ArrayList<Highlight> getHighlights(){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            return mEPubViewer.getHighlights();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            return mFixedLayoutView.getHighlights();
        }
        return new ArrayList<>();
    }

    public void deleteHighlight(Highlight high){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.deleteHighlight(high);
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
//            mFixedLayoutView.deleteHighlight(high);
        }
    }

    public void hasBookmark() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.hasBookmark();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.hasBookmark();
    }

    public void doBookmark() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.doBookmark();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.doBookmark();
    }

    public void deleteBookmark(Bookmark bookmark) {
        if( mLayoutMode ==LayoutMode.Reflowable )
            mEPubViewer.deleteBookmark(bookmark);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.deleteBookmark(bookmark);
    }

    public ArrayList<Bookmark> getBookMarks() {
        if( mLayoutMode == LayoutMode.Reflowable )
            return mEPubViewer.getBookMarks();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            return mFixedLayoutView.getBookmarkList();
        return new ArrayList<>();
    }

    public UnModifiableArrayList<String> getImageList()  {
        return mImageReader.getPublicationImageList();
    }

    public int getSpineIndexFromPercent(double percent){
        if( mLayoutMode == LayoutMode.Reflowable )
            return mEPubViewer.getSpineIndexFromPercent(percent);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            return mFixedLayoutView.getSpineIndexFromPercent(percent);
        return 0;
    }

    public ReadingOrderInfo getSpineInfoFromChapterInfo(ChapterInfo chapterInfo){
        String chapterFilePath = chapterInfo.getChapterFilePath();
        if( chapterFilePath.indexOf("#") != -1 ) {
            chapterFilePath = chapterFilePath.substring(0, chapterFilePath.indexOf("#"));
        }
        ReadingOrderInfo spine = mSpineInfo.getSpineInfo(chapterFilePath);
        return spine;
    }

    public ChapterInfo getChapterFromSpineIndex(int index){
        if( mLayoutMode == LayoutMode.Reflowable )
            return mEPubViewer.getChapterFromSpineIndex(index);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            return mFixedLayoutView.getChapterFromSpineIndex(index);
        return null;
    }

    public int getChapterStartPage(String filePath){     // TODO : 프론트에서 안쓰면 없애자
        return mSpineInfo.getSpineIndex(filePath);
    }

    public void setPreviewMode(boolean onoff){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setPreviewMode(onoff);
    }

    public void getCurrentTopPath(){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.getCurrentTopPath();
    }

    public void changeFontDirect(String fontName, String faceName, String fontPath){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeFontDirect(fontName, faceName, fontPath);
    }

    public void changeFontSizeDirect(String value) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeFontSizeDirect(value);
    }

    public void changeLineHeightDirect(String value) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeLineHeightDirect(value);
    }

    public void changeBackgroundColorDirect(int color, boolean nightMode) {
        setBackgroundColor(color);
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeBackgroundColorDirect(color, nightMode);
    }

    public void changeParaHeightDirect(String value){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeParaHeightDirect(value);
    }

    public void changeMarginDirect(int left, int top, int right, int bottom) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeMarginDirect(left, top, right, bottom);
    }

    public void changeIndentDirect(boolean value) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.changeIndentDirect(value);
    }

    public void	setDefaultReadingStyle(ReadingStyle readingStyle){
        if(mLayoutMode==LayoutMode.Reflowable){
            mEPubViewer.setDefaultReadingStyle(readingStyle);
        }
    }

    public void	setDefaultReadingStyle(){
        if(mLayoutMode==LayoutMode.Reflowable){
            mEPubViewer.setDefaultReadingStyle();
        }
    }

    public void saveAllStyle(String stylePath, Integer fontSize, String fontName, String faceName, String fontPath,
                             Integer lineSpace, Integer paraSpace, Integer indent, Integer marginLeft, Integer marginTop, Integer marginRight, Integer marginBottom ) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.saveAllStyle(stylePath, fontSize, fontName, faceName, fontPath, lineSpace, paraSpace, indent, marginLeft, marginTop, marginRight, marginBottom);
    }

    public void saveAllViewerData() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.saveAllViewerData();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.saveUserBookData();
    }

    public boolean saveHighlights(){
        if( mLayoutMode == LayoutMode.Reflowable ){
            return mEPubViewer.saveHighlights();
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            return mFixedLayoutView.saveHighlights();
        }
        return false;
    }

    public boolean saveBookmarks() {
        if( mLayoutMode == LayoutMode.Reflowable )
            return mEPubViewer.saveBookmarks();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            return mFixedLayoutView.saveBookmarks();

        return false;
    }

    public boolean saveOption(){
        if( mLayoutMode == LayoutMode.Reflowable ){
            return mEPubViewer.saveOption();
        }
        return false;
    }

    public void restoreHighlights(){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.restoreHighlights();
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.loadBookmarkData();
        }
    }

    public void restoreBookmarks() {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.restoreBookmarks();
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.loadBookmarkData();
    }

    public void onClose() {
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.onClose();
        } else if ( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.onClose();
        }
    }

    public void setMoveToLinearNoChapter(OnMoveToLinearNoChapterListener listener) {
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.setMoveToLinearNoChapter(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.setMoveToLinearNochapter(listener);
        }
    }

    public void setOnVideoInfoListener(OnVideoInfoListener listener) {
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.setOnVideoInfoListener(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ){
            mFixedLayoutView.setOnVideoInfoListener(listener);
        }
    }

    public void setOnMediaControlListener(OnMediaControlListener listener){
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.setOnMediaControlListener(listener);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.setOnMediaControlListener(listener);
    }

    public void preventPageMove(boolean isPrevent) {
        if( mLayoutMode == LayoutMode.Reflowable )
            mEPubViewer.preventPageMove(isPrevent);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            mFixedLayoutView.preventPageMove(isPrevent);
    }

    public void setOnTTSHighlighterListener(OnHighlighterListener listener) {
        if( ttsHighlighter != null ){
            if(mLayoutMode==LayoutMode.Reflowable ){
                ttsHighlighter.setOnHighlighterListener(listener);
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                ttsHighlighter.setOnHighlighterListener(listener);
            }
        }
    }

    public void setOnTTSDataInfoListener(OnTTSDataInfoListener listener) {
        if( ttsDataInfoManager != null ){
            if(mLayoutMode==LayoutMode.Reflowable ){
                ttsDataInfoManager.setOnTTSDataInfoManager(listener);
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                ttsDataInfoManager.setOnTTSDataInfoManager(listener);
            }
        }
    }

    public void addTTSHighlight(TTSDataInfo ttsDataInfo) {
        if( ttsHighlighter != null ){
            if( mLayoutMode == LayoutMode.Reflowable ){
                ttsHighlighter.add(mEPubViewer, ttsDataInfo);
            } else if( mLayoutMode == LayoutMode.FixedLayout ){
                ttsHighlighter.add(mFixedLayoutView.getCurrentWebView(ttsDataInfo.getFilePath()), ttsDataInfo);
            }
        }
    }

    public void removeTTSHighlight() {
        if( ttsHighlighter != null ){
            ttsHighlighter.remove();
        }
    }

    public void requestTTSDataFromSelection(){

        if( ttsDataInfoManager != null ) {
            if( mLayoutMode == LayoutMode.Reflowable ){
                ttsDataInfoManager.requestTTSDataFromSelection(-1);
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                ttsDataInfoManager.requestTTSDataFromSelection(mFixedLayoutView.getTouchedWebviewPosition());
            }
        }

        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.removeSelection();
        } else if(mLayoutMode == LayoutMode.FixedLayout){
            mFixedLayoutView.removeSelection();
        }
    }

    public void clearAllTTSDataInfo() {
        if( ttsDataInfoManager != null )
            ttsDataInfoManager.clear();
    }

    public ArrayList<TTSDataInfo> makeTTSDataInfo() {

        ArrayList<TTSDataInfo> ttsDataList;

        if( ttsDataInfoManager != null ) {
            if( mLayoutMode == LayoutMode.Reflowable ){
                ttsDataList = ttsDataInfoManager.readContents(mEPubViewer, mEPubViewer.getCurrentHtmlString(), mSpineInfo.getCurrentSpineInfo().getSpinePath());
                return ttsDataList;
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                ttsDataList = ttsDataInfoManager.readContents(mFixedLayoutView.getLeftWebView(), mFixedLayoutView.getRightWebView(), mFixedLayoutView.getPageData(false));
                return ttsDataList;
            }
        }
        return new ArrayList<>();
    }

    public ArrayList<TTSDataInfo> makeTTSDataInfoInBackground(){
        if( ttsDataInfoManager != null ){
            if( mLayoutMode == LayoutMode.Reflowable ){
                int nextChapterIndex = mEPubViewer.getNextChapterIndex();
                if(nextChapterIndex!=-1){
                    return ttsDataInfoManager.readContents(mEPubViewer, mEPubViewer.getCurrentHtmlString(), mSpineInfo.getCurrentSpineInfo().getSpinePath());
                }
            } else if( mLayoutMode == LayoutMode.FixedLayout ){
                FixedLayoutPageData currentPageData = mFixedLayoutView.getPageData(true);
                if(currentPageData!=null) {
                    return ttsDataInfoManager.readContentsFromPageData(currentPageData);
                }
            }
        }
        return null;
    }

    public void moveByTTSData(TTSDataInfo ttsDataInfo){
        if(mLayoutMode == LayoutMode.Reflowable){
            mEPubViewer.moveByTTSData(ttsDataInfo);
        } else if(mLayoutMode == LayoutMode.FixedLayout){
            mFixedLayoutView.goPage(ttsDataInfo.getFilePath());
        }
    }

    public void requestTTSStartPosition() {
        if( ttsDataInfoManager != null ) {
            if( mLayoutMode == LayoutMode.Reflowable )
                ttsDataInfoManager.requestStartPosition();
            else if( mLayoutMode == LayoutMode.FixedLayout )
                ttsDataInfoManager.requestStartPosition();
        }
    }

    @Override
    public void requestDrawRect(JSONArray rectArray, String currentFilePath) {
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.setTTSHighlightRect(rectArray);
            mEPubViewer.invalidate();
        } else if( mLayoutMode==LayoutMode.FixedLayout){
            mFixedLayoutView.setTTSHighlightRect(rectArray, currentFilePath);
        }
    }

    @Override
    public void requestRemoveRect() {
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.removeTTSHighlightRect();
            mEPubViewer.invalidate();
        } else if( mLayoutMode==LayoutMode.FixedLayout){
            mFixedLayoutView.removeTTSHighlightRect();
        }
    }

    public interface OnMediaOverlayStateListener{
        void existMediaOverlay(boolean hasMediaOverlay);
        void setPositoinOfMediaOverlayDone();
        void selectedMediaOverlayPlaying();
        void finishMediaOverlayPlay();
        void didMediaOverlayPlayListener();
        void didMediaOverlayPauseListener();
        void didMediaOverlayStopListener();
    }

    public void clearAllMediaOverlayInfo() {
        if( mediaOverlayController != null )
            mediaOverlayController.clear();
    }

    public void setOnMediaOverlayStateListerner(OnMediaOverlayStateListener listener){
        if(mLayoutMode == LayoutMode.Reflowable){
            mEPubViewer.setOnMediaOverlayStateListener(listener);
        } else if(mLayoutMode == LayoutMode.FixedLayout){
            mFixedLayoutView.setOnMediaOverlayStateListener(listener);
        }
    }

    public void setOnMediaOverlayListener(OnMediaOverlayListener listener) {
        if( mediaOverlayController != null )
            mediaOverlayController.setOnMediaOverlayListener(listener);
    }

    public boolean existMediaOverlayOnChapter(){

        if( mediaOverlayController !=null && mSpineInfo!=null){

            if(mSpineInfo.getCurrentSpineInfo().isHasMediaOverlay()){
                return true;
            }

            if(mLayoutMode == LayoutMode.FixedLayout && mFixedLayoutView.mPageMode == FixedLayoutScrollView.PageMode.TwoPage){
                int nextSpineIndex = mSpineInfo.getCurrentSpineIndex()+1;
                if(nextSpineIndex<mSpineInfo.getSpineInfos().size() && mSpineInfo.getSpineInfoBySpineIndex(nextSpineIndex).isHasMediaOverlay()){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean initMediaOverlay() {

        if( mediaOverlayController !=null){

            if (mLayoutMode == LayoutMode.Reflowable){
                SmilContentProvider smilContentProvider = new SmilContentProvider();
                smilContentProvider.setSmilPath(mEpubFile.getSmilFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath()));
                smilContentProvider.load(new SmilDocumentReader(mEpubFile.getSmilFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath())));
                mediaOverlayController.setSmilSyncs(smilContentProvider.getSmilSyncsByFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath()));
                return true;
            } else if(mLayoutMode == LayoutMode.FixedLayout){

                int nextSpineIndex = mSpineInfo.getCurrentSpineIndex()+1;
                LinkedHashMap<String, SmilSync> smilSyncs = new LinkedHashMap<>();
                SmilContentProvider smilContentProvider;

                if( mSpineInfo.getCurrentSpineInfo().isHasMediaOverlay()){
                    smilContentProvider = new SmilContentProvider();
                    smilContentProvider.setSmilPath(mEpubFile.getSmilFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath()));
                    smilContentProvider.load(new SmilDocumentReader(mEpubFile.getSmilFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath())));
                    smilSyncs = smilContentProvider.getSmilSyncsByFilePath(mSpineInfo.getCurrentSpineInfo().getSpinePath());
                    mediaOverlayController.setSmilSyncs(smilSyncs);
                }

                if(mFixedLayoutView.mPageMode == FixedLayoutScrollView.PageMode.TwoPage  && mSpineInfo.getSpineInfoBySpineIndex(nextSpineIndex).isHasMediaOverlay()){
                    smilContentProvider = new SmilContentProvider();
                    smilContentProvider.setSmilPath(mEpubFile.getSmilFilePath(mSpineInfo.getSpineInfoBySpineIndex(nextSpineIndex).getSpinePath()));
                    smilContentProvider.load(new SmilDocumentReader(mEpubFile.getSmilFilePath(mSpineInfo.getSpineInfoBySpineIndex(nextSpineIndex).getSpinePath())));
                    smilSyncs.putAll(smilContentProvider.getSmilSyncsByFilePath(mSpineInfo.getSpineInfoBySpineIndex(nextSpineIndex).getSpinePath()));
                    mediaOverlayController.setSmilSyncs(smilSyncs);
                    return true;
                } else
                    return true;
            }
        }
        return false;
    }

    public void existMediaOverlayOnPage(){
        if(mLayoutMode==LayoutMode.Reflowable){
            mEPubViewer.existMediaOverlayOnPage();
        }
    }

    public void setPositionOfMediaOverlay(){

        mediaOverlayController.clear();

        if(mediaOverlayController!=null){
            if(mLayoutMode == LayoutMode.Reflowable){
                mediaOverlayController.getIdList(mLayoutMode);
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                mediaOverlayController.getIdList(mLayoutMode);
            }
        }
    }

    public void playMediaOverlay(){
        if(mediaOverlayController!=null)
            mediaOverlayController.play();
    }

    public void pauseMediaOverlay(){
        if(mediaOverlayController!=null)
            mediaOverlayController.pause();
    }

    public void stopMediaOverlay(){
        if(mediaOverlayController!=null)
            mediaOverlayController.stop(true);
    }

    private void addMediaOverlayHighlight(String currentFilePath, String id){
        if(ttsHighlighter!=null){
            if(mLayoutMode == LayoutMode.Reflowable){
                ttsHighlighter.addMediaOverlayHighlight(mEPubViewer, id,  mEpubFile.getActiveClass(), mEpubFile.getPlaybackActiveClass());
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                ttsHighlighter.addMediaOverlayHighlight(mFixedLayoutView.getCurrentWebView(currentFilePath), id, mEpubFile.getActiveClass(), mEpubFile.getPlaybackActiveClass());
            }
        }
    }

    private void removeMediaOverlayHighlight(){
        if(ttsHighlighter!=null && mEpubFile!=null){
            if(mLayoutMode == LayoutMode.Reflowable){
                ttsHighlighter.removeMediaOverlayHighlight(mEPubViewer, mEpubFile.getActiveClass(), mEpubFile.getPlaybackActiveClass());
            } else if(mLayoutMode == LayoutMode.FixedLayout){
                if(mFixedLayoutView.getLeftWebView()!=null)
                    ttsHighlighter.removeMediaOverlayHighlight(mFixedLayoutView.getLeftWebView(), mEpubFile.getActiveClass(), mEpubFile.getPlaybackActiveClass());
                if(mFixedLayoutView.getRightWebView()!=null)
                    ttsHighlighter.removeMediaOverlayHighlight(mFixedLayoutView.getRightWebView(), mEpubFile.getActiveClass(), mEpubFile.getPlaybackActiveClass());
            }
        }
    }

    public interface OnAudioListener{
        void finishAudioList(HashMap<String, AudioContent> audioContents);
        void existAudioContentsOncurrentPage(ArrayList<String> audioIdList);
        void didPlayAudio(String xPath, double startTime);
        void didFinishAudio(String xPath);
        void didPauseAudio(String xPath, double pauseTime);
        void didStopAudio(String xPath);
        void updateCurrentPlayingPosition(String xPath, double currentTime);
    }

    public void setOnAudioListener(OnAudioListener listener){
        if(mLayoutMode == LayoutMode.Reflowable){
            mEPubViewer.setOnAudioListener(listener);
        }
    }

    public void setPreventMediaControl(boolean isPrevent){
        if(mLayoutMode == LayoutMode.Reflowable)
            mEPubViewer.setPreventMediaControl(isPrevent);
        else if(mLayoutMode == LayoutMode.FixedLayout)
            mFixedLayoutView.setPreventMediaControl(isPrevent);
    }

    public void playAudioContent(AudioContent audioContent){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.audioContentPlayer.play(audioContent.getXPath());
        }
    }

    public void playAudioContent(AudioContent audioContent, double startTime){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.audioContentPlayer.play(audioContent.getXPath(), startTime);
        }
    }

    public void pauseAudioContent(AudioContent audioContent){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.audioContentPlayer.pause(audioContent.getXPath());
        }
    }

    public void stopAudioContent(AudioContent audioContent){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.audioContentPlayer.stop(audioContent.getXPath());
        }
    }

    public void loopAudioContent(AudioContent audioContent, boolean loop){
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.audioContentPlayer.loop(audioContent.getXPath(), loop);
        }
    }

    public void moveAudioPlayingPosition(String xPath, double movingUnit){
        if(mLayoutMode == LayoutMode.Reflowable)
            mEPubViewer.moveAudioPlayingPosition(xPath, movingUnit);
    }

    public boolean isSetLoop() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            return false;
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            String releaseVersion = Build.VERSION.RELEASE;
            String[] versionSplit = releaseVersion.split("\\.");
            if(Integer.parseInt(versionSplit[2])<=2){
                return false;
            } else{
                return true;
            }
        } else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
            return true;
        }
        return true;
    }

    public interface OnBGMControlListener{
        void didBGMPlayListener();
        void didBGMPauseListener();
        void didBGMStopListener();
    }

    public interface OnBGMStateListener{
        void setCurrentBGMState();
    }

    public void setOnBGMControlListener(OnBGMControlListener listener){
        if(bgmPlayer!=null)
            bgmPlayer.setOnBGMControlListener(listener);
    }

    public boolean isPlayingBGM(){
        return BGMMediaPlayer.getBgmMediaPlayerClass().isPlaying;
    }

    public void playBGM(){
        if(bgmPlayer!=null)
            bgmPlayer.play();
    }

    public void pauseBGM(){
        if(bgmPlayer!=null)
            bgmPlayer.pause();
    }

    public void stopBGM(){
        if(bgmPlayer!=null)
            bgmPlayer.stop();
    }

    public String getDocumentVersion(){
        if(mEpubFile!=null)
            return mEpubFile.getVersion();
        return "";
    }

    public boolean isValidNavigation(){     // TODO : 프론트에서 안쓰면 없애자
        return mEpubFile.isValidNavigation();
    }

    public void unpluggedHeadSet(boolean isUnplugged){
        if(mLayoutMode == LayoutMode.Reflowable && mEPubViewer!=null){
            mEPubViewer.unpluggedHeadSet(isUnplugged);
        } else if (mLayoutMode == LayoutMode.FixedLayout && mFixedLayoutView!=null){
            mFixedLayoutView.unpluggedHeadSet(isUnplugged);
        }
    }

    public boolean hasMediaOverlayOnContents(){

        boolean hasMediaOverlayContents = false;

        Iterator<String> iterator = mEpubFile.getPublicationResources().keySet().iterator();
        while (iterator.hasNext()) {
            String ids = iterator.next();
            XmlItem xmlItem = mEpubFile.getPublicationResources().get(ids);
            if(xmlItem!=null && !xmlItem.getMediaOverlay().isEmpty()){
                hasMediaOverlayContents = true;
                break;
            }
        }
        return hasMediaOverlayContents;
    }

//    public interface OnNoterefListener {
//        void didShowNoterefPopup();
//        void didHideNoterefPopup();
//    }

//    public void setOnNoterefListener(OnNoterefListener listener) {
//        if( mLayoutMode == LayoutMode.Reflowable ){
//            mEPubViewer.setOnNoterefListener(listener);
//        } else if( mLayoutMode == LayoutMode.FixedLayout ){
//            mFixedLayoutView.setOnNoterefListener(listener);
//        }
//    }

//    public boolean isNoterefEnabled(){
//        if( mLayoutMode == LayoutMode.Reflowable ){
//            return mEPubViewer.isNoterefEnabled();
//        } else if( mLayoutMode == LayoutMode.FixedLayout ){
//            return mFixedLayoutView.isNoterefEnabled();
//        }
//        return false;
//    }
//
//    public void hideNoteref(){
//        if( mLayoutMode == LayoutMode.Reflowable ){
//            mEPubViewer.hideNoteref();
//        } else if( mLayoutMode == LayoutMode.FixedLayout ){
//            mFixedLayoutView.hideNoteref();
//        }
//    }
//
//    public void setPreventNoteref(boolean isPrevent){
//        if( mLayoutMode == LayoutMode.Reflowable ){
//            mEPubViewer.setPreventNoteref(isPrevent);
//        } else if( mLayoutMode == LayoutMode.FixedLayout ){
//            mFixedLayoutView.setPreventNoteref(isPrevent);
//        }
//    }

    public void setMemoIconPath(String iconPath){
        BookHelper.memoIconPath = iconPath;
    }

    public ReadpositionData getReadpositionData(String filePath){   // TODO : 프론트에서 안쓰면 없애자
        return BookHelper.getReadpositionData(filePath);
    }

    public void setPageColumOnLandscape(int pageNum){
        BookHelper.pageNumOnLandscape=pageNum;
    }

    public void setPageEffect(int effect){
        BookHelper.animationType=effect;
        if( mLayoutMode == LayoutMode.FixedLayout && mFixedLayoutView!=null ){
            if(BookHelper.animationType==0){
                mFixedLayoutView.pagerAnimation = false;
            } else {
                mFixedLayoutView.pagerAnimation = true;
            }
        }
    }

    public boolean checkEpubCondition(String epubPath){     // TODO : 프론트에서 안쓰면 없애자

        boolean isMetaInfFolderExist = false;
        boolean isContainerFileExist = false;

        String metaInfFolderPath;
        String containerPath;

        if(epubPath.endsWith("/")){
            metaInfFolderPath = epubPath + EpubFileSystem.META_INF;
            containerPath = metaInfFolderPath + "/" + EpubFileSystem.CONTAINER;
        } else{
            metaInfFolderPath = epubPath + "/" + EpubFileSystem.META_INF;
            containerPath = metaInfFolderPath + "/" + EpubFileSystem.CONTAINER;
        }

        File mataInf = new File(metaInfFolderPath);
        File container = new File(containerPath);

        if( mataInf.exists() ){
            isMetaInfFolderExist = true;
        }

        if(container.exists()){
            isContainerFileExist = true;
        }

        if(isMetaInfFolderExist && isContainerFileExist)
            return true;
        else
            return false;
    }

    public void setPreload(boolean isPreload){
        BookHelper.isPreload = isPreload;
    }

    public String getViewerVersion(){
        return BookHelper.VIEWER_VERSION;
    }

    public void setSwipeThreshold(int threshold){   // TODO : 프론트에서 안쓰면 없애자
        BookHelper.swipeThreshold = threshold;
    }

    public UnModifiableArrayList<AudioNavigationInfo> getAudioNavigations(){
        return mReadingAudioNavigation.getAudioNavigations();
    }

    public void moveByAudioNavigation(AudioNavigationInfo audioNavigationInfo){
        if(mLayoutMode == LayoutMode.Reflowable){
            mEPubViewer.goPageByLink(audioNavigationInfo.getReferenceFilePath(), audioNavigationInfo.getReferenceFragment());
        } else if(mLayoutMode == LayoutMode.FixedLayout){
            mFixedLayoutView.goPage(audioNavigationInfo.getReferenceFilePath());
        }
    }

    public String getCurrentUserAgent(){
        if(mLayoutMode == LayoutMode.Reflowable){
            return mEPubViewer.getCurrentUserAgent();
        } else if(mLayoutMode == LayoutMode.FixedLayout){
            return mFixedLayoutView.getCurrentUserAgent();
        }
        return "";
    }

    /***************************** s: new custom selection */
    public void addAnnotation(){
        // TODO : new custom selection - doHighlight() -> addAnnotation()
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.addAnnotation();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.addAnnotation();
        }
    }

    public void addAnnotationWithMemo(String memoContent, boolean isMemoMerged){
        // TODO : new custom selection doMemo() -> addAnnotationWithMemo(String memoContent, boolean isMemoMerged)
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.addAnnotationWithMemo(memoContent, isMemoMerged);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.addAnnotationWithMemo(memoContent, isMemoMerged);
        }
    }

    public void  modifyAnnotationColorAndRange(int colorIndex){
        // TODO : new custom selection changeHighlightColorDirect(int colorIndex) -> modifyAnnotationColorAndRange(int colorIndex)
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.modifyAnnotationColorAndRange(colorIndex);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.modifyAnnotationColorAndRange(colorIndex);
        }
    }

    public void changeMemoText(String currentMemoId, String memoContent){
        // TODO : new custom selection
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.changeMemoText(currentMemoId, memoContent);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.changeMemoText(currentMemoId, memoContent);
        }
    }

    public void requestAllMemoText(){
        // TODO : new custom selection
        if( mLayoutMode == LayoutMode.Reflowable ){
            mEPubViewer.requestAllMemoText();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.requestAllMemoText();
        }
    }

    public void deleteAnnotation(){
        // TODO : new custom selection deleteHighlight(Highlight highlight) -> deleteAnnotation()
        if(mLayoutMode == LayoutMode.Reflowable) {
            mEPubViewer.deleteAnnotation();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.deleteAnnotation();
        }
    }

    public void selectionContinue(boolean isHighlight){
        // TODO : new
        if(mLayoutMode == LayoutMode.Reflowable){
            mEPubViewer.selectionContinue(isHighlight);
        }
    }

    public void handleBackKeyEvent(){
        if(mLayoutMode == LayoutMode.Reflowable) {
            mEPubViewer.handleBackKeyEvent();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.handleBackKeyEvent();
        }
    }

    public void finishTextSelectionMode(){
        // TODO : new
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.finishTextSelectionMode();
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.finishTextSelectionMode();
        }
    }
    /***************************** e: new custom selection */

    public void setOnAnalyticsListener(ViewerContainer.OnAnalyticsListener listener){
        if( mLayoutMode == LayoutMode.Reflowable ) {
            mEPubViewer.setOnAnalyticsListener(listener);
        } else if( mLayoutMode == LayoutMode.FixedLayout ) {
            mFixedLayoutView.setOnAnalyticsListener(listener);
        }
    }

    public boolean isTwoPageMode() {
        if(mLayoutMode == LayoutMode.FixedLayout){
            String pageMode = mEpubFile.getRenditionSpread();
            if(pageMode == null || pageMode.equals("none") || pageMode == "") {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void setSelectionDisabled(boolean isTextSelectionDisabled){
        if (mLayoutMode == LayoutMode.Reflowable) {
            mEPubViewer.setSelectionDisabled(isTextSelectionDisabled);
        } else if (mLayoutMode == LayoutMode.FixedLayout) {
            mFixedLayoutView.setSelectionDisabled(isTextSelectionDisabled);
        }
    }

    public boolean saveLastPositionByTTSData(TTSDataInfo ttsDataInfo){
        if( mLayoutMode == LayoutMode.Reflowable )
            return mEPubViewer.saveLastPositionByTTSData(ttsDataInfo);
        else if( mLayoutMode == LayoutMode.FixedLayout )
            return mFixedLayoutView.saveLastPositionByTTSData(ttsDataInfo);
        return false;
    }
}