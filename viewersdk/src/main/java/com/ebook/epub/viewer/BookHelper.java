package com.ebook.epub.viewer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author  syw
 */
public class BookHelper {

    public static String VIEWER_VERSION = "3.105";

    final public static int PHONE = 1;
    final public static int TABLET = 2;

    /** style type 정의 */
    final public static int VIEW_STYLE_USER = 100;
    final public static int VIEW_STYLE_ORIGINAL = 200;

    public static Context mContext;

    /** page 계산된 전체 페이지 값 */
    public static String landPages = "[]";
    public static String portPages = "[]";
    public static int landTotalPage = 0;
    public static int portTotalPage = 0;
    public static int lastViewMode=0;
    /** Viewer의 배경 색상 ( Color value )  */
    public static Integer backgroundColor = null;
    public static String[] Colors = { "#fbdab9", "#f8c9cd", "#f9f0ab", "#a8e3c7", "#d0c6f5"};
    public static int lastHighlightColor =2;
//    public static int lastMemoHighlightColor = 4;
    public static int focusTextColor = 0x809e9e9e;          // light gray
    public static int ttsHighlightColor = 0x4D0087ff;       // TTS 하이라이트 색상
    public static int textSelectionColor = 0x300087ff;      // default 0x4D0087ff ( front 설정값 )

    public static int maxSelectionLength = 1000;            // 셀렉션 글자 수 제한값 (default 1000)

    /**
     * 사용자 클릭 영역에 대한 정보 값 (Left, Right, Middle, Top, Bottom, Left_Corner)
     */
    public enum ClickArea {
        Left,
        Right,
        Middle,
        Top,
        Bottom,
        Left_Corner }

    /**
     * 메뉴 타입에 대한 정보 값
     */
    public enum ContextMenuType {
        TYPE_NEW,               // 신규 추가 대상 0
        TYPE_NEW_CONTINUE,      // 신규 추가 및 이어긋기 대상 1
        TYPE_MODIFY,            // 재활성화 수정 대상 2
        TYPE_MODIFY_CONTINUE,   // 재활성화 수정 및 이어긋기 대상 3
        TYPE_CONTINUE,          // 퀵하이라이트 이어긋기 대상 4
        TYPE_EXTRA              // 기타 5
    }

    /**
     * 셀렉션 관련 에러 값
     */
    public enum SelectionErrorType {
        TYPE_NEW_OVERFLOW,              // 실제 셀렉션 글자 수 제한 시
        TYPE_MERGE_OVERFLOW,            // 병합 후 글자 수 제한 시
        TYPE_CONTINUE_DISABLE,          // 페이지 넘김 제한 시
    }

    public static boolean fontStyleChanged = false;
    public static boolean fontSizeChanged = false;
    public static boolean fontChanged = false;
    public static boolean lineSpaceChanged = false;
    public static boolean paraSpaceChanged = false;
    public static boolean marginChanged = false;
    public static boolean indentChanged = false;
    public static boolean styleInited = false;
    public static boolean styleInit = false;
    public static boolean useDefault = false;    // using default book values ( font-size, line-height, indent ... )
    public static boolean defaultTextIndent = false;        // em
    public static int defaultFontSize = 16;
    public static int defaultLineSpace = 24;
    public static int defaultParaSpace = 0;
    public static String defaultFontStyle = "";

