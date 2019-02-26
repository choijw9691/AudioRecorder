package com.ebook.epub.parser.ocf;

import java.io.UnsupportedEncodingException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlRootfile {
	
	private String fullPath;
	private String mediaType;
	
	public XmlRootfile(Node node) throws XmlContainerException {
		fullPath = setFullPath(node);
		mediaType = setMediaType(node);
	}
	
	public String getFullPath() {
		return fullPath;
	}

	public String getMediaType() {
		return mediaType;
	}

	private String setFullPath(Node node) throws XmlContainerException {
		
		NamedNodeMap attr = node.getAttributes();
		
		if( attr == null )
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.ROOTFILE);
		
		String fullPathDecode="";
		
        Node fullPath = attr.getNamedItem(AttributeName.FULL_PATH);

        if (fullPath == null)
        	throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.FULL_PATH);

        try {
        	fullPathDecode = EpubFileUtil.getURLDecode(fullPath.getNodeValue());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		}
		return fullPathDecode;
//        return fullPath.getNodeValue();
    }
	
	private String setMediaType(Node node) throws XmlContainerException {
		
		NamedNodeMap attr = node.getAttributes();
		
		if( attr == null )
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.ROOTFILE);
		
        Node mediaType = attr.getNamedItem(AttributeName.MEDIA_TYPE);

        if (mediaType == null)
        	throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.MEDIA_TYPE);

        return mediaType.getNodeValue();
    }
	
	
}
