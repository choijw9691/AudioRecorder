package com.ebook.epub.parser.ops;

import com.ebook.epub.parser.common.INavigation;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlNavFile implements INavigation<XmlLi>{

    private ArrayList<XmlLi> tableOfContents;
    private String title;

    private XmlHtml navRoot;

    public XmlNavFile(String fileString) throws XmlNavigationException {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentbuilder = factory.newDocumentBuilder();
            Element element = documentbuilder.parse(BookHelper.String2InputStream(fileString)).getDocumentElement();

            navRoot = new XmlHtml(element);
            title = setTitle(navRoot);
            tableOfContents = setTableOfContents(navRoot);

        } catch (ParserConfigurationException e) {
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
        } catch (SAXException e) {
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
        } catch (IOException e) {
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
        } catch (DOMException e) {
            throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
        }
    }

//	public XmlNavFile(String path) throws XmlNavigationException {
//
//		try {
//
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	        factory.setNamespaceAware(true);
//	        DocumentBuilder documentbuilder = factory.newDocumentBuilder();
//	        Element element = documentbuilder.parse(new File(path)).getDocumentElement();
//
//	        XmlHtml navRoot = new XmlHtml(element);
//	        title = setTitle(navRoot);
//	        tableOfContents = setTableOfContents(navRoot);
//
//		} catch (ParserConfigurationException e) {
//			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
//		} catch (SAXException e) {
//			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
//		} catch (IOException e) {
//			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
//		} catch (DOMException e) {
//			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, "");
//		}
//	}

    private ArrayList<XmlLi> setTableOfContents(XmlHtml navRoot) throws XmlNavigationException {
        return navRoot.getBody().getNav().getOl().getLis();
    }

    private String setTitle(XmlHtml navRoot) throws XmlNavigationException {
        return navRoot.getBody().getNav().getH() == null ? "" : navRoot.getBody().getNav().getH().getValue();
    }

    @Override
    public Iterator<XmlLi> getTableOfContents() {
        return tableOfContents.iterator();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public XmlHtml getNavRoot() {
        return navRoot;
    }
}