    /** 페이지 모드 설정값 ( 0 - 1page, 1 - 2page ) */
    public static int twoPageMode = 0;
    /** 왼쪽 여백 percent 단위 */
    public static Integer leftMargin=null;
    /** 오른쪽 여백 percent 단위 */
    public static Integer rightMargin=null;
    /** 상단 여백 percent 단위 */
    public static Integer topMargin=null;
    /** 하단 여백 percent 단위 */
    public static Integer bottomMargin=null;
    /** 폰트 사이즈 퍼센트 값 */
    public static Integer fontSize=null;
    /** 도서 뷰 스타일 타입 */
    private static int viewStyleType = VIEW_STYLE_ORIGINAL;
    /** 폰트 이름 d/p 용 */
    public static String fontName = "";
    /** 폰트 family에 정의된 face 이름 */
    public static String faceName = "";
    /** 폰트 face에 정의된 path */
    public static String fontPath = "";
    /** 설정된 테마 이름 (d/p용) */
    public static String themeName = "";        // TODO :: 사용 X
    /** default 테마 CSS file 명 */
    public static String themeDefault = "";     // TODO :: 사용 X
    /** Special 테마 CSS file 명 */
    public static String themeSpecial = "";     // TODO :: 사용 X
    /** 줄간격 설정 값 */
    public static Integer lineSpace = null;
    /** 문단간격 설정 값 */
    public static Integer paraSpace = null;
    /** 들여쓰기 설정 값 */
    public static Integer indent=null;
    /** 야간모드 확인 값 */
    public static int nightMode = 0;
    /** 리스트 스타일 설정 값*/
    public static String listStyle = "";        // TODO :: 사용 X
    /** 폰트 굵기 설정 값*/
    public static String fontWeight = "";       // TODO :: 사용 X
    /** 폰트 스타일 설정 값*/
    public static String fontStyle = "";
    /** 강조 텍스트 설정 값*/
    public static String textEmphasis = "";     // TODO :: 사용 X

    public static boolean allowTagSelect = true;        // <IMG ... <A ...

    public static String epubFilePath = "/mnt/sdcard/Download/";

    /** 애니메이션 효과 타입 ( 0-default(없음), 1-slide, 3-scroll ) */
    public static int animationType = 0;

    /** fixedlayout viewpager animation 없음 설정 시 velocity threshold */
    public static int swipeThreshold = 3000;

    /** 가로모드 시 페이지 숫자 (1-한 페이지, 2-두 페이지) */
    public static int pageNumOnLandscape = 1;

    public static boolean useHistory = true;   // 주석 히스토리를 viewer에서 관리 여부

    public static boolean useEPUB3Viewer = false;

    public static int deviceType = PHONE;

    public static long animationDuration = 100;

    public static String annotationVersion = "3.0";
    public static String bookmarkVersion = "3.0";

    /** 북마크 파일 네임 (Default : "bookmark.flk") */
    public static String bookmarkFileName = "bookmark.flk";
    /** 마지막 읽은 위치 파일 네임 (Default : "readposition.flk") */
    public static String readPositionFileName = "readposition.flk";
    /** 주석 파일 네임 (Default : "annotation.flk") */
    public static String annotationFileName = "annotation.flk";
    /** 북마크 히스토리 파일 네임 (Default : "bookmarkhistory.flk") */
    public static String bookmarkHistoryFileName = "bookmarkhistory.flk";
    /** 주석 히스토리 파일 네임 (Default : "annotationhistory.flk") */
    public static String annotationHistoryFileName = "annotationhistory.flk";

    public static String optionFileName = "options.flk";

    public static String ACTIVE_CLASS = "-flk-epub-media-overlay-active";

    public static String memoIconPath = "";

    /**
     * percent value for left click area margin
     *  - unit %
     */
    private static int leftClickMargin = 20;
    private static int rightClickMargin = 20;
    private static int topClickMargin = 0;
    private static int bottomClickMargin = 0;

    /**
     * front 에서 일부 환경변수를 변경하고자 할경우 true로 셋팅
     */
    public static boolean handleFront = false;

    public static ReadingStyle defaultReadingStyle;

    public static boolean isPreload = false;

    /**
     * 폰트 사이즈 값 요청 메소드
     * @return int : 폰트 사이즈 값
     */
    public static Integer getFontSize() {
        if( useDefault ) {
            if( deviceType == TABLET)
                fontSize = 130;
            else
                fontSize = 100;
        }
        return fontSize;
    }

    /**
     * 스타일 타입 설정값 요청 메소드
     * @return int : 스타일 타입 설정값
     */
    public static int getViewStyleType() {
        return viewStyleType;
    }

    /**
     * 줄간격 값을 요청하는 메소드
     *
     * @return int : 줄간격 값
     */
    public static Integer getLineSpace() {
        if( useDefault ) {
            if( defaultLineSpace != 0 )
                lineSpace = Math.max(((defaultLineSpace * 100) / defaultFontSize), 100);
            else
                lineSpace = 100;
        }
        return lineSpace;
    }

    /**
     * 문단간격 값을 요청하는 메소드
     *
     * @return int : 문단간격 값
     */
    public static Integer getParaSpace() {
        if( useDefault ) {
            paraSpace = defaultParaSpace;
        }
        return paraSpace;
    }

