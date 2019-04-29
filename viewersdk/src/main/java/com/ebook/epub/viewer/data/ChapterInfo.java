package com.ebook.epub.viewer.data;

/**
 * 목차 정보 데이터
 * @author djHeo
 *
 */
public class ChapterInfo {

	private String chapterFilePath;     //파일 경로
	private String chapterName;         //목자 타이틀
	private int chapterDepth;           //목차 Depth (대챕터/소챕터 개념)
    private String chapterId;           //내부 목차 아이디

	public ChapterInfo(String path, String name, int depth) {
		this.chapterFilePath = path;
		this.chapterName = name;
		this.chapterDepth = depth;
	}

    public ChapterInfo(String path, String name, int depth, String chapterId) {
        this.chapterFilePath = path;
        this.chapterName = name;
        this.chapterDepth = depth;
        this.chapterId = chapterId;
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

    public String getChapterId() {
        return chapterId;
    }
}
