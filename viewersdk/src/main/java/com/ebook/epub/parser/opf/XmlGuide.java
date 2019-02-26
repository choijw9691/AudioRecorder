package com.ebook.epub.parser.opf;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ebook.epub.parser.common.ElementName;

public class XmlGuide {

	private ArrayList<XmlReference> references;
	
	public XmlGuide(Node node) throws XmlPackageException {
		references = setReferences(node);
	}
	
	public ArrayList<XmlReference> getReferences() {
		return references;
	}

	private ArrayList<XmlReference> setReferences(Node node) throws XmlPackageException {
		
		NodeList childNodes = node.getChildNodes();
		
		//표준 무시 -> 필수아님
//		if( childNodes.getLength() <= 0 )
//			throw new XmlPackageException("references");
		
		ArrayList<XmlReference> referenceList = new ArrayList<XmlReference>();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if( child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ElementName.REFERENCE) )
				referenceList.add(new XmlReference(child));
		}
		
		return referenceList;
	}
	
}
