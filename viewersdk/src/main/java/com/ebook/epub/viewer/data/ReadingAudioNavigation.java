package com.ebook.epub.viewer.data;

import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ops.XmlLi;
import com.ebook.epub.parser.ops.XmlNav;
import com.ebook.epub.parser.ops.XmlOl;
import com.ebook.epub.viewer.EpubFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ReadingAudioNavigation {

    private String navigationRootPath;

    private ArrayList<AudioNavigationInfo> audioNavigationInfos = new ArrayList<>();

    public ReadingAudioNavigation(EpubFile epubFile) {

        FileInfo navigationInfo = EpubFileUtil.getResourceFile(epubFile.getPublicationPath(), epubFile.getNavigation());
        navigationRootPath = navigationInfo.filePath.substring(0, navigationInfo.filePath.lastIndexOf("/"));

        setReadingAudioNavigation(epubFile);
    }


    private void setReadingAudioNavigation(EpubFile epubFile) {

        XmlNav listOfAudio = epubFile.getNavigationAudio();
        if(listOfAudio==null)
            return;

        XmlOl ol = listOfAudio.getOl();

        parseNavWithLoa(ol, audioNavigationInfos);
    }

    private void parseNavWithLoa(XmlOl ol, ArrayList<AudioNavigationInfo> audioNavigationInfos ){

        ArrayList<XmlLi> lis = ol.getLis();

        for(XmlLi li : lis){

            String audioPath="";
            String refPath="";
            String title="";
            boolean hasChild=false;

            if(li.getA()!=null){
                audioPath = li.getA().gethRef();
                refPath = li.getA().getDataValue();
                title = li.getA().getValue();

                if(!audioPath.startsWith("http")){
                    try {
                        audioPath = new File(navigationRootPath, audioPath).getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(refPath!=null || !refPath.isEmpty()){
                    try {
                        refPath = new File(navigationRootPath, refPath).getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if(li.getSpan()!=null){
                title = li.getSpan().getValue();
            }

            ArrayList<AudioNavigationInfo> children = new ArrayList<>();
            if(li.getOl()!=null){
                hasChild=true;
                parseNavWithLoa(li.getOl(), children);
            }


            audioNavigationInfos.add(new AudioNavigationInfo(audioPath, refPath, title, hasChild, children));
        }
    }

    public UnModifiableArrayList<AudioNavigationInfo> getAudioNavigations(){
        return new UnModifiableArrayList(audioNavigationInfos);
    }
}
