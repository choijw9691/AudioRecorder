package com.ebook.epub.fixedlayoutviewer.manager;

import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.viewer.AnnotationConst;
import com.ebook.epub.viewer.AnnotationHistory;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Bookmark;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.DeviceInfoUtil;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.MyZip;
import com.ebook.epub.viewer.data.ReadingChapter;
import com.ebook.epub.viewer.data.ReadingOrderInfo;
import com.ebook.epub.viewer.data.ReadingSpine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class UserBookDataFileManager {

    private String TAG = "UserBookDataManager";

    private static String epubPath = "";

    public static ReadingSpine mReadingSpine;

    public static ReadingChapter mChapter;

    public AnnotationHistory __bmHistory = new AnnotationHistory();

    public UserBookDataFileManager(ReadingSpine spine) {
        mReadingSpine = spine;
    }

    public UserBookDataFileManager(ReadingSpine spine, ReadingChapter chapter) {
        mReadingSpine = spine;
        mChapter = chapter;
    }

    public static ReadingChapter getChapter() {
        return mChapter;
    }

    public void setEpubPath(String epubPath) {
        this.epubPath = epubPath;
    }

    /**
     * 	 북마크 데이터를 파일로 저장 요청하는 메소드 
     *   @return boolean : 성공 시 true 리턴
     */
    public boolean saveBookmarks(ArrayList<Bookmark> bookmarks) {

        try {

            String fileName = getFullPath(BookHelper.bookmarkFileName);
            if( fileName.length() <= 0 ) return false;

            DebugSet.d(TAG, "save Bookmark ................ " + fileName);

            File bookmarkDataFile = new File(fileName);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

            JSONObject object = new JSONObject();
            object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.BOOKMARK);
            object.put(AnnotationConst.FLK_BOOKMARK_VERSION, BookHelper.bookmarkVersion);

            JSONArray array = new JSONArray();
            for(Bookmark bm: bookmarks) {
                array.put(bm.get());
            }
            object.put(AnnotationConst.FLK_BOOKMARK_LIST, array);

            DebugSet.d(TAG, "save array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();

            return true;

        } catch( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    public void saveBookmarkHistory(){
        if(BookHelper.useHistory) {
            __bmHistory.write(getFullPath(BookHelper.bookmarkHistoryFileName));
        }
    }

    public boolean saveLastPosition(Bookmark bm) {

        if( bm == null || (bm!=null && (bm.path.length()==0)) ) {
            try {
                bm = new Bookmark();
                ReadingOrderInfo spine = mReadingSpine.getCurrentSpineInfo();
                if( spine != null )
                    bm.chapterFile = spine.getSpinePath().toLowerCase();

                int pageInChapter = 0;
                UnModifiableArrayList<ReadingOrderInfo> spines = mReadingSpine.getSpineInfos();
                for(int i=0; i< spines.size(); i++){
                    ReadingOrderInfo sp = spines.get(i);
                    if( bm.chapterFile.trim().equals(sp.getSpinePath().toLowerCase().trim()) ){
                        pageInChapter = i + 1;
                        break;
                    }
                }

                bm.percent = ((double)pageInChapter / (double)spines.size()) * 100;
                if(Double.isInfinite(bm.percent) )
                    bm.percent = 0.0;
                DebugSet.e(TAG, "saveLastPosition::percent >> " + bm.percent);
            }
            catch(Exception e) {
                return false;
            }
        }

        boolean bSuccess = false;

        try {

            String filePath = getFullPath(BookHelper.readPositionFileName);
            DebugSet.d(TAG, "save last .................... " + bm.path + " | " + filePath);

            File bookmarkDataFile = new File(filePath);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }

            FileOutputStream output = new FileOutputStream(bookmarkDataFile);

            JSONObject object = new JSONObject();
            //2013.10.31
            //DeviceModel, OSVersion, FileVersion, FileType 추가

            object.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.READPOSITION);
            object.put(AnnotationConst.FLK_READPOSITION_VERSION, "2.0");
            object.put(AnnotationConst.FLK_READPOSITION_MODEL, DeviceInfoUtil.getDeviceModel());
            object.put(AnnotationConst.FLK_READPOSITION_OS_VERSION, DeviceInfoUtil.getOSVersion());
            object.put(AnnotationConst.FLK_READPOSITION_TIME, System.currentTimeMillis()/1000L);
            object.put(AnnotationConst.FLK_READPOSITION_PATH, bm.path);
            object.put(AnnotationConst.FLK_READPOSITION_FILE, BookHelper.getRelFilename(bm.chapterFile) );
            object.put(AnnotationConst.FLK_READPOSITION_CHAPTER_PERCENT, bm.percent);
            object.put(AnnotationConst.FLK_READPOSITION_TOTAL_PERCENT, bm.percent);

            DebugSet.d(TAG, "json array ................. " + object.toString(1));
            output.write(object.toString(1).getBytes());
            output.close();

            bSuccess = true;

        } catch( Exception e ) {
            e.printStackTrace();
            bSuccess = false;
        }

        return bSuccess;
    }

    public void restoreLastPosition() {

        String filePath = getFullPath(BookHelper.readPositionFileName);
        DebugSet.d(TAG, "restore last ........................ " + filePath);

        JSONObject object = EpubFileUtil.getJSONObjectFromFile(filePath);

        try {
            if( object == null ) {
                throw new Exception();
            }

            String file = "";
            if( !object.isNull(AnnotationConst.FLK_READPOSITION_FILE) ) {
                file = object.getString(AnnotationConst.FLK_READPOSITION_FILE);
            }

            if( file.trim().length() <= 0 ) {
                mReadingSpine.setCurrentSpineIndex(0);
            } else {
                file = getFullPath(file);
                if( file.indexOf('\\') != -1 ) {
                    file = file.replace('\\', '/');
                }
                mReadingSpine.setCurrentSpineIndex(file);
            }
        } catch(Exception e) {
            e.printStackTrace();

            // first load시 exception 강제 발생
            mReadingSpine.setCurrentSpineIndex(0);
        }

    }

    /**
     * 	 북마크 파일을 데이터 형태로 불러오기를 요청하는 메소드
     */
    public ArrayList<Bookmark> restoreBookmarks() {

        String fileName = getFullPath(BookHelper.bookmarkFileName);
        JSONObject object = EpubFileUtil.getJSONObjectFromFile(fileName);
        if( object == null ) return new ArrayList<Bookmark>();

        ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
        try {
            DebugSet.d(TAG, "restore Bookmark ................ " + object.toString(1));

            JSONArray array = object.getJSONArray(AnnotationConst.FLK_BOOKMARK_LIST );

            for(int i=0; i<array.length(); i++) {

                JSONObject item = array.getJSONObject(i);

                long uniqueID;
                if( item.isNull(AnnotationConst.FLK_BOOKMARK_ID) ) {
                    uniqueID = System.currentTimeMillis() / 1000L;
                } else {
                    uniqueID = item.getLong(AnnotationConst.FLK_BOOKMARK_ID);
                }

                String creationTime = item.getString(AnnotationConst.FLK_BOOKMARK_CREATION_TIME);
                String file = item.getString(AnnotationConst.FLK_BOOKMARK_FILE);
                String path = item.getString(AnnotationConst.FLK_BOOKMARK_PATH);

                if( file.trim().length() == 0 ) {
                } else {
                    file = getFullPath(file);
                }

                String model = item.has(AnnotationConst.FLK_BOOKMARK_MODEL) ? item.optString(AnnotationConst.FLK_BOOKMARK_MODEL) : DeviceInfoUtil.getDeviceModel();
                String osVersion = item.has(AnnotationConst.FLK_BOOKMARK_OS_VERSION) ? item.optString(AnnotationConst.FLK_BOOKMARK_OS_VERSION) : DeviceInfoUtil.getOSVersion();

                double percent = item.getDouble(AnnotationConst.FLK_BOOKMARK_PERCENT);
                String chapterName = item.getString(AnnotationConst.FLK_BOOKMARK_CHAPTER_NAME);
                int color = item.getInt(AnnotationConst.FLK_BOOKMARK_COLOR);
                String type = item.getString(AnnotationConst.FLK_BOOKMARK_TYPE);

                String text="";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_TEXT) ) {
                    text = item.getString(AnnotationConst.FLK_BOOKMARK_TEXT);
                }

                String extra1="";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA1) )
                    extra1 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA1);

                String extra2 = "";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA2) )
                    extra2 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA2);

                String extra3 = "";
                if( !item.isNull(AnnotationConst.FLK_BOOKMARK_EXTRA3))
                    extra3 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA3);

                if( file.indexOf('\\') != -1 ) {
                    file = file.replace('\\', '/');
                }

                Bookmark bm = new Bookmark("", 0, path);
                bm.chapterFile = file.toLowerCase();
                bm.chapterName = chapterName;
                bm.uniqueID = uniqueID;
                bm.text = text;
                bm.percent = percent;
                bm.page = (int) percent;
                bm.percentInBook = (int) percent;
                bm.creationTime = creationTime;
                bm.color = color;
                bm.type = type;
                bm.extra1 = extra1;
                bm.extra2 = extra2;
                bm.extra3 = extra3;
                bm.deviceModel = model;
                bm.osVersion = osVersion;

                bookmarkList.add(bm);
            }

            if(BookHelper.useHistory)
                __bmHistory.read(getFullPath(BookHelper.bookmarkHistoryFileName));

            return bookmarkList;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Highlight> restoreHighlights() {

        String fileName = getFullPath(BookHelper.annotationFileName);

        JSONObject object = EpubFileUtil.getJSONObjectFromFile(fileName);

        if( object == null ) return new ArrayList<Highlight>();

        ArrayList<Highlight> highlightList = new ArrayList<Highlight>();

        try {
            DebugSet.d(TAG, "restore highlight ................ " + object.toString(1));

            JSONArray array = object.getJSONArray(AnnotationConst.FLK_ANNOTATION_LIST);

            for(int i=0; i<array.length(); i++) {

                JSONObject jobj = array.getJSONObject(i);

                long uniqueID;
                if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_ID) ) {
                    uniqueID = System.currentTimeMillis() / 1000L;
                } else {
                    uniqueID = Long.parseLong(jobj.getString(AnnotationConst.FLK_ANNOTATION_ID));
                }

                String creationTime = jobj.getString(AnnotationConst.FLK_ANNOTATION_CREATION_TIME);
                String file = getFullPath(jobj.getString(AnnotationConst.FLK_ANNOTATION_FILE));
                String startPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_START_ELEMENT_PATH);
                int startChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHILD_INDEX);
                int startCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_START_CHAR_OFFSET);
                String endPath = jobj.getString(AnnotationConst.FLK_ANNOTATION_END_ELEMENT_PATH);
                int endChildIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHILD_INDEX);
                int endCharOffset = jobj.getInt(AnnotationConst.FLK_ANNOTATION_END_CHAR_OFFSET);
                double percent = jobj.getDouble(AnnotationConst.FLK_ANNOTATION_PERCENT);
                String chapterName = jobj.getString(AnnotationConst.FLK_ANNOTATION_CHAPTER_NAME);
                String type = jobj.getString(AnnotationConst.FLK_ANNOTATION_TYPE);
                String text = jobj.getString(AnnotationConst.FLK_ANNOTATION_TEXT);
                String memo = jobj.getString(AnnotationConst.FLK_ANNOTATION_MEMO);
                String extra1 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA1);
                String extra2 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA2);
                String extra3 = jobj.getString(AnnotationConst.FLK_ANNOTATION_EXTRA3);
                String model = jobj.has(AnnotationConst.FLK_ANNOTATION_MODEL) ? jobj.optString(AnnotationConst.FLK_ANNOTATION_MODEL) : DeviceInfoUtil.getDeviceModel();
                String osVersion = jobj.has(AnnotationConst.FLK_ANNOTATION_OS_VERSION) ? jobj.optString(AnnotationConst.FLK_ANNOTATION_OS_VERSION) : DeviceInfoUtil.getOSVersion();

                int colorIndex;
                if( jobj.isNull(AnnotationConst.FLK_ANNOTATION_COLOR) ) {
                    colorIndex = BookHelper.lastHighlightColor;
                }
