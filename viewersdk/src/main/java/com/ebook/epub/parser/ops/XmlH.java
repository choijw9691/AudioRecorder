package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;

public class XmlH {
	
	private String value;

	public XmlH(Node node) {
		value = setValue(node);
	}
	
	public String getValue() {
		return value;
	}

	private String setValue(Node node) {
		
		boolean isEmpty = node.getTextContent() == null /*|| node.getTextContent().equals("")*/;
		
		return  isEmpty ? "" : node.getTextContent();
	}
}
