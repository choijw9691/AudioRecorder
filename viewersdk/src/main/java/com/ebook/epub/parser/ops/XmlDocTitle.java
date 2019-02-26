package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlDocTitle {
	private XmlText text;
	
	public XmlDocTitle(Node node) throws XmlNavigationException  {
		text = setText(node);
	}
	
	public XmlText getText() {
		return text;
	}

	private XmlText setText(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.TEXT) )
				return new XmlText(child);
		}
		
//		throw new XmlNavigationException("text"); //필수아님
		return null;
	}
}
