package com.ebook.media;

import java.util.HashMap;

import android.webkit.WebView;

public class AudioContentReader extends MediaContentReader{

	public WebView webview;
	public HashMap<String, AudioContent> audioContents = new HashMap<String, AudioContent>();
	
	public AudioContentReader(WebView webview){
		this.webview = webview;
		getAudioContentsByJavascript();
	}
	
	public void getAudioContentsByJavascript(){
		webview.loadUrl("javascript:getAudioContents()");
	}
	
	public HashMap<String, AudioContent> getAudioContents(){
		return audioContents;
	}
	
	@Override
	MediaContent getMediaContentById(String id) {
		return null;
	}
}
