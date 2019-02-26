package com.ebook.epub.parser.ocf;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlCipherData {
	
	private XmlCipherValue cipherValue;
	private XmlCipherReference cipherReference;
	
	public XmlCipherData(Node node){
		cipherValue = setCipherValue(node);
		cipherReference = setCipherReference(node);
	}
	
	private XmlCipherValue setCipherValue(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.CIPHERVALUE) ){
				return new XmlCipherValue(child);
			}
		}
		return null;
	}
	
	public XmlCipherValue getCipherValue() {
		return cipherValue;
	}
	
	private XmlCipherReference setCipherReference(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.CIPHERREFERENCE) ){
				return new XmlCipherReference(child);
			}
		}
		return null;
	}

	public XmlCipherReference getCipherReference() {
		return cipherReference;
	}
	
	
}
