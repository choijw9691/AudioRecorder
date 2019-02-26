package com.ebook.epub.viewer.data;

/**
 * 목차 정보 데이터
 * @author djHeo
 *
 */
public class ChapterInfo {
	private String chapterFilePath; //파일 경로
	private String chapterName; //목자 타이틀
	private int chapterDepth; //목차 Depth (대챕터/소챕터 개념)
	
	public ChapterInfo(String path, String name, int depth) {
		this.chapterFilePath = path;
		this.chapterName = name;
		this.chapterDepth = depth;
	}

	public String getChapterFilePath() {
		return chapterFilePath;
	}

	public String getChapterName() {
		return chapterName;
	}

	public int getChapterDepth() {
		return chapterDepth;
	}
	
	
}
