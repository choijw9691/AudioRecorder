package com.ebook.epub.parser.opf;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlCollection {
	
	private String lang;
	private String dir;
	private String id;
	private String role;
	private ArrayList<XmlLink> links;
	private HashMap<String, XmlCollection> collections;
	private XmlMetadata metadata;
	
	public XmlCollection(Node node) throws XmlPackageException{
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.COLLECTION);
		
		lang = setLang(node);
		dir = setDir(node);
		id = setId(node);
		role = setRole(node);
		collections = setCollection(node);
		metadata = setMetadata(node);
		links = setLinks(node);
		
		validation();
	}

	public String getDir() {
		return dir;
	}

	private String setDir(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node dir = attr.getNamedItem(AttributeName.DIR);
		
		return dir == null ? "" : dir.getNodeValue();
	}

	public String getId() {
		return id;
	}

	private String setId(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
		Node id = attr.getNamedItem(AttributeName.ID);
		
		return id == null ? "" : id.getNodeValue();
	}

	public String getRole() {
		return role;
	}

	private String setRole(Node node) throws XmlPackageException{
		
		Node role = node.getAttributes().getNamedItem(AttributeName.ROLE);

		if(role==null || role.getNodeValue().equalsIgnoreCase(""))
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ROLE);

		return role.getNodeValue();
	}

	public ArrayList<XmlLink> getLinks() {
		return links;
	}

	private HashMap<String, XmlCollection> setCollection(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
		HashMap<String, XmlCollection> collection = new HashMap<String, XmlCollection>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			
			if(child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals( ElementName.COLLECTION )){
				
				if(child.getAttributes()==null)
					throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ROLE);

				Node role = child.getAttributes().getNamedItem(AttributeName.ROLE);

				if( role == null || role.getNodeValue().equals("") )
					throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ROLE);
				
				collection.put(role.getNodeValue(), new XmlCollection(child));
			}
		}
		return collection;
	}
	
	private ArrayList<XmlLink> setLinks(Node node) throws XmlPackageException{
		
		NodeList childNodes = node.getChildNodes();
		
		// TODO : comic 
		if( childNodes.getLength() <= 0 )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.LINK);
		
		ArrayList<XmlLink> linkList = new ArrayList<XmlLink>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.LINK) )
				linkList.add(new XmlLink(child));
		}
		
		return linkList;
	}
	
	public String getLang() {
		return lang;
	}
	
	private String setLang(Node node){
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			return "";
		
        Node lang = attr.getNamedItem(AttributeName.LANG);

        return lang == null ? "" : lang.getNodeValue();
	}
	
	public XmlMetadata getMetadata() {
		return metadata;
	}
	
	private XmlMetadata setMetadata(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.METADATA) )
				return new XmlMetadata(child);
		}
		return null;
	}
	
	private void validation(){
//		( collection [1 or more] or ( collection [0 or more], link [1 or more] ))
	}
}
