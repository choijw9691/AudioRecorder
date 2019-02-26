package com.ebook.media;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AudioContentPlayer extends MediaContentPlayer{

	public WebView webView;
	public AudioContentReader audioContentReader;
	public OnAudioContentPlayerListener audioContentPlayerListener;

	public AudioContentPlayer(){}
	
	public void setWebView(WebView webView) {
		this.webView = webView;
	}
	
	public void addAudioPlayerJSInterface() {
		webView.addJavascriptInterface(new AudioPlayerJavaScriptInterface(), "audioplayer");
	}
	
	public void setAudioContentReader(AudioContentReader audioContentReader) {
		this.audioContentReader = audioContentReader;
	}
	
	public void setOnAudioContentPlayerListener(OnAudioContentPlayerListener listener) {
		audioContentPlayerListener = listener;
	}
	
	public void findAudioContentOnCurrentPage(){
		webView.loadUrl("javascript:findAudioContentOnCurrentPage()");
	}

	@Override
	public void play(String id) {
		play(id,0);
	}

	@Override
	public void play(String id, double startTime) {
		String script = (new StringBuilder())
				.append("javascript:playAudioAtTime('")
				.append(id).append("',")
				.append(startTime)
				.append(")").toString();
		webView.loadUrl(script);
	}

	@Override
	public void pause(String id) {
		webView.loadUrl("javascript:pauseAudio('"+id+"')");
	}

	@Override
	public void stop(String id) {
		webView.loadUrl("javascript:stopAudio('"+id+"')");
	}

	@Override
	public void loop(String id, boolean loop) {
		String script = (new StringBuilder())
				.append("javascript:setLoopAudio('")
				.append(id).append("',")
				.append(loop ? "true" : "false")
				.append(")").toString();
		webView.loadUrl(script);
	}
	
	public void moveAudioPlayingPosition(String xPath, double movingUnit) {
		String script = (new StringBuilder())
				.append("javascript:moveAudioPlayingPosition('")
				.append(xPath).append("',")
				.append(movingUnit)
				.append(")").toString();
		webView.loadUrl(script);
	}

//	@Override
//	public String getPlayMediaContent() {
//		if(audioContentReader.getAudioContents().size()>0)
//			return audioContentReader.getAudioContents().get(playIndex).id;
//		else
//			return "";
//	}
	
//	public void setPlayIndex(String id){
//		
//		boolean isExist = false;
//		
//		ArrayList<AudioContent> audioContents = audioContentReader.getAudioContents();
//		
//		for(int idx=0; idx<audioContents.size(); idx++){
//			String audioId = audioContents.get(idx).id;
//			if(audioId!=null && audioId.equals(id)){
//				playIndex = idx;
//				isExist = true;
//			}
//		}
//		if(!isExist)
//			audioContentPlayerListener.didFailAudio(id);
//	}
	
	private class AudioPlayerJavaScriptInterface {
		
		@JavascriptInterface
		public void getAudioContentsOnCurrentPage(String json) {
			
			ArrayList<String> audioIdList = new ArrayList<>();
			
			try{
				JSONArray jsArray = new JSONArray(json);
				
				for(int i = 0; i < jsArray.length(); i++){
					audioIdList.add(jsArray.getString(i));
				}
				audioContentPlayerListener.existAudioContentsOncurrentPage(audioIdList);
				
			} catch (JSONException e) {
				Log.d("DEBUG","getAudioContentsOnCurrentPage() JSONException");
			}
		}
		
		@JavascriptInterface
		public void didFailPlay(String id) {
			Log.d("DEBUG","didFailPlay() id : "+id);
		}
	}
}