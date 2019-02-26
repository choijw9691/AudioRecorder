package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.NamespaceURI;

/**
@class ParElement
@brief SMIL의 par element 정보 class  
 */
public class ParElement {
	
	private Element _node;
	
	public String id;
	public String type;
	
	public TextElement textNode;
	public AudioElement audioNode;
	
	/**
	@breif ParElement 생성자
	@param Par node
	@return 생성된 ParElement 객체
	 */
	public ParElement(Node node){
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }
		
		id = readIdAttribute();
		type = readTypeAttribute();
		
		textNode = readChildTextElement();
		audioNode = readChildAudioElement();
	}

	public String getId() {
		return id;
	}

	/**
	@breif Par element의 id 속성 값을 가져오는 method
	@return Par element의 id 속성 값 
	 */
	private String readIdAttribute() {
		
		String id = _node.getAttribute(AttributeName.ID);
		return id == null ? "" : id;
	}

	public String getType() {
		return type;
	}

	/**
	@breif Par element의 type 속성 값을 가져오는 method
	@return Par element의 type 속성 값
	 */
	private String readTypeAttribute() {
		
		Attr type = _node.getAttributeNodeNS(NamespaceURI.EPUB, AttributeName.TYPE);
		return  type == null ? "" :  type.getNodeValue();
	}

	public TextElement getTextElement() {
		return textNode;
	}

	/**
	@breif Par element의 하위 text 값을 가져오는 method
	@return Par element의 하위 text 값
	 */
	private TextElement readChildTextElement() {

		NodeList childNodes = _node.getElementsByTagName(ElementName.TEXT);
		if(childNodes.getLength()>0){
			return new TextElement(childNodes.item(0));
		}
		throw new NullPointerException();	// TODO :: temp exception 
	}

	public AudioElement getAudioElement() {
		return audioNode;
	}

	/**
	@breif Par element의 하위 audio 값을 가져오는 method
	@return Par element의 하위 audio 값
	 */
	private AudioElement readChildAudioElement() {
		
		NodeList childNodes = _node.getElementsByTagName(ElementName.AUDIO);
		if(childNodes.getLength()>0){
			return new AudioElement(childNodes.item(0));
		}
		return audioNode;
	}
	
	/**
	@breif 해당 이름의 child element 존재 유무 체크 
	@param child node 이름
	@return 해당 이름의 child element 존재 유무
	 */
	public boolean hasChildElement(String name){
		
		NodeList childNodes = _node.getChildNodes();
		for(int index=0; index<childNodes.getLength(); index++){
			if(childNodes.item(index).getNodeName().equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}
}
