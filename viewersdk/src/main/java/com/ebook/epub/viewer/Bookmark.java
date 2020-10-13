package com.ebook.epub.viewer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Bookmark implements Serializable {
    private static final long serialVersionUID = -5283266883849962135L;

    public String path;
    /** 북마크 위치의 page 값 */
    public int page;
    /** 북마크 위치의 챕터내 % 값 */
    public double percent;

    /** 북마크 위치의 도서전체의 % 값 */
    public double percentInBook;

    /** chapter ID */
    public String chapterId;
    /** 북마크가 위치한 쳅터의 이름 */
    public String chapterName;
    /** 북마크가 위치한 파일 명 */
    public String chapterFile;
    /** 북마크 생성 고유 아이디 */
    public long uniqueID;
    /** 북마크 위치의 문장( or image ) */
    public String text;

    public String creationTime;

    /** image, text) */
    public String type;

    public String extra1,extra2,extra3;

    public String deviceModel ;
    public String osVersion ;

    public int color=0;

    public int left;
    public int top;

    public boolean duplicate=false;

    public int startCharOffset=0;

    public Bookmark() {
        uniqueID = System.currentTimeMillis();
        creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd HH:mm:ss");
        uniqueID /= 1000L;
        deviceModel = DeviceInfoUtil.getDeviceModel();
        osVersion = DeviceInfoUtil.getOSVersion();
        chapterFile = "";
        percent = 0.0;
        percentInBook = 0.0;
        path = "";
        startCharOffset = 0;
    }

    public Bookmark(String chapterId, int page, String path) {
        this.chapterId = chapterId;
        this.path = path;
        this.page = page;
        chapterFile = "";
        chapterName = "";
        text = "";
        type = "";
        extra1 = extra2 = extra3 = "";
        uniqueID = System.currentTimeMillis();
        creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd HH:mm:ss");
        startCharOffset = 0;
    }

    public Bookmark(String path, String chapterFile) {
        this.path = path;
        this.chapterFile = chapterFile;
        text = "";
        type = "";
        extra1 = extra2 = extra3 = "";
        uniqueID = System.currentTimeMillis();
        creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd HH:mm:ss");
    }

    public Bookmark(String chapterId, int page, String path, int startCharOffset) {
        this.chapterId = chapterId;
        this.path = path;
        this.page = page;
        this.startCharOffset = startCharOffset;
        chapterFile = "";
        chapterName = "";
        text = "";
        type = "";
        extra1 = extra2 = extra3 = "";
        uniqueID = System.currentTimeMillis();
        creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd hh:mm:ss");
    }

    public JSONObject get() {
        try {

            if( chapterName.startsWith("*") ) {
                chapterName = chapterName.replace("*", "");
            }

            if( chapterName.startsWith("\t") ) {
                chapterName = chapterName.replace("\t", "");
            }

            JSONObject object = new JSONObject();
            object.put(AnnotationConst.FLK_BOOKMARK_MODEL, deviceModel);
            object.put(AnnotationConst.FLK_BOOKMARK_OS_VERSION, osVersion);
            object.put(AnnotationConst.FLK_BOOKMARK_ID, uniqueID);
            object.put(AnnotationConst.FLK_BOOKMARK_CREATION_TIME, creationTime);
            object.put(AnnotationConst.FLK_BOOKMARK_FILE, BookHelper.getRelFilename(chapterFile));
            object.put(AnnotationConst.FLK_BOOKMARK_PATH, path);
            object.put(AnnotationConst.FLK_BOOKMARK_PERCENT, percent);
            object.put(AnnotationConst.FLK_BOOKMARK_CHAPTER_NAME, chapterName);
            object.put(AnnotationConst.FLK_BOOKMARK_COLOR, color);
            object.put(AnnotationConst.FLK_BOOKMARK_TYPE, type);
            object.put(AnnotationConst.FLK_BOOKMARK_TEXT, text);
            object.put(AnnotationConst.FLK_BOOKMARK_EXTRA1, extra1);
            object.put(AnnotationConst.FLK_BOOKMARK_EXTRA2, extra2);
            object.put(AnnotationConst.FLK_BOOKMARK_EXTRA3, extra3);
            object.put(AnnotationConst.FLK_BOOKMARK_TEXT_INDEX, startCharOffset);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toString() {
        return ""+ uniqueID + "|" + creationTime + " | " + chapterFile + " | " + page + " | " + path + " | " + percent + " | extra1: " + extra1;
    }

    @Override
    public int hashCode(){
        return (this.chapterFile.hashCode() + this.path.hashCode());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Bookmark){
            Bookmark temp = (Bookmark) obj;
            if(this.chapterFile.equalsIgnoreCase(temp.chapterFile) && this.path.equalsIgnoreCase(temp.path)){
                temp.duplicate=true;
                return true;
            }
        }
        return false;
    }
}