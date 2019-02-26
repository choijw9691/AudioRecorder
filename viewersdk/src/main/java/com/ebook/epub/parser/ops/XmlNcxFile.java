package com.ebook.epub.parser.ops;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ebook.epub.parser.common.INavigation;
import com.ebook.epub.viewer.ViewerErrorInfo;

public class XmlNcxFile implements INavigation<XmlNavPoint>{
	
	private ArrayList<XmlNavPoint> tableOfContents;
	private String title;
	
	public XmlNcxFile(String path) throws XmlNavigationException {
		
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder documentbuilder = factory.newDocumentBuilder();
	        Element element = documentbuilder.parse(new File(path)).getDocumentElement();
	        
	        XmlNcx navRoot = new XmlNcx(element);
	        
	        title = setTitle(navRoot);
	        tableOfContents = setTableOfContents(navRoot);
        
		} catch (ParserConfigurationException e) {
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, e);
		} catch (SAXException e) {
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, e);
		} catch (IOException e) {
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, e);
		} catch (DOMException e) {
			throw new XmlNavigationException(ViewerErrorInfo.CODE_ERROR_NAV_INVALID_XML, ViewerErrorInfo.MSG_ERROR_NAV_INVALID_XML, e);
		}
	}
	
	public String setTitle(XmlNcx navRoot) throws XmlNavigationException{
		return navRoot.getDocTitle() == null || navRoot.getDocTitle().getText() == null ? "" : navRoot.getDocTitle().getText().getValue();
	}
	
	public ArrayList<XmlNavPoint> setTableOfContents(XmlNcx navRoot) throws XmlNavigationException {
		return navRoot.getNavMap().getNavPoints();
	}

	@Override
	public Iterator<XmlNavPoint> getTableOfContents() {
		return tableOfContents.iterator();
	}

	@Override
	public String getTitle() {
		return title;
	}
}
