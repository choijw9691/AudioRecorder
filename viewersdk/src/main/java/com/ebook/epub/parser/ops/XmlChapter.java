package com.ebook.epub.parser.ops;

import java.util.ArrayList;

public class XmlChapter {
	private ArrayList<XmlChapter> chapterList = new ArrayList<XmlChapter>();
	
	private String hRef = "";
	private String value = "";
	
	public XmlChapter(String hRef, String val) {
		this.hRef = hRef;
		this.value = val;
	}
	
	public void add(XmlChapter chapter) {
		chapterList.add(chapter);
	}
	
	public void setChapterList(ArrayList<XmlChapter> chapters) {
		this.chapterList = chapters;
	}
	
	public ArrayList<XmlChapter> getChapterList() {
		return chapterList;
	}
	public String gethRef() {
		return hRef;
	}
	public String getValue() {
		return value;
	}
	
	public String toString() {
		String str = "href : " + hRef 
				+ "\nvalue : " + value + "\n";
		
		for (int i = 0; i < chapterList.size(); i++) {
			str += "sub " + i + "\n" +chapterList.get(i).toString();
		}
		
		
		return str;
	}
	
}
