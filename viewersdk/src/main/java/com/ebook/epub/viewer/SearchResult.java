package com.ebook.epub.viewer;

import android.graphics.Rect;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResult implements Serializable {

    public String chapterId;
    /** 북마크/주석 데이터 ( 쳅터 파일명 ) */
    public String chapterFile;
    /** 북마크/주석 생성시 해당 위치의 쳅터 이름 */
    public String chapterName;

    public int spineIndex=0;

    public String path;

    /** 북마크/주석 생성시 해당 위치의 텍스트 */
    public String text;

    public int charOffset;
    /** 북마크/주석 데이터 ( 위치에 대한 page 값 ) */
    public int page;

    public String keyword;

    public int pageOffset;
    /** 북마크/주석 데이터 ( 위치에 대한 %값 ) */
    public double percent;

    public int currentKeywordIndex;

    ArrayList<Rect> rects = new ArrayList<>();

    public SearchResult() {}

    public SearchResult(String path, String text, int page) {
        this.path = path;
        this.text = text;
        this.page = page;
    }

    public void clear() {
        rects.clear();
    }

    public String toString() {
        return chapterId + " | " + chapterFile + " | " + chapterName + " | " + spineIndex + " | " + text;
    }
}
