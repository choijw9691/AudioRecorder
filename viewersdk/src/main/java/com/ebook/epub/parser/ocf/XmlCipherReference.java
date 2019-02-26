package com.ebook.epub.parser.ocf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;

import android.util.Log;

public class XmlCipherReference {

	private String uri;
	
	public XmlCipherReference(Node node){
		uri = setUri(node);
	}
	
	private String setUri(Node node){
		NamedNodeMap attr = node.getAttributes();
		Node uri = attr.getNamedItem(AttributeName.URI);
		return uri == null ? "" : uri.getNodeValue();
	}

	public String getUri() {
		return uri;
	}
}
