package com.ebook.tts;

public interface OnTTSDataInfoListener {
	void onSpeechPositionChanged(int position);
	void ttsDataFromSelection(TTSDataInfo selectedTTSDataInfo, int index);
}
