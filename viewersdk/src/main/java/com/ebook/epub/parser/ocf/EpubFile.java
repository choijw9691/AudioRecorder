package com.ebook.epub.parser.ocf;

import com.ebook.epub.parser.common.ExceptionParameter;
import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.opf.XmlCollection;
import com.ebook.epub.parser.opf.XmlDCMES;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.parser.opf.XmlItemRef;
import com.ebook.epub.parser.opf.XmlMediaType;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.parser.opf.XmlPackageFile;
import com.ebook.epub.parser.opf.XmlReference;
import com.ebook.epub.parser.ops.XmlChapter;
import com.ebook.epub.parser.ops.XmlNav;
import com.ebook.epub.parser.ops.XmlNavigationException;
import com.ebook.epub.parser.ops.XmlNavigationFile;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.epub.viewer.ViewerErrorInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 @class EpubFile
 @brief Epub 전체 정보 class
 */
public class EpubFile {

    private String epubPath;
    private String publicationPath;

    private EpubContainer epubContainer;
    private XmlContainerFile containerFile;
    private XmlPackageFile packageFile;
    //	private XmlNavigationFile<XmlLi> navigationFile;
//	private XmlNavigationFile<XmlNavPoint> ncxFile;
    private XmlNavigationFile navigationFile;

    private XmlEncryptionFile encryptionFile;

//	public interface OnRequestStringOfFileListener {
//		public String requestStringOfFile(String filePath);
//	}

    /**
     @breif EpubFile 생성자
     @param EPub file path
     @return 생성된 EpubFile 객체
     */
    public EpubFile(String epubPath) throws EpubFileSystemException, XmlContainerException, XmlPackageException, XmlNavigationException {
        this.epubPath = epubPath;

        epubContainer = setEpubContainer();
        containerFile = setContainerDocument();

        publicationPath = setPublicationPath();
        packageFile = setPackageDocument();

        navigationFile = setNavigationDocument();
    }

    /**
     @breif EpubFile 생성자
     @param Epub file path, Drm request listener
     @return 생성된 EpubFile 객체
     */
    public EpubFile(String epubPath, ViewerContainer.RequestStringOfFileListener listener) throws EpubFileSystemException, XmlContainerException, XmlPackageException, XmlNavigationException {
        this.epubPath = epubPath;


        epubContainer = setEpubContainer();
        containerFile = setContainerDocument();

//		encryptionFile= setEncryptionDocument();	//TODO : encryption.xml

        publicationPath = setPublicationPath();
        packageFile = setPackageDocument();

        navigationFile = setNavigationDocument(listener);

//		if( PackageVersion.EPUB2.equals(packageFile.getVersion()) )
//			ncxFile = setNcxDocument();
//		else if( PackageVersion.EPUB3.equals(packageFile.getVersion()) )
//			navigationFile = setNavDocument();
    }

    public String getEpubPath() {
        return epubPath;
    }

    public String getPublicationPath() {
        return publicationPath;
    }
//
//	public EpubContainer getEpubContainer() {
//		return epubContainer;
//	}
//
//	public XmlContainerFile getContainerDocument() {
//		return containerFile;
//	}
//
//	public XmlPackageFile getPackageDocument() {
//		return packageFile;
//	}

//	public XmlNavigationFile<XmlLi> getNavDocument() {
//		return navigationFile;
//	}
//
//	public XmlNavigationFile<XmlNavPoint> getNcxDocument() {
//		return ncxFile;
//	}

    private EpubContainer setEpubContainer() throws EpubFileSystemException {
        return new EpubContainer(epubPath);
    }

    private XmlContainerFile setContainerDocument() throws XmlContainerException {
        return new XmlContainerFile(getContainerPath());
    }

    /**
     * Package File Parsing 객체 전달
     * @return
     * @throws XmlContainerException
     * @throws EpubFileSystemException
     * @throws XmlPackageException
     * @throws Exception
     */
    private XmlPackageFile setPackageDocument() throws XmlContainerException, EpubFileSystemException, XmlPackageException {
        DebugSet.d("TAG", "getPackageDocument() start");

        FileInfo packagePath = EpubFileUtil.getResourceFile(publicationPath, getPackageFileName());

        if( packagePath == null )
            throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.PACKAGE);

