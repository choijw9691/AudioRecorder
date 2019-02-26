package com.ebook.epub.parser.opf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlItemRef {
	
	private String id;
	private String idRef;
	private String properties;
	private String linear;

	public XmlItemRef(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.ITEMREF);
		
		idRef = setIdRef(node);
		properties = setProperties(node);
		linear = setLinear(node);
		id = setId(node);
	}
	
	public String getId() {
		return id;
	}

	public String getIdRef() {
		return idRef;
	}

	public String getProperties() {
		return properties;
	}

	public String getLinear() {
		return linear;
	}

	private String setId(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node id = attr.getNamedItem(AttributeName.ID);
		
		return id == null ? "" : id.getNodeValue();
	}
	
	private String setIdRef(Node node) throws XmlPackageException {
		
		Node idRef = node.getAttributes().getNamedItem(AttributeName.IDREF);
		
		if( idRef == null /*|| idRef.getNodeValue().equals("")*/ )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.IDREF);
		
		return idRef.getNodeValue();
	}
	
	private String setProperties(Node node) throws XmlPackageException {
		
		Node properties = node.getAttributes().getNamedItem(AttributeName.PROPERTIES);
		
		return properties == null ? "" :  properties.getNodeValue();
	}
	

	private String setLinear(Node node) throws XmlPackageException {
		
		Node linear = node.getAttributes().getNamedItem(AttributeName.LINEAR);
		
		return linear == null ? "" :  linear.getNodeValue();
	}
	
}
