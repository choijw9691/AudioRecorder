package com.ebook.tts;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class TTSDataInfoManager {

    private final int MSG_GET_START_POSITION = 0;
    private final int MSG_GET_FIRST_SENTENCE = 1;
    private final int MSG_GET_SELECTED_DATA = 2;

    private WebView leftWebView;
    private WebView rightWebView;

    private boolean isTwoPageMode = false;

    private ArrayList<TTSDataInfo> ttsDataInfoList;

    private OnTTSDataInfoListener onTTSDataInfoListener;

    private JSONArray ttsDataJsonArr;
    private JSONArray elementXPathJsonArr;

    public String selectedStartElementPath;
    public String selectedEndElementPath;
    public int selectedStartCharOffset;
    public int selectedEndCharOffset;

    public TTSDataInfoManager() {
        ttsDataInfoList = new ArrayList<>();
    }

    public void addTTSDataInfoJSInterface(WebView webview) {
        webview.addJavascriptInterface(new TTSDataInfoJSInterface(), "ttsDataInfo");
    }

    public void setOnTTSDataInfoManager(OnTTSDataInfoListener listener) {
        onTTSDataInfoListener = listener;
    }

    /** reflowable */
    public ArrayList<TTSDataInfo> readContents(WebView webView, String contents, String filePath){

        if( ttsDataInfoList.size() > 0 && onTTSDataInfoListener != null){
            return ttsDataInfoList;
        }

        leftWebView = webView;

        ttsDataJsonArr = new JSONArray();
        elementXPathJsonArr = new JSONArray();

        Element rootElement = getContentText(contents);

        if(rootElement!=null){

            NodeList childNodes = rootElement.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); ++i) {

                if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;

                getTextFromNode(childNodes.item(i), filePath);

            }
            setTTSDataList(filePath);
        }
        return ttsDataInfoList;
    }

    /** fixed */
    public ArrayList<TTSDataInfo> readContents(WebView left, WebView right, FixedLayoutPageData pageData){

        if( ttsDataInfoList.size() > 0 && onTTSDataInfoListener != null){
            return ttsDataInfoList;
        }

        leftWebView = left;
        rightWebView = right;

        if(rightWebView!=null)
            isTwoPageMode=true;

        ttsDataJsonArr = new JSONArray();
        elementXPathJsonArr = new JSONArray();

        for(int idx=0; idx<pageData.getContentsCount(); idx++){

            FixedLayoutPageData.ContentsData contentsData = pageData.getContentsDataList().get(idx);

            Element rootElement = getContentText(contentsData.getContentsString());

            if(rootElement!=null){

                NodeList childNodes = rootElement.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); ++i) {

                    if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    getTextFromNode(childNodes.item(i), pageData.getContentsDataList().get(idx).getContentsFilePath());
                }
                setTTSDataList(pageData.getContentsDataList().get(idx).getContentsFilePath());
            }
        }
        return ttsDataInfoList;
    }

    /** fixed in background */
    public ArrayList<TTSDataInfo> readContentsFromPageData(FixedLayoutPageData data){

        ttsDataInfoList.clear();
        FixedLayoutPageData pageData = data;

        ttsDataJsonArr = new JSONArray();
        elementXPathJsonArr = new JSONArray();

        for(int idx=0; idx<pageData.getContentsCount(); idx++){

            FixedLayoutPageData.ContentsData contentsData = pageData.getContentsDataList().get(idx);

            Element rootElement = getContentText(contentsData.getContentsString());

            if(rootElement!=null){

                NodeList childNodes = rootElement.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); ++i) {

                    if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    getTextFromNode(childNodes.item(i), pageData.getContentsDataList().get(idx).getContentsFilePath());

                }
                setTTSDataList(pageData.getContentsDataList().get(idx).getContentsFilePath());
            }
        }
        return ttsDataInfoList;
    }

    private void getTextFromNode(Node node, String filePath){

        NodeList childNodes = node.getChildNodes();

//		if(node.getNodeType() == Node.TEXT_NODE){
//
//			if(node.getTextContent().trim().length()>0){
//
//				try {
//					String xPath = getXPath(node.getParentNode());
//
//					JSONObject jsonObj = new JSONObject();
//					jsonObj.put("text", node.getTextContent());
//					jsonObj.put("path", xPath);
//					jsonObj.put("parentText", "");
//					jsonObj.put("filePath", filePath);
//
//					elementXPathJsonArr.put(xPath);
//
//					ttsDataJsonArr.put(jsonObj);
//
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//		} else
        if (node.getNodeType() == Node.ELEMENT_NODE){

//		    if(node.hasAttributes() && node.getAttributes().getNamedItem("style")!=null){
//		        String nodeAttr = node.getAttributes().getNamedItem("style").getNodeValue().replaceAll(" ","");
//		        if(nodeAttr.indexOf("display:none")!=-1){
//		            return;
//                }
//            }

            if(node.getNodeName().equalsIgnoreCase("div") ||
                    node.getNodeName().equalsIgnoreCase("section") ||
                    node.getNodeName().equalsIgnoreCase("figure") ||
                    node.getNodeName().equalsIgnoreCase("blockquote")){

                for( int i =0; i < childNodes.getLength(); i++ ){
                    Node child = childNodes.item(i);

                    getTextFromNode(child, filePath);
                }

            } else if(node.getNodeName().equalsIgnoreCase("img")){

                try {

                    String xPath = getXPath(node);

                    JSONObject jsonObj = new JSONObject();
                    Node alt = node.getAttributes().getNamedItem(AttributeName.ALT);
                    if(alt!=null){
                        jsonObj.put("text","flk_image:"+alt.getNodeValue());
                    }else {
                        jsonObj.put("text","flk_image:");
                    }
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(node.getNodeName().equalsIgnoreCase("math")){

                try {

                    String xPath = getXPath(node);

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("text","flk_math:"+node.getTextContent());
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(node.getNodeName().equalsIgnoreCase("svg")){

                try {
                    String xPath = getXPath(node);

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("text","flk_svg:");
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(node.getNodeName().equalsIgnoreCase("video")){

                try {
                    String xPath = getXPath(node);

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("text","flk_video:");
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if(node.getNodeName().equalsIgnoreCase("audio")){

                try {
                    String xPath = getXPath(node);

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("text","flk_audio:");
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                if(node.getTextContent()!=null && node.getTextContent().trim().replaceAll("\\p{Z}", "").length()>0){

                    try {
                        String xPath = getXPath(node);

                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("text", node.getTextContent());
                        jsonObj.put("path", xPath);
                        jsonObj.put("parentText", "");
                        jsonObj.put("filePath", filePath);

                        elementXPathJsonArr.put(xPath);

                        ttsDataJsonArr.put(jsonObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    for( int i=0; i < childNodes.getLength(); i++ ){
                        Node child = childNodes.item(i);
                        getTextFromNode(child, filePath);
                    }
                }
            }
        } else if(node.getNodeType() == Node.TEXT_NODE){

            if(node.getTextContent().trim().length()>0){

                try {
                    String xPath = getXPath(node.getParentNode());

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("text", node.getTextContent());
                    jsonObj.put("path", xPath);
                    jsonObj.put("parentText", "");
                    jsonObj.put("filePath", filePath);

                    elementXPathJsonArr.put(xPath);

                    ttsDataJsonArr.put(jsonObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String getXPath(Node node){

        String xPath = "";

        while(node!=null && !node.getNodeName().equalsIgnoreCase("html")){

            String name = node.getNodeName();

            Node parentNode = node.getParentNode();

            if(parentNode==null)
                break;

            NodeList childNodes = parentNode.getChildNodes();

            int index=0;
            for(int idx=0; idx<childNodes.getLength(); idx++){

                Node child = childNodes.item(idx);

                if(node.equals(child)){
                    break;
                }

                if(node.getNodeName().equalsIgnoreCase(child.getNodeName())){
                    index++;
                }
            }
            name+=(":eq("+index+")");
            xPath = name+(xPath != null && xPath != "" ? ">" +xPath : "" );
            node = parentNode;
        }

        return xPath;
    }

    private boolean setTTSDataList(String currentFilePath){

        try {
            for(int idx=0; idx<ttsDataJsonArr.length(); idx++){

                JSONObject obj = ttsDataJsonArr.getJSONObject(idx);
                String text = obj.getString("text");
                String path = obj.getString("path");
                String parentText = obj.getString("parentText");
                String filePath = obj.getString("filePath");

                if(!currentFilePath.equalsIgnoreCase(filePath))
                    continue;

                int prevIndex = 0;
                if(!parentText.isEmpty() && !text.equalsIgnoreCase(parentText)){
                    int tempIdx = parentText.indexOf(text);
                    if(tempIdx!=-1)
                        prevIndex = tempIdx;
                }

                if( text.startsWith("flk_") ){ // math, svg, img 태그 처리
                    ttsDataInfoList.add(new TTSDataInfo(text , path, 0, 0, currentFilePath));
                } else {
                    BreakIterator iterator = BreakIterator.getSentenceInstance();
                    iterator.setText(text);

                    int index = 0;
                    while (iterator.next() != BreakIterator.DONE) {
                        String sentence = text.substring(index, iterator.current());
                        DebugSet.d("TAG","Sentence: " + sentence);
                        DebugSet.d("TAG","Path: " + path);

                        if(sentence.trim().length()>0)  // sentence.trim() 으로 데이터 넣지 않도록 수정함
                            ttsDataInfoList.add(new TTSDataInfo(sentence, path, index+prevIndex, iterator.current()+prevIndex,  currentFilePath));
                        index = iterator.current();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void requestStartPosition() {
        SendMessage(MSG_GET_START_POSITION, null);
    }

    public void requestTTSDataFromSelection(int touchedposition){   // TODO :: new custom selection - modified
        if(touchedposition == -1)
            return;

        if(touchedposition==0)
            leftWebView.loadUrl("javascript:getSelectedElementPath()");
        else if(touchedposition==1)
            rightWebView.loadUrl("javascript:getSelectedElementPath()");
    }

    public void getTTSDataFromSelection() {
        // TODO :: new custom selection - modified

        TTSDataInfo selectedTTSDataInfo = null;
        String currentReadingText="";
        int startTextIndex = -1;

        for(int i=0; i<ttsDataInfoList.size(); i++) {

            String ttsPath = ttsDataInfoList.get(i).getXPath().trim().toLowerCase();
            int ttsStartOffset = ttsDataInfoList.get(i).getStartOffset();
            int ttsEndOffset = ttsDataInfoList.get(i).getEndOffset();

            if( selectedStartElementPath.trim().toLowerCase().equals(ttsPath) ){
                if( ttsStartOffset <= selectedStartCharOffset && selectedStartCharOffset < ttsEndOffset){
                    startTextIndex = i;
                    currentReadingText = ttsDataInfoList.get(i).getText().substring(selectedStartCharOffset-ttsStartOffset);
                    selectedTTSDataInfo = new TTSDataInfo(currentReadingText, ttsDataInfoList.get(i).getXPath(), selectedStartCharOffset, ttsEndOffset, ttsDataInfoList.get(i).getFilePath());
                    break;
                }
            }
        }

        if( onTTSDataInfoListener != null )
            onTTSDataInfoListener.ttsDataFromSelection(selectedTTSDataInfo, startTextIndex);
    }

    @SuppressLint("HandlerLeak")
    private Handler ttsDataInfoHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case MSG_GET_FIRST_SENTENCE: {
                    String param = (String)msg.obj;
                    if(isTwoPageMode){
                        if(((FixedLayoutPageData.ContentsData)leftWebView.getTag()).getContentsFilePath().equalsIgnoreCase("about:blank")){
                            rightWebView.loadUrl( param );
                        } else {
                            leftWebView.loadUrl( param );
                        }
                    } else {
                        if(leftWebView!=null)
                            leftWebView.loadUrl( param );
                        else
                            rightWebView.loadUrl( param );
                    }
                    break;
                }

                case MSG_GET_SELECTED_DATA : {
                    getTTSDataFromSelection();
                    break;
                }

                case MSG_GET_START_POSITION: {
                    if(isTwoPageMode) {
                        if(((FixedLayoutPageData.ContentsData)leftWebView.getTag()).getContentsFilePath().equalsIgnoreCase("about:blank")){
                            rightWebView.loadUrl("javascript:getPathOfFirstElement(" + elementXPathJsonArr.toString() + ")");
                        } else {
                            leftWebView.loadUrl("javascript:getPathOfFirstElement(" + elementXPathJsonArr.toString() + ")");
                        }
                    } else {
                        if(leftWebView!=null) {
                            leftWebView.loadUrl("javascript:getPathOfFirstElement(" + elementXPathJsonArr.toString() + ")");
                        } else {
                            rightWebView.loadUrl("javascript:getPathOfFirstElement(" + elementXPathJsonArr.toString() + ")");
                        }
                    }
                    break;
                }

                default:
                    break;
            }
        }
    };

    void SendMessage(int what, Object obj) {
        Message msg = ttsDataInfoHandler.obtainMessage(what, obj);
        ttsDataInfoHandler.sendMessage(msg);
    }

    /** ttsDataInfoList clear - 챕터 이동 시 반드시 호출 */
    public void clear() {
        if( ttsDataInfoList != null )
            ttsDataInfoList.clear();
    }

    public class TTSDataInfoJSInterface {

        @JavascriptInterface
        public void setTTSCurrentIndex(final String path) {

            if( path != null ) {

                JSONArray textArray = new JSONArray();

                int startIndex = -1;
                for(int i=0; i<ttsDataInfoList.size(); i++) {

                    String ttsPath = ttsDataInfoList.get(i).getXPath().trim().toLowerCase();

                    if( path.trim().toLowerCase().equals(ttsPath) ){
                        if( startIndex == -1 )
                            startIndex = i;
                        textArray.put(ttsDataInfoList.get(i).getJSONObjectTTSData());
                    } else {

                        if(textArray.length() > 0)
                            break;
                    }
                }

                if( textArray.length() > 0 ) {

                    StringBuilder script = (new StringBuilder()).append("javascript:getFirstSentence(");
                    script.append(textArray.toString()).append(",");
                    script.append("'").append(path).append("',").append(startIndex).append(")");

                    SendMessage(MSG_GET_FIRST_SENTENCE, script.toString());
                } else {
                    if( onTTSDataInfoListener != null )
                        onTTSDataInfoListener.onSpeechPositionChanged(-1);
                }
            } else {
                if( onTTSDataInfoListener != null )
                    onTTSDataInfoListener.onSpeechPositionChanged(-1);
            }
        }

        @JavascriptInterface
        public void setStartPosition(String path, int sentenceIndex, int listStartIndex) {

            int ttsStartIndex = -1;

            if( sentenceIndex == -1 )
                ttsStartIndex = ttsDataInfoList.size()-1;
            else
                ttsStartIndex = listStartIndex + sentenceIndex;

            if( onTTSDataInfoListener != null )
                onTTSDataInfoListener.onSpeechPositionChanged(ttsStartIndex);
        }

        @JavascriptInterface
        public void setSelectedElementPath(String startElementPath, String endElementPath, int startCharOffset, int endCharOffset){
            // TODO :: new custom selection modified

            selectedStartElementPath = startElementPath;
            selectedEndElementPath = endElementPath;
            selectedStartCharOffset = startCharOffset;
            selectedEndCharOffset = endCharOffset;

            SendMessage(MSG_GET_SELECTED_DATA, null);
        }
    }

    private Element getContentText(String decodeStr){

        if(decodeStr==null || decodeStr.isEmpty())
            return null;

        decodeStr = decodeStr.replaceAll("&nbsp;"," ");

        try {
            String bodyStr = BookHelper.getHtmlBody(decodeStr);
            if(bodyStr.contains("feelingk_booktable")){
                bodyStr = new StringBuilder("<body>").append(bodyStr).append("</body>").toString();
            } else {
                bodyStr = new StringBuilder("<body><div id='feelingk_booktable'><div id='feelingk_bookcontent'>").append(bodyStr).append("</div></div></body>").toString();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringComments(true);
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(bodyStr)));
            xml.normalize();

            XPath xpath = XPathFactory.newInstance().newXPath();
            Element element = (Element) xpath.evaluate("//body", xml, XPathConstants.NODE);

            return element;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
