package com.ebook.epub.parser.ops;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.NamespaceURI;
import com.ebook.epub.parser.common.NavType;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XmlBody {

    private XmlNav nav;
    private XmlNav landmarks;
    private XmlNav pageLists;
    private XmlNav loa;

    public XmlBody(Node node) throws XmlNavigationException {

        Element el = (Element)node;

        nav = setNav(el);
        landmarks = setLandmarks(el);
        pageLists = setPageList(el);
        loa = setLoa(el);
    }

    public XmlNav getNav() {
        return nav;
    }

    public XmlNav getLandmarks() {
        return landmarks;
    }

    public XmlNav getPageLists() {
        return pageLists;
    }

    public XmlNav getLoa() {
        return loa;
    }

    private XmlNav setNav(Element el) throws XmlNavigationException {

        NodeList navNodes = el.getElementsByTagName(ElementName.NAV);
        Node toc = null;

        for (int i = 0; i < navNodes.getLength(); i++) {
            Node child = navNodes.item(i);

            Node typeAttr = child.getAttributes() == null ? null : child.getAttributes().getNamedItemNS(NamespaceURI.EPUB, AttributeName.TYPE);

            if( typeAttr != null && typeAttr.getNodeValue().equals(NavType.TOC) ) {
                if( toc == null )
                    toc = child;
                else
                    throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_TOC_DUPLICATION, ViewerErrorInfo.MSG_ERROR_NAV_TOC_DUPLICATION);
            }
        }

        if( toc == null )
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, NavType.TOC);

        return new XmlNav(toc);
    }

    private XmlNav setLandmarks(Element el) throws XmlNavigationException {

        NodeList navNodes = el.getElementsByTagName(ElementName.NAV);
        Node landmark = null;

        for (int i = 0; i < navNodes.getLength(); i++) {
            Node child = navNodes.item(i);

            Node typeAttr = child.getAttributes() == null ? null : child.getAttributes().getNamedItemNS(NamespaceURI.EPUB, AttributeName.TYPE);

            if( typeAttr != null && typeAttr.getNodeValue().equals(NavType.LANDMARKS) ) {
                if( landmark == null )
                    landmark = child;
                else
                    throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_LANDMARKS_DUPLICATION, ViewerErrorInfo.MSG_ERROR_NAV_LANDMARKS_DUPLICATION);
            }
        }

        return landmark == null ? null : new XmlNav(landmark);
    }

    private XmlNav setPageList(Element el) throws XmlNavigationException {

        NodeList navNodes = el.getElementsByTagName(ElementName.NAV);
        Node pageList = null;

        for (int i = 0; i < navNodes.getLength(); i++) {
            Node child = navNodes.item(i);

            Node typeAttr = child.getAttributes() == null ? null : child.getAttributes().getNamedItemNS(NamespaceURI.EPUB, AttributeName.TYPE);

            if( typeAttr != null && typeAttr.getNodeValue().equals(NavType.PAGE_LIST) ) {
                if( pageList == null )
                    pageList = child;
                else
                    throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_PAGELIST_DUPLICATION, ViewerErrorInfo.MSG_ERROR_NAV_PAGELIST_DUPLICATION);
            }
        }

        return pageList == null ? null : new XmlNav(pageList);
    }

    private XmlNav setLoa(Element el) throws XmlNavigationException {

        NodeList navNodes = el.getElementsByTagName(ElementName.NAV);
        Node loa = null;

        for (int i = 0; i < navNodes.getLength(); i++) {
            Node child = navNodes.item(i);

            Node typeAttr = child.getAttributes() == null ? null : child.getAttributes().getNamedItemNS(NamespaceURI.EPUB, AttributeName.TYPE);

            if( typeAttr != null && typeAttr.getNodeValue().equals(NavType.LOA) ) {
                if( loa == null )
                    loa = child;
            }
        }

        return loa == null ? null : new XmlNav(loa);
    }
}
