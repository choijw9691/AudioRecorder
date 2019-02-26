package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlNav {
	
	private XmlH xmlH;
	private XmlOl xmlOl;
	private boolean hidden;

	public XmlNav(Node node) throws XmlNavigationException {
		xmlH = setH(node);
		xmlOl = setOl(node);
		hidden = setHidden(node);
	}
	
	public XmlH getH() {
		return xmlH;
	}

	public XmlOl getOl() {
		return xmlOl;
	}

	public boolean getHidden() {
		return hidden;
	}

	private XmlH setH(Node node) throws XmlNavigationException {
		
		NodeList childnodes = node.getChildNodes();
		
		Node olNode = null;
		Node hNode = null;
		Node previous = null;
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE ){ 
				if( child.getLocalName().equals(ElementName.OL) ){
					olNode = child;
					break;
				}
				previous = child; //olNode 바로 앞 node를 추출하기 위함.
			}
		}
		
		if( olNode != null ) {
			
			//h 요소가 존재할 경우 반드시 ol 요소 앞(형제)에 존재해야함.
			if( previous != null && previous.getNodeType() == Node.ELEMENT_NODE ){
				
				if( previous.getLocalName().equals(ElementName.H1) ||
					previous.getLocalName().equals(ElementName.H2) ||
					previous.getLocalName().equals(ElementName.H3) ||
					previous.getLocalName().equals(ElementName.H4) ||
					previous.getLocalName().equals(ElementName.H5) ||
					previous.getLocalName().equals(ElementName.H6))  {
					
					hNode = previous;
					
				}
			}
		}
		
		return hNode == null ? null : new XmlH(hNode);
	}
	
	private XmlOl setOl(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.OL) )
				return new XmlOl(child);
		}
		
		throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.OL);
	}
	
	private boolean setHidden(Node node) throws XmlNavigationException {
		
		Node hidden = node.getAttributes().getNamedItem(AttributeName.HIDDEN);
		
		return hidden != null;
	}
}
