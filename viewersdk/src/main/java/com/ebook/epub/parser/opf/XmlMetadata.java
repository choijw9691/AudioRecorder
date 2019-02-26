package com.ebook.epub.parser.opf;

import com.ebook.epub.parser.common.ElementName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class XmlMetadata {

	private ArrayList<XmlMeta> metas;
	
	private ArrayList<XmlDCMES> drms;
	private ArrayList<XmlDCMES> titles;
	private ArrayList<XmlDCMES> creators;
	private ArrayList<XmlDCMES> contributors;
	private ArrayList<XmlDCMES> Dates;
	private ArrayList<XmlDCMES> coverages;
	private ArrayList<XmlDCMES> descriptions;
	private ArrayList<XmlDCMES> formats;
	private ArrayList<XmlDCMES> identifiers;
	private ArrayList<XmlDCMES> languages;
	private ArrayList<XmlDCMES> links;
	private ArrayList<XmlDCMES> publishers;
	private ArrayList<XmlDCMES> relations;
	private ArrayList<XmlDCMES> rightses;
	private ArrayList<XmlDCMES> types;
	private ArrayList<XmlDCMES> sources;
	private ArrayList<XmlDCMES> subjects;
	
	
	public XmlMetadata(Node node) throws XmlPackageException {
		
		metas = setMetas(node);
		
		drms = setDrms(node);
		titles = setTitles(node);
		creators = setCreators(node);
		contributors = setContributors(node);
		Dates = setDates(node);
		coverages = setCoverages(node);
		descriptions = setDescriptions(node);
		formats = setFormats(node);
		identifiers = setIdentifiers(node);
		languages = setLanguages(node);
		links = setLinks(node);
		publishers = setPublishers(node);
		relations = setRelations(node);
		rightses = setRightses(node);
		types = setTypes(node);
		sources = setSources(node);
		subjects = setSubjects(node);
	}
	
	public ArrayList<XmlMeta> getMetas() {
		return metas;
	}

	public ArrayList<XmlDCMES> getDrms() {
		return drms;
	}

	public ArrayList<XmlDCMES> getTitles() {
		return titles;
	}

	public ArrayList<XmlDCMES> getCreators() {
		return creators;
	}

	public ArrayList<XmlDCMES> getContributors() {
		return contributors;
	}

	public ArrayList<XmlDCMES> getDates() {
		return Dates;
	}

	public ArrayList<XmlDCMES> getCoverages() {
		return coverages;
	}

	public ArrayList<XmlDCMES> getDescriptions() {
		return descriptions;
	}

	public ArrayList<XmlDCMES> getFormats() {
		return formats;
	}

	public ArrayList<XmlDCMES> getIdentifiers() {
		return identifiers;
	}

	public ArrayList<XmlDCMES> getLanguages() {
		return languages;
	}

	public ArrayList<XmlDCMES> getLinks() {
		return links;
	}

	public ArrayList<XmlDCMES> getPublishers() {
		return publishers;
	}

	public ArrayList<XmlDCMES> getRelations() {
		return relations;
	}

	public ArrayList<XmlDCMES> getRightses() {
		return rightses;
	}

	public ArrayList<XmlDCMES> getTypes() {
		return types;
	}

	public ArrayList<XmlDCMES> getSources() {
		return sources;
	}

	public ArrayList<XmlDCMES> getSubjects() {
		return subjects;
	}

//	/**
//	 * EPUB 2 Meta ����Ʈ�� �����Ѵ�.
//	 */
//	private ArrayList<XmlOpf2Meta> setOpf2Meta(Node node) throws XmlPackageException {
//		NodeList childNodes = node.getChildNodes();
//		ArrayList<XmlOpf2Meta> opf2MetaList = new ArrayList<XmlOpf2Meta>();
//		
//		for (int i = 0; i < childNodes.getLength(); i++) {
//			Node child = childNodes.item(i);
//			
//			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals( ElementName.META ) 
//					&& child.hasAttributes() && child.getAttributes().getNamedItem(AttributeName.PROPERTY) == null ) {
//				
//				opf2MetaList.add(new XmlOpf2Meta(child));
//			}
//		}
//		
//		return opf2MetaList;
//	}
//	
	/**
	 * Meta ����Ʈ�� �����Ѵ�.
	 */
    private ArrayList<XmlMeta> setMetas(Node node) throws XmlPackageException {
        NodeList childNodes = node.getChildNodes();
        ArrayList<XmlMeta> metaList = new ArrayList<XmlMeta>();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if( child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals( ElementName.META )
                    && child.hasAttributes()) {
//					&& child.getAttributes().getNamedItem(AttributeName.PROPERTY) != null ) {
                metaList.add(new XmlMeta(child));
            }
        }

//		if( metaList.size() <= 0 )
//			throw new XmlPackageException("meta");

        return metaList;
    }
	
	/**
	 * TagName�� �ش� name�� �׸��� ����Ʈ�� �����Ѵ�. 
	 */
	private ArrayList<XmlDCMES> getXmlDublinCoreByTagName(Node node, String name) {
		
		NodeList childNodes = node.getChildNodes();
		ArrayList<XmlDCMES> dublincoreList = new ArrayList<XmlDCMES>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			
			Node child = childNodes.item(i);
			
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals( name ) )
				dublincoreList.add(new XmlDCMES(child));
		}
		
		return dublincoreList;
	}
	
	private ArrayList<XmlDCMES> setTitles(Node node) throws XmlPackageException {
		
//		ArrayList<XmlDCMES> titles = getXmlDublinCoreByTagName(node, ElementName.TITLE);
		
//		if( titles.size() <= 0 )
//			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.TITLE);
//		
//		return titles;
		return getXmlDublinCoreByTagName(node, ElementName.TITLE);
	}
	
	private ArrayList<XmlDCMES> setDrms(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.DRM);
	}
	
	private ArrayList<XmlDCMES> setCreators(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.CREATOR);
	}
	
	private ArrayList<XmlDCMES> setContributors(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.CONTRIBUTOR);
	}
	
	private ArrayList<XmlDCMES> setDates(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.DATE);
	}
	
	private ArrayList<XmlDCMES> setCoverages(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.COVERAGE);
	}

	private ArrayList<XmlDCMES> setDescriptions(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.DESCRIPTION);
	}

	private ArrayList<XmlDCMES> setFormats(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.FORMAT);
	}
	
	private ArrayList<XmlDCMES> setIdentifiers(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.IDENTIFIER);
	}

	private ArrayList<XmlDCMES> setLanguages(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.LANGUAGE);
	}
	
	private ArrayList<XmlDCMES> setLinks(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.LINK);
	}
	
	private ArrayList<XmlDCMES> setPublishers(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.PUBLISHER);
	}
	
	private ArrayList<XmlDCMES> setRelations(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.RELATION);
	}
	
	private ArrayList<XmlDCMES> setRightses(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.RIGHTS);
	}
	
	private ArrayList<XmlDCMES> setTypes(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.TYPE);
	}
	private ArrayList<XmlDCMES> setSources(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.SOURCE);
	}
	
	private ArrayList<XmlDCMES> setSubjects(Node node) {
		
		return getXmlDublinCoreByTagName(node, ElementName.SUBJECT);
	}
}
