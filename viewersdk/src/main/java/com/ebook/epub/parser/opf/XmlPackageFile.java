package com.ebook.epub.parser.opf;

import com.ebook.epub.parser.common.AttributeName;
import com.ebook.epub.parser.common.ExceptionParameter;
import com.ebook.epub.parser.common.ItemProperties;
import com.ebook.epub.parser.common.MIMEType;
import com.ebook.epub.parser.common.MetaProperties;
import com.ebook.epub.parser.common.PackageVersion;
import com.ebook.epub.parser.common.ReferenceType;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.ViewerErrorInfo;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlPackageFile {

    private String version;
    private String identifier;
    private String languageInContent;
    private String baseTextDirection;
    private String glovalTextDirection;
    private ArrayList<XmlItem> publicationImages;
    private XmlItem navigation;
    private String coverImage;
    private ArrayList<XmlMediaType> customHandlers;
    private ArrayList<XmlDCMES> dublinCoredrms;
    private LinkedHashMap<XmlDCMES, ArrayList<String>> dublinCoreCreators;
    private LinkedHashMap<XmlDCMES, ArrayList<String>> dublinCoreTitles;
    private LinkedHashMap<XmlDCMES, ArrayList<String>> dublinCorePublishers;
    private LinkedHashMap<XmlDCMES, ArrayList<String>> dublinCoreIdentifiers;
    private LinkedHashMap<XmlDCMES, ArrayList<String>> dublinCoreLanguages;

    private ArrayList<XmlReference> structuralComponents;
    private LinkedHashMap<String, XmlItem> publicationResources;
    private ArrayList<XmlItemRef> readingOrders;

    private String renditionLayout;
    private String renditionOrientation;
    private String renditionSpread;

    private String activeClass;
    private String playbackActiveClass;

    private HashMap<String, XmlCollection> collections;

    public XmlPackageFile(String filePath) throws XmlPackageException, EpubFileSystemException  {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentbuilder = factory.newDocumentBuilder();
            Element element = documentbuilder.parse(new File(filePath)).getDocumentElement();

            XmlPackage packageRoot = new XmlPackage(element);

            version = setVersion(packageRoot);
            identifier = setIdentifier(packageRoot);
            languageInContent = setLanguageInContent(packageRoot);
            baseTextDirection = setBaseTextDirection(packageRoot);
            glovalTextDirection = setGlovalTextDirection(packageRoot);

            customHandlers = setCustomHandlers(packageRoot);
            dublinCoredrms = setDublinCoreDrms(packageRoot);
            dublinCoreCreators = setDublinCoreCreators(packageRoot);
            dublinCoreTitles = setDublinCoreTitles(packageRoot);
            dublinCorePublishers = setDublinCorePublishers(packageRoot);
            dublinCoreLanguages = setDublinCoreLanguages(packageRoot);
            dublinCoreIdentifiers = setDublinCoreIdentifiers(packageRoot);

            structuralComponents = setStructuralComponents(packageRoot);
            publicationResources = setPublicationResources(packageRoot);
            readingOrders = setReadingOrders(packageRoot);

            renditionLayout = setRenditionLayout(packageRoot);
            renditionOrientation = setRenditionOrientation(packageRoot);
            renditionSpread = setRenditionSpread(packageRoot);

            activeClass = setActiveClass(packageRoot);
            playbackActiveClass = setPlaybackActiveClass(packageRoot);

            publicationImages = setPublicationImages();
            navigation = setNavigation(packageRoot);
            coverImage = setCoverImage(packageRoot);

            collections = setCollections(packageRoot);

            DebugSet.d("TAG", "XmlPackageFile() 생성 완료");

        } catch (ParserConfigurationException e) {
            throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_PACKAGE_INVALID_XML, "");
        } catch (SAXException e) {
            throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_PACKAGE_INVALID_XML, "");
        } catch (IOException e) {
            throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_PACKAGE_INVALID_XML, "");
        } catch (DOMException e) {
            throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_INVALID_XML, e, ViewerErrorInfo.MSG_ERROR_PACKAGE_INVALID_XML, "");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLanguageInContent() {
        return languageInContent;
    }

    public String getBaseTextDirection() {
        return baseTextDirection;
    }

    public String getGlovalTextDirection() {
        return glovalTextDirection;
    }

    public Iterator<XmlItem> getPublicationImages() {
        return publicationImages.iterator();
    }


    public String getCoverImage() {
        return coverImage;
    }

    public Iterator<XmlMediaType> getCustomHandlers() {
        return customHandlers.iterator();
    }

    public Iterator<XmlDCMES> getDublinCoreDrms() {
        return dublinCoredrms.iterator();
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreCreators() {
        return dublinCoreCreators;
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreTitles() {
        return dublinCoreTitles;
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCorePublishers() {
        return dublinCorePublishers;
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreIdentifiers() {
        return dublinCoreIdentifiers;
    }

    public LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCoreLanguages() {
        return dublinCoreLanguages;
    }

    public Iterator<XmlReference> getStructuralComponents() {
        return structuralComponents.iterator();
    }

    public LinkedHashMap<String, XmlItem> getPublicationResources() {
        return publicationResources;
    }

    public Iterator<XmlItemRef> getReadingOrders() {
        return readingOrders.iterator();
    }

    public String getRenditionLayout() {
        return renditionLayout;
    }

    public String getRenditionOrientation() {
        return renditionOrientation;
    }

    public String getRenditionSpread() {
        return renditionSpread;
    }

    public String getActiveClass() {
        return activeClass;
    }

    public String getPlaybackActiveClass(){
        return playbackActiveClass;
    }

    private String setVersion(XmlPackage packageRoot) {
        return packageRoot.getVersion();
    }

    private String setIdentifier(XmlPackage packageRoot)  {
        return packageRoot.getUniqueIdentifier();
    }

    private String setLanguageInContent(XmlPackage packageRoot) {
        return packageRoot.getLang();
    }

    private String setBaseTextDirection(XmlPackage packageRoot) {
        return packageRoot.getDir();
    }

    private String setGlovalTextDirection(XmlPackage packageRoot) {
        return packageRoot.getSpine().getPageProgressionDirection();
    }

    /**
     * manifest 내 Image Item를 전달한다.
     * @return
     * @throws XmlPackageException
     */
    private ArrayList<XmlItem> setPublicationImages() throws XmlPackageException {
        ArrayList<XmlItem> imageItems = new ArrayList<XmlItem>();
        LinkedHashMap<String, XmlItem> items = getPublicationResources();
        Set<String> keys = items.keySet();

        for (String key : keys) {
            XmlItem item = items.get(key);
            if( item.getMediaType().contains(MIMEType.IMAGE) ){
                DebugSet.d("TAG", "image :" + item.getHRef());
                imageItems.add(item);
            }
        }

        return imageItems;
    }

    /**
     * Epub의 커버 이미지를 전달 한다.
     */
    private String setCoverImage(XmlPackage packageRoot) throws XmlPackageException {

/** 20170821 cover 기존 EPUB2.0 형식은 무시 (PC와 맞춤)
 //Epub 2 커버 이미지는 Reference 요소의 Type 속성 값으로 가져옴
 if( PackageVersion.EPUB2.equals(getVersion()) ) {

 ArrayList<XmlReference> references = structuralComponents;

 for (XmlReference xmlReference : references) {
 if( ReferenceType.COVER.equals(xmlReference.getType()) ) {
 cover = xmlReference.getHRef();
 return cover;
 //					coverCount++;
 }

 //하나 이상인 경우 Exception 발생
 //				if( coverCount > 1 )
 //					throw new XmlPackageException("cover duplication");
 }
 //Epub 3 커버 이미지는 Item 요소의 Properties 속성 값이 cover-image로 설정된 값을 가져옴
 } else if( PackageVersion.EPUB3.equals(getVersion()) ) {

 HashMap<String, XmlItem> items = getPublicationResources();

 Set<String> keys = items.keySet();

 for (String key : keys) {

 XmlItem item = items.get(key);

 if( ItemProperties.COVER_IMAGE.equals(item.getProperties()) ) {
 cover = item.getHRef();
 return cover;
 //					coverCount++;
 }

 //하나 이상인 경우 Exception 발생
 //				if( coverCount > 1 )
 //					throw new XmlPackageException("cover duplication");
 }
 }
 */
        // 1. EPUB 3.0 표준
        // 2. <meta name="cover" />
        // 3. 파일 명 규칙 (cover*, standard_coverimage*, coverimage*, bookcover*, img0*, img1*)
        String cover = null;

        HashMap<String, XmlItem> items = getPublicationResources();
        Set<String> keys = items.keySet();
        for (String key : keys) {
            XmlItem item = items.get(key);
            if( ItemProperties.COVER_IMAGE.equals(item.getProperties()) ) {
                cover = item.getHRef();
                return cover;
            }
        }

        if(cover==null) {
            ArrayList<XmlMeta> metaList = packageRoot.getMetadata().getMetas();
            for(XmlMeta meta : metaList){
                if(meta.getName().equalsIgnoreCase(ReferenceType.COVER)){
                    LinkedHashMap<String, XmlItem> itemsMap = packageRoot.getManifest().getItems();
                    if(itemsMap.get(meta.getContent())!=null){
                        cover = itemsMap.get(meta.getContent()).getHRef();
                        return cover;
                    }
                }
            }

//            String[] covernames = { "cover", "standard_coverimage", "coverimage", "bookcover", "img0", "img1" };
//
//            for(XmlItem item : publicationImages){
//                String[] splitStr = item.getHRef().split("/");
//                for (String name : covernames) {
//                    if(splitStr[splitStr.length-1].startsWith(name)){
//                        cover = splitStr[splitStr.length-1];
//                        return cover;
//                    }
//                }
//            }
        }

        return cover;
    }

    /**
     * Navigation 파일 정보 Item을 전달 한다.
     * @return
     * @throws XmlPackageException
     * @throws EpubFileSystemException
     */
    private XmlItem setNavigation(XmlPackage packageRoot) throws XmlPackageException, EpubFileSystemException {

        XmlItem navigation = null;

        if( getVersion().equals(PackageVersion.EPUB3) ){

            // EPUB3 네비게이션은 item 요소의 properties 속성 값에 nav 가 포함된 값을 가져옴
            HashMap<String, XmlItem> items = getPublicationResources();

            int ncxCount = 0;

            Set<String> keys = items.keySet();

            for (String key : keys) {

                XmlItem item = items.get(key);
                String properties = item.getProperties();

                String[] split = properties.split(" ");
                for (int i = 0; i < split.length; i++) {
                    if( split[i].contains(ItemProperties.NAV) ) {
                        navigation = item;
                        ncxCount++;
                        break;
                    }
                }

                //하나 이상인 경우 Exception 발생
                if( ncxCount > 1 )
                    throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_NAV_FILE_NAME_DEFINE_DUPLICATION, ViewerErrorInfo.MSG_ERROR_PACKAGE_NAV_FILE_NAME_DEFINE_DUPLICATION);
            }

            if(navigation==null){

                String tocKey = packageRoot.getSpine().getToc();

                if( tocKey == null || tocKey.equals("") )
                    throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.TOC);

                XmlItem item = items.get(tocKey);
                navigation = item;
            }

        } else if( getVersion().equals(PackageVersion.EPUB2) ){

            //EPUB 2의 네비게이션은 item 요소의 media-type 속성 값이 Application/X-Dtbncx+Xml인 값을 가져옴
            HashMap<String, XmlItem> items = getPublicationResources();

            String tocKey = packageRoot.getSpine().getToc();

            if( tocKey == null || tocKey.equals("") )
                throw new XmlPackageException(ViewerErrorInfo.CODE_ERROR_PACKAGE_PARSING_FAILED_ATTRIBUTE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_ATTRIBUTE_NOT_FOUND, AttributeName.TOC);

            XmlItem item = items.get(tocKey);
            navigation = item;
        }

        if( navigation == null )
            throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.NAVIGATION);

        return navigation;
    }



    private ArrayList<XmlMediaType> setCustomHandlers(XmlPackage packageRoot) throws XmlPackageException {
        return packageRoot.getBindings() == null ? new ArrayList<XmlMediaType>() : packageRoot.getBindings().getMediaTypes();
    }

    private ArrayList<XmlDCMES> setDublinCoreDrms(XmlPackage packageRoot) throws XmlPackageException {
        return packageRoot.getMetadata().getDrms();
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> setDublinCoreCreators(XmlPackage packageRoot) throws XmlPackageException {
        return getDublinCore(packageRoot.getMetadata().getCreators(), packageRoot);
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> setDublinCoreIdentifiers(XmlPackage packageRoot) throws XmlPackageException {
        return getDublinCore(packageRoot.getMetadata().getIdentifiers(), packageRoot);
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> setDublinCoreLanguages(XmlPackage packageRoot) throws XmlPackageException {
        return getDublinCore(packageRoot.getMetadata().getLanguages(), packageRoot);
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> setDublinCorePublishers(XmlPackage packageRoot) throws XmlPackageException {
        return getDublinCore(packageRoot.getMetadata().getPublishers(), packageRoot);
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> setDublinCoreTitles(XmlPackage packageRoot) throws XmlPackageException {
        return getDublinCore(packageRoot.getMetadata().getTitles(), packageRoot);
    }

    private LinkedHashMap<XmlDCMES, ArrayList<String>> getDublinCore(ArrayList<XmlDCMES> dublinCores, XmlPackage packageRoot) throws XmlPackageException {

        LinkedHashMap<XmlDCMES, ArrayList<String>> map = new LinkedHashMap<XmlDCMES, ArrayList<String>>();

        if( PackageVersion.EPUB2.equals(getVersion()) ) {

            for (XmlDCMES xmlDCMES : dublinCores) {
                //Epub2 Meta 요소는 DC와 관련사항이 없어 빈 컬렉션을 반환
                map.put(xmlDCMES, new ArrayList<String>());
            }

        } else if( PackageVersion.EPUB3.equals(getVersion()) ) {

            for (XmlDCMES xmlDCMES : dublinCores) {
                ArrayList<String> arr = new ArrayList<String>();

                if(xmlDCMES.getId().length() > 0) {

                    ArrayList<XmlMeta> metas = packageRoot.getMetadata().getMetas();

                    for (XmlMeta xmlMeta : metas) {

                        //dc의 id 와 meta의 refine을 매칭하여 데이터 세팅
                        if( ("#"+xmlDCMES.getId()).equals(xmlMeta.getRefines()) ){
                            arr.add(xmlMeta.getValue());
                        }
                    }
                }
                map.put(xmlDCMES, arr);
            }
        }

        return map;
    }

    private String setRenditionLayout(XmlPackage packageRoot) throws XmlPackageException {
        ArrayList<XmlMeta> meta = packageRoot.getMetadata().getMetas();

        for (XmlMeta xmlMeta : meta) {

            if( xmlMeta.getProperties().equals(MetaProperties.RENDITION_LAYOUT) )
                return xmlMeta.getValue();
        }

        return "";
    }

    private String setRenditionOrientation(XmlPackage packageRoot) throws XmlPackageException {
        ArrayList<XmlMeta> meta = packageRoot.getMetadata().getMetas();

        for (XmlMeta xmlMeta : meta) {

            if( xmlMeta.getProperties().equals(MetaProperties.RENDITION_ORIENTATION) )
                return xmlMeta.getValue();
        }

        return "";
    }

    private String setRenditionSpread(XmlPackage packageRoot) throws XmlPackageException {
        ArrayList<XmlMeta> meta = packageRoot.getMetadata().getMetas();

        for (XmlMeta xmlMeta : meta) {
            if( xmlMeta.getProperties().equals(MetaProperties.RENDITION_SPREAD) )
                return xmlMeta.getValue();
        }

        return "";
    }

    private String setActiveClass(XmlPackage packageRoot) {

        ArrayList<XmlMeta> meta = packageRoot.getMetadata().getMetas();

        for (XmlMeta xmlMeta : meta) {
            if( xmlMeta.getProperties().equals(MetaProperties.ACTIVE_CLASS) )
                return xmlMeta.getValue();
        }

        return null;
    }

    private String setPlaybackActiveClass(XmlPackage packageRoot){
        ArrayList<XmlMeta> meta = packageRoot.getMetadata().getMetas();

        for (XmlMeta xmlMeta : meta) {
            if( xmlMeta.getProperties().equals(MetaProperties.PLAYBACK_ACTIVE_CLASS) )
                return xmlMeta.getValue();
        }

        return null;
    }

    private ArrayList<XmlReference> setStructuralComponents(XmlPackage packageRoot) throws XmlPackageException {

        ArrayList<XmlReference> referenceList = new ArrayList<XmlReference>();

        if( packageRoot.getGuide() != null )
            return packageRoot.getGuide().getReferences();

        return referenceList;
    }

    private LinkedHashMap<String, XmlItem> setPublicationResources(XmlPackage packageRoot) throws XmlPackageException {
        return packageRoot.getManifest().getItems();
    }

    private ArrayList<XmlItemRef> setReadingOrders(XmlPackage packageRoot) throws XmlPackageException {
        return packageRoot.getSpine().getItemRefs();
    }

    public XmlItem getNavigation() {
        return navigation;
    }

    public boolean hasLinearNo(String chapterFilePath){

        boolean hasLinear=false;

        int tmpStart = chapterFilePath.indexOf("#");

        if(tmpStart!=-1){
            chapterFilePath=chapterFilePath.substring(0,tmpStart).replace("/", "");
        }

        HashMap<String, XmlItem> items = publicationResources;

        Set<String> keys = items.keySet();
        String spineId = "";
        for (String key : keys) {
            XmlItem item = items.get(key);
            if( item.getHRef().contains(chapterFilePath) ){
                spineId = item.getId();
                break;

            }
        }

        Iterator<XmlItemRef> itemRefs = readingOrders.iterator();

        while(itemRefs.hasNext()){
            XmlItemRef xmlItemRef = (XmlItemRef) itemRefs.next();
            if(xmlItemRef.getIdRef().equalsIgnoreCase(spineId)){
                hasLinear = xmlItemRef.getLinear().equalsIgnoreCase("no") ? true : false;
                break;
            }
        }

        return hasLinear;
    }

    public String getSmilFilePath(String path){
        HashMap<String, XmlItem> items = publicationResources;
        Set<String> keys = items.keySet();
        for (String key : keys) {
            XmlItem item = items.get(key);
            if(path.contains(item.getHRef())){
                return items.get(item.getMediaOverlay()).getHRef();
            }
        }
        return "";
    }

    //	[ssin-BG] s
    private HashMap<String, XmlCollection> setCollections(XmlPackage packageRoot){
        return packageRoot.getCollections();
    }

    public HashMap<String, XmlCollection> getCollections(){
        return collections;
    }
//	[ssin-BG] e
}
