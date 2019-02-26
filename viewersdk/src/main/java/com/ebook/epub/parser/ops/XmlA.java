package com.ebook.epub.parser.ops;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;


public class XmlA {

    private String hRef;
    private String value;

    private String dataValue;

    public XmlA(Node node) throws XmlNavigationException {

        NamedNodeMap attr = node.getAttributes();
        if( attr == null )
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, ElementName.A);

        hRef = setHRef(node);
        value = setValue(node);

        dataValue = setDataValue(node);
    }

    public String gethRef() {
        return hRef;
    }

    public String getValue() {
        return value;
    }

    public String getDataValue() {
        return dataValue;
    }

    private String setHRef(Node node) throws XmlNavigationException {
        String hRefDecode="";

        Node hRef = node.getAttributes().getNamedItem(AttributeName.HREF);

        if( hRef == null )
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.HREF);

        try {
            hRefDecode = EpubFileUtil.getURLDecode(hRef.getNodeValue());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        }

        return hRefDecode;
//		return hRef.getNodeValue();
    }

    private String setValue(Node node) {

        boolean isEmpty = node.getTextContent() == null /*|| node.getTextContent().equals("")*/;

        return  isEmpty ? "" : node.getTextContent();
    }

    private String setDataValue(Node node) throws XmlNavigationException {
        String dataValueDecode = "";

        Node dataValue = node.getAttributes().getNamedItem(AttributeName.DATA_AUDIO_REF);

        if( dataValue == null )
            return "";

        try {
            dataValueDecode = EpubFileUtil.getURLDecode(dataValue.getNodeValue());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        }

        return dataValueDecode;
    }
}
