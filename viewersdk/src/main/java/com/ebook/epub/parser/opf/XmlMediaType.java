package com.ebook.epub.parser.opf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlMediaType {
	
	private String handler;
	private String mediaType;

	public XmlMediaType(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.MEDIATYPE);
		
		handler = setHandler(node);
		mediaType = setMediaType(node);
	}
	
	public String getHandler() {
		return handler;
	}

	public String getMediaType() {
		return mediaType;
	}

	private String setHandler(Node node) throws XmlPackageException {
		
		Node handler = node.getAttributes().getNamedItem(AttributeName.HANDLER);
		
		if( handler == null || handler.getNodeValue().equals("") )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.HANDLER);
		
		return handler.getNodeValue();
	}

	private String setMediaType(Node node) throws XmlPackageException {
		
		Node mediaType = node.getAttributes().getNamedItem(AttributeName.MEDIA_TYPE);
		
		if( mediaType == null || mediaType.getNodeValue().equals("") )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.MEDIA_TYPE);
		
		return mediaType.getNodeValue();
	}
	
}
