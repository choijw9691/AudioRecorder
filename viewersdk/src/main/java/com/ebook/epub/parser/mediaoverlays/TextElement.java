package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.UriPath;

/**
@class TextElement
@brief SMIL의 text element 정보 class
 */
public class TextElement {

	private Element _node;

	public String id;
	public String src;

	/**
	@breif TextElement 생성자 
	@param Text node 
	@return 생성된 TextElement 객체
	 */
	public TextElement(Node node){

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }

		id = readIdAttribute();
		src = readSrcAttribute();
	}

	public String getId() {
		return id;
	}

	/**
	@breif Text element의 id 속성 값을 가져오는 method
	@return Text element의 id 속성 값
	 */
	private String readIdAttribute() {

		String id = _node.getAttribute(AttributeName.ID);
		return id == null ? "" : id;
	}

	public String getSrc() {
		return src;
	}

	/**
	@breif Text element의 src 속성 값을 가져오는 method
	@return Text element의 src속성 값
	 */
	private String readSrcAttribute() {

		String src = _node.getAttribute(AttributeName.SRC);
		if(src == null || src.isEmpty())
			throw new NullPointerException();	// TODO :: temp exception 
		return src;
	}

	public String getUriDirectoryName(){
		return UriPath.getUriDirectoryName(src);
	}
	
	public String getUriFileName(){
		return UriPath.getUriFileName(src);
	}
	
	public String getUriFilePath(){
		return UriPath.getUriFilePath(src);
	}
	
	public String getUriFragment(){
		return UriPath.getUriFragmentValue(src);
	}
}
