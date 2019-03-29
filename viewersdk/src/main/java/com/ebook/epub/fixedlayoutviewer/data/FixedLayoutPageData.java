package com.ebook.epub.fixedlayoutviewer.data;

import java.util.ArrayList;

public class FixedLayoutPageData {

    public class ContentsData {

        private String 		mContentsFilePath; 		// html file path
        private String 		mContentsString; 		// html file String
        private int 		mContentsWidth;			// contents viewport width
        private int 		mContentsHeight;		// contents viewport height
        private int 		mContentsPosition;		// left : 0, right : 1
        private int			mContentsPage;			// 페이지 번호(spine index + 1)
        private int 		mContentsScrollIndex;  	// 해당 컨텐츠의 scroll index
        private float		mContentsInitialScale;

        public ContentsData(String path, int width, int height, int position, int page, int scrollIndex){
            mContentsFilePath = path;
            mContentsWidth = width;
            mContentsHeight = height;
            mContentsPosition = position;
            mContentsPage = page;
            mContentsScrollIndex = scrollIndex;
        }

        public void setContentsFilePath(String contentsFilePath) {
            this.mContentsFilePath = contentsFilePath;
        }
        public void setContentsString(String contentsString) {
            this.mContentsString = contentsString;
        }
        public void setContentsWidth(int contentsWidth) {
            this.mContentsWidth = contentsWidth;
        }
        public void setContentsHeight(int contentsHeight) {
            this.mContentsHeight = contentsHeight;
        }
        public void setContentsPosition(int contentsPosition) {
            this.mContentsPosition = contentsPosition;
        }
        public void setContentsInitalScale(float scale) {
            this.mContentsInitialScale = scale;
        }

        public String getContentsFilePath() {
            return mContentsFilePath;
        }
        public String getContentsString() {
            return mContentsString;
        }
        public int getContentsWidth() {
            return mContentsWidth;
        }
        public int getContentsHeight() {
            return mContentsHeight;
        }
        public int getContentsPosition() {
            return mContentsPosition;
        }
        public int getContentsPage() {
            return mContentsPage;
        }
        public int getContentsScrollIndex(){
            return mContentsScrollIndex;
        }
        public float getContentsInitalScale() {
            return mContentsInitialScale;
        }
    }

    //	private ArrayList<ContentsData> mContentsDataList;
    private ArrayList<ContentsData> mContentsDataList=new ArrayList<>();

    public ArrayList<ContentsData> getContentsDataList() {
        return mContentsDataList;
    }

    public void addContentsList(String path, int width, int height, int position, int page, int scrollIndex){
        if( mContentsDataList == null )
            mContentsDataList = new ArrayList<>();
        mContentsDataList.add(new ContentsData(path, width, height, position, page, scrollIndex));
    }

    public int getContentsCount() {
        return mContentsDataList.size();
    }
}

