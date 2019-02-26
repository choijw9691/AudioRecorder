package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
@class MetadataElement
@brief SMIL content의 metadata element 정보 class
 */
public class MetadataElement {
	
	private Element _node; 
	
	/**
	@breif MetadataElement 생성자
	@param Metadata node
	@return 생성된 MetadataElement 객체
	 */
	public MetadataElement(Node node){
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }
	}
	
	/**
	@breif child element 존재 유무 체크 
	@return child element 존재 유무
	 */
	public boolean hasChildElement(){
		
		NodeList childNodes = _node.getChildNodes();
		if(childNodes.getLength()>0){
			return true;
		}
		return false;
	}
}
