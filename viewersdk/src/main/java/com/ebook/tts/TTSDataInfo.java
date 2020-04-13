package com.ebook.tts;

import org.json.JSONException;
import org.json.JSONObject;

public class TTSDataInfo {
	private String text;
	private String xPath;
	private int startOffset;
	private int endOffset;
	private String filePath;

	public TTSDataInfo(String text, String path, int start, int end, String filePath){
		this.text = text;
		this.xPath = path;
		this.startOffset = start;
		this.endOffset = end;
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getText() {
		return text;
	}

	public String getXPath() {
		return xPath;
	}
	
	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public JSONObject getJSONObjectTTSData(){
		try{
			JSONObject object = new JSONObject();
	        object.put("path", xPath);
	        object.put("text", text);
	        object.put("start", startOffset);
	        object.put("end", endOffset);
	        return object;
		} catch(JSONException e){
			return null;
		}
	}
}
