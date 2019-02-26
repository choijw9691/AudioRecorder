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

    public Bookmark() {
        this.uniqueID = System.currentTimeMillis();
        this.creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd hh:mm:ss");
        this.uniqueID /= 1000L;
        this.deviceModel = DeviceInfoUtil.getDeviceModel();
        this.osVersion = DeviceInfoUtil.getOSVersion();

        chapterFile = "";
        percent = 0.0;
        percentInBook = 0.0;
        path = "";
    }
    public Bookmark(String chapterId, int page, String path) {
        this.chapterId = chapterId;
        this.path = path;
        this.page = page;
        this.chapterFile = "";
        this.chapterName = "";
        this.text = "";
        this.type = "";
        extra1 = extra2 = extra3 = "";

        this.uniqueID = System.currentTimeMillis();
        this.creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd hh:mm:ss");
//        this.uniqueID /= 1000L;
    }
    public Bookmark(String path, String file) {
        this.path = path;
        this.chapterFile = file;
        this.text = "";
        this.type = "";
        extra1 = extra2 = extra3 = "";

        this.uniqueID = System.currentTimeMillis();
        this.creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd hh:mm:ss");
//        this.uniqueID /= 1000L;
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
            if(this.chapterFile.equals(temp.chapterFile) && this.path.equals(temp.path)){
                temp.duplicate=true;
                return true;
            }
        }
        return false;
    }
}

