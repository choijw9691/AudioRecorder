package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlHtml {

	private XmlBody body;
	
	public XmlHtml(Node node) throws XmlNavigationException {
		body = setBody(node);
	}
	
	public XmlBody getBody() {
		return body;
	}

	private XmlBody setBody(Node node) throws XmlNavigationException {
		
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.BODY) )
				return new XmlBody(child);
		}
		
		throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.BODY);
	}
}
