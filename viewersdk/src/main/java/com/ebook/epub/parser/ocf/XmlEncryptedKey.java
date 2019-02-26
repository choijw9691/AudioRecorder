package com.ebook.epub.parser.ocf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;

public class XmlEncryptedKey {
	
	private String id;
	private XmlEncryptionMethod encryptionMethod;
	private XmlKeyInfo keyInfo;
	private XmlCipherData cipherData;
	
	public XmlEncryptedKey(Node node){
		
		id = setId(node);
		
		encryptionMethod = setEncryptionMethod(node);
		keyInfo = setKeyInfo(node);
		cipherData = setCipherData(node);
	}
	
	private String setId(Node node){
		NamedNodeMap attr =  node.getAttributes();
		Node id = attr.getNamedItem(AttributeName.Id);
		return  id == null ? "" :  id.getNodeValue();
	}
	
	public String getId(){
		return id;
	}
	
	private XmlEncryptionMethod setEncryptionMethod(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.ENCRYPTIONMETHOD) ){
				return new XmlEncryptionMethod(child);
			}
		}
		return null;
	}
	
	public XmlEncryptionMethod getEncryptionMethod() {
		return encryptionMethod;
	}
	
	private XmlKeyInfo setKeyInfo(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.KEYINFO) ){
				return new XmlKeyInfo(child);
			}
		}
		return null;
	} 
	
	public XmlKeyInfo getKeyInfo() {
		return keyInfo;
	}
	
	private XmlCipherData setCipherData(Node node){
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.CIPHERDATA) ){
				return new XmlCipherData(child);
			}
		}
		return null;
	}
	
	public XmlCipherData getCipherData() {
		return cipherData;
	}
}

