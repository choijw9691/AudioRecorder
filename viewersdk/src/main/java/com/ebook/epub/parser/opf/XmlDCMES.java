package com.ebook.epub.parser.opf;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;


public class XmlDCMES {
	
	private String lang;
	private String id;
	private String dir;
	private String value;
	
	public XmlDCMES(Node node) {
		lang = setLang(node);
		id = setId(node);
		dir = setDir(node);
		value = setValue(node);
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
	
	public String getValue() {
		return value;
	}

	private String setLang(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		
		if( attr == null )
			return "";
		
        Node lang = attr.getNamedItem(AttributeName.LANG);

        return lang == null ? "" : lang.getNodeValue();
    }
	
	private String setId(Node node) {
		
		NamedNodeMap attr = node.getAttributes();
		
		if( attr == null )
			return "";
		
        Node id = attr.getNamedItem(AttributeName.ID);

        return id == null ? "" : id.getNodeValue();
    }

	private String setDir(Node node) {
	
		NamedNodeMap attr = node.getAttributes();
		
		if( attr == null )
			return "";
		
        Node dir = attr.getNamedItem(AttributeName.DIR);

	    return dir == null ? "" : dir.getNodeValue();
	}
	
	private String setValue(Node node) {

		boolean isEmpty = node.getTextContent() == null || node.getTextContent().equals("");
		
		return isEmpty ? "" : node.getTextContent();
	}

}
