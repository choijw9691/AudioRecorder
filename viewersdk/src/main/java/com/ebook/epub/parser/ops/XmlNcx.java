package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlNcx {

	private XmlDocTitle docTitle;
	private XmlNavMap navMap;
	private String version;
	
	public XmlNcx(Node node) throws XmlNavigationException {
		version = setVersion(node);
		navMap = setNavMap(node);
		docTitle = setDocTitle(node);
	}
	
	public XmlDocTitle getDocTitle() {
		return docTitle;
	}

	public XmlNavMap getNavMap() {
		return navMap;
	}

	public String getVersion() {
		return version;
	}

	private XmlDocTitle setDocTitle(Node node) throws XmlNavigationException {
		
		NodeList childnodes = node.getChildNodes();
		
		Node docTitleNode = null;
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.DOCTITLE) ){
				docTitleNode = child;
				break;
			}
		}
		
		return docTitleNode == null ? null : new XmlDocTitle(docTitleNode);
	}
	
	private XmlNavMap setNavMap(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.NAVMAP) )
				return new XmlNavMap(child);
		}
		
		throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.NAVMAP);
	}
	
	private String setVersion(Node node) {
		
		Node version = node.getAttributes().getNamedItem(AttributeName.VERSION);
		
		return version == null ? "" : version.getNodeValue();
	}
}