    /**
     * 폰트 스타일 값을 요청하는 메소드
     * @return
     */
    public static String getFontStyle() {
        if( useDefault ) {
            fontStyle = defaultFontStyle;
        }
        return fontStyle;
    }

    public static int getColorInt(int index) {
        return Color.parseColor(getColorString(index));
    }

    public static String getColorString(int index) {
        if( index < 0 || index >= Colors.length )
            return Colors[0];

        return Colors[index];
    }

    public static int getColorIndex(String color) {
        for(int i=0; i<Colors.length; i++) {
            if( color.equals(Colors[i]) ) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 디바이스 타입 구하기.
     *  TABLET, PHONE
     * @param context Context :
     * @return int : 디바이스 타입
     */
    public static int getDevice(Context context) {
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        int heightPixels = context.getResources().getDisplayMetrics().heightPixels;
        int portrait_width_pixel=Math.min(widthPixels, heightPixels);
        int dots_per_virtual_inch=context.getResources().getDisplayMetrics().densityDpi;
        float virutal_width_inch=portrait_width_pixel/dots_per_virtual_inch;
        if (virutal_width_inch<=2) {
            return PHONE;
        } else {
            return TABLET;
        }
    }

    public static String getRootPath() {
        return MyZip.mUnzipDir + "/";
    }

    /**
     * full Path 중에서 파일의 이름만 가져오는 메소드 (확장자 포함)
     * @param path String : 파일 전체경로
     * @return String : 확장자 포함 파일명
     */
    public static String getFilename(String path) {
        String fileName;
        if(path == null) {
            fileName = "";
        } else {
            int i = path.lastIndexOf("/");
            if(i == -1) {
                fileName = path;
            } else {
                int j = i + 1;
                int k = path.length();
                fileName = path.substring(j, k);
            }
        }
        return fileName;
    }

    public static String getRelFilename(String path) {
        int length = MyZip.mUnzipDir.length();
        if( path.length() > length ) {
            return path.substring(length+1);
        } else {
            return path;
        }
    }

    public static InputStream String2InputStream(String s)
    {
        byte abyte0[] = s.getBytes();
        return new ByteArrayInputStream(abyte0);
    }

    /**
     * 주어진 string 에서 파일이 있을경우 파일의 확장자를 뺀 파일명만 구함.
     * @param fullName String : 파일의 전체 경로
     * @return String : 파일명
     */
    public static String getOnlyFilename(String fullName) {
        String s1;
        if(fullName == null)
            return "";

        String s2 = getFilename(fullName);
        int i = s2.lastIndexOf(".");
        if(i == -1)
            s1 = s2;
        else
            s1 = s2.substring(0, i);
        return s1;
    }


    /**
     * getFileExt
     *   - . 까지 포함된 string
     * @return String
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName) {
        if( fileName == null )
            return "";

        String ret = "";
        int index = fileName.lastIndexOf(".");
        if( index != -1) {
            ret = fileName.substring(index, fileName.length()).toLowerCase();
        }
        return ret;
    }

    public static String file2String(String fileName){
        File file = new File(fileName);
        InputStream is;
        String fileString = "";
        try {
            if( file.exists() ) {
                is = new FileInputStream(file);
                fileString = inputStream2String(is);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileString;
    }

    public static String inputStream2String(InputStream inputstream) {
        try {
            return inputStream2String(inputstream, "UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String inputStream2String(InputStream inputstream, String enc) throws Exception {
        return inputStream2String(inputstream, enc, false);
    }

    public static String inputStream2String(InputStream inputstream, String enc, boolean flag) throws Exception {
        if( inputstream == null )
            return null;

        BufferedReader bufferedreader;
        InputStreamReader inputstreamreader;
        if( enc == null || enc.equals("") ) {
            inputstreamreader = new InputStreamReader(inputstream);
            bufferedreader = new BufferedReader(inputstreamreader);
        } else {
            inputstreamreader = new InputStreamReader(inputstream, enc);
            bufferedreader = new BufferedReader(inputstreamreader);
        }

        int length = inputstream.available() + 16;
        StringBuilder stringbuilder = new StringBuilder(length);

        while(true) {
            try {
                String str = bufferedreader.readLine();
                if( str != null ) {
                    String tmp = String.valueOf(str);
                    String text = (new StringBuilder(tmp)).append("\n").toString();
                    stringbuilder.append(text);
                } else {
                    break;
                }
            } catch(OutOfMemoryError e) {
                inputstreamreader.close();
                bufferedreader.close();
                System.gc();
                e.printStackTrace();
                break;
            } catch(Exception e) {
                inputstreamreader.close();
                bufferedreader.close();
                e.printStackTrace();
                break;
            }
        }
        inputstreamreader.close();
        bufferedreader.close();
        return stringbuilder.toString();
    }

    public static String getHtmlAttribute(String s) {
        String result = "";
        String temp = s.toLowerCase();
        int start = temp.indexOf("<html>");
        int end = temp.indexOf("</html>");
        if( start != -1 && end != -1 ) {
            result = s.substring(start, start+6);
        } else {
            if( end != -1 ) {
                start = temp.indexOf("<html");
                if(start == -1 )
                    return "";

                int index = temp.indexOf(">", start);
                if( index <= 0 || index >= end )
                    return "";

                result = s.substring(start, index+1);
            } else {
                return "";
            }
        }
        return result;
    }

    public static String getDoctType(String s){
        String result = "";
        String temp = s.toLowerCase();
        int start = temp.indexOf("<!doctype");
        int end = temp.indexOf(">", start);
        if( end != -1 ) {
            start = temp.indexOf("<!doctype");
            if(start == -1 )
                return "";

            int index = temp.indexOf(">", start);
            if( index <= 0 || index > end )
                return "";

            result = s.substring(start, index+1);
        } else {
            return "";
        }
        return result;
    }

    public static String getBodyAttribute(String s) {
        String result = "";
        String temp = s.toLowerCase();
        int start = temp.indexOf("<body>");
        int end = temp.indexOf("</body>");
        if( start != -1 && end != -1 ) {
            result = s.substring(start, start+6);
        } else {
            if( end != -1 ) {
                start = temp.indexOf("<body");
                if(start == -1 )
                    return "";

                int index = temp.indexOf(">", start);
                if( index <= 0 || index >= end )
                    return "";

                result = s.substring(start, index+1);
            } else {
                return "";
            }
        }
        return result;
    }

    public static String getHtmlBody(String s)  {
        String result = "";
        String temp = s.toLowerCase();
        int start = temp.indexOf("<body>");
        int end = temp.indexOf("</body>");
        try {
            if( start != -1 && end != -1 ) {
                result = s.substring(start + 6, end);
            } else {
                if( end != -1 ) {
                    int index = temp.indexOf("<body");
                    if(index == -1 )
                        throw new Exception();

                    index = temp.indexOf(">", index);
                    if( index <= 0 || index >= end )
                        throw new Exception();

                    result = s.substring(index+1, end);
                } else {
                    // body tag를 못찾은 경우
                    start = temp.indexOf("<html>");
                    end = temp.indexOf("</html>");
                    if( start != -1 && end != -1 ) {
                        result = s.substring(start + 6, end);
                    } else {
                        if( end != -1 ) {
                            int index = temp.indexOf("<html");
                            if(index == -1 )
                                throw new Exception();

                            index = temp.indexOf(">", index);
                            if(index <=0 || index >= end )
                                throw new Exception();

                            result = s.substring(index+1, end);
                        }
                    }
                }
            }
        } catch( Exception e ) {
            result = "";
            e.printStackTrace();
        }
        System.gc();
        return result;
    }

    public static String getBodyDetail(String bodyStr){

        bodyStr+="</body>";
        String result = "<body";

        try {
            DocumentBuilder documentBuilder= DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(bodyStr.getBytes()));
            NamedNodeMap attrs = document.getElementsByTagName("body").item(0).getAttributes();

            if(attrs.getNamedItem("class")!=null){
                result = result+" class=\""+attrs.getNamedItem("class").getNodeValue()+"\"";
            }

            if(attrs.getNamedItem("id")!=null){
                result = result+" id=\""+attrs.getNamedItem("id").getNodeValue()+"\"";
            }

            if(attrs.getNamedItem("onload")!=null){
                result = result+" onload=\""+attrs.getNamedItem("onload").getNodeValue()+"\"";
            }

            if(attrs.getNamedItem("style")!=null){

                String tmpBodyStyle = attrs.getNamedItem("style").getNodeValue();
                String styleDetail[] = tmpBodyStyle.split(";");
                result +=" style=\"";

                boolean isExist=false;
                for(int idx=0; idx<styleDetail.length; idx++){
                    if(styleDetail[idx].indexOf("background-color:")!=-1){
                        result = result+" background-color:"+styleDetail[idx].split(":")[1]+";";
                        isExist=true;
                    } else if(styleDetail[idx].indexOf("color:")!=-1){
                        result = result + " color:" +styleDetail[idx].split(":")[1]+";";
                        isExist=true;
                    }
                }

                if(!isExist){
                    result=result.replace(" style=\"", "");
                } else{
                    result+="\"";
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result+=">";
        return result;
    }

    public static String getHtmlHead(String s) {
        String result = "";
        String temp = s.toLowerCase();
        int start = temp.indexOf("<head>");
        int end = temp.indexOf("</head>");
        try {
            if( start != -1 && end != -1 ) {
                result = s.substring(start + 6, end);
            } else {
                if( end != -1 ) {
                    int index = temp.indexOf("<head");
                    if(index == -1 )
                        throw new Exception();

                    index = temp.indexOf(">", index);
                    if( index <= 0 || index >= end )
                        throw new Exception();

                    result = s.substring(index+1, end);
                }
            }
        } catch( Exception e ) {
            result = "";
            e.printStackTrace();
        }
        System.gc();

        return result;
    }

    public static int getFileType(String file) {

        String s = file.toLowerCase();
        int i;
        if(s.endsWith(".txt"))
            i = 0;
        else if(s.endsWith(".html") || s.endsWith(".htm"))
            i = 1;
        else if(s.endsWith(".epub"))
            i = 2;
        else if(s.endsWith(".zip"))
            i = 3;
        else
            i = 0;

        return i;
    }

    /**
     * 파일 이름을 받아 확장자에 따라 종류를 리턴
     *
     * @param file String : 타입을 확인할 파일명
     * @return int : 파일타입
     */
    public static int getBookType(String file) {
        return getFileType(file);
    }

    public static void setUseEPUB3Viewer(boolean use) {
        useEPUB3Viewer = use;
    }

    /**
     * click margin 값을 반환한다.
     * 기준이 되는 단위는 percent 이다.
     */
    public static int getLeftClickMargin() {
        return leftClickMargin;
    }
    public static int getRightClickMargin() {
        return (100 - rightClickMargin);
    }
    public static int getTopClickMargin() {
        return topClickMargin;
    }
    public static int getBottomClickMargin() {
        return (100 - bottomClickMargin);
    }

    /**
     * 사용자의 x, y 입력을 받아 ClickArea type의 열거형 값을 리턴.
     * @param view EPubViewer EpubViewer객체
     * @param x float touch x 좌표
     * @param y float touch y 좌표
     * @return ClickArea 클릭 영역에 대한 정보 값
     */
    public static ClickArea getClickArea(View view, float x, float y) {

        int w = view.getWidth();
        int h = view.getHeight();

        if( x >= (w-100) && y <= (100) )
            return ClickArea.Left_Corner;
        if( x <= ((w * getLeftClickMargin()) / 100) )
            return ClickArea.Left;
        if( x >= ((w * getRightClickMargin()) / 100) )
            return ClickArea.Right;
        if( y <= ((h * getTopClickMargin()) / 100) )
            return ClickArea.Top;
        if( y >= ((h * getBottomClickMargin()) / 100) )
            return ClickArea.Bottom;
        return ClickArea.Middle;
    }

    public static String getHeadText(Context context) {
        return getTemplateScript(context);
    }

    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    public static void initReadingStyle(){
        BookHelper.faceName = defaultReadingStyle.fontFace;
        BookHelper.fontPath = defaultReadingStyle.fontPath;
        BookHelper.fontSize = defaultReadingStyle.fontSize;
        BookHelper.lineSpace = defaultReadingStyle.lineSpace;
        BookHelper.paraSpace = defaultReadingStyle.paragraphSpace;
        BookHelper.leftMargin = defaultReadingStyle.leftMargin;
        BookHelper.rightMargin = defaultReadingStyle.rightMargin;
        BookHelper.topMargin = defaultReadingStyle.topMargin;
        BookHelper.bottomMargin = defaultReadingStyle.bottomMargin;
        BookHelper.indent = defaultReadingStyle.textIndent;
    }

    /**
     * 환경설정에서 초기환된 경우 true로 셋팅
     * @param init boolean : 초기화 유무 값
     * @return boolean : 초기화 유무
     */
    public static boolean setStyleInit(boolean init) {
        styleInited = styleInit == init ? false : true;
        styleInit = init;
        return styleInited;
    }

    /**
     * 폰트 사이즈를 설정하는 메소드
     * @param percent int : 설정할 폰트크기 비율 값
     * @return boolean : 폰트 변경 적용 확인.
     */
    public static boolean setFontSize(Integer percent) {

        if(percent!=null){
            if(fontSize==null){
                fontSize=percent;
                return true;
            }
            fontSizeChanged =  fontSize.compareTo(Integer.valueOf(percent)) == 0 ? false : true;
            fontSize = percent;
            return fontSizeChanged;
        } else {
            return false;
        }
    }

    /**
     * 뷰어 스타일을  설정하는 메소드
     * @param viewStyle int : 스타일 설정값
     */
    public static void setViewStyleType(int viewStyle) {
        viewStyleType = viewStyle;
    }

    /**
     * 폰트 종류를 설정하는 메소드
     * @param fontName String : 화면에 보여줄 폰트명
     * @param faceName String : 폰트 family에 정의된 face 명
     * @param fontPath String : 폰트 face에 정의된 path
     * @return boolean : 폰트변경 적용 확인
     */
    public static boolean setFont(String fontName, String faceName, String fontPath) {
        if(fontName!=null && fontName!=null && fontPath!=null){
            BookHelper.fontName = fontName;
            BookHelper.faceName = faceName;
            BookHelper.fontPath = fontPath;
            fontChanged = true;
            return true;
        }
        return true;
    }

    /**
     * 뷰어 배경색을 설정하는 메소드
     * @param color int : 알파 값 포함된 16진수 컬러 값
     * @return boolean :
     */
    public static boolean setBackgroundColor(Integer color) {
        if(color!=null){
            BookHelper.backgroundColor = color;
        }
        return true;
    }

    /**
     * 뷰어의 줄간격을 설정하는 메소드
     * @param value int : 설정할 줄간격 값
     * @return boolean : 줄간격 설정 적용 확인
     */
    public static boolean setLineSpace(Integer value) {
        if(value!=null){
            lineSpaceChanged = lineSpace == value ? false : true;
            lineSpace = value;
            return lineSpaceChanged;
        }
        return true;
    }

    /**
     * 뷰어의 문단간격을 설정하는 메소드
     *
     * @param value int : 설정할 문단간격 값
     * @return boolean : 문단간격 설정 적용 확인
     */
    public static boolean setParaSpace(Integer value) {
        if(value!=null){
            paraSpaceChanged = paraSpace == value ? false : true;
            paraSpace = value;
            return paraSpaceChanged;
        }
        return true;
    }

    /**
     * 뷰어의 상하좌우 여백을 설정하는 메소드
     * @param l int : 좌측 마진 값
     * @param t int : 상단 마진 값
     * @param r int : 우측 마진 값
     * @param b int : 하단 마진 값
     * @return boolean : 마진 설정 적용 확인
     */
    public static boolean setMargin(Integer l, Integer t, Integer r, Integer b) {
        if( leftMargin == l && topMargin == t && rightMargin == r && bottomMargin == b ) {
            marginChanged = false;
            return false;
        }
        leftMargin = l;
        topMargin = t;
        rightMargin = r;
        bottomMargin = b;
        marginChanged = true;
        return true;
    }

    /**
     * 뷰어의 들여쓰기 여부를 설정하는 메소드
     * @param value boolean : 들여쓰기 여부
     * @return boolean : 들여쓰기 설정 적용 확인
     */
    public static boolean setIndent(Integer value) {
        if(value!=null){
            if(indent == null){
                indent = value;
                return true;
            }
            indentChanged = indent.compareTo(Integer.valueOf(value)) == 0 ? false : true;
            indent = value;
            return indentChanged;
        } else {
            return false;
        }
    }

    public static boolean setFontStyle(String value) {
        fontStyleChanged = fontStyle == value ? false : true;
        fontStyle = value;

        return fontStyleChanged;
    }

    private static String getTemplateScript(Context context) {
        return (new StringBuilder())
                .append("\n<script src=\"file:///android_asset/json2.js\"></script>")
                .append("\n<script src=\"file:///android_asset/jquery-2.1.4.min.js\"></script>")
                .append("\n<script src=\"file:///android_asset/bgmplayer.js\"></script>")
                .append("\n<script src=\"file:///android_asset/noteref.js\"></script>")
                .append("\n<script src=\"file:///android_asset/svg_check_min.js\"></script>")
                .append("\n<script src=\"file:///android_asset/epub_extensions.js\"></script>")
                .append("\n<script src=\"file:///android_asset/selection.js\"></script>")
                .append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/noteref.css\"></link>")
                .append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/highlight.css\"></link>")
                .append("<meta charset=\"utf-8\"/>")
                .append("<meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />")
                .toString();
    }

