package com.ebook.epub.parser.ops;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlNavPoint {
	
	private XmlContent content;
	private XmlNavLabel navLabel;
	private ArrayList<XmlNavPoint> navPoints;
	
	private String id;
	private String playOrder;

	public XmlNavPoint(Node node) throws XmlNavigationException  {
		
		id = setId(node);
		playOrder = setPlayOrder(node);
		
		content = setContent(node);
		navLabel = setNavLabel(node);
		navPoints = setNavPoints(node);
	}
	
	public XmlContent getContent() {
		return content;
	}

	public XmlNavLabel getNavLabel() {
		return navLabel;
	}

	public Iterator<XmlNavPoint> getNavPoints() {
		return navPoints.iterator();
	}

	public String getId() {
		return id;
	}

	public String getPlayOrder() {
		return playOrder;
	}

	private XmlContent setContent(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.CONTENT) )
				return new XmlContent(child);
		}
		
		throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.CONTENT);
	}
	
	private XmlNavLabel setNavLabel(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.NAVLABEL) )
				return new XmlNavLabel(child);
		}
		
		throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.NAVLABEL);
	}
	
	private ArrayList<XmlNavPoint> setNavPoints(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		ArrayList<XmlNavPoint> navPointList = new ArrayList<XmlNavPoint>();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.NAVPOINT) )
				navPointList.add(new XmlNavPoint(child));
		}
		
		return navPointList;
	}
	
	private String setId(Node node) {
		
		Node id = node.getAttributes().getNamedItem(AttributeName.ID);
		
		return id == null ? "" : id.getNodeValue();
	}
	
	private String setPlayOrder(Node node) {
		
		Node playOrder = node.getAttributes().getNamedItem(AttributeName.PLAYORDER);
		
		return playOrder == null ? "" : playOrder.getNodeValue();
	}
}
