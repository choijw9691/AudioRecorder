package com.ebook.epub.parser.opf;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;

public class XmlReference {

	private String title;
	private String type;
	private String hRef;

	public XmlReference(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.REFERENCE);
		
		title = setTitle(node);
		type = setType(node);
		hRef = setHRef(node);
	}
	
	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}
	
	public String getHRef() {
		return hRef;
	}

	private String setTitle(Node node) throws XmlPackageException {
		
		Node title = node.getAttributes().getNamedItem(AttributeName.TITLE);
		
		return title == null ? "" :  title.getNodeValue();
	}
	
	private String setType(Node node) throws XmlPackageException {
		
		Node type = node.getAttributes().getNamedItem(AttributeName.TYPE);
		
		return type == null ? "" : type.getNodeValue();
	}
	
	private String setHRef(Node node) throws XmlPackageException {
		
		String hRefDecode="";
		
		Node hRef = node.getAttributes().getNamedItem(AttributeName.HREF);
		
		if( hRef == null || hRef.getNodeValue().equals("") )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.HREF);
		
		try {
			hRefDecode = EpubFileUtil.getURLDecode(hRef.getNodeValue());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		}
		
		return hRefDecode;
//		return hRef.getNodeValue();
	}
	
	
}
