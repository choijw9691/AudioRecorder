package com.ebook.epub.parser.opf;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.PackageVersion;
import com.ebook.epub.viewer.ViewerErrorInfo;

/**
@class XmlPackage 
@brief Package element 정보 class 
 */
public class XmlPackage {

	private String version;
	private String uniqueIdentifier;
	private String prefix;
	private String lang;
	private String id;
	private String dir;
	
	private XmlMetadata metadata;
	private XmlManifest manifest;
	private XmlSpine spine;
	private XmlGuide guide;
	private XmlBindings bindings;
	
	private HashMap<String,XmlCollection> collections;

	/**
	@breif XmlPackage 생성자
	@param Package element
	@return 생성된 XmlPackage 객체
	 */
	public XmlPackage(Element el) throws XmlPackageException {
		
		version = setVersion(el);
		uniqueIdentifier = setUniqueIdentifier(el);
		prefix = setPrefix(el);
		lang = setLang(el);
		id = setId(el);
		dir = setDir(el);
		
		metadata = setMetadata(el);
		manifest = setManifest(el);
		spine = setSpine(el);
		guide = setGuide(el);
		bindings = setBindings(el);
		
		collections = setCollections(el);
	}
	
	public String getVersion() {
		return version;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getLang() {
		return lang;
	}

	public String getId() {
		return id;
	}

	public String getDir() {
		return dir;
	}

	public XmlMetadata getMetadata() {
		return metadata;
	}

	public XmlManifest getManifest() {
		return manifest;
	}

	public XmlSpine getSpine() {
		return spine;
	}

	public XmlGuide getGuide() {
		return guide;
	}

	public XmlBindings getBindings() {
		return bindings;
	}

	public HashMap<String, XmlCollection> getCollections(){
		return collections;
	}
	
	/************************************ Package Attribute ****************************************/
	
	private String setVersion(Element el) throws XmlPackageException {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.VERSION);
		
        Node version = attr.getNamedItem(AttributeName.VERSION);
        
        if (version == null)
        	throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.VERSION);

        // version이 2.0, 3.0 이외의 값이면 2.0으로 봄
        if(!PackageVersion.EPUB2.equals(version.getNodeValue()) && !PackageVersion.EPUB3.equals(version.getNodeValue()))
        	return PackageVersion.EPUB2;
        else 
        	return version.getNodeValue();
    }
	
	private String setUniqueIdentifier(Element el) throws XmlPackageException {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.UNIQUE_IDENTIFIER);
		
        Node uniqueIdentifier = attr.getNamedItem(AttributeName.UNIQUE_IDENTIFIER);

        if (uniqueIdentifier == null)
        	throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.UNIQUE_IDENTIFIER);

        return uniqueIdentifier.getNodeValue();
    }
	
	private String setPrefix(Element el) {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			return "";
		
        Node prefix = attr.getNamedItem(AttributeName.PREFIX);

        return prefix == null ? "" : prefix.getNodeValue();
    }
	
	private String setLang(Element el) {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			return "";
		
        Node lang = attr.getNamedItem(AttributeName.LANG);

        return lang == null ? "" : lang.getNodeValue();
    }
	
	private String setId(Element el) {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			return "";
		
        Node id = attr.getNamedItem(AttributeName.ID);

        return id == null ? "" : id.getNodeValue();
    }

	private String setDir(Element el) {
	
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			return "";
		
		Node dir = attr.getNamedItem(AttributeName.DIR);

	    return dir == null ? "" : dir.getNodeValue();
	}
	
	private XmlMetadata setMetadata(Element el) throws XmlPackageException {
		
		Node metadata = el.getElementsByTagNameNS(el.getNamespaceURI(),ElementName.METADATA).item(0);
		
		if( metadata == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.METADATA);
		
		return new XmlMetadata(metadata);
		
	}
	
	private XmlManifest setManifest(Element el) throws XmlPackageException {
		
		Node manifest = el.getElementsByTagNameNS(el.getNamespaceURI(), ElementName.MANIFEST).item(0);
		
		if( manifest == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.MANIFEST);
		
		return new XmlManifest(manifest);
		
	}
	
	private XmlSpine setSpine(Element el) throws XmlPackageException {
		
		Node spine = el.getElementsByTagNameNS(el.getNamespaceURI(), ElementName.SPINE).item(0);
		
		if( spine == null )
			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.SPINE);
		
		return new XmlSpine(spine);
		
	}
	
	private XmlGuide setGuide(Element el) throws XmlPackageException {
		
		Node guide = el.getElementsByTagNameNS(el.getNamespaceURI(), ElementName.GUIDE).item(0);
		
		return guide == null ? null : new XmlGuide(guide);
		
	}
	
	private XmlBindings setBindings(Element el) throws XmlPackageException {
		
		Node bindings = el.getElementsByTagNameNS(el.getNamespaceURI(), ElementName.BINDINGS).item(0);
		
		return bindings == null ? null : new XmlBindings(bindings);
		
	}
	
	private HashMap<String,XmlCollection> setCollections(Element el) throws XmlPackageException {
		
		NodeList childNodes = el.getElementsByTagName(ElementName.COLLECTION);

		HashMap<String,XmlCollection> collections = new HashMap<String,XmlCollection>();
		
		for(int idx=0; idx<childNodes.getLength(); idx++){
			Node child = childNodes.item(idx);
			
			if(child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals( ElementName.COLLECTION )){
				
				if(child.getAttributes()==null)
					throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ROLE);
				
				Node role = child.getAttributes().getNamedItem(AttributeName.ROLE);
				
				if( role == null || role.getNodeValue().equals("") )
					throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.ROLE);
				
				collections.put(role.getNodeValue(), new XmlCollection(child));
			}
		}
		return collections;
	}
}
