package com.ebook.epub.parser.ocf;

import org.w3c.dom.Node;

public class XmlCipherValue {

	private String value;
	
	public XmlCipherValue(Node node){
		value = setValue(node);
	}
	
	private String setValue(Node node){
		return node.getTextContent() == null ? "" : node.getTextContent();
	}

	public String getValue() {
		return value;
	}
}
