package com.ebook.epub.parser.mediaoverlays;

import com.ebook.epub.parser.common.EscapabilityOption;
import com.ebook.epub.parser.common.SkippabilityOption;
import com.ebook.epub.parser.common.TextToEscapabilityConverter;
import com.ebook.epub.parser.common.TextToSkippabilityConverter;

/**
@class SmilSync	
@brief SMIL data class
 */
public class SmilSync {
	
	public String chapterFilePath;
	public String fragment;
	public String audioFilePath;
	public long audioEndTime;
	public long audioStartTime;
	public long audioDurationTime;
	public String optionOfEscapability;
	public String optionOfSkippability;
	
	public String getChapterFilePath() {
		return chapterFilePath;
	}
	public void setChapterFilePath(String chapterFilePath) {
		this.chapterFilePath = chapterFilePath;
	}
	public String getFragment() {
		return fragment;
	}
	public void setFragment(String fragment) {
		this.fragment = fragment;
	}
	public String getAudioFilePath() {
		return audioFilePath;
	}
	public void setAudioFilePath(String audioFilePath) {
		this.audioFilePath = audioFilePath;
	}
	public long getAudioEndTime() {
		return audioEndTime;
	}
	public void setAudioEndTime(long audioEndTime) {
		this.audioEndTime = audioEndTime;
	}
	public long getAudioStartTime() {
		return audioStartTime;
	}
	public void setAudioStartTime(long audioStartTime) {
		this.audioStartTime = audioStartTime;
	}
	public long getAudioDurationTime() {
		return audioDurationTime;
	}
	public void setAudioDurationTime(long audioDurationTime) {
		this.audioDurationTime = audioDurationTime;
	}
	public String getOptionOfEscapability() {
		return optionOfEscapability;
	}
	public void setOptionOfEscapability(String optionOfEscapability) {
		this.optionOfEscapability = optionOfEscapability;
	}
	public String getOptionOfSkippability() {
		return optionOfSkippability;
	}
	public void setOptionOfSkippability(String optionOfSkippability) {
		this.optionOfSkippability = optionOfSkippability;
	}
	
	public SkippabilityOption getOptionOfSkippabilityType(){
		
		TextToSkippabilityConverter converter = new TextToSkippabilityConverter();
		return converter.convert(optionOfSkippability);
	}
	
	public EscapabilityOption getOptionOfEscapabilityType(){
		
		TextToEscapabilityConverter converter = new TextToEscapabilityConverter();
		return converter.convert(optionOfEscapability);
	}
}
