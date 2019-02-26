package com.ebook.epub.parser.common;

import java.util.Iterator;

import com.ebook.epub.parser.ops.XmlNavigationException;

public interface INavigation<T> {
	
	public Iterator<T> getTableOfContents() ;
	public String getTitle() ;
}
