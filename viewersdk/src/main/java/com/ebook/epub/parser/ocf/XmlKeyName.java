package com.ebook.epub.parser.ocf;

import org.w3c.dom.Node;

import android.util.Log;

public class XmlKeyName {
	
	private String value;
	
	public XmlKeyName(Node node){
		value = setValue(node);
	}
	
	private String setValue(Node node){
		return node.getTextContent() == null ? "" : node.getTextContent();
	}
}
