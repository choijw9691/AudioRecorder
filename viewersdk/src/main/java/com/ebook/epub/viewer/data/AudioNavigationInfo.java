package com.ebook.epub.viewer.data;

import java.util.ArrayList;

public class AudioNavigationInfo {

    public String audioPath;
    public String referencePath;
    public String title;

    public boolean hasChildAudioNavigationInfo = false;

    public ArrayList<AudioNavigationInfo> audioNavigationInfos;

    public AudioNavigationInfo(String audioPath, String referencePath, String title, boolean hasChildAudioNavigationInfo, ArrayList<AudioNavigationInfo> audioNavigationInfos){
        this.audioPath = audioPath;
        this.referencePath = referencePath;
        this.title = title;
        this.hasChildAudioNavigationInfo = hasChildAudioNavigationInfo;
        this.audioNavigationInfos = audioNavigationInfos;
    }

    public String getReferenceFilePath(){
        if(referencePath.indexOf("#")!=-1)
            return referencePath.substring(0, referencePath.indexOf("#"));
        return referencePath;
    }

    public String getReferenceFragment(){
        String[] split = referencePath.split("#");
        if(split.length>0 && !split[1].isEmpty())
            return split[1];

        return "";
    }
}
