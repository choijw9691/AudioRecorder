package com.ebook.epub.viewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {

    private EPubViewer mViewer;
    private AnnotationHistory mBookmarkHistory;

    public BookmarkManager(EPubViewer viewer, AnnotationHistory bookmarkHistory){
        mViewer = viewer;
        mBookmarkHistory = bookmarkHistory;
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
                String extra1 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA1);
                String extra2 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA2);
                String extra3 = item.getString(AnnotationConst.FLK_BOOKMARK_EXTRA3);

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

                mergeBookmarkList.add(bm);
            }

            Set<Bookmark> listSet = new HashSet<Bookmark>(mergeBookmarkList);

            ArrayList<Bookmark> removeDataList = new ArrayList<Bookmark>();

            for(Bookmark orgData : mergeBookmarkList){
                boolean isSame =false;
                for(Bookmark compareData : mergeBookmarkList){
                    if(orgData.uniqueID!=compareData.uniqueID
                            && orgData.chapterFile.equalsIgnoreCase(compareData.chapterFile)
                            && orgData.path.equalsIgnoreCase(compareData.path) ){
                        isSame=true;
                    }
                }

                if(isSame)
                    removeDataList.add(orgData);
            }

            for(Bookmark removeData : removeDataList){
                mBookmarkHistory.remove(removeData.uniqueID);
            }

            ArrayList<Bookmark> finalSaveData = new ArrayList<Bookmark>(listSet);
            for(Bookmark finalData : finalSaveData){
                if(finalData.duplicate){
                    finalData.uniqueID = System.currentTimeMillis();
                    mBookmarkHistory.add(finalData.uniqueID);
                }
            }


//			ArrayList<Bookmark> processedList = new ArrayList<Bookmark>( listSet);
//			ArrayList<Long> historyRemoveData = new ArrayList<Long>();
//			ArrayList<Long> historyAddData = new ArrayList<Long>();
//			ArrayList<Bookmark> finalSaveData = new ArrayList<Bookmark>();
//			
//			for (int i=0; i<mergeBookmarkList.size(); ++i) {
//				
//				Bookmark org = mergeBookmarkList.get(i);
//				
//				if(org.uniqueID==-1)
//					continue;
//
//				boolean sameData = false;
//
//
//				for (int j=i+1; j<mergeBookmarkList.size(); ++j){
//					
//					Bookmark currentBookmark = mergeBookmarkList.get(j);
//					
//					if(	org.chapterFile.equalsIgnoreCase(currentBookmark.chapterFile) 
//							&& org.path.equalsIgnoreCase(currentBookmark.path)){
//
//						historyRemoveData.add(currentBookmark.uniqueID);
//						mergeBookmarkList.get(j).uniqueID=-1;
//						sameData=true;
//					}
//				}
//
//				if (sameData){
//					historyRemoveData.add(org.uniqueID);
//					Bookmark addData = org;
//					addData.uniqueID= System.currentTimeMillis();
//					addData.creationTime = BookHelper.getDate(addData.uniqueID, "yyyy-MM-dd hh:mm:ss");
//					finalSaveData.add(addData);
//					historyAddData.add(addData.uniqueID);
//				}else{
//					finalSaveData.add(org);
//				}
//			}
//			Log.d("TESTTIME", "saveBookmarks ing2 : " + (Double)((System.currentTimeMillis()-startTime)/1000.00));
//			for(Long removeData : historyRemoveData){
//				mBookmarkHistory.remove(removeData);
//			}
//			
//			for(Long addData : historyAddData){
//				mBookmarkHistory.add(addData);
//			}

            mViewer.saveBookmarks(finalSaveData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
