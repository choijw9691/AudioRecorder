package com.ebook.epub.parser.ops;

import com.ebook.epub.parser.common.ExceptionParameter;
import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.common.ItemProperties;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.epub.viewer.ViewerErrorInfo;

import java.util.ArrayList;
import java.util.Iterator;

public class XmlNavigationFile {

    private ArrayList<XmlChapter> chapters = new ArrayList<XmlChapter>();
    private String publicationPath;
    private XmlItem xmlItem;
    private boolean isValidNavigation=false;

    private XmlNav listOfAudio;

    ViewerContainer.RequestStringOfFileListener mRequestStringOfFile = null;
    private String mDrmKey;

    public XmlNavigationFile(String publicationPath, XmlItem xmlItem) throws XmlNavigationException, EpubFileSystemException {
        this.publicationPath = publicationPath;
        this.xmlItem = xmlItem;

        convertNavToChapter(publicationPath, xmlItem, chapters);
    }

    public XmlNavigationFile(String publicationPath, XmlItem xmlItem, ViewerContainer.RequestStringOfFileListener listener, String drmKey) throws XmlNavigationException, EpubFileSystemException {
        this.publicationPath = publicationPath;
        this.xmlItem = xmlItem;

        mRequestStringOfFile = listener;
        mDrmKey = drmKey;

        convertNavToChapter(publicationPath, xmlItem, chapters);
    }

    public ArrayList<XmlChapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<XmlChapter> chapters) {
        this.chapters = chapters;
    }

    private void convertNavToChapter(String publicationPath, XmlItem xmlItem, ArrayList<XmlChapter> chapterList) throws EpubFileSystemException, XmlNavigationException {

        if(xmlItem.getProperties().contains(ItemProperties.NAV)){

            FileInfo nav = EpubFileUtil.getResourceFile(publicationPath, xmlItem);

            if( nav == null )
                throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.NAV);

//			XmlNavFile navigation = new XmlNavFile(nav.filePath);
            try{
                XmlNavFile navigation = new XmlNavFile(mRequestStringOfFile.requestStringOfFile(nav.filePath, mDrmKey));

                Iterator<XmlLi> xmlLis = (Iterator<XmlLi>) navigation.getTableOfContents();

                listOfAudio = navigation.getNavRoot().getBody().getLoa();

                parseNavWithTableOfContents(xmlLis, chapterList);
            }catch(XmlNavigationException e){
                isValidNavigation = true;
            }
        } else {

            FileInfo nav = EpubFileUtil.getResourceFile(publicationPath, xmlItem);

            if( nav == null )
                throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.NCX);

            try{
                XmlNcxFile navigation = new XmlNcxFile(nav.filePath);

                Iterator<XmlNavPoint> navPoints = (Iterator<XmlNavPoint>) navigation.getTableOfContents();

                parseNcxWithTableOfContents(navPoints, chapterList);
            }catch(XmlNavigationException e){
                isValidNavigation = true;
            }
//			XmlNcxFile navigation = new XmlNcxFile(nav.filePath);
//
//			Iterator<XmlNavPoint> navPoints = (Iterator<XmlNavPoint>) navigation.getTableOfContents();
//
//			parseNcxWithTableOfContents(navPoints, chapterList);
        }
    }

    private void parseNcxWithTableOfContents(Iterator<XmlNavPoint> navPoints, ArrayList<XmlChapter> chapterList){

        while (navPoints.hasNext()) {

            XmlNavPoint point = navPoints.next();

            if( point != null ) {

                String src = point.getContent().getSrc();
                String value = point.getNavLabel().getText().getValue();

                XmlChapter chapter = new XmlChapter(src, value);


//				chapterList.add(chapter);

                if( point.getNavPoints().hasNext()){
                    parseNcxWithTableOfContents(point.getNavPoints(), chapter.getChapterList());
                }

                chapterList.add(chapter);
            }
        }
    }

    private void parseNavWithTableOfContents(Iterator<XmlLi> xmlLis, ArrayList<XmlChapter> chapterList){

        while (xmlLis.hasNext()) {

            XmlLi xmlLi = xmlLis.next();

            if( xmlLi != null ) {
                String hRef = "";
                String value = "";

                if( xmlLi.getA() != null ){
                    hRef = xmlLi.getA().gethRef();
                    value = xmlLi.getA().getValue();
                } else {
                    value = xmlLi.getSpan().getValue();
                }

                XmlChapter chapter = new XmlChapter(hRef, value);

                if( xmlLi.getOl() != null ){
                    parseNavWithTableOfContents(xmlLi.getOl().getLis().iterator(), chapter.getChapterList());
                }

                chapterList.add(chapter);
            }
        }
    }

    public XmlNav getListOfAudio(){
        return listOfAudio;
    }

    public boolean isValidNavigation(){
        return isValidNavigation;
    }
}
