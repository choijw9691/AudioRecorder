package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

/**
@class HeadElement
@brief SMIL의 head element 정보 class  
 */
public class HeadElement {

	private Element _node;
	
	public MetadataElement metaNode;
	
	/**
	@breif HeadElement 생성자
	@param Head node
	@return 생성된 HeadElement 객체
	 */
	public HeadElement(Node node){
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }
		
		metaNode = readChildMetaElement();
	}
	
	/**
	@breif Head element의 하위 metadata 값을 가져오는 method
	@return Head element의 하위 metadata 값
	 */
	private MetadataElement readChildMetaElement(){
		
		NodeList childNodes = _node.getElementsByTagName(ElementName.METADATA);
		if(childNodes.getLength()>0){
			return new MetadataElement(childNodes.item(0));			
		}
		return null;
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
