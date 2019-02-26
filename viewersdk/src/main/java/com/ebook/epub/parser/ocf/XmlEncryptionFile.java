package com.ebook.epub.parser.ocf;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlEncryptionFile {

	public XmlEncryption xmlEncryption;
	
	public XmlEncryptionFile(String epubPath, String encryptionFilePath){
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder documentbuilder = factory.newDocumentBuilder();
			Element element = documentbuilder.parse(new File(encryptionFilePath)).getDocumentElement();
			
			xmlEncryption = new XmlEncryption(epubPath, element);
		} 
        catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public XmlEncryption getXmlEncryption() {
		return xmlEncryption;
	}
}
