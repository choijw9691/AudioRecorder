package com.ebook.epub.parser.opf;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlBindings {
	
	private ArrayList<XmlMediaType> mediaTypes;

	public XmlBindings(Node node) throws XmlPackageException {
		
		mediaTypes = setMediaType(node);
	}
	
	public ArrayList<XmlMediaType> getMediaTypes() {
		return mediaTypes;
	}

	private ArrayList<XmlMediaType> setMediaType(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
//		if( childNodes.getLength() <= 0 )
//			throw new XmlPackageException("mediaTypes"); //필수 아님
		
		ArrayList<XmlMediaType> mediaTypeList = new ArrayList<XmlMediaType>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.MEDIATYPE) )
				mediaTypeList.add(new XmlMediaType(child));
		}
		
		return mediaTypeList;
	}
	
}
