package com.ebook.epub.parser.ops;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;

public class XmlContent {
	
	private String src;

	public XmlContent(Node node) throws XmlNavigationException {
		
		NamedNodeMap attr = node.getAttributes();
		if( attr == null )
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.CONTENT);
		
		src = setSrc(node);
	}
	
	public String getSrc() {
		return src;
	}

	private String setSrc(Node node) throws XmlNavigationException {
		String hRefDecode="";
		
		Node src = node.getAttributes().getNamedItem(AttributeName.SRC);
		
		if( src == null )
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.SRC);
		
		try {
			hRefDecode = EpubFileUtil.getURLDecode(src.getNodeValue());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		}
		
		return hRefDecode;
		
//		return src.getNodeValue();
	}
	
}