        return new XmlPackageFile(packagePath.filePath);
    }

    /**
     * Package File Path 전달
     * @return
     * @throws XmlContainerException
     * @throws EpubFileSystemException
     * @throws Exception
     */
    private String setPublicationPath() throws XmlContainerException, EpubFileSystemException {
        FileInfo publication = EpubFileUtil.getResourceDirectory(epubPath, getPublicationDirName());

        if( publication == null )
            throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.PUBLICATION);

        return publication.filePath;
    }

    private XmlNavigationFile setNavigationDocument() throws XmlNavigationException, EpubFileSystemException {
        return new XmlNavigationFile(publicationPath, getNavigation());
    }

    private XmlNavigationFile setNavigationDocument(ViewerContainer.RequestStringOfFileListener listener) throws XmlNavigationException, EpubFileSystemException {
        return new XmlNavigationFile(publicationPath, getNavigation(), listener, getDrmKey());
    }

    public String getVersion() {
        if(packageFile!=null)
            return packageFile.getVersion();
        return "";
    }

    public String getIdentifier() {
        return packageFile.getIdentifier();
    }

    public String getLanguageInContent() {
        return packageFile.getLanguageInContent();
    }

    public String getBaseTextDirection() {
        return packageFile.getBaseTextDirection();
    }

    public String getGlovalTextDirection() {
        return packageFile.getGlovalTextDirection();
    }

    public Iterator<XmlItem> getPublicationImages() {
        return packageFile.getPublicationImages();
    }

    public String getCoverImage() {
        return packageFile.getCoverImage();
    }

    public Iterator<XmlMediaType> getCustomHandlers() {
        return packageFile.getCustomHandlers();
    }

    public Iterator<XmlDCMES> getDublinCoreDrms() {
        return packageFile.getDublinCoreDrms();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreCreators() {
        return packageFile.getDublinCoreCreators();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreTitles() {
        return packageFile.getDublinCoreTitles();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCorePublishers() {
        return packageFile.getDublinCorePublishers();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreIdentifiers() {
        return packageFile.getDublinCoreIdentifiers();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreLanguages() {
        return packageFile.getDublinCoreLanguages();
    }

    public Iterator<XmlReference> getStructuralComponents() {
        return packageFile.getStructuralComponents();
    }

    public LinkedHashMap<String, XmlItem> getPublicationResources() {
        return packageFile.getPublicationResources();
    }

    public Iterator<XmlItemRef> getReadingOrders() {
        return packageFile.getReadingOrders();
    }

    public String getRenditionLayout() {
        return packageFile.getRenditionLayout();
    }

    public String getRenditionOrientation() {
        return packageFile.getRenditionOrientation();
    }

    public String getRenditionSpread() {
        return packageFile.getRenditionSpread();
    }

    public XmlItem getNavigation() {
        return packageFile.getNavigation();
    }

    public boolean hasLinearNo(String chapterFilePath){
        return packageFile.hasLinearNo(chapterFilePath);
    }

    public String getActiveClass(){
        return packageFile.getActiveClass() != null ?  packageFile.getActiveClass() : BookHelper.ACTIVE_CLASS;
    }

    public String getPlaybackActiveClass(){
        return packageFile.getPlaybackActiveClass() != null ?  packageFile.getPlaybackActiveClass() : "";
    }

    /* XmlContainerFile */
    public XmlRootfiles getRendition() {
        return containerFile.getRendition();
    }

    public XmlRootfile getDefaultRendition() {
        return containerFile.getDefaultRendition();
    }

    public String getPackageFileName() {
        return containerFile.getPackageFileName();
    }

    public String getPublicationDirName() {
        return containerFile.getPublicationDirName();
    }

    /* EpubContainer */
    public String getContainerPath() {
        return epubContainer.getContainerPath();
    }

    public String getMetaInfPath() {
        return epubContainer.getMetaInfPath();
    }

    public String getEncryptionFilePath(){
        return epubContainer.getEncryptionFilePath();
    }

    public String getMimeTypePath() {
        return epubContainer.getMimeTypePath();
    }

    public ArrayList<XmlChapter> getChapters() {
        return navigationFile.getChapters();
    }

    public String getSmilFilePath(String path){
        return publicationPath+"/"+packageFile.getSmilFilePath(path);
    }

    public boolean isValidNavigation(){
        return navigationFile.isValidNavigation();
    }

    //	[ssin-BG] s
    public HashMap<String, XmlCollection> getCollections(){
        return packageFile.getCollections();
    }
//	[ssin-BG] e

    private XmlEncryptionFile setEncryptionDocument(){

        if(!epubContainer.getEncryptionFilePath().isEmpty())
            return new XmlEncryptionFile(epubPath, epubContainer.getEncryptionFilePath());

        return null;
    }

    public HashMap<String, XmlEncryptedData> getEncryptedData(){
        if(encryptionFile==null){
            return null;
        }
        return encryptionFile.getXmlEncryption().getEncryptedData();
    }

    public HashMap<String, XmlEncryptedKey> getEncryptedKey(){
        if(encryptionFile==null){
            return null;
        }
        return encryptionFile.getXmlEncryption().getEncryptedKey();
    }

    public XmlNav getNavigationAudio(){
        return navigationFile.getListOfAudio();
    }

    private String getDrmKey()  {

        String drmKey=null;

        Iterator<XmlDCMES> drm = null;

        drm = getDublinCoreDrms();

        while (drm.hasNext()) {
            XmlDCMES xmlDublinCore = (XmlDCMES) drm.next();

            drmKey = xmlDublinCore.getValue();
            break;
        }
        if( drmKey == null )
            drmKey = "";

        return drmKey;
    }
}