    public static String getBaseStyle() {

        String strBase = base_style;

        if(fontSize!=null){
            strBase=strBase.replaceAll("%feelingk_fontsize_value%", ""+ "font-size: "+fontSize +"%;");
        } else {
            strBase=strBase.replaceAll("%feelingk_fontsize_value%", "");
        }

        if(leftMargin!=null){
            strBase=strBase.replaceAll("%feelingk_marginleft_value%", ""+ leftMargin);
        } else {
            strBase=strBase.replaceAll("%feelingk_marginleft_value%", ""+ 0);
        }

        if(rightMargin!=null){
            strBase=strBase.replaceAll("%feelingk_marginright_value%", ""+ rightMargin);
        } else {
            strBase=strBase.replaceAll("%feelingk_marginright_value%", ""+ 0);
        }

        if(bottomMargin!=null){
            strBase=strBase.replaceAll("%feelingk_paddingbottom_value%", ""+ bottomMargin);
        } else {
            strBase=strBase.replaceAll("%feelingk_paddingbottom_value%", ""+ 0);
        }

        if(topMargin!=null){
            strBase=strBase.replaceAll("%feelingk_booktablemargintop_value%", ""+ topMargin);
        } else {
            strBase=strBase.replaceAll("%feelingk_booktablemargintop_value%", ""+ 0);
        }

        if(backgroundColor!=null && backgroundColor.compareTo(Integer.valueOf(16777215))!=0){
            strBase=strBase.replaceAll("%feelingk_backgroundcolor_value%", "background-color : "+String.format("#%06X", backgroundColor)+" !important;");
        } else {
            strBase=strBase.replaceAll("%feelingk_backgroundcolor_value%", "");
        }

        strBase+="\np,div,span {";

        if(indent!=null){
            strBase+="\n text-indent : "+ indent+"em !important;";
        }

        if(paraSpace!=null){
            strBase+="\n margin-bottom : "+ paraSpace+"% !important;";
        }

        if(lineSpace!=null){
            strBase+="\n line-height : "+ lineSpace+"% !important;";
        }
//        else { // TODO :: 20190107 font size 큰 경우 테스트 필요
//            strBase+="\n line-height : initial !important;";
//        }

        strBase+="}";

        if(nightMode==1){
            strBase+="\n* :not(.flk_note .flk_note *){ background-color : "+ String.format("#%06X", backgroundColor) +" !important; }";
            strBase+=aTag_Style;
        }

        return strBase;
    }

