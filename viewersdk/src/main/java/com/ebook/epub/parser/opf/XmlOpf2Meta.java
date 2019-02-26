package com.ebook.epub.parser.opf;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlOpf2Meta {

	private String name;
	private String content;

	public XmlOpf2Meta(Node node) throws XmlPackageException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.META);
		
		name = setName(node);
		content = setContent(node);
	}
	
	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	private String setName(Node node) {
		
		Node name = node.getAttributes().getNamedItem(AttributeName.NAME);
		
		return name == null ? "" :  name.getNodeValue();
	}
	
	private String setContent(Node node) throws XmlPackageException {
		
		Node name = node.getAttributes().getNamedItem(AttributeName.NAME);
		Node httpEquiv = node.getAttributes().getNamedItem(AttributeName.HTTP_EQUIV);
		Node content = node.getAttributes().getNamedItem(AttributeName.CONTENT);
		
		if( name != null || httpEquiv != null ) {
			if( content == null )
				throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.CONTENT);
		}
		
		return content == null ? "" : content.getNodeValue();
	}

	public String getValue() {
		// Opf2 Meta요소는 요소의 값이 없으나 
        // 구현의 통일성을 위해 IElementValue 인터페이스를 구현합니다.
		return "";
	}
	
}
