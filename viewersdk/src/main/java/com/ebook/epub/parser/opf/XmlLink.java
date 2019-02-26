package com.ebook.epub.parser.opf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlLink {
	
	private String id;
	private String rel;
	private String hRef;
	private String mediaType;
	private String refines;

	public XmlLink(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.LINK);
		
		rel = setRel(node);
		hRef = setHRef(node);
		id = setId(node);
		mediaType = setMediaType(node);
		refines = setRefines(node);
	}
	
	public String getId() {
		return id;
	}

	public String getRel() {
		return rel;
	}

	public String gethRef() {
		return hRef;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getRefines() {
		return refines;
	}

	private String setId(Node node) {
		
		Node id = node.getAttributes().getNamedItem(AttributeName.ID);
		
		return id == null ? "" :  id.getNodeValue();
	}
	
	private String setMediaType(Node node) {
		
		Node mediaType = node.getAttributes().getNamedItem(AttributeName.MEDIA_TYPE);
		
		return mediaType == null ? "" :  mediaType.getNodeValue();
	}
	
	private String setRefines(Node node) {
		
		Node refines = node.getAttributes().getNamedItem(AttributeName.REFINES);
		
		return refines == null ? "" : refines.getNodeValue();
	}
	
	private String setRel(Node node) throws XmlPackageException {
		
		Node rel = node.getAttributes().getNamedItem(AttributeName.REL);
		
//		if( rel == null || rel.getNodeValue().equals("") )
//			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.REL);
//		return rel.getNodeValue();
		return rel == null ? "" :  rel.getNodeValue();
	}
	
	private String setHRef(Node node) throws XmlPackageException {
		
		Node hRef = node.getAttributes().getNamedItem(AttributeName.HREF);
		
		if( hRef == null || hRef.getNodeValue().equals("") )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.HREF);
		
		return hRef.getNodeValue();
	}
}
