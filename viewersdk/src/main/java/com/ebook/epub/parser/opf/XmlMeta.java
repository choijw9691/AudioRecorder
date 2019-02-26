package com.ebook.epub.parser.opf;

import com.ebook.epub.parser.common.AttributeName;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class XmlMeta {

    private String id;
    private String properties;
    private String lang;
    private String scheme;
    private String dir;
    private String refines;
    private String value;

    private String content;
    private String name;

    public XmlMeta(Node node) throws XmlPackageException {

        properties = setProperty(node);
        id = setId(node);
        lang = setLang(node);
        scheme = setScheme(node);
        dir = setDir(node);
        refines = setRefines(node);
        value = setValue(node);

        content = setContent(node);
        name = setName(node);
    }

    public String getId() {
        return id;
    }

    public String getProperties() {
        return properties;
    }

    public String getLang() {
        return lang;
    }

    public String getScheme() {
        return scheme;
    }

    public String getDir() {
        return dir;
    }

    public String getRefines() {
        return refines;
    }

    public String getValue() {
        return value;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    private String setId(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node id = attr.getNamedItem(AttributeName.ID);

        return id == null ? "" :  id.getNodeValue();
    }

    private String setLang(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node lang = attr.getNamedItem(AttributeName.LANG);

        return lang == null ? "" : lang.getNodeValue();
    }

    private String setScheme(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node scheme = attr.getNamedItem(AttributeName.REFINES);

        return scheme == null ? "" : scheme.getNodeValue();
    }

    private String setRefines(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node refines = attr.getNamedItem(AttributeName.REFINES);

        return refines == null ? "" : refines.getNodeValue();
    }

    private String setProperty(Node node) throws XmlPackageException {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

//		if( attr == null )
//			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.PROPERTY);

        Node property = attr.getNamedItem(AttributeName.PROPERTY);
//
//		if( property == null /*|| property.getNodeValue().equals("")*/ )
//			throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.PROPERTY);
//      return property.getNodeValue();
        return property == null ? "" : property.getNodeValue();
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

        return  isEmpty ? "" : node.getTextContent();
    }

    private String setContent(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node content = attr.getNamedItem(AttributeName.CONTENT);

        return content == null ? "" : content.getNodeValue();
    }

    private String setName(Node node) {

        NamedNodeMap attr = node.getAttributes();

        if( attr == null )
            return "";

        Node name = attr.getNamedItem(AttributeName.NAME);

        return name == null ? "" : name.getNodeValue();
    }
}
