package com.ebook.epub.parser.mediaoverlays;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ebook.epub.parser.common.ElementName;


/**
@class SmilDocumentReader
@brief SMIL 전체에 대한 정보 class
 */
public class SmilDocumentReader {

	private SmilElement _root; 

	/**
	@breif SmilDocumentReader 생성자
	@param Smil file path
	@return 생성된 SmilDocumentReader 객체
	 */
	public SmilDocumentReader(String path){
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder documentbuilder = factory.newDocumentBuilder();
			Element element = documentbuilder.parse(new File(path)).getDocumentElement();
			
			_root = new SmilElement(element);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	@breif SMIL의 전체 par 값을 가져오는 method
	@return SMIL의 전체 par 값 
	 */
	public ArrayList<ParElement> getParNodes(){
		
		ArrayList<ParElement> parNodes = new ArrayList<ParElement>();
		
		BodyElement bodyNode =_root.getBodyElement();
		if(bodyNode.hasChildElement(ElementName.PAR)){
			for(int idx=0; idx<bodyNode.getParElement().size(); idx++){
				parNodes.add(bodyNode.getParElement().get(idx));
			}
		}
		
		if(bodyNode.hasChildElement(ElementName.SEQ)){
			for(int idx=0; idx<bodyNode.getSeqElement().size(); idx++){
				getChildParNodes(bodyNode.getSeqElement().get(idx), parNodes);
			}
		}
		return parNodes;
	}
	
	/**
	@breif SMIL의 par 값을 가져오는 method
	@param par 값을 가져올 부모 seq element와 부모 par 리스트 
	@return 해당 seq element 및 par 리스트의 하위 par 정보
	 */
	private void getChildParNodes(SeqElement seqNode, ArrayList<ParElement> parNodes){
		
		if(seqNode.hasChildElement(ElementName.PAR)){
			for(int idx=0; idx<seqNode.getParElements().size(); idx++){
				parNodes.add(seqNode.getParElements().get(idx));
			}
		}
		
		if(seqNode.hasChildElement(ElementName.SEQ)){
			for(int idx=0; idx<seqNode.getSeqElements().size(); idx++){
				getChildParNodes(seqNode.getSeqElements().get(idx), parNodes);
			}
		}
	}
	
	
	public ArrayList<SeqElement> getSeqNodes(){
		
		ArrayList<SeqElement> seqNodes = new ArrayList<SeqElement>();
		
		BodyElement bodyNode = _root.getBodyElement();
		if(bodyNode.hasChildElement(ElementName.SEQ)){
			for(int idx=0; idx<bodyNode.getSeqElement().size(); idx++){
				seqNodes.add(bodyNode.getSeqElement().get(idx));
				if(bodyNode.getSeqElement().get(idx).hasChildElement(ElementName.SEQ)){
					getChildSeqNodes(bodyNode.getSeqElement().get(idx), seqNodes);
				}
			}
		}
		return seqNodes;
	}
	
	private void getChildSeqNodes(SeqElement seqNode, ArrayList<SeqElement> seqNodes){
		
		if(seqNode.hasChildElement(ElementName.SEQ)){
			for(int idx=0; idx<seqNode.getSeqElements().size(); idx++){
				seqNodes.add(seqNode.getSeqElements().get(idx));
				getChildSeqNodes(seqNode.getSeqElements().get(idx), seqNodes);
			}
		}
	}
	
	public String getVersion(){
		return _root.getVersion();
	}
	
	public boolean hasAudioNode(ParElement parNode){

		if(parNode.hasChildElement(ElementName.AUDIO)){
			return true;
		}
		return false;
	}
	
	public boolean hasAudioNode(ArrayList<ParElement> parNodes){

		for(int idx=0; idx<parNodes.size(); idx++){
			if(parNodes.get(idx).hasChildElement(ElementName.AUDIO)){
				return true;
			}
		}
		return false;
	}
	
	public AudioElement findAudioNodeByParNode(ParElement node){
		return node.getAudioElement();
		
	}
	
	public ParElement findParNode(ArrayList<ParElement> parNodes, String chapterFile, String fragment){
		
		for(int idx=0; idx<parNodes.size(); idx++){
			if(parNodes.get(idx).getTextElement().getSrc().indexOf(chapterFile+"#"+fragment)!=-1){
				return parNodes.get(idx);
			}
		}
		return null;
	}
	
	public SeqElement findSeqNode(ArrayList<SeqElement> seqNodes, String chapterFile, String fragment){
		
		for(int idx=0; idx<seqNodes.size(); idx++){
			if(seqNodes.get(idx).getTextRef().indexOf(chapterFile+"#"+fragment)!=-1){
				return seqNodes.get(idx);
			}
		}
		return null;
	}
	
}