//                else if(jobj.getInt(AnnotationConst.FLK_ANNOTATION_COLOR) == 5){
//                    colorIndex = BookHelper.lastMemoHighlightColor;
//                }
                else {
                    colorIndex = jobj.getInt(AnnotationConst.FLK_ANNOTATION_COLOR);
                }

                boolean isPercentUpdated=true;
                int page = -1;
                if(jobj.isNull(AnnotationConst.FLK_ANNOTATION_PAGE)){
                    page = (int) percent;
                    percent = 0;
                    isPercentUpdated = false;

                    uniqueID = addPageDataHistory(uniqueID, i);
                } else {
                    page = jobj.getInt(AnnotationConst.FLK_ANNOTATION_PAGE);
                }

                Highlight highlight = new Highlight();
                highlight.highlightID = generateUniqueID(highlightList);
                highlight.startPath = startPath;
                highlight.endPath = endPath;
                highlight.startChild = startChildIndex;
                highlight.startChar = startCharOffset;
                highlight.endChild = endChildIndex;
                highlight.endChar = endCharOffset;
                highlight.deleted = false;
                highlight.annotation = memo.trim().length() > 0 ? true : false;
                highlight.chapterFile = file;
                highlight.text = text;
                highlight.memo = memo;
                highlight.spanId = highlight.highlightID + "_-_2";
                highlight.uniqueID = uniqueID;
                highlight.colorIndex = colorIndex;
                highlight.page = page;
                highlight.percent = percent;
                highlight.percentInBook = (double)page / (double)mReadingSpine.getSpineInfos().size() * 100.0d;
                highlight.creationTime = creationTime;
                highlight.chapterName = chapterName;
                highlight.type = type;
                highlight.extra1 = extra1;
                highlight.extra2 = extra2;
                highlight.extra3 = extra3;
                highlight.deviceModel = model;
                highlight.osVersion = osVersion;
                highlight.isPercentUpdated = isPercentUpdated;

                highlightList.add(highlight);
            }

            FileOutputStream output = new FileOutputStream(fileName);

            JSONObject jobject = new JSONObject();
            jobject.put(AnnotationConst.FLK_DATA_TYPE, AnnotationConst.ANNOTATION);
            jobject.put(AnnotationConst.FLK_ANNOTATION_VERSION, BookHelper.annotationVersion);

            JSONArray jarray = new JSONArray();
            for(Highlight hilite: highlightList) {
                jarray.put(hilite.get());
            }

            jobject.put(AnnotationConst.FLK_ANNOTATION_LIST, jarray);

            DebugSet.d(TAG, "json array ................. " + jobject.toString(1));
            output.write(jobject.toString(1).getBytes());
            output.close();

            if(BookHelper.useHistory) {
                __hlHistory.read(UserBookDataFileManager.getFullPath(BookHelper.annotationHistoryFileName));
            }

            return highlightList;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFullPath(String fileName) {

        String fullName="";
        String file =fileName;

        String path = epubPath;

        if( MyZip.mIsUnzip ) {
            BookHelper.epubFilePath = path;
            if( !MyZip.mUnzipDir.endsWith("/") )
                BookHelper.epubFilePath = path + "/";
        }
        else {
            BookHelper.epubFilePath = "";
        }

        fullName = BookHelper.epubFilePath + file;

        return fullName;
    }

    private String generateUniqueID(ArrayList<Highlight> highlightList) {
        String randID = "kyoboId" + (int)Math.floor(Math.random()*10001);

        for(Highlight h: highlightList) {
            if( h.highlightID.equals(randID) ) {
                randID = "kyoboId" + (int)Math.floor(Math.random()*10001);
            }
        }

        return randID;
    }

    public static AnnotationHistory __hlHistory = new AnnotationHistory();
    public long addPageDataHistory(long highlightID, int i) {
        if( BookHelper.useHistory ){
            long newID = System.currentTimeMillis() / 1000L +i;
            __hlHistory.modifyRemove(highlightID, newID);
            __hlHistory.modifyAdd(newID);
            return newID;
        }
        return highlightID;
    }


    public void mergeBookmark(String mergeBookmarkData) {
        ArrayList<Bookmark> mergeBookmarkList = new ArrayList<Bookmark>();

        try {

            JSONObject jsonObj = new JSONObject(mergeBookmarkData);
            JSONArray array = jsonObj.getJSONArray(AnnotationConst.FLK_BOOKMARK_LIST );

            for(int i=0; i<array.length(); i++) {

                JSONObject item = array.getJSONObject(i);

                long uniqueID = item.getLong(AnnotationConst.FLK_BOOKMARK_ID);
                String creationTime = item.getString(AnnotationConst.FLK_BOOKMARK_CREATION_TIME);
                String file = item.getString(AnnotationConst.FLK_BOOKMARK_FILE);
                String path = item.getString(AnnotationConst.FLK_BOOKMARK_PATH);
                String model = item.has(AnnotationConst.FLK_BOOKMARK_MODEL) ? item.optString(AnnotationConst.FLK_BOOKMARK_MODEL) : DeviceInfoUtil.getDeviceModel();
                String osVersion = item.has(AnnotationConst.FLK_BOOKMARK_OS_VERSION) ? item.optString(AnnotationConst.FLK_BOOKMARK_OS_VERSION) : DeviceInfoUtil.getOSVersion();
                double percent = item.getDouble(AnnotationConst.FLK_BOOKMARK_PERCENT);
                String chapterName = item.getString(AnnotationConst.FLK_BOOKMARK_CHAPTER_NAME);
                int color = item.getInt(AnnotationConst.FLK_BOOKMARK_COLOR);
                String type = item.getString(AnnotationConst.FLK_BOOKMARK_TYPE);
                String text = item.getString(AnnotationConst.FLK_BOOKMARK_TEXT);

                Bookmark bm = new Bookmark("", 0, path);
                bm.chapterFile = file.toLowerCase();
                bm.chapterName = chapterName;
                bm.uniqueID = uniqueID;
                bm.text = text;
                bm.percent = percent;
                bm.page = (int) percent;
                bm.percentInBook = (int) percent;
                bm.creationTime = creationTime;
                bm.color = color;
                bm.type = type;
                bm.deviceModel = model;
                bm.osVersion = osVersion;

                mergeBookmarkList.add(bm);
            }

            ArrayList<Long> historyRemoveData = new ArrayList<Long>();
            ArrayList<Long> historyAddData = new ArrayList<Long>();
            ArrayList<Bookmark> finalSaveData = new ArrayList<Bookmark>();

            for (int i=0; i<mergeBookmarkList.size(); ++i) {

                Bookmark org = mergeBookmarkList.get(i);

                if(org.uniqueID==-1)
                    continue;

                boolean sameData = false;


                for (int j=i+1; j<mergeBookmarkList.size(); ++j){

                    Bookmark currentBookmark = mergeBookmarkList.get(j);

                    if(	org.chapterFile.equalsIgnoreCase(currentBookmark.chapterFile)
                            && org.path.equalsIgnoreCase(currentBookmark.path)){

                        historyRemoveData.add(currentBookmark.uniqueID);
                        mergeBookmarkList.get(j).uniqueID=-1;
                        sameData=true;
                    }
                }

                if (sameData){
                    historyRemoveData.add(org.uniqueID);
                    Bookmark addData = org;
                    addData.uniqueID= System.currentTimeMillis();
                    addData.creationTime = BookHelper.getDate(addData.uniqueID, "yyyy-MM-dd hh:mm:ss");
                    finalSaveData.add(addData);
                    historyAddData.add(addData.uniqueID);
                }else{
                    finalSaveData.add(org);
                }
            }

            for(Long removeData : historyRemoveData){
                __bmHistory.remove(removeData);
            }

            for(Long addData : historyAddData){
                __bmHistory.add(addData);
            }

            saveBookmarkHistory();
            saveBookmarks(finalSaveData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
