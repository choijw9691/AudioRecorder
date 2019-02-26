package com.ebook.bgm;

import android.media.MediaPlayer;

public class BGMMediaPlayer {
	private static BGMMediaPlayer bgmMediaPlayer;
	
	public MediaPlayer mediaPlayer= new MediaPlayer();
	
	public int playIndex = 0;
	
	public boolean isPlaying = false;
	
	public static BGMMediaPlayer getBgmMediaPlayerClass(){
		if(bgmMediaPlayer == null)
			bgmMediaPlayer = new BGMMediaPlayer();
		
		return bgmMediaPlayer;
	}
}
