package com.ebook.epub.viewer.data;
import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.common.ItemMediaType;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.parser.opf.XmlItemRef;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.EpubFileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Chapter Percentage 정보를 관리하는 클래스
 * @author djHeo
 *
 */
public class ReadingSpine {
    private EpubFile epub;
    private ArrayList<ReadingOrderInfo> spineInfos = new ArrayList<>();
    private ArrayList<ReadingOrderInfo> nonLinearSpineInfos = new ArrayList<>();
    private int currentSpineIndex = 0;


    public ReadingSpine(EpubFile epub) throws XmlPackageException, XmlContainerException, EpubFileSystemException{
        this.epub = epub;

        readingChapterOfContents();
    }

    public UnModifiableArrayList<ReadingOrderInfo> getSpineInfos() {
        return new UnModifiableArrayList<ReadingOrderInfo>(spineInfos);
    }

    public UnModifiableArrayList<ReadingOrderInfo> getNonLinearSpineInfos() {
        return new UnModifiableArrayList<ReadingOrderInfo>(nonLinearSpineInfos);
    }

    private void readingChapterOfContents() throws XmlPackageException, XmlContainerException, EpubFileSystemException{

        HashMap<String, XmlItem> items = epub.getPublicationResources();
        Iterator<XmlItemRef> itemRefs = epub.getReadingOrders();

        String publicationPath = epub.getPublicationPath();
        //		EpubContainer container = epub.getEpubContainer();

        double totalSize = 0l;
        ArrayList<String> pathList = new ArrayList<String>();
        ArrayList<Double> fileSizeList = new ArrayList<Double>();
        ArrayList<Boolean> mediaOverlayList = new ArrayList<Boolean>();
        boolean hasMediaOverlay = false;

        DebugSet.d("TAG", "spine setting start");
        while (itemRefs.hasNext()) {

            XmlItemRef xmlItemRef = (XmlItemRef) itemRefs.next();

            XmlItem item = items.get(xmlItemRef.getIdRef());

            if(item==null) {
                continue;
            }

            if( item.getMediaType().equals(ItemMediaType.APPLICATION__XHTML__XML) || item.getMediaType().equals(ItemMediaType.TEXT_HTML) ){
//                    || item.getMediaType().equals(ItemMediaType.SVG__XML) ){

                // 				[ssin] add : checking mediaoverlay
                if(item.getMediaOverlay().isEmpty()){
                    hasMediaOverlay = false;
                } else{
                    hasMediaOverlay = true;
                }

                FileInfo fileInfo = EpubFileUtil.getResourceFile(publicationPath, item);

                if( fileInfo == null )
                    continue;

                if(xmlItemRef.getLinear().equalsIgnoreCase("NO")){
                    nonLinearSpineInfos.add(new ReadingOrderInfo(fileInfo.filePath, 0.0, 0.0, hasMediaOverlay));
                    continue;
                }

                totalSize += fileInfo.fileSize;

                fileSizeList.add((double) fileInfo.fileSize);
                pathList.add(fileInfo.filePath);
                mediaOverlayList.add(hasMediaOverlay);
            }
        }

        for (int i = 0; i < pathList.size(); i++) {

            double percent = (double)(fileSizeList.get(i) / totalSize * 100.0);
            double startPercent = 0.0;

            if( i != 0 ){
                startPercent = spineInfos.get(i-1).getSpineStartPercentage() + spineInfos.get(i-1).getSpinePercentage();
            }
            ReadingOrderInfo spineInfo = new ReadingOrderInfo(pathList.get(i), startPercent, percent, mediaOverlayList.get(i));
            spineInfos.add(spineInfo);

        }
        DebugSet.d("TAG", "spine setting end");
    }

    public void setCurrentSpineIndex(String filePath) {
        for (int i = 0; i < spineInfos.size(); i++) {
            ReadingOrderInfo info = spineInfos.get(i);
            if(info.getSpinePath().toLowerCase().equals(filePath.toLowerCase())) {
                currentSpineIndex = i;
                break;
            }

        }
    }

    public void setCurrentSpineIndex(int index) {
        currentSpineIndex = index;
    }

    public int getCurrentSpineIndex() {
        return currentSpineIndex;
    }

    public ReadingOrderInfo getCurrentSpineInfo() {
        if(spineInfos.size()<currentSpineIndex){
            return new ReadingOrderInfo("", 0.0, 0.0, false);
        }
        return spineInfos.get(currentSpineIndex);
    }

    public ReadingOrderInfo getSpineInfo(String filePath) {
        for (int i = 0; i < spineInfos.size(); i++) {

            ReadingOrderInfo info = spineInfos.get(i);
            if(info.getSpinePath().toLowerCase().equals(filePath.toLowerCase())) {
                return info;
            }
        }

        return new ReadingOrderInfo("", 0.0, 0.0, false);
    }

    public int getSpineIndex(String filePath){

        for (int i = 0; i < spineInfos.size(); i++) {

            ReadingOrderInfo info = spineInfos.get(i);
            if(info.getSpinePath().toLowerCase().equals(filePath.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    public ReadingOrderInfo getSpineInfoBySpineIndex(int spineIndex) {
        return spineInfos.get(spineIndex);
    }
}
