package com.flk.epubviewersample.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class BookPreference {
	
	public final static String PREF_NAME		= "com.ebook.bookshelf.multi.percenttest";
	
	public final static String VIEWER_TTS_RATE	= "viewer_tts_rate";
	public final static String VIEWER_TTS_PITCH	= "viewer_tts_pitch";
	public final static String VIEWER_TTS_VOLUME= "viewer_tts_volume";
	public final static String VIEWER_TTS_LANGUAGE= "viewer_tts_language";
	
	private final static String DEFAULT_LANGUAGE = "ko";
	
	
	public static float getTTSRate(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return settings.getFloat(VIEWER_TTS_RATE, 1.0f);
	}

	public static void setTTSRate(Context context, float rate){
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(VIEWER_TTS_RATE, rate);
		editor.commit();
	}
	
	public static float getTTSPitch(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return settings.getFloat(VIEWER_TTS_PITCH, 1.0f);
	}

	public static void setTTSPitch(Context context, float pitch){
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(VIEWER_TTS_PITCH, pitch);
		editor.commit();
	}
	
	public static int getTTSVolume(Context context)
	{
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return settings.getInt(VIEWER_TTS_VOLUME, currentVolume);
	}

	public static void setTTSVolume(Context context, int volume){
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(VIEWER_TTS_VOLUME, volume);
		editor.commit();
	}
	
	public static String getTTSLanguage(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return settings.getString(VIEWER_TTS_LANGUAGE, DEFAULT_LANGUAGE);
	}

	public static void setTTSLanguage(Context context, String language){
		SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(VIEWER_TTS_LANGUAGE, language);
		editor.commit();
	}
}
