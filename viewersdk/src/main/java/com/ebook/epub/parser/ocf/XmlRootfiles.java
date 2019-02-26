package com.ebook.epub.parser.ocf;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlRootfiles {

	private ArrayList<XmlRootfile> xmlRootfile;

	public XmlRootfiles(Node node) throws XmlContainerException {
		xmlRootfile = setRootFiles(node);
	}
	
	public Iterator<XmlRootfile> getXmlRootfiles() {
		return xmlRootfile.iterator();
	}

	private ArrayList<XmlRootfile> setRootFiles(Node node) throws XmlContainerException {
		
		NodeList rootfileNodes = node.getChildNodes();
		ArrayList<XmlRootfile> rootfiles = new ArrayList<XmlRootfile>();
		
		for (int i = 0; i < rootfileNodes.getLength(); i++) {
			
			Node child = rootfileNodes.item(i);
			
			if( child.getNodeName().equals(ElementName.ROOTFILE) ){
				rootfiles.add(new XmlRootfile(child));
			}
		}
		
		int size = rootfiles.size();
		if( size <= 0 )
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.ROOTFILE);
		
        return rootfiles;
	}
	
}
