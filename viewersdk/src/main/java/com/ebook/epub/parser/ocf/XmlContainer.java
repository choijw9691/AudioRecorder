package com.ebook.epub.parser.ocf;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlContainer {
	
	private XmlRootfiles xmlRootfiles;
	private String version;

	public XmlContainer(Element el) throws XmlContainerException {
		version = setVersion(el);
		xmlRootfiles = setRootFiles(el);
	}
	
	public XmlRootfiles getXmlRootfiles() {
		return xmlRootfiles;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private XmlRootfiles setRootFiles(Element el) throws XmlContainerException {
		
		NodeList rootfileNodes = el.getElementsByTagNameNS(el.getNamespaceURI(), ElementName.ROOTFILES);
		
		int size = rootfileNodes.getLength();
		if( size <= 0 )
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.ROOTFILES);
		
        return new XmlRootfiles(rootfileNodes.item(0));
	}
	
	private String setVersion(Element el) throws XmlContainerException {
		
		NamedNodeMap attr = el.getAttributes();
		
		if( attr == null )
			return "";
		
        Node version = attr.getNamedItem(AttributeName.VERSION);

        if (version == null)
        	return "";

        return version.getNodeValue();
    }
}
