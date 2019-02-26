package com.ebook.epub.parser.ops;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;

public class XmlLi {
	
	private XmlA a;
	private XmlOl ol;
	private XmlSpan span;
	private boolean hidden;

	public XmlLi(Node node) throws XmlNavigationException  {
		
//		checkLiElement(node);
		
		a = setA(node);
		ol = setOl(node);
		span = setSpan(node);
		hidden = setHidden(node);
	}
	
	public XmlA getA() {
		return a;
	}

	public XmlOl getOl() {
		return ol;
	}

	public XmlSpan getSpan() {
		return span;
	}

	public boolean getHidden() {
		return hidden;
	}

	private XmlA setA(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.A) )
				return new XmlA(child);
		}
		
		return null;
	}
	
	private XmlOl setOl(Node node) throws XmlNavigationException {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.OL) )
				return new XmlOl(child);
		}
		
		return null;
	}
	
	private XmlSpan setSpan(Node node) {
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.SPAN) )
				return new XmlSpan(child);
		}
		
		return null;
	}
	
	private boolean setHidden(Node node) {
		
		Node hidden = node.getAttributes().getNamedItem(AttributeName.HIDDEN);
		
		return hidden != null;
	}
	
	private boolean checkLiElement(Node node) throws XmlNavigationException {
		
		
		NodeList childnodes = node.getChildNodes();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.A) )
				return true;
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.SPAN) )
				return true;
		}
		
		throw new XmlNavigationException("li no child");
	}
}
