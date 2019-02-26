package com.ebook.epub.parser.ops;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlOl {
	
	private ArrayList<XmlLi> lis;
	private boolean hidden;

	public XmlOl(Node node) throws XmlNavigationException {
		hidden = setHidden(node);
		lis = setLis(node);
	}
	
	public ArrayList<XmlLi> getLis() {
		return lis;
	}

	public boolean getHidden() {
		return hidden;
	}

	private ArrayList<XmlLi> setLis(Node node) throws XmlNavigationException {
		
		NodeList childnodes = node.getChildNodes();
		ArrayList<XmlLi> liList = new ArrayList<XmlLi>();
		
		for (int i = 0; i < childnodes.getLength(); i++) {
			Node child = childnodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.LI) ){
				XmlLi li = new XmlLi(child);
				
				if( li.getA() == null && li.getSpan() == null ){
					continue;
				}
				
				liList.add(li);
			}
		}
		
		if( liList.size() <= 0 )
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.LI);
		
		return liList;
		
	}
	
	private boolean setHidden(Node node) {
		
		Node hidden = node.getAttributes().getNamedItem(AttributeName.HIDDEN);
		
		return hidden != null;
	}
}
