/**
 * @file AudioElement.java 
 */
package com.ebook.epub.parser.mediaoverlays;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.TimeToMillisecondConverter;
import com.ebook.epub.parser.common.UriPath;

/**
@class AudioElement
@brief SMIL의 audio element 정보 class 
 */
public class AudioElement {
	
	private Element _node;
	
	public String id;
	public String src;
	public long clipBegin;
	public long clipEnd;
	
	/**
	@breif AudioElement 생성자
	@param Audio node
	@return 생성된 AudioElement 객체
	 */
	public AudioElement(Node node){
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }
		
		id = readIdAttribute(node);
		src = readSrcAttribute(node);
		clipBegin = readClipBeginAttribute(node);
		clipEnd = readClipEndAttribute(node);
	}

	public String getId() {
		return id;
	}

	/**
	@breif Audio element의 id 속성 값을 가져오는 method 
	@param Audio node
	@return Audio element의 id 속성 값 
	 */
	private String readIdAttribute(Node node) {
		
		String id = _node.getAttribute(AttributeName.ID);
		return id == null ? "" : id;
	}

	public String getSrc() {
		return src;
	}

	/**
	@breif Audio element의 src 속성 값을 가져오는 method 
	@param Audio node
	@return Audio element의 src 속성 값 
	 */
	private String readSrcAttribute(Node node) {
		
		String src = _node.getAttribute(AttributeName.SRC);
		if(src == null || src.isEmpty())	
			throw new NullPointerException();	// TODO :: temp exception 
		return src;
	}

	public long getClipBegin() {
		return clipBegin;
	}

	/**
	@breif Audio element의 clipBegin 속성 값을 가져오는 method 
	@param Audio node
	@return Audio element의 clipBegin 속성 값
	 */
	private long readClipBeginAttribute(Node node) {

		TimeToMillisecondConverter converter = new TimeToMillisecondConverter();

		Node clipBegin = node.getAttributes().getNamedItem(AttributeName.CLIPBEGIN);
		return clipBegin == null ? 0 : converter.convert(clipBegin.getNodeValue());
	}

	public long getClipEnd() {
		return clipEnd;
	}

	/**
	@breif Audio element의 clipEnd 속성 값을 가져오는 method 
	@param Audio node
	@return Audio element의 clipEnd 속성 값
	 */
	private long readClipEndAttribute(Node node) {
		
		TimeToMillisecondConverter converter = new TimeToMillisecondConverter();
		
		Node clipEnd = node.getAttributes().getNamedItem(AttributeName.CLIPEND);
		return  clipEnd == null ? 0 : converter.convert(clipEnd.getNodeValue());
	}
	
	public String getUriDirectoryName(){
		return UriPath.getUriDirectoryName(src);
	}
	
	public String getUriFileName(){
		return UriPath.getUriFileName(src);
	}
}
