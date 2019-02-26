package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.DocumentVersions;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.TextToDocumentVersionConverter;

/**
@class SmilElement
@brief SMIL의 smil element 정보 class
 */
public class SmilElement {

	public Element _node; 

	public String version;
	public String id;
	public String prefix;

	public HeadElement headNode;
	public BodyElement bodyNode;

	/**
	@breif SmilElement 생성자
	@param Smil node
	@return 생성된 SmilElement 객체
	 */
	public SmilElement(Element node){

		_node = node;

		version = readVersionAttribute();
		id = readIdAttribute();
		prefix = readPrefixAttribute();

		headNode = readChildHeadElement();
		bodyNode = readChildBodyElement();
	}

	/**
	@breif Smil element의 version 속성 값을 가져오는 method
	@return Smil element의 version 속성 값
	 */
	private String readVersionAttribute(){
		
		String version = _node.getAttribute(AttributeName.VERSION);
		if(version == null || version.isEmpty())
			throw new IllegalArgumentException();	// TODO :: temp exception 
		return version;
	}

	public String getVersion(){
		return version;
	}                                                

	/**
	@breif Smil element의 id 속성 값을 가져오는 method
	@return Smil element의 id 속성 값
	 */
	private String readIdAttribute(){

		String id = _node.getAttribute(AttributeName.ID);
		return id==null ? "" : id;		// TODO :: "" <-define
	}

	public String getId() {
		return id;
	}

	/**
	@breif Smil element의 prefix 속성 값을 가져오는 method
	@return Smil element의 prefix 속성 값
	 */
	private String readPrefixAttribute(){

		String prefix = _node.getAttribute(AttributeName.PREFIX);
		return prefix==null ? "" : prefix;
	}


	public String getPrefix() {
		return prefix;
	}

	/**
	@breif Smil element의 하위 head element 값을 가져오는 method
	@return Smil element의 하위 head element 값
	 */
	private HeadElement readChildHeadElement() {

		NodeList childNodes = _node.getElementsByTagName(ElementName.HEAD);
		if(childNodes.getLength()>0){
			return new HeadElement(childNodes.item(0));			
		}
		return null;
	}

	public HeadElement getHeadElement() {
		return headNode;
	}

	/**
	@breif Smil element의 하위 body element 값을 가져오는 method
	@return Smil element의 하위 body element 값
	 */
	private BodyElement readChildBodyElement() {

		NodeList childNodes = _node.getElementsByTagName(ElementName.BODY);
		if(childNodes.getLength()>0){
			return new BodyElement(childNodes.item(0));			
		}
		throw new NullPointerException();	// TODO :: temp exception 
	}

	public BodyElement getBodyElement() {
		return bodyNode;
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

	public DocumentVersions getVersionType() {

		TextToDocumentVersionConverter converter = new TextToDocumentVersionConverter();
		return converter.convert(version);
	}
}
