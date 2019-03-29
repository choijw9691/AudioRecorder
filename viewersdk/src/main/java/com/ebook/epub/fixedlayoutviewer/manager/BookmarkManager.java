package com.ebook.epub.fixedlayoutviewer.manager;

import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.viewer.Bookmark;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.epub.viewer.data.ReadingChapter;
import com.ebook.epub.viewer.data.ReadingSpine;

import java.util.ArrayList;


public class BookmarkManager {

    private ArrayList<Bookmark> mBookmarkList;
    private ReadingSpine mSpine;
    private ReadingChapter mChapter;

    public BookmarkManager(ReadingSpine spine, ReadingChapter chapter){
        mBookmarkList = new ArrayList<Bookmark>();
        mSpine = spine;
        mChapter = chapter;
    }

    public void setBookmarkList(ArrayList<Bookmark> list){
        mBookmarkList = list;
    }
    public ArrayList<Bookmark> getBookmarkList() {
        return mBookmarkList;
    }

    /**
     * pageData를 이용한 북마크 추가 세팅
     * @param bookmark
     * @param pageData
     */
    public void setBookmarkData(Bookmark bookmark, FixedLayoutPageData pageData){

        int position = 0;
        if( pageData.getContentsDataList().get(0).getContentsPosition() == 1 ){
            if( pageData.getContentsDataList().get(1).getContentsFilePath().equals("about:blank") )
                position = 0;
            else
                position = 1;
        } else {
            if( pageData.getContentsDataList().get(0).getContentsFilePath().equals("about:blank") )
                position = 1;
            else
                position = 0;
        }

        FixedLayoutPageData.ContentsData data = pageData.getContentsDataList().get(position);

        bookmark.page = data.getContentsPage();
//		bookmark.percentInBook = (double)data.getContentsPage() / (double)mSpine.getSpineInfos().size() * 100.0d;
        bookmark.percentInBook = data.getContentsPage();
        bookmark.percent = data.getContentsPage();

        for (int i = 0; i < mChapter.getChapters().size(); i++) {
            ChapterInfo sp = mChapter.getChapters().get(i);

            if( sp.getChapterFilePath().trim().contains(data.getContentsFilePath().trim()) ){
                bookmark.chapterName = sp.getChapterName();
                break;
            }
        }

        if( bookmark.chapterName == null || bookmark.chapterName.equals(""))
            bookmark.chapterName = data.getContentsFilePath();
//		for( Chapter ch : BookHelper.mBook.getChapters() ){
//			if( ch.mFilename.trim().equals(data.getContentsFilePath().trim()) ){
//				bookmark.chapterName = ch.mName;
//				break;
//			}
//		}
    }

    //	[ssin-bookmark] s : 1page<->2page 정책 적용
    public ArrayList<Bookmark> checkBookmarkInCurrentPage(FixedLayoutPageData pageData){

        ArrayList<Bookmark> currentBookmarkList = new ArrayList<Bookmark>();

        if( mBookmarkList != null ){

            Bookmark bm = null;

//			boolean exist = false;

            String currentFileName = "";
            String currentLeftFileName = "";
            String currentRightFileName = "";

            for( int i = 0 ; i < pageData.getContentsCount(); i++ ){
                FixedLayoutPageData.ContentsData data = pageData.getContentsDataList().get(i);
                if( data.getContentsPosition() == 0 && !data.getContentsFilePath().equalsIgnoreCase("about:blank")){
//					currentFileName = data.getContentsFilePath().toLowerCase();
                    currentLeftFileName  = data.getContentsFilePath().toLowerCase();
                }

                if( data.getContentsPosition() == 1 && !data.getContentsFilePath().equalsIgnoreCase("about:blank")){
                    currentRightFileName  = data.getContentsFilePath().toLowerCase();
                }
            }

//			if(currentFileName.isEmpty()){
//				currentFileName = pageData.getContentsDataList().get(1).getContentsFilePath().toLowerCase();
//			}

            for( int i = 0 ; i<mBookmarkList.size(); i++ ){
                Bookmark bookmark = mBookmarkList.get(i);

                if( bookmark.chapterFile.trim().toLowerCase().equals(currentLeftFileName.trim())){
                    currentBookmarkList.add(bookmark);
                }

                if(bookmark.chapterFile.trim().toLowerCase().equals(currentRightFileName.trim())){
                    currentBookmarkList.add(bookmark);
                }
            }

            return currentBookmarkList;
        }
        return currentBookmarkList;
    }
//	[ssin-bookmark] e : 1page<->2page 정책 적용

//	/**
//	 * 현재 페이지에 북마크 유무 체크
//	 * @param pageData
//	 * @return
//	 */
//	public Bookmark checkBookmarkInCurrentPage(FixedLayoutPageData pageData){
//		if( mBookmarkList != null ){
//
//			Bookmark bm = null;
//
//			boolean exist = false;
//
//			String currentFileName = "";
//			for( int i = 0 ; i < pageData.getContentsCount(); i++ ){
//				ContentsData data = pageData.getContentsDataList().get(i);
//				if( data.getContentsPosition() == 0 && !data.getContentsFilePath().equalsIgnoreCase("about:blank")){
//					currentFileName = data.getContentsFilePath().toLowerCase();
//				}
//			}
//
//			if(currentFileName.isEmpty()){
//				currentFileName = pageData.getContentsDataList().get(1).getContentsFilePath().toLowerCase();
//			}
//
//			for( int i = 0 ; i<mBookmarkList.size(); i++ ){
//				Bookmark bookmark = mBookmarkList.get(i);
//
//				if( bookmark.chapterFile.trim().toLowerCase().equals(currentFileName.trim()) ){
//					exist = true;
//					bm = bookmark;
//					break;
//				}
//			}
//			return bm;
//		}
//		return null;
//	}

    public void deleteBookmark(Bookmark bm){
        if( mBookmarkList != null ){
            mBookmarkList.remove(bm);
        }
    }

    public void addBookmark(Bookmark bm){
        if( mBookmarkList == null ){
            mBookmarkList = new ArrayList<Bookmark>();
        }

        mBookmarkList.add(bm);
    }
}
