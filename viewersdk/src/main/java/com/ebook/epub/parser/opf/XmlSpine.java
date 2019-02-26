package com.ebook.epub.parser.opf;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlSpine {

	private String id;
	private String toc;
	private String pageProgressionDirection;
	private ArrayList<XmlItemRef> itemRefs;
	
	public XmlSpine(Node node) throws XmlPackageException {
		
		id = setId(node);
		toc = setToc(node);
		pageProgressionDirection = setPageProgressionDirection(node);
		itemRefs = setItemRefs(node);
	}
	
	public String getId() {
		return id;
	}

	public String getToc() {
		return toc;
	}

	public String getPageProgressionDirection() {
		return pageProgressionDirection;
	}

	public ArrayList<XmlItemRef> getItemRefs() {
		return itemRefs;
	}

	private String setId(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node id = attr.getNamedItem(AttributeName.ID);
		
		return id == null ? "" : id.getNodeValue();
	}
	
	private String setToc(Node node)  {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node toc = attr.getNamedItem(AttributeName.TOC);
		
		return toc == null ? "" : toc.getNodeValue();
	}
	
	private String setPageProgressionDirection(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node pageProgressionDirection = attr.getNamedItem(AttributeName.PAGE_PROGRESSION_DIRECTION);
		
		return pageProgressionDirection == null ? "" : pageProgressionDirection.getNodeValue();
	}
	
	/**
	 * @return
	 * @throws XmlPackageException
	 */
	private ArrayList<XmlItemRef> setItemRefs(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
		if( childNodes.getLength() <= 0 )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.ITEMREF);
		
		ArrayList<XmlItemRef> itemRefList = new ArrayList<XmlItemRef>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.ITEMREF) )
				itemRefList.add(new XmlItemRef(child));
		}
		
		return itemRefList;
	}
	
}
