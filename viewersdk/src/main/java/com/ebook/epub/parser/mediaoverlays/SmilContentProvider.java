package com.ebook.epub.parser.mediaoverlays;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.util.Log;

import com.ebook.epub.viewer.EpubFileUtil;

/**
@class SmilContentProvider
@brief SMIL content 정보 제공 클래스
 */
public class SmilContentProvider {

	private String smilPath="";
	
	private SmilDocumentReader smilReader; 
	private LinkedHashMap<String, LinkedHashMap<String, SmilSync>> smilSyncInfo;
	
	public SmilContentProvider(){}
	
	public SmilDocumentReader getSmilReader() {
		return smilReader;
	}

	public LinkedHashMap<String, LinkedHashMap<String, SmilSync>> getSmilSyncInfo() {
		return smilSyncInfo;
	}
	
	public void load(SmilDocumentReader reader){
		if(reader!=null){
			smilReader = reader;
			initialize();
		}
	}
	
	private void initialize(){
		
		smilSyncInfo = new LinkedHashMap<String, LinkedHashMap<String,SmilSync>>();
		
		ArrayList<ParElement> parNodes = smilReader.getParNodes();
		for(int index=0; index<parNodes.size(); index++){
			SmilSync smilSync = new SmilSync();
			smilSync.chapterFilePath = EpubFileUtil.getResourceFile(smilPath, parNodes.get(index).getTextElement().getUriFilePath()).filePath;
			smilSync.fragment = parNodes.get(index).getTextElement().getUriFragment();
			smilSync.audioFilePath =  EpubFileUtil.getResourceFile(smilPath,parNodes.get(index).getAudioElement().getSrc()).filePath;
			smilSync.audioEndTime = parNodes.get(index).getAudioElement().getClipEnd();
			smilSync.audioStartTime = parNodes.get(index).getAudioElement().getClipBegin();
			smilSync.audioDurationTime = smilSync.audioEndTime-smilSync.audioStartTime;
//			smilSync.optionOfEscapability
//			smilSync.optionOfSkippability
			
			if(smilSyncInfo.get(smilSync.chapterFilePath)==null){
				LinkedHashMap<String, SmilSync> map = new LinkedHashMap<String, SmilSync>();
//				map.put(smilSync.fragment, smilSync);
				map.put(smilSync.chapterFilePath+"#"+smilSync.fragment, smilSync);
				smilSyncInfo.put(smilSync.chapterFilePath, map);
			} else{
//				smilSyncInfo.get(smilSync.chapterFilePath).put(smilSync.fragment, smilSync);
				smilSyncInfo.get(smilSync.chapterFilePath).put(smilSync.chapterFilePath+"#"+smilSync.fragment, smilSync);
			}
		}
	}
	
	public void release(){
		smilReader = null;
	}
	
	/** fragment list for chapter */
	public ArrayList<String> getPlayList(String path){
		
		ArrayList<String> playList = new ArrayList<String>();
		
		LinkedHashMap<String,SmilSync> map = smilSyncInfo.get(path);
		Iterator<String> iter = map.keySet().iterator();
		while(iter.hasNext()){
			playList.add(map.get(iter.next()).fragment);
		}   
		return playList;
	}
	
	/** smil info for par */
	public SmilSync getSmilSync(String path, String fragment){
		
		LinkedHashMap<String,SmilSync> map = smilSyncInfo.get(path);
		return map.get(fragment);
	}
	
	public LinkedHashMap<String, SmilSync> getSmilSyncsByFilePath(String filePath){
		return smilSyncInfo.get(filePath);
	}
	
	public void setSmilPath(String path) {
		this.smilPath = path.substring(0,path.lastIndexOf("/"));
	}
	
//	public void setPublicationPath(String path) {
//	this.publicationPath = path;
//}
}
