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
@class SeqElement
@brief SMIL의 seq element 정보 class
 */
public class SeqElement {

	private Element _node; 
	
	public String id;
	public String textRef;
	public String type;
	
	public ArrayList<ParElement> parNodes;
	public ArrayList<SeqElement> seqNodes;
	
	/**
	@breif SeqElement 생성자
	@param Seq node
	@return 생성된 SeqElement 객체
	 */
	public SeqElement(Node node){
		
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
	@breif Seq element의 id 속성 값을 가져오는 method
	@return Seq element의 id 속성 값
	 */
	private String readIdAttribute() {
		
		String id = _node.getAttribute(AttributeName.ID);
		return id == null ? "" : id;
	}

	public String getTextRef() {
		return textRef;
	}

	/**
	@breif Seq element의 textRef 속성 값을 가져오는 method
	@return Seq element의 textRef 속성 값
	 */
	private String readTextRefAttribute() {

		Attr textRef = _node.getAttributeNodeNS(NamespaceURI.EPUB, AttributeName.TEXTREF);
		if( textRef == null || textRef.getNodeValue().isEmpty())
			throw new NullPointerException();	// TODO :: temp exception 
		return textRef.getNodeValue();
	}

	public String getType() {
		return type;
	}

	/**
	@breif Seq element의 type 속성 값을 가져오는 method
	@return Seq element의 type 속성 값
	 */
	private String readTypeAttribute() {
		
		Attr type = _node.getAttributeNodeNS(NamespaceURI.EPUB, AttributeName.TYPE);
		return type == null ? "" : type.getNodeValue();
	}

	public ArrayList<ParElement> getParElements() {
		return parNodes;
	}

	/**
	@breif Seq element의 하위 par 값을 가져오는 method
	@return Seq element의 하위 par 값
	 */
	private ArrayList<ParElement> readChildParElements() {
		
		ArrayList<ParElement> parNodes = new ArrayList<ParElement>();
		
		NodeList childNodes = _node.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			if(childNodes.item(index).getNodeName().equalsIgnoreCase(ElementName.PAR)){
				parNodes.add(new ParElement(childNodes.item(index)));
			}
			
		}
		return parNodes;
	}

	public ArrayList<SeqElement> getSeqElements() {
		return seqNodes;
	}

	/**
	@breif Seq element의 하위 seq 값을 가져오는 method
	@return Seq element의 하위 seq 값
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
