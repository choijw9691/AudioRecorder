package com.ebook.mediaoverlay;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ebook.epub.fixedlayoutviewer.view.FixedLayoutWebview;
import com.ebook.epub.parser.mediaoverlays.SmilSync;
import com.ebook.epub.viewer.EPubViewer;
import com.ebook.epub.viewer.ViewerContainer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MediaOverlayController {

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private final int MEDIAOVERLAY_SET_ID_LIST = 600;
    private final int EPUB_ADD_HIGHLIGHT = 602;
    private final int EPUB_DELETE_HIGHLIGHT = 603;
    private final int MEDIAOVERLAY_SET_PLAY_ID = 604;

    private int idIndex=0;

    private CountDownTimer timer;

    public ArrayList<String> contentElementIds = new ArrayList<String>();

    public LinkedHashMap<String, SmilSync> smilSyncs;

    public String playingFilePath = "";

    private WebView leftWebView;
    private WebView rightWebView;

    private ViewerContainer.LayoutMode mLayoutMode;

    private boolean isLGetIdFinished=false;
    private boolean isRGetIdFinished=false;
    private boolean isExistOnPage = false;

    private OnMediaOverlayListener mMediaOverlayListener;
    public void setOnMediaOverlayListener(OnMediaOverlayListener listener) {
        mMediaOverlayListener = listener;
    }
    private ViewerContainer.OnMediaOverlayStateListener mMediaOverlayStateListener;
    public void setOnMediaOverlayStateListener(ViewerContainer.OnMediaOverlayStateListener listener) {
        mMediaOverlayStateListener = listener;
    }

    public MediaOverlayController(){

//		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//
//			@Override
//			public void onCompletion(MediaPlayer arg0) {
//
//				idIndex++;
//
//				if(contentElementIds.size()>0)
//					play();
//			}
//		});
    }

    public LinkedHashMap<String, SmilSync> getSmilSyncs() {
        return smilSyncs;
    }

    public void setSmilSyncs(LinkedHashMap<String, SmilSync> smilSyncs) {
        this.smilSyncs = smilSyncs;
    }

    public void addMediaOverlayJSInterface(int position, WebView webView){
        if(position==0){
            leftWebView = webView;
            leftWebView.addJavascriptInterface(new MediaOverlayJSInterface(), "mediaoverlay");
        } else if(position==1){
            rightWebView = webView;
            rightWebView.addJavascriptInterface(new MediaOverlayJSInterface(), "mediaoverlay");
        }
    }

    public void getIdList(ViewerContainer.LayoutMode layoutMode){

        mLayoutMode = layoutMode;

        if(mLayoutMode== ViewerContainer.LayoutMode.FixedLayout){
            if(!isLGetIdFinished){
                leftWebView.loadUrl("javascript:getIDList('"+((FixedLayoutWebview)leftWebView).getCurrentPageData().getContentsFilePath()+ "')");
            }else if(rightWebView!=null && !isRGetIdFinished)
                rightWebView.loadUrl("javascript:getIDList('"+((FixedLayoutWebview)rightWebView).getCurrentPageData().getContentsFilePath()+ "')");
        } else{
            leftWebView.loadUrl("javascript:getIDList('"+((EPubViewer)leftWebView).getCurrentChapterFile()+ "')");
        }
    }

    public void getIDofFirstVisibleElement(WebView webView){

        JSONArray array = new JSONArray();

        for(int idx=0; idx<contentElementIds.size(); idx++){
            String[] currentInfo = contentElementIds.get(idx).split("#");
            array.put(currentInfo[1]);
        }

        if(mLayoutMode== ViewerContainer.LayoutMode.FixedLayout)
            webView.loadUrl("javascript:getIDofFirstVisibleElement('"+((FixedLayoutWebview)webView).getCurrentPageData().getContentsFilePath()+ "',"+array.toString()+")");
        else
            webView.loadUrl("javascript:getIDofFirstVisibleElement('"+((EPubViewer)webView).getCurrentChapterFile()+ "',"+array.toString()+")");
    }

    public void setPlayId(String id, boolean play){

        for(int index=0; index<contentElementIds.size(); index++){
            if(contentElementIds.get(index).equalsIgnoreCase(id)){
                idIndex = index;
                mMediaOverlayStateListener.setPositoinOfMediaOverlayDone();
                break;
            }
        }
    }

    public void play(){

        if(isPaused){
            isPaused=false;
            tempTimer(leftTime);
            mediaPlayer.start();
        } else{

            isExistOnPage = false;

            if(isMediaOverlayPlaying())
                stop(false);

            for(int idx=idIndex; idx<contentElementIds.size(); idx++){

                SmilSync smilSync = getSmilSyncs().get(contentElementIds.get(idx));

                if(smilSync!=null){

                    idIndex=idx;
                    isExistOnPage = true;

                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(smilSync.audioFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo((int) smilSync.audioStartTime);
                        tempTimer(smilSync.getAudioDurationTime());
                        mediaPlayer.start();
                        if(mMediaOverlayStateListener!=null){
                            mMediaOverlayStateListener.didMediaOverlayPlayListener();
                        }
                        if( mMediaOverlayListener != null ){
                            String[] currentInfo = contentElementIds.get(idIndex).split("#");
                            playingFilePath=currentInfo[0];
                            SendMessage(EPUB_ADD_HIGHLIGHT, smilSync);
                        }
                        break;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(!isExistOnPage)
                mMediaOverlayStateListener.finishMediaOverlayPlay();
        }
    }

    private long leftTime=-1;
    private void tempTimer(final long audioDurationTime){

        timer = new CountDownTimer(audioDurationTime, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                leftTime=millisUntilFinished;
            }

            @Override
            public void onFinish() {
                leftTime=-1;
                continuePlay();
            }
        };
        timer.start();
    }

    private boolean isPaused=false;
    public void pause(){

        isPaused = true;

        if (mediaPlayer.isPlaying() && timer!=null) {
            timer.cancel();
            mediaPlayer.pause();
            if(mMediaOverlayStateListener!=null){
                mMediaOverlayStateListener.didMediaOverlayPauseListener();
            }
        }
    }

    public void stop(boolean fromUser){

        isPaused = false;

        if(timer!=null)
            timer.cancel();

        mediaPlayer.stop();

        if(mMediaOverlayStateListener!=null){
            mMediaOverlayStateListener.didMediaOverlayStopListener();
        }

        if(fromUser)
            idIndex=0;

        SendMessage(EPUB_DELETE_HIGHLIGHT, null);
    }

    private void continuePlay(){

        idIndex++;

        play();
    }

    private class MediaOverlayJSInterface {

        @JavascriptInterface
        public void setIDofFirstVisibleElement(String filePath, final String id){
//			setPlayId(filePath+"#"+id, false);
            SendMessage(MEDIAOVERLAY_SET_PLAY_ID, filePath+"#"+id);
        }

        @JavascriptInterface
        public void setIdList(final String json, String filePath){

            if(!isLGetIdFinished)
                isLGetIdFinished=true;
            else if(isLGetIdFinished && !isRGetIdFinished)
                isRGetIdFinished=true;

            ArrayList<String> tempIdList = new ArrayList<String>();

            try {
                JSONArray jsonArr = new JSONArray(json);
                for(int index=0; index<jsonArr.length(); index++){
                    tempIdList.add(filePath+"#"+jsonArr.getString(index));
                }
                SendMessage(MEDIAOVERLAY_SET_ID_LIST, tempIdList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        idIndex=0;
        isLGetIdFinished=false;
        isRGetIdFinished=false;
        isPaused=false;
        contentElementIds.clear();
        if(timer!=null)
            timer.cancel();
    }

    public boolean isMediaOverlayPlaying(){
        return mediaPlayer.isPlaying();
    }

    private void SendMessage(int what, Object obj) {
        if( mediaOverlayHandler != null ){
            Message msg = mediaOverlayHandler.obtainMessage(what, obj);
            mediaOverlayHandler.sendMessage(msg);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mediaOverlayHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MEDIAOVERLAY_SET_ID_LIST:

                    if(msg.obj!=null){

                        ArrayList<String> idList = (ArrayList<String>) msg.obj;
                        contentElementIds.addAll(idList);

                        if(isLGetIdFinished && !isRGetIdFinished && rightWebView!=null){
                            // 투 페이지 fixedlayout 왼쪽 setId 완료 후
                            getIdList(mLayoutMode);
                        }else if(isLGetIdFinished && isRGetIdFinished && rightWebView!=null){
                            // 투 페이지 fixedlayout 왼쪽/오른쪽 setId 완료 후
                            isLGetIdFinished = false;
                            isRGetIdFinished = false;
                            getIDofFirstVisibleElement(leftWebView);
                        }else if(isLGetIdFinished && rightWebView==null){
                            // 한페이지 fixedlayout or reflowable setId 완료 후
                            isLGetIdFinished = false;
                            getIDofFirstVisibleElement(leftWebView);
                        }
                    }
                    break;
                case EPUB_ADD_HIGHLIGHT :
                    if(msg.obj!=null){
                        SmilSync smilSync = (SmilSync)msg.obj;
                        mMediaOverlayListener.addMediaOverlayHighlighter(smilSync.getChapterFilePath(), smilSync.getFragment());
                    }
                    break;
                case EPUB_DELETE_HIGHLIGHT :
                    if(mMediaOverlayListener!=null)
                        mMediaOverlayListener.removeMediaOverlayHighlighter();
                    break;

                case MEDIAOVERLAY_SET_PLAY_ID :
                    setPlayId((String)msg.obj, false);
                default:
                    break;
            }
        }
    };
}
