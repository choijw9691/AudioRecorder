package com.ebook.epub.parser.ops;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlNavMap {
	
	private ArrayList<XmlNavPoint> navPoints;

	public XmlNavMap(Node node) throws XmlNavigationException {
		navPoints = setNavPoints(node);
	}
	
	public ArrayList<XmlNavPoint> getNavPoints() {
		return navPoints;
	}

	private ArrayList<XmlNavPoint> setNavPoints(Node node) throws XmlNavigationException {
		
		NodeList childnodes = node.getChildNodes();
		ArrayList<XmlNavPoint> liList = new ArrayList<XmlNavPoint>();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.NAVPOINT) )
				liList.add(new XmlNavPoint(child));
		}
		
		if( liList.size() <= 0 )
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.NAVPOINT);
		
		return liList;
		
	}
}
