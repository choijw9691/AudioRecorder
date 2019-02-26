package com.ebook.epub.parser.ocf;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlEncryption {

	private String epubPath;
	
	public HashMap<String, XmlEncryptedKey> encryptedKey;
	public HashMap<String, XmlEncryptedData> encryptedData;
	
	public XmlEncryption(String epubPath, Element el){
		this.epubPath = epubPath;
		
		encryptedKey = setXmlEncryptedKey(el);
		encryptedData = setXmlEncryptedData(el);
	}
	
	private HashMap<String, XmlEncryptedKey> setXmlEncryptedKey(Element el){

		HashMap<String, XmlEncryptedKey> encryptedKey = new HashMap<String, XmlEncryptedKey>();
		
		NodeList childNodes = el.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			Node child = childNodes.item(index);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.ENCRYPTEDKEY) ){
				XmlEncryptedKey xmlEncryptedKey = new XmlEncryptedKey(child);
				if(xmlEncryptedKey!=null && !xmlEncryptedKey.getId().isEmpty()){
					encryptedKey.put(xmlEncryptedKey.getId(), xmlEncryptedKey);
				}
			}
		}
		return encryptedKey;
	}

	public HashMap<String, XmlEncryptedKey> getEncryptedKey() {
		return encryptedKey;
	}
	
	private HashMap<String, XmlEncryptedData> setXmlEncryptedData(Element el){
	
		HashMap<String, XmlEncryptedData> encryptedData = new HashMap<String, XmlEncryptedData>();
		
		NodeList childNodes = el.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			Node child = childNodes.item(index);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.ENCRYPTEDDATA) ){
				XmlEncryptedData xmlEncryptedData = new XmlEncryptedData(child);
				if(xmlEncryptedData!=null 
						&& xmlEncryptedData.getCipherData()!=null 
						&& xmlEncryptedData.getCipherData().getCipherReference()!=null 
						&& !xmlEncryptedData.getCipherData().getCipherReference().getUri().isEmpty())
				encryptedData.put(epubPath+"/"+xmlEncryptedData.getCipherData().getCipherReference().getUri(), xmlEncryptedData);
			}
		}
		return encryptedData;
	}

	public HashMap<String, XmlEncryptedData> getEncryptedData() {
		return encryptedData;
	}
}