    private final static String base_style =
            "\nimg { " +
//                    "\nmax-width:90%; " +
//    				"\nmax-height:90%; " +
//    				"\nheight: 100%; " + // expression(this.height>90% ? 80% : true); " +
//    				"\nwidth: 100%; " + // expression(this.width>90% ? 80% : true ); " +
//    				"\ndisplay:block;" +
                    "\n}" +
                    "\nsvg { " +
                    "\n-webkit-user-select: none !important; " +
                    "\n max-width:90% !important; " +
                    "\n max-height:90% !important; " +
                    "\n}" +
                    "\nvideo { " +
                    "\nmax-width: 90% !important; " +
                    "\n}"+
                    "\ndiv#feelingk_booktable { " +
                    "\n padding: 0px; " +
                    "\n margin: 0px; " +
                    "\n -webkit-column-gap: 0px; " +
                    "\n height : %feelingk_booktableheight_value%;" +
                    "\n margin-top : %feelingk_booktablemargintop_value%px;" +
                    "\n %feelingk_booktablecolumnwidth_value%"+
                    "\n column-fill : auto !important;"+
                    "\n}" +
                    "\ndiv#feelingk_bookcontent { " +
                    "\n %feelingk_fontsize_value% " +
                    "\n margin-left: %feelingk_marginleft_value%px; " +
                    "\n margin-right: %feelingk_marginright_value%px; " +
                    "\n padding-bottom: %feelingk_paddingbottom_value%px; " +
                    "\n text-align: justify; " +
                    "\n}" +
                    "\nh1,h2,h3,h4,h5,h6 { " +
                    "\n line-height:115% !important; " +
                    "\n} " +
                    "\nbody { " +
                    "\n %feelingk_backgroundcolor_value%" +
                    "\n margin : 0px !important;" +
                    "\n padding : 0px !important;" +
                    "\n margin-top : %feelingk_bodymargintop_value%px !important;" +
                    "\n margin-bottom : %feelingk_bodymarginbottom_value%px !important;" +
                    "\n -webkit-nbsp-mode : space !important;" +
                    "\n -webkit-touch-callout : none !important;" +
                    "\n user-select: none !important;" +
                    "\n -webkit-user-select : none !important;" +
                    "\n width : %feelingk_body_width_value%;" +
                    "\n %feelingk_body_touch_action%"+
                    "\n}" +
                    "\npre {" +
                    "\n white-space : pre-wrap;"+
                    "\n word-wrap : break-word;" +
                    "\n}";

