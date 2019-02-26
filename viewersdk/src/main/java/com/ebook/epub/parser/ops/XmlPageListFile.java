package com.ebook.epub.parser.ops;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ebook.epub.parser.common.INavigation;

public class XmlPageListFile implements INavigation<XmlLi>{

	private ArrayList<XmlLi> tableOfContents;
	private String title;

	public XmlPageListFile(String path) throws XmlNavigationException {
		
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder documentbuilder = factory.newDocumentBuilder();
	        Element element = documentbuilder.parse(new File(path)).getDocumentElement();
	        
	        XmlHtml navRoot = new XmlHtml(element);
	        
	        title = setTitle(navRoot);
	        tableOfContents = setTableOfContents(navRoot);
	        
		} catch (ParserConfigurationException e) {
			throw new XmlNavigationException(e);
		} catch (SAXException e) {
			throw new XmlNavigationException(e);
		} catch (IOException e) {
			throw new XmlNavigationException(e);
		}
	}
	
	private ArrayList<XmlLi> setTableOfContents(XmlHtml navRoot) throws XmlNavigationException {
		return navRoot.getBody().getPageLists().getOl().getLis();
	}
	
	private String setTitle(XmlHtml navRoot) throws XmlNavigationException {
		return navRoot.getBody().getPageLists().getH() == null ? "" : navRoot.getBody().getPageLists().getH().getValue();
	}

	@Override
	public Iterator<XmlLi> getTableOfContents() {
		return tableOfContents.iterator();
	}

	@Override
	public String getTitle() {
		return title;
	}
}
