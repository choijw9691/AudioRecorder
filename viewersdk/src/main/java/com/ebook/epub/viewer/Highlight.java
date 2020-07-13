package com.ebook.epub.viewer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Highlight implements Serializable {
    private static final long serialVersionUID = 4462428130891863851L;
    
    public String highlightID;
    public String startPath;
    public int startChild;
    public int startChar;
    public String endPath;
    public int endChild;
    public int endChar;
    public boolean deleted=false;
    public boolean annotation;
    
    public String deviceModel;
    public String osVersion;
    
    public String chapterId="";
    public String chapterName="";
    public String chapterFile="";
    public String text="";
    public String memo="";
    public int x;
    public int y;
    
    public String spanId="";
    
    public long uniqueID=0;
    public String creationTime="";
    
    public int page;
    public double percent;
    public double percentInBook;
    public int colorIndex=-1;
    
    public String extra1="";
    public String extra2="";
    public String extra3="";
    
    public String type="";

    public boolean isPercentUpdated;
    
    public Highlight() {
    	uniqueID = System.currentTimeMillis();
    	creationTime = BookHelper.getDate(uniqueID, "yyyy-MM-dd HH:mm:ss");
    }

    public String getId() {
        return highlightID;
    }
    
    // 이 메소드는 javascript와의 데이타 교환용으로 사용한다.
    public JSONObject convertJsonData() {
        
        try {
            JSONObject object = new JSONObject();
            object.put("highlightID", highlightID);
            object.put("startElementPath", startPath);
            object.put("startChildIndex", startChild);
            object.put("startCharOffset", startChar);
            object.put("endElementPath", endPath);
            object.put("endChildIndex", endChild);
            object.put("endCharOffset", endChar);
            object.put("isDeleted", deleted);
            object.put("isAnnotation", annotation);
            object.put("file", chapterFile);
            object.put("text", text);
            object.put("memo", memo);
            object.put("spanId", spanId);
            object.put("uniqueID", ""+uniqueID);
            object.put("page", page);
            object.put("colorIndex", colorIndex);
            object.put("percent", ""+percent);
            object.put("chapterId", chapterId);
            object.put("chapterName", chapterName);
            object.put("creationTime", creationTime);
            object.put("extra1", extra1);
            object.put("extra2", extra2);
            object.put("extra3", extra3);
            
            return object;
        
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    //
    // 이 함수는 데이타 마이그레이션 및 데이타 저장용으로 사용한다.
    //
    public JSONObject get() {
        
        try {
            
            JSONObject object = new JSONObject();
            
            object.put(AnnotationConst.FLK_ANNOTATION_MODEL, deviceModel);
            object.put(AnnotationConst.FLK_ANNOTATION_OS_VERSION, osVersion);
            object.put(AnnotationConst.FLK_ANNOTATION_ID, ""+uniqueID);
            object.put(AnnotationConst.FLK_ANNOTATION_CREATION_TIME, creationTime);
            object.put(AnnotationConst.FLK_ANNOTATION_FILE, BookHelper.getRelFilename(chapterFile));
            object.put(AnnotationConst.FLK_ANNOTATION_START_ELEMENT_PATH, startPath);
            object.put(AnnotationConst.FLK_ANNOTATION_START_CHILD_INDEX, startChild);
            object.put(AnnotationConst.FLK_ANNOTATION_START_CHAR_OFFSET, startChar);
            object.put(AnnotationConst.FLK_ANNOTATION_END_ELEMENT_PATH, endPath);
            object.put(AnnotationConst.FLK_ANNOTATION_END_CHILD_INDEX, endChild);
            object.put(AnnotationConst.FLK_ANNOTATION_END_CHAR_OFFSET, endChar);
            object.put(AnnotationConst.FLK_ANNOTATION_PERCENT, percent);
            object.put(AnnotationConst.FLK_ANNOTATION_CHAPTER_NAME, chapterName);
            
            if( colorIndex == -1 )
                colorIndex = BookHelper.lastHighlightColor;
            object.put(AnnotationConst.FLK_ANNOTATION_COLOR, colorIndex);
            
            if( memo.length() > 0 ) {
                object.put(AnnotationConst.FLK_ANNOTATION_TYPE, AnnotationConst.FLK_ANNOTATION_TYPE_MEMO);
            }
            else {
                object.put(AnnotationConst.FLK_ANNOTATION_TYPE, AnnotationConst.FLK_ANNOTATION_TYPE_HIGHLIGHT);
            }
            
            object.put(AnnotationConst.FLK_ANNOTATION_TEXT, text);
            object.put(AnnotationConst.FLK_ANNOTATION_MEMO, memo);
            object.put(AnnotationConst.FLK_ANNOTATION_EXTRA1, extra1);
            object.put(AnnotationConst.FLK_ANNOTATION_EXTRA2, extra2);
            object.put(AnnotationConst.FLK_ANNOTATION_EXTRA3, extra3);
            object.put(AnnotationConst.FLK_ANNOTATION_PAGE, page);
            
            return object;
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 주석동기화 에서 병합후 저장용 
     * 
     */
    public JSONObject get1() {
    	
    	try {
    		
    		JSONObject object = new JSONObject();
    		
    		object.put(AnnotationConst.FLK_ANNOTATION_MODEL, deviceModel);
            object.put(AnnotationConst.FLK_ANNOTATION_OS_VERSION, osVersion);
    		object.put(AnnotationConst.FLK_ANNOTATION_ID, ""+uniqueID);
    		object.put(AnnotationConst.FLK_ANNOTATION_CREATION_TIME, creationTime);
    		object.put(AnnotationConst.FLK_ANNOTATION_FILE, chapterFile);
    		object.put(AnnotationConst.FLK_ANNOTATION_START_ELEMENT_PATH, startPath);
    		object.put(AnnotationConst.FLK_ANNOTATION_START_CHILD_INDEX, startChild);
    		object.put(AnnotationConst.FLK_ANNOTATION_START_CHAR_OFFSET, startChar);
    		object.put(AnnotationConst.FLK_ANNOTATION_END_ELEMENT_PATH, endPath);
    		object.put(AnnotationConst.FLK_ANNOTATION_END_CHILD_INDEX, endChild);
    		object.put(AnnotationConst.FLK_ANNOTATION_END_CHAR_OFFSET, endChar);
    		object.put(AnnotationConst.FLK_ANNOTATION_PERCENT, percent);
    		object.put(AnnotationConst.FLK_ANNOTATION_CHAPTER_NAME, chapterName);
    		
    		if( colorIndex == -1 )
    			colorIndex = BookHelper.lastHighlightColor;
    		object.put(AnnotationConst.FLK_ANNOTATION_COLOR, colorIndex);
    		
    		if( memo.length() > 0 ) {
    			object.put(AnnotationConst.FLK_ANNOTATION_TYPE, AnnotationConst.FLK_ANNOTATION_TYPE_MEMO);
    		}
    		else {
    			object.put(AnnotationConst.FLK_ANNOTATION_TYPE, AnnotationConst.FLK_ANNOTATION_TYPE_HIGHLIGHT);
    		}
    		
    		object.put(AnnotationConst.FLK_ANNOTATION_TEXT, text);
    		object.put(AnnotationConst.FLK_ANNOTATION_MEMO, memo);
    		object.put(AnnotationConst.FLK_ANNOTATION_EXTRA1, extra1);
    		object.put(AnnotationConst.FLK_ANNOTATION_EXTRA2, extra2);
    		object.put(AnnotationConst.FLK_ANNOTATION_EXTRA3, extra3);
            object.put(AnnotationConst.FLK_ANNOTATION_PAGE, page);
            
    		return object;
    		
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	
    	return null;
    }
    public int getLength() {
        return text.length();
    }
    
    public boolean isMemo() {
        return memo.length() > 0;
    }
    
    public String toString() {
        String s =  " id: " + highlightID + " StartPath: " + startPath + " EndPath: " + endPath + 
                " StartOffset: " + startChar+ " EndOffset: " + endChar + 
                " isAnnotation: " + annotation;
        
        return s;
    }
    
}