    public static String getFontStyle(String faceName, String fontPath) {
        return String.format(font_style, faceName, faceName, fontPath, faceName);
    }

    private final static String font_style = "\n\n<style type='text/css' id='FONTFACE_%s'> @font-face { font-family: '%s'; src: url(%s); } \n* { font-family : '%s' !important; } </style>";

    private final static String aTag_Style = "\n:not(a) { color : #999999 !important; } \na { color : #6887f7 !important; } ";

    /**
     * 요청하는 dateFormat 형식으로 date 스트링을 가져옴.
     * @param milliSeconds long : 변환할 milliSecond 단위의 시간
     * @param dateFormat String : 변환한 포멧
     * @return String : 변환한 date 스트링
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static long getMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone( TimeZone.getTimeZone("Asia/Seoul") );
        return cal.getTimeInMillis();
    }

    public static ReadpositionData getReadpositionData(String filePath){

        ReadpositionData readPosition = new ReadpositionData();
        if(filePath.endsWith("/")){
            filePath = filePath + BookHelper.readPositionFileName;
        } else{
            filePath = "/"+filePath + BookHelper.readPositionFileName;
        }

        try {
            JSONObject jsonObj = EpubFileUtil.getJSONObjectFromFile(filePath);

            if( jsonObj == null ) return null;

            readPosition.setType(jsonObj.getString(AnnotationConst.FLK_DATA_TYPE));
            readPosition.setVersion(jsonObj.getString(AnnotationConst.FLK_READPOSITION_VERSION));
            readPosition.setDeviceModel(jsonObj.getString(AnnotationConst.FLK_READPOSITION_MODEL));
            readPosition.setDeviceOsVersion(jsonObj.getString(AnnotationConst.FLK_READPOSITION_OS_VERSION));
            readPosition.setTime(Long.parseLong(jsonObj.getString(AnnotationConst.FLK_READPOSITION_TIME)));
            readPosition.setPath(jsonObj.getString(AnnotationConst.FLK_READPOSITION_PATH));
            readPosition.setFile(jsonObj.getString(AnnotationConst.FLK_READPOSITION_FILE));
            readPosition.setPercent(Double.parseDouble(jsonObj.getString(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT)));
            if(!jsonObj.isNull(AnnotationConst.FLK_READPOSITION_TOTAL_PERCENT)){
                readPosition.setTotalPercent(Double.parseDouble(jsonObj.getString(AnnotationConst.FLK_READPOSITION_TOTAL_PERCENT)));
            }
            return readPosition;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
