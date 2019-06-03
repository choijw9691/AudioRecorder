package com.ebook.epub.fixedlayoutviewer.manager;

import android.content.Context;

import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.ViewerContainer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class HtmlContentsManager {

    private String TAG = "HtmlContentsManager";

    public String makeHtmlTemplate(Context context, String filePath, String htmlString){

        String bodySrc = "";
        String headSrc = "";
        String bodyAttr = "<body>";
        String docType = "";

        if( htmlString == null )
            return null;

        if( filePath.toLowerCase().endsWith(".svg") ){
            headSrc = getTemplateScript(context) + makeSVGContentsHead(htmlString);
            bodySrc = makeSVGContentsBody(htmlString);
        } else {
            headSrc = getTemplateScript(context) + "\n" + BookHelper.getHtmlHead(htmlString) + "\n"+ setJsLink(context);

            StringBuilder bodybuilder = new StringBuilder(BookHelper.getHtmlBody(htmlString));
            bodySrc = bodybuilder.toString();
            bodybuilder.setLength(0);
            bodybuilder=null;

            bodyAttr = BookHelper.getBodyAttribute(htmlString);
            bodyAttr = BookHelper.getBodyDetail(bodyAttr);	// [ssin] add : content body id,dir,style

            docType = BookHelper.getDoctType(htmlString);
        }

        StringBuilder templateBuilder = new StringBuilder();
        templateBuilder.append("\n")
                .append(docType)
                .append("\n")
                .append(BookHelper.getHtmlAttribute(htmlString))
                .append("\n")
                .append("<head>")
                .append(headSrc.replaceAll("<title/>", ""))
                .append("\n</head>\n")
                .append(bodyAttr)
                .append("\n")
                .append("<div id='feelingk_booktable'>\n<div id='feelingk_bookcontent'>")
                .append(bodySrc)
                .append("\n</div>\n</div>\n</body>\n")
                .append("</html>");

        String resultHtml = templateBuilder.toString();
        templateBuilder.setLength(0);
        templateBuilder=null;

        return resultHtml;
    }

    public String getDecodeContentsString(String fileName, boolean isIgnoreDrm, String drmKey, ViewerContainer.OnDecodeContent decodeListener, boolean isViewerClosed){

        String decodeStr = null;

        if(isViewerClosed) {
            return "";
        }

        if( !fileName.equals("about:blank") ){
            if( isIgnoreDrm ) {
                decodeStr = BookHelper.file2String(fileName);
            } else {
                if( decodeListener != null ) {
                    try {
                        decodeStr = decodeListener.onDecode(drmKey, fileName);
                    } catch (Exception e) {
                        DebugSet.e( TAG, "ChapterStringGet.run() Exception : " + e.getMessage() );
                    }
                } else {
                    DebugSet.e( TAG, "DRM decoder not defined!" );
                }
            }
        }

        return decodeStr;
    }

    private String makeSVGContentsHead(String fileName){

        StringBuilder svgHead = new StringBuilder();

        int styleTagStart = fileName.indexOf("<?xml-stylesheet");
        int styleTagEnd = fileName.indexOf(">", styleTagStart) + 1;
        int styleLastTagEnd = fileName.lastIndexOf("?>") + 2;

        if( styleTagStart != -1 ){
            svgHead.append(fileName.substring(styleTagStart, styleLastTagEnd));
            svgHead = new StringBuilder(svgHead.toString().replace("<?xml-stylesheet", "<link rel=\"stylesheet\" ").replace("?>", "/>"));
        }

//    	DebugSet.d("TAG", "makeSVGContentsBody : " + svgHead.toString());

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

//    	DebugSet.d("TAG", "makeSVGContentsBody : " + svgBody.toString());

        return svgBody.toString();
    }

    private String setJsLink(Context context){

        StringBuilder bgmJsBuilder = new StringBuilder();
        bgmJsBuilder.append("\n<script src=\"file:///android_asset/json2.js\"></script>")
                .append("\n<script src=\"file:///android_asset/noteref_fixed.js\"></script>")
                .append("\n<script src=\"file:///android_asset/epub_extensions_fixed.js\"></script>")
                .append("\n<script src=\"file:///android_asset/bgmplayer.js\"></script>");

        String bgmJs = bgmJsBuilder.toString();
        bgmJsBuilder.setLength(0);
        bgmJsBuilder = null;

        return bgmJs;
    }

    private String getTemplateScript(Context context) {

        StringBuilder templateBuilder = new StringBuilder();
        templateBuilder.append("\n<script src=\"file:///android_asset/jquery-2.1.4.min.js\"></script>")
                .append("\n<script src=\"file:///android_asset/fixedlayout_script.js\"></script>")
                .append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/noteref.css\" />")
                .append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/highlight.css\" />")
                .append("\n<style>")
                .append("\n body { margin : 0; border : 0 !important; }#feelingk_booktable, #feelingk_bookcontent { -webkit-user-select: none !important; position: static !important; }")  // body{ position: fixed;}
                .append("\n</style>");

        String templateStr = templateBuilder.toString();

        templateBuilder.setLength(0);
        templateBuilder=null;

        return templateStr;
    }

    //
    // img tag의 src 값을 파일이 위치한  fullPath 이름으로 변환하는 작업을 수행
    // 구해진 fileInfo class 에서 정확한 이름의 이미지 파일을 얻어 오는 것이 관건.
    // 내부에 getFileItem(...) 함수를 통해 정확한 이미지의 파일 이름을 얻어옴.
    //
