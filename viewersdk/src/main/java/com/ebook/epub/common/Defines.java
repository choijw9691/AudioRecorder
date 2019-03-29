package com.ebook.epub.common;

public class Defines {

    public static final int TEST = 999;

    /********************************************************************** s : reflowable */
    public static final int REF_IMG_RESIZING_DONE = 100;
    public static final int REF_PAGE_READY = 101;
    public static final int REF_CHAPTER_LOADING = 102;
    public static final int REF_FORCE_CHAPTER_CHANGING = 103;
    public static final int REF_CHECK_MERGE_ANNOTATION = 104;
    public static final int REF_DRAW_SELECTION_RECT = 105;
    public static final int REF_MERGE_ALL_MEMO = 106;
    public static final int REF_SET_LANDING_PAGE = 107;
    public static final int REF_ADD_HIGHLIGHTED_DATA = 108;
    public static final int REF_CONTEXT_MENU_SHOW = 109;
    public static final int REF_CONTEXT_MENU_HIDE = 110;
    public static final int REF_BOOKMARK_SHOW = 111;
    public static final int REF_BOOKMARK_CHECK = 112;
    public static final int REF_SAVE_LAST_POSITION = 113;
    public static final int REF_SCROLL_PAGE_OR_CHAPTER = 114;       // 챕터 외 이동 가능
    public static final int REF_STOP_SCROLLING = 115;
    public static final int REF_START_SCROLL_ANIMATION = 116;
    public static final int REF_LOAD_PRIOR_CHAPTER = 117;
    public static final int REF_LOAD_NEXT_CHAPTER = 118;
    public static final int REF_PAGE_SCROLL = 119;                  // 챕터 내 이동
    public static final int REF_SCROLL_BY_PERCENT = 120;
    public static final int REF_SCROLL_BY_PERCENT_IN_CHAPTER = 121;
    public static final int REF_SCROLL_BY_FOCUS = 122;
    public static final int REF_SCROLL_BY_POSITION = 123;
    public static final int REF_SCROLL_BY_OFFSET = 124;
    public static final int REF_SCROLL_BY_KEYWORD_INDEX = 125;
    public static final int REF_SCROLL_BY_PAGE = 126;
    public static final int REF_SCROLL_BY_ID = 127;
    public static final int REF_SCROLL_BY_HIGHLIGHT_ID = 128;
    public static final int REF_SCROLL_BY_PATH = 129;
    public static final int REF_SCROLL_BY_CLICK = 130;
    public static final int REF_PAGE_SCROLL_AFTER = 131;
    public static final int REF_HAS_MEDIAOVERLAY = 132;
    public static final int REF_MEDIAOVERLAY_PAUSE = 133;
    public static final int REF_PLAY_SELECTED_MEDIAOVERLAY = 134;
    public static final int REF_SEARCH_START = 135;
    public static final int REF_SEARCH_END = 136;
    public static final int REF_SEARCH_RESULT = 137;
    public static final int REF_REPORT_FOCUS_RECT = 138;
    public static final int REF_NIGHT_MODE = 139;
    public static final int REF_CURRENT_PAGE_INFO = 140;
    public static final int REF_NOTIFY_CURRENT_PAGE_INFO = 141;
    public static final int REF_GET_CURRENT_TOP_PATH = 142;
    public static final int REF_IMAGE_TAG_CLICK = 143;
    public static final int REF_LINK_TAG_CLICK = 144;
    public static final int REF_SCROLL_AUTO = 145;
    public static final int REF_OVERFLOW_TEXT_SELECTION = 146;
    public static final int REF_OVERFLOW_MEMO_CONTENT = 147;

    public static final int REF_VIDEO_TAG_CLICK = 160;
    public static final int REF_VIDEO_CONTROL = 161;

    public static final int REF_REPORT_ERROR = 170;
    public static final int REF_VIEWER_CLOSE = 171;
    public static final int REF_VIEWER_REFRESH = 172;
    /********************************************************************** e : reflowable */

    /********************************************************************** s : fixed */
    public static final int FIXEDLAYOUT_LOAD_BOOK = 200;
    public static final int FIXEDLAYOUT_REPORT_TOUCH_POSITION = 201;
    public static final int FIXEDLAYOUT_DRAW_SELECTION_RECT = 202;
    public static final int FIXEDLAYOUT_CHECK_MERGE_ANNOTATION = 203;
    public static final int FIXEDLAYOUT_CONTEXT_MENU_SHOW = 204;
    public static final int FIXEDLAYOUT_CONTEXT_MENU_HIDE = 205;
    public static final int FIXEDLAYOUT_MERGE_ALL_MEMO = 206;
    public static final int FIXEDLAYOUT_ADD_HIGHLIGHTED_DATA = 207;
    public static final int FIXEDLAYOUT_SCROLL_PAGE = 208;
    public static final int FIXEDLAYOUT_PLAY_SELECTED_MEDIAOVERLAY = 209;
    public static final int FIXEDLAYOUT_MEDIAOVERLAY_PAUSE = 210;
    public static final int FIXEDLAYOUT_PREVENT_MEDIA_CONTROL = 211;
    public static final int FIXEDLAYOUT_SET_BGM_STATE = 212;
    public static final int FIXEDLAYOUT_SEARCH_RESULT = 213;
    public static final int FIXEDLAYOUT_SEARCH_END = 214;
    public static final int FIXEDLAYOUT_REPORT_CURRENT_PAGE_INFO = 215;
    public static final int FIXEDLAYOUT_LINK_CLICK = 216;
    public static final int FIXEDLAYOUT_SET_ASIDEPOPUP_STATUS = 217;
    public static final int FIXEDLAYOUT_OVERFLOW_MEMO_CONTENT = 218;

    public static final int FIXEDLAYOUT_REPORT_VIDEO_INFO = 230;

    public static final int FIXEDLAYOUT_REPORT_ERROR = 250;
    /********************************************************************** e : fixed */

    /********************************************************************** s: common */
    public static final int ADD_TTS_HIGHLIGHT = 300;
}
