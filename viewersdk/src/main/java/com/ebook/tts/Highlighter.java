package com.ebook.tts;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ebook.epub.common.Defines;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Highlighter {

	private final int MSG_REQUEST_DRAW_RECT = 1;

	private OnHighlighterListener onHighlighterListener;
	private OnHighlightRectInfoListener onHighlightRectInfoListener;

	public interface OnHighlightRectInfoListener {
		void requestDrawRect(JSONArray rectArray, String currentFilePath);
		void requestRemoveRect();
	}
	
	private WebView currentWebView;
	
    @SuppressLint("HandlerLeak")
	private Handler highlighterHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_REQUEST_DRAW_RECT:
				if(msg.obj==null){
					if( onHighlightRectInfoListener != null ){
						onHighlightRectInfoListener.requestRemoveRect();
					}
				} else{
                    try {
                        String json = (String)msg.obj;
                        JSONObject object = new JSONObject(json);
                        JSONArray rectArr = object.getJSONArray("bounds");
                        String filePath = object.getString("filePath");
                        if( onHighlightRectInfoListener != null ){
                            onHighlightRectInfoListener.requestDrawRect(rectArr, filePath);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

				}
				break;
				
			case Defines.ADD_TTS_HIGHLIGHT:
				JSONObject highlightData = (JSONObject)msg.obj;
				if(currentWebView!=null)
                    currentWebView.loadUrl("javascript:setTTSHighlight("+highlightData.toString()+")");
				break;
			default:
				break;
			}
		}

	};

	void SendMessage(int what, Object obj) {
		Message msg = highlighterHandler.obtainMessage(what, obj);
		highlighterHandler.sendMessage(msg);
	}

	public void addHighlightJSInterface(WebView webview) {
		webview.addJavascriptInterface(new HighlightJSInterface(), "highlighter");
	}

	public void setOnHighlightRectInfoListener(OnHighlightRectInfoListener listener) {
		onHighlightRectInfoListener = listener;
	}

	public void setOnHighlighterListener(OnHighlighterListener listener) {
		onHighlighterListener = listener;
	}

    public void add(WebView webview, TTSDataInfo ttsDataInfo){
        currentWebView = webview;
        try{
            if( ttsDataInfo.getText() == null || ttsDataInfo.getXPath() == null )
                return;

            JSONObject highlightData = new JSONObject();
            highlightData.put("path", ttsDataInfo.getXPath());
            highlightData.put("text", ttsDataInfo.getText());
            highlightData.put("start", ttsDataInfo.getStartOffset());
            highlightData.put("end", ttsDataInfo.getEndOffset());
            highlightData.put("filePath", ttsDataInfo.getFilePath());

            SendMessage(Defines.ADD_TTS_HIGHLIGHT, highlightData);
        } catch(JSONException e){
        }
    }

	public void addMediaOverlayHighlight(WebView webview, String id, String activeClass, String playbackActiveClass){
		webview.loadUrl("javascript:addMediaOverlayHighlight('"+id+"','"+activeClass+"','"+playbackActiveClass+"')");
	}
	
	public void removeMediaOverlayHighlight(WebView webview, String activeClass, String playbackActiveClass){
		webview.loadUrl("javascript:removeMediaOverlayHighlight('"+activeClass+"','"+playbackActiveClass+"')");
	}
	
	public void remove() {
		if( onHighlightRectInfoListener != null )
			onHighlightRectInfoListener.requestRemoveRect();
	}

	/**
	 * Highlight 용 JavaScriptInterface
	 * @author djHeo
	 */
	private class HighlightJSInterface {

		@JavascriptInterface
		public void requestHighlightRect(final String json, boolean nextPage) {
            if(json==null){
                SendMessage(MSG_REQUEST_DRAW_RECT, null);
            } else if( json.trim().length() > 0 ) {
                SendMessage(MSG_REQUEST_DRAW_RECT, json);
                if( nextPage && onHighlighterListener != null ){
                    onHighlighterListener.moveToNextForHighlighter();
                }
            }
        }
	}
}