//    private boolean getImageSource(StringBuilder body) {
//
//        String bodySrc = body.toString();
//
//        ArrayList<String> imgTags = BookHelper.getMatcherTexts("<img .*?>", bodySrc);
//
//        boolean result=false;
//        for(int i=0; i<imgTags.size(); i++) {
//
//            String srcValue = BookHelper.getMatcherText("src=\".*?\"", imgTags.get(i) );
//            if( srcValue.length() <= 0 )
//                srcValue = BookHelper.getMatcherText("src='.*?'", imgTags.get(i));
//
//            if(srcValue.length() > 0)
//            {
////                DebugSet.d(TAG, "src value ===== " + srcValue);
//                int k = srcValue.length() - 1;
//                int start = srcValue.indexOf("/");
//
//                if( start == -1 ) {
//                    start = 5;
//                } else {
//                    start += 1;
//                }
//
//                String fileName = srcValue.substring(start, k);
//                String fullFileName = getFileItem(fileName);
//
//                if( fullFileName == null )
//                    return false;
//
//                String newSrc = "src='file://" + fullFileName + "'";
//
//                int index = body.indexOf(srcValue);
//                body.replace(index, index+k+1, newSrc);
//
//                result = true;
//            }
//        }
//
//        return result;
//    }
//
// // fileName으로 부터 정확한 FileInfo 객체를 얻어온다.
//    private String getFileItem(String fileName) {
//        return ((Epub)BookHelper.mBook).getFileInfoByName(fileName);
//    }

    /**
     * Resource 파일을 String으로 read
     *
     * @param context
     * @param filepath
     */
    private String readResourceFile(Context context, String filepath){

        StringBuffer sb = new StringBuffer();
        try {
            InputStream fis = context.getClass().getResourceAsStream(filepath);

            try {
                byte[] b = new byte[4096];
                for (int n; (n = fis.read(b)) != -1;) {
                    sb.append(new String(b, 0, n));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(fis != null)fis.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getViewPort( String decodeStr){

        if(decodeStr==null){
            return null;
        } else{
            try {
                Element rootElement = getElement(decodeStr);
                NodeList metalist = rootElement.getElementsByTagName("meta");
                for( int i = 0 ; i < metalist.getLength(); i++ ){
                    Node meta = metalist.item(i);
                    NamedNodeMap attrs = meta.getAttributes();
                    if( attrs.getNamedItem("name") != null && attrs.getNamedItem("name").getNodeValue().equalsIgnoreCase("viewport")) {
                        String viewPort = attrs.getNamedItem("content") != null ? attrs.getNamedItem("content").getNodeValue() : null;
                        return viewPort.trim();
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Element getElement(String decodeStr) throws ParserConfigurationException, SAXException, IOException {

        decodeStr = decodeStr.substring(decodeStr.toLowerCase().indexOf("<html"), decodeStr.length());

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentbuilder = factory.newDocumentBuilder();
            Element element = documentbuilder.parse(new InputSource(new StringReader(decodeStr))).getDocumentElement();

            return element;

        } catch (ParserConfigurationException e) {
            throw e;
        } catch (SAXException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    private String getStringFromFile(String filePath) {

        try {
            File file = new File( filePath );
            if( !file.exists() ) {
                return null;
            }

            InputStream input = new FileInputStream( file );

            String inputByte = inputStream2String(input, "UTF-8");
            input.close();

            return inputByte;

        } catch( Exception e ) {
            e.printStackTrace();
        }

        return null;
    }

    public String inputStream2String(InputStream inputstream, String enc) throws Exception {
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
                }
                else {
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
}
