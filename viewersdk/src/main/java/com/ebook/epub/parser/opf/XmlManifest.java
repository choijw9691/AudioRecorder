package com.ebook.epub.parser.opf;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlManifest {
	
	private String id;
	private LinkedHashMap<String, XmlItem> items;

	public XmlManifest(Node node) throws XmlPackageException {
		
		id = setId(node);
		items = setItems(node);
	}
	
	public String getId() {
		return id;
	}

	public LinkedHashMap<String, XmlItem> getItems() {
		return items;
	}

	private String setId(Node node)  {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node id = attr.getNamedItem(AttributeName.ID);
		
		return id == null ? "" : id.getNodeValue();
	}
	
	private LinkedHashMap<String, XmlItem> setItems(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
		if( childNodes.getLength() <= 0 )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.ITEM);
		
		LinkedHashMap<String, XmlItem> itemMap = new LinkedHashMap<String, XmlItem>();
		
		ArrayList<XmlItem> itemList = new ArrayList<XmlItem>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			
			Node child = childNodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.ITEM) ){
				XmlItem item = new XmlItem(child);
				itemList.add(item);
				itemMap.put(item.getId(), item);
			}
		}
		
		return itemMap;
	}
	
}
