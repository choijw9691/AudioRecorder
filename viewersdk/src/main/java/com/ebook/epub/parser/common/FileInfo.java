package com.ebook.epub.parser.common;

public class FileInfo {
	public String filePath;
	public long fileSize;
	
	public FileInfo(String path, long size) {
		filePath = path;
		fileSize = size;
	}
}
