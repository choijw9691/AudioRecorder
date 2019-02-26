package com.ebook.epub.parser.ocf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;

public class XmlRetrievalMethod {
	
	private String uri;
	private String type;
	
	public XmlRetrievalMethod(Node node){
		uri  = setUri(node);
		type = setType(node);
	}
	
	private String setUri(Node node){
		NamedNodeMap attr =  node.getAttributes();
		Node uri = attr.getNamedItem(AttributeName.URI);
        return uri==null ? "" : uri.getNodeValue();
	}
	
	public String getUri() {
		return uri;
	}
	
	private String setType(Node node){
		NamedNodeMap attr =  node.getAttributes();
		Node type = attr.getNamedItem(AttributeName.Type);
        return type==null ? "" : type.getNodeValue();
	}

	public String getType() {
		return type;
	}
}
