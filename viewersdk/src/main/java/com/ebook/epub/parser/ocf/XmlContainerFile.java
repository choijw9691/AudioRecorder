package com.ebook.epub.parser.ocf;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ElementName;
import com.ebook.epub.parser.common.RootfileMediaType;
import com.ebook.epub.viewer.ViewerErrorInfo;

/**
@class XmlContainerFile 
@brief Container.xml parsing class 
 */
public class XmlContainerFile {
	
	private XmlRootfiles rendition;
	private XmlRootfile defaultRendition;
	private String packageFileName;
	private String publicationDirName;

	/**
	@breif container.xml 파일 파싱 
	@param filePath : container file path
	@return 생성된 XmlContainerFile 객체
	@throws XmlContainerException
	 */
	public XmlContainerFile(String filePath) throws XmlContainerException  {
		
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder documentbuilder = factory.newDocumentBuilder();
	        Element element = documentbuilder.parse(new File(filePath)).getDocumentElement();
	        
	        XmlContainer containerRoot = new XmlContainer(element);
	        
	        rendition = setRendition(containerRoot);
	        defaultRendition = setDefaultRendition();
	        packageFileName = setPackageFileName();
	        publicationDirName = setPublicationDirName();
	        
		} catch (ParserConfigurationException e) {
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_CONTAINER_INVALID_XML, "");
		} catch (SAXException e) {
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_CONTAINER_INVALID_XML, "");
		} catch (IOException e) {
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_CONTAINER_INVALID_XML, "");
		} catch (DOMException e) {
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_CONTAINER_INVALID_XML, "");
		}
	}
	
	public XmlRootfiles getRendition() {
		return rendition;
	}

	public XmlRootfile getDefaultRendition() {
		return defaultRendition;
	}

	public String getPackageFileName() {
		return packageFileName;
	}

	public String getPublicationDirName() {
		return publicationDirName;
	}

	/**
	 * 렌디션 리스트를 가져온다.
	 * @return XmlRootfiles
	 * @throws Exception
	 */
	private XmlRootfiles setRendition(XmlContainer containerRoot) {
		
        return containerRoot.getXmlRootfiles();
	}
	
	/**
	 * 기본(첫번째) 렌디션 정보를 가져온다.
	 * @return
	 * @throws Exception
	 */
	private XmlRootfile setDefaultRendition() throws XmlContainerException {
		
		XmlRootfile defaultRendition = getRendition().getXmlRootfiles().next(); //첫번째 RootFile을 가져온다.
		
		boolean isEqual = defaultRendition.getMediaType().equals(RootfileMediaType.APPLICATION__OEBPS_PACKAGE__XML);
		
		if( !isEqual )
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ELEMENT_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ELEMENT_NOT_FOUND, ElementName.ROOTFILE);
		
		return defaultRendition;
	}
	
	/**
	 * 컨텐츠 패키지 파일 이름을 가져온다.
	 * @return
	 * @throws Exception
	 */
	private String setPackageFileName() throws XmlContainerException {
		String packageFileName = getDefaultRendition().getFullPath();
		
		boolean isNullOrEmpty = packageFileName.isEmpty();
		
		if (isNullOrEmpty)
			throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.FULL_PATH);
		
		if( packageFileName.indexOf("/") == -1 ) {
			return packageFileName;
		}
		
		String[] fileName = packageFileName.split("/");
		
		return fileName[fileName.length-1];
	}
	
	
	/**
	 * 컨텐츠 루트 디렉토리 이름을 가져온다.
	 * @return
	 * @throws Exception
	 */
	private String setPublicationDirName() throws XmlContainerException {
		
        String publicationDirName = getDefaultRendition().getFullPath();
        boolean isNullOrEmpty = publicationDirName.isEmpty();

        if (isNullOrEmpty)
        	throw new XmlContainerException(ViewerErrorInfo.CODE_ERROR_CONTAINER_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.FULL_PATH);
        
        int lastIndexOfDevider = publicationDirName.lastIndexOf("/");
        if( lastIndexOfDevider == -1 ) {
			return "";
		}
        
        return publicationDirName.substring(0, lastIndexOfDevider);

//        return publicationDirName;
    }
}
