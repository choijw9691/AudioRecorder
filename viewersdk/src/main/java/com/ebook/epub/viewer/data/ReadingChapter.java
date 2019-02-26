package com.ebook.epub.viewer.data;
import java.util.ArrayList;

import com.ebook.epub.parser.common.ExceptionParameter;
import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.common.PackageVersion;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubContainer;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.parser.ops.XmlChapter;
import com.ebook.epub.parser.ops.XmlNavigationException;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

/**
 * 목차 정보 및 현재 챕터를 관리하는 클래스
 * @author djHeo
 *
 */
public class ReadingChapter {
	
	private EpubFile mEpubFile;
	private ArrayList<ChapterInfo> chapterInfos = new ArrayList<ChapterInfo>();
	private ChapterInfo currentChapter; 
	private int currentChapterIndex = 0;
	
	/**
	 * 생성자에 현재 챕터 인덱스를 받던, 현재 챕터 set 메소드가 있던지 해야함.
	 * @param chapterInfos
	 * @throws XmlNavigationException 
	 * @throws XmlContainerException 
	 * @throws EpubFileSystemException 
	 * @throws XmlPackageException 
	 */
	public ReadingChapter(EpubFile epub) throws XmlPackageException, XmlContainerException, EpubFileSystemException, XmlNavigationException {
		this.mEpubFile = epub;
		this.chapterInfos = modifyChapterHRef(mEpubFile.getChapters(), 0);
		// [ssin] 목차 없는 책도 존재할 수 있어 수정
		if(chapterInfos.size()>0){
			currentChapter = chapterInfos.get(0);
		}
	}

	/**
	 * 목차 정보의 href를 fullPath로 변경한다.
	 * @param chapterList
	 * @return
	 * @throws XmlPackageException
	 * @throws EpubFileSystemException
	 * @throws XmlContainerException
	 */
	private ArrayList<ChapterInfo> modifyChapterHRef(ArrayList<XmlChapter> chapterList, int depth) throws XmlPackageException, EpubFileSystemException, XmlContainerException {
		
		DebugSet.d("TAG", "modifyChapterHRef start");
		
		ArrayList<ChapterInfo> modifyChapters = new ArrayList<ChapterInfo>();
//		EpubContainer container = mEpubFile.getEpubContainer();
		
		FileInfo navigationInfo = EpubFileUtil.getResourceFile(mEpubFile.getPublicationPath(), mEpubFile.getNavigation());
		if( navigationInfo == null )
			throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.NAVIGATION);
		
		String navigationFullPath = navigationInfo.filePath;
		String navigationRoot = navigationFullPath.substring(0, navigationFullPath.lastIndexOf("/"));
		
		for (int i = 0; i < chapterList.size(); i++) {
			XmlChapter chapter = chapterList.get(i);
			
			FileInfo info = EpubFileUtil.getResourceFile(navigationRoot, chapter); //TODO //epub3.0에서 a태그 없이 span만 있는 경우 추가가 안되고 있음. 협의 필요
			if( info == null )
				continue;
			
			String fullPath = info.filePath;
			
			ChapterInfo newChapter = new ChapterInfo(fullPath, chapter.getValue(), depth);
			modifyChapters.add(newChapter);
			if( chapter.getChapterList().size() > 0 ) {
//				newChapter.setChapterInfo(modifyChapterHRef(chapter.getChapterList(), depth + 1));
				modifyChapters.addAll(modifyChapters.size(), modifyChapterHRef(chapter.getChapterList(), depth + 1));
			}
		}
		
		DebugSet.d("TAG", "modifyChapterHRef end");
		return modifyChapters;
	}
	
	public UnModifiableArrayList<ChapterInfo> getChapters() {
		return new UnModifiableArrayList<>(chapterInfos);
	}
	
	public ChapterInfo getCurrentChapter() {
		return currentChapter;
	}
	
	public ChapterInfo getChapterInfoFromPath(String filePath) {
		
		for (int i = 0; i < chapterInfos.size(); i++) {
			
			ChapterInfo info = chapterInfos.get(i);
			if(info.getChapterFilePath().toLowerCase().contains(filePath.toLowerCase())) {
				return info;
			}
		}
		return new ChapterInfo(filePath, "", 0);
	}

	public void setCurrentChapter(ChapterInfo currentChapter) {
		this.currentChapter = currentChapter;
	}
	
	/**
	 * Chapter File Path로 현재 챕터 세팅
	 * @param filePath
	 */
	public void setCurrentChapter(String filePath) {
		
		boolean changed = false;
		
		for (int i = 0; i < chapterInfos.size(); i++) {
			
			ChapterInfo info = chapterInfos.get(i);
			if(info.getChapterFilePath().toLowerCase().contains(filePath.toLowerCase())) {
				this.currentChapter = info;
				currentChapterIndex = i;
				changed = true;
				break;
			}
		}
		
		if( !changed ) {
			this.currentChapter = new ChapterInfo("", "", 0);
		}
			
	}
	
	public int getCurrentChapterIndex() {
		return currentChapterIndex;
	}
	
	public int getChapterIndex(String filePath) {
		
		for (int i = 0; i < chapterInfos.size(); i++) {
			
			ChapterInfo info = chapterInfos.get(i);
			if(info.getChapterFilePath().toLowerCase().contains(filePath.toLowerCase())) {
				return i;
			}
			
		}
		
		return 0; //TODO 0을 주는게 맞느가??
	}
}
