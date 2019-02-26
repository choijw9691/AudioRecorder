package com.ebook.bgm;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.opf.XmlCollection;
import com.ebook.epub.parser.opf.XmlLink;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerContainer.OnBGMControlListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BGMPlayer {

	private static String ROLE = "BGM";
	
	private final int BGM_PLAY = 0;
	private final int BGM_PAUSE = 1;
	private final int BGM_STOP = 2;
	
//	private MediaPlayer mediaPlayer;

	private ArrayList<BGMContent> bgmContents = new ArrayList<BGMContent>();

	private WebView leftWebView;
	private WebView rightWebView;

	private int position=0;

	private boolean isReset = true;
	public boolean isClose=false;
	
	private OnBGMControlListener mBgmControllListener;
	private BGMMediaPlayer bgmMediaPlayer;
	
	public BGMPlayer(EpubFile epubFile){

		bgmMediaPlayer =BGMMediaPlayer.getBgmMediaPlayerClass();
		
		HashMap<String, XmlCollection> collections = epubFile.getCollections();

		Iterator<String> iterator = collections.keySet().iterator();

		while (iterator.hasNext()) {

			String role = (String) iterator.next();

			if(role.equalsIgnoreCase(ROLE)){
				ArrayList<XmlLink> links = collections.get(role).getLinks();

				for(int idx=0; idx<links.size(); idx++){
					BGMContent bgmContent = new BGMContent();
					FileInfo bgmFileInfo = EpubFileUtil.getResourceFile(epubFile.getPublicationPath(), links.get(idx).gethRef());
					if(bgmFileInfo!=null){
						bgmContent.setFilePath(bgmFileInfo.filePath);
						bgmContents.add(bgmContent);
					}
				}
			}
			break;
		}
		
		bgmMediaPlayer.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				
				isReset=true;
				
				bgmMediaPlayer.playIndex+=1;
				
				if(bgmMediaPlayer.playIndex>=bgmContents.size()){
					bgmMediaPlayer.playIndex=0;
				}
				
				play();
			}
		});
	}

	public void setOnBGMControlListener(OnBGMControlListener listener){
		mBgmControllListener = listener;
	}

	public void setCurrentWebView(WebView leftWebView, WebView rightWebView){
		this.leftWebView = leftWebView;
		this.rightWebView = rightWebView;

		addBGMPlayerJSInterface();
	}

	public void addBGMPlayerJSInterface(){
		leftWebView.addJavascriptInterface(new BGMPlayerJavaScriptInterface(), "bgmplayer");
		if(rightWebView!=null)
			rightWebView.addJavascriptInterface(new BGMPlayerJavaScriptInterface(), "bgmplayer");
	}

	public void play(){

		if(isClose || bgmContents.size()<=0)
			return;
		
		if(bgmMediaPlayer.playIndex>=bgmContents.size()){
			bgmMediaPlayer.playIndex=0;
		}
		
		bgmMediaPlayer.isPlaying = true;

		try {
			if(isReset){
				isReset=false;
				bgmMediaPlayer.mediaPlayer.reset();
				bgmMediaPlayer.mediaPlayer.setDataSource(bgmContents.get(bgmMediaPlayer.playIndex).filePath);
				bgmMediaPlayer.mediaPlayer.prepare();
				position=0;
			}
			bgmMediaPlayer.mediaPlayer.seekTo(position);
			bgmMediaPlayer.mediaPlayer.start();
		} catch (IllegalStateException e) {
			leftWebView.loadUrl("javascript:onErrorBGM()");
			if(rightWebView!=null)
				rightWebView.loadUrl("javascript:onErrorBGM()");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		leftWebView.loadUrl("javascript:onPlayBGM("+bgmMediaPlayer.playIndex+")");
		if(rightWebView!=null)
			rightWebView.loadUrl("javascript:onPlayBGM("+bgmMediaPlayer.playIndex+")");
	
		if(mBgmControllListener!=null)
			mBgmControllListener.didBGMPlayListener();
	}	

	public void pause(){

		bgmMediaPlayer.isPlaying = false;

		bgmMediaPlayer.mediaPlayer.pause();

		position = bgmMediaPlayer.mediaPlayer.getCurrentPosition();

		leftWebView.loadUrl("javascript:onPauseBGM("+bgmMediaPlayer.playIndex+")");
		if(rightWebView!=null)
			rightWebView.loadUrl("javascript:onPauseBGM("+bgmMediaPlayer.playIndex+")");

		if(mBgmControllListener!=null)
			mBgmControllListener.didBGMPauseListener();
	}

	public void stop(){

		bgmMediaPlayer.isPlaying = false;

		isReset = true;

		bgmMediaPlayer.mediaPlayer.stop();

		leftWebView.loadUrl("javascript:onStopBGM("+bgmMediaPlayer.playIndex+")");
		if(rightWebView!=null)
			rightWebView.loadUrl("javascript:onStopBGM("+bgmMediaPlayer.playIndex+")");

		if(mBgmControllListener!=null)
			mBgmControllListener.didBGMStopListener();
	}

	public void setBGMState(){
		
		if(leftWebView!=null)
			leftWebView.loadUrl("javascript:$.bgmPlayer.setBGMState("+bgmMediaPlayer.isPlaying+","+bgmMediaPlayer.playIndex+")");
		if(rightWebView!=null)
			rightWebView.loadUrl("javascript:$.bgmPlayer.setBGMState("+bgmMediaPlayer.isPlaying+","+bgmMediaPlayer.playIndex+")");
	}
	
	class BGMPlayerJavaScriptInterface {

		@JavascriptInterface
		public void playBGM(int index){

			if(index!=bgmMediaPlayer.playIndex){
				isReset = true;
				bgmMediaPlayer.playIndex=index;
			}

			SendMessage(BGM_PLAY, null);
		}

		@JavascriptInterface
		public void pauseBGM(){
			SendMessage(BGM_PAUSE, null);
		}

		@JavascriptInterface
		public void stopBGM(){
			SendMessage(BGM_STOP, null);
		}
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case BGM_PLAY :
				play();
				break;
			case BGM_PAUSE :
				pause();
				break;
			case BGM_STOP :
				stop();
				break;
			default :
				break;
			}
		}
	};

	private void SendMessage(int what, Object obj) {
		Message msg = handler.obtainMessage(what, obj);
		handler.sendMessage(msg);
	}
	
	public void clearMediaPlayer(){
		if(bgmMediaPlayer.mediaPlayer!=null){
			bgmMediaPlayer.mediaPlayer.stop();
			bgmMediaPlayer.playIndex=0;
		}
	}
}
