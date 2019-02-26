package com.ebook.epub.parser.ocf;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlKeyInfo {

	private XmlKeyName keyName;
	private XmlRetrievalMethod retrievalMethod;
	
	public XmlKeyInfo(Node node){
		keyName = setKeyName(node);
		retrievalMethod = setRetrievalMethod(node);
	}
	
	private XmlKeyName setKeyName(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.KEYNAME) )
				return new XmlKeyName(child);
		}
		return null;
	}
	
	
	public XmlKeyName getKeyName() {
		return keyName;
	}

	private XmlRetrievalMethod setRetrievalMethod(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.RETRIEVALMETHOD) )
				return new XmlRetrievalMethod(child);
		}
		return null;
	}

	public XmlRetrievalMethod getRetrievalMethod() {
		return retrievalMethod;
	}
}
