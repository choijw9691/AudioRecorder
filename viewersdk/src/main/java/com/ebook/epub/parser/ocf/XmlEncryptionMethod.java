package com.ebook.epub.parser.ocf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;

public class XmlEncryptionMethod {

	private String algorithm;
	
	public XmlEncryptionMethod(Node node){
		algorithm = setAlgorithm(node);
	}
	
	private String setAlgorithm(Node node){
		NamedNodeMap attr =  node.getAttributes();
		Node algorithm = attr.getNamedItem(AttributeName.ALGORITHM);
        return algorithm==null ? "" : algorithm.getNodeValue();
	}

	public String getAlgorithm() {
		return algorithm;
	}
}
