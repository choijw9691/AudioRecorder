package com.ebook.media;

import java.util.ArrayList;

public interface OnAudioContentPlayerListener {
	void existAudioContentsOncurrentPage(ArrayList<String> audioIdList);
	void didFailAudio(String id);
}
