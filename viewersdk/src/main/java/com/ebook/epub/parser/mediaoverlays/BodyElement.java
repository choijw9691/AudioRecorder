package com.ebook.epub.parser.mediaoverlays;

import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.NamespaceURI;
import com.ebook.epub.parser.common.UriPath;

/**
@class BodyElement
@brief SMIL의 body element 정보 class
 */
public class BodyElement {
	
	private Element _node;
	
	public String id;
	public String textRef;
	public String type;
	
	public ArrayList<ParElement> parNodes;
	public ArrayList<SeqElement> seqNodes;
	
	/**
	@breif BodyElement 생성자
	@param Body node
	@return 생성된 BodyElement 객체
	 */
	public BodyElement(Node node){
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			_node = (Element)node;
	    }
		
		id = readIdAttribute();
		textRef = readTextRefAttribute();
		type = readTypeAttribute();
		
		parNodes = readChildParElements();
		seqNodes = readChildSeqElements();
	
		validation();
	}
	
	private void validation(){
		
		boolean isParExist = (parNodes.size()<=0);
		boolean isSeqExist = (seqNodes.size()<=0);
		if(isParExist && isSeqExist)
			throw new NullPointerException();	// TODO :: temp exception 
	}
	
	public String getId() {
		return id;
	}

	/**
	@breif Body element의 id 속성 값을 가져오는 method
	@return Body element의 id 속성 값
	 */
	private String readIdAttribute() {
		
		String id = _node.getAttribute(AttributeName.ID);
		return id == null ? "" : id;
	}

	public String getTextRef() {
		return textRef;
	}

	/**
	@breif Body element의 textRef 속성 값을 가져오는 method
	@return Body element의 textRef 속성 값
	 */
	private String readTextRefAttribute() {
		
		Attr textRef = _node.getAttributeNodeNS(NamespaceURI.EPUB, AttributeName.TEXTREF);
		return textRef == null ? "" : textRef.getNodeValue();
	}

	public String getType() {
		return type;
	}

	/**
	@breif Body element의 type 속성 값을 가져오는 method
	@return Body element의 type 속성 값
	 */
	private String readTypeAttribute() {
	
		Attr type = _node.getAttributeNodeNS(NamespaceURI.EPUB, AttributeName.TYPE);
		return type == null ? "" : type.getNodeValue();
	}

	public ArrayList<ParElement> getParElement() {
		return parNodes;
	}

	/**
	@breif Body element의 하위 par element 정보를 가져오는 method
	@return Body element의 하위 par element 정보
	 */
	private ArrayList<ParElement> readChildParElements() {
		
		ArrayList<ParElement> parNodes = new ArrayList<ParElement>();
		
//		NodeList childNodes = _node.getElementsByTagName()(ElementName.PAR);
		NodeList childNodes = _node.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			if(childNodes.item(index).getNodeName().equalsIgnoreCase(ElementName.PAR)){
				parNodes.add(new ParElement(childNodes.item(index)));
			}
			
		}
		return parNodes;
	}

	public ArrayList<SeqElement> getSeqElement() {
		return seqNodes;
	}

	/**
	@breif Body element의 하위 seq element 정보를 가져오는 method
	@return Body element의 하위 seq element 정보
	 */
	private ArrayList<SeqElement> readChildSeqElements() {
		
		ArrayList<SeqElement> seqNodes = new ArrayList<SeqElement>();
		
		NodeList childNodes = _node.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			if(childNodes.item(index).getNodeName().equalsIgnoreCase(ElementName.SEQ)){
				seqNodes.add(new SeqElement(childNodes.item(index)));
			}
		}
		return seqNodes;
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
	
	public String getUriDirectoryName(){
		return UriPath.getUriDirectoryName(textRef);
	}
	
	public String getUriFileName(){
		return UriPath.getUriFileName(textRef);
	}
	
	public String getUriFragment(){
		return UriPath.getUriFragmentValue(textRef);
	}
}
