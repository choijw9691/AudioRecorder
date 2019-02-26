package com.ebook.epub.parser.opf;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;

public class XmlItem {
	
	private String id;
	private String properties;
	private String mediaType;
	private String mediaOverlay;
	private String fallback;
	private String hRef;

	public XmlItem(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.ITEM);
		
		id = setId(node);
		hRef = setHRef(node);
		mediaType = setMediaType(node);
		properties = setProperties(node);
		mediaOverlay = setMediaOverlay(node);
		fallback = setFallback(node);
		
	}
	
	public String getId() {
		return id;
	}

	public String getProperties() {
		return properties;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getMediaOverlay() {
		return mediaOverlay;
	}

	public String getFallback() {
		return fallback;
	}
	
	public String getHRef() {
		return hRef;
	}

	private String setId(Node node) throws XmlPackageException {
		
		Node id = node.getAttributes().getNamedItem(AttributeName.ID);
		
		if( id == null || id.getNodeValue().equals("") )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ID);
		
		return id.getNodeValue();
	}

	private String setProperties(Node node) throws XmlPackageException {
		
		Node properties = node.getAttributes().getNamedItem(AttributeName.PROPERTIES);
		
		return properties == null ? "" :  properties.getNodeValue();
	}
	
	private String setMediaType(Node node) throws XmlPackageException {
		
		Node mediaType = node.getAttributes().getNamedItem(AttributeName.MEDIA_TYPE);
		
		if( mediaType == null /*|| mediaType.getNodeValue().equals("")*/ )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.MEDIA_TYPE);
		
		return mediaType.getNodeValue();
	}
	
	private String setMediaOverlay(Node node) throws XmlPackageException {
		
		Node mediaOverlay = node.getAttributes().getNamedItem(AttributeName.MEDIA_OVERLAY);
		
		return mediaOverlay == null ? "" :  mediaOverlay.getNodeValue();
	}
	
	private String setFallback(Node node) throws XmlPackageException {
		
		Node fallback = node.getAttributes().getNamedItem(AttributeName.FALLBACK);
		
		return fallback == null ? "" :  fallback.getNodeValue();
	}
	
	
	private String setHRef(Node node) throws XmlPackageException {
		
		String hRefDecode="";
		
		Node hRef = node.getAttributes().getNamedItem(AttributeName.HREF);
		
		if( hRef == null /*|| hRef.getNodeValue().equals("")*/ )
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
