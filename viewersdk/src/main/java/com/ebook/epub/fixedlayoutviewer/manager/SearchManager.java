package com.ebook.epub.fixedlayoutviewer.manager;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;

import com.ebook.epub.common.Defines;
import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.SearchResult;
import com.ebook.epub.viewer.data.ReadingSpine;

import java.util.ArrayList;

public class SearchManager {

    private Handler mViewerHandler;
    private ReadingSpine mSpine;

    public SearchManager(Handler handler, ReadingSpine spine){
        mViewerHandler = handler;
        mSpine = spine;
    }

    public int getIndexBySearchResult(SearchResult sr, ArrayList<FixedLayoutPageData> pageDataList){

        for(int i = 0 ; i<pageDataList.size(); i++){
            FixedLayoutPageData pageData = pageDataList.get(i);

            for( int j=0; j<pageData.getContentsCount(); j++ ){
                FixedLayoutPageData.ContentsData contents = pageData.getContentsDataList().get(j);

                String srFilePath = sr.chapterFile.toLowerCase().trim();
                String ctFilePath = contents.getContentsFilePath().toLowerCase().trim();
                if( srFilePath.equals(ctFilePath) )
                    return contents.getContentsPage();
            }
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    public void searchText(String keyword, ArrayList<FixedLayoutPageData> pageDataList){
        SearchTask st = new SearchTask(keyword, pageDataList);
        st.execute();
    }

    @SuppressWarnings("rawtypes")
    class SearchTask extends AsyncTask {

        String keyword;
        ArrayList<FixedLayoutPageData> pageDataList;

        public SearchTask(String keyword, ArrayList<FixedLayoutPageData> list) {
            this.keyword = keyword;
            this.pageDataList = list;
        }

        @Override
        protected Object doInBackground(Object... arg0) {

            for(int i=0; i<pageDataList.size(); i++){

                FixedLayoutPageData pageData = pageDataList.get(i);

                for( int j=0; j<pageData.getContentsCount(); j++ ){

                    FixedLayoutPageData.ContentsData contents = pageData.getContentsDataList().get(j);

                    if( contents.getContentsString() == null || contents.getContentsString().trim().length() <= 0 ){
//        				HtmlContentsManager manager = new HtmlContentsManager();
                        continue;
                    }

                    String fileStr = "";
                    String bodyText = "";
                    fileStr = contents.getContentsString();
                    bodyText = Html.fromHtml( BookHelper.getHtmlBody(fileStr) ).toString();

                    int keyPos = 0;

                    int index = bodyText.indexOf(keyword, 0);
                    while(index>=0) {

                        int textLen = bodyText.length();
                        int startPos = index-20;
                        int endPos = index + keyword.length() + 30;
                        int leftStartLength = 0;
                        int leftEndLength = 0;

                        if(startPos < 0 ){
                            leftStartLength = Math.abs(index-20);
                            startPos = 0;
                        }

                        if(endPos>textLen){
                            leftEndLength = Math.abs(endPos-textLen);
                            endPos = textLen;
                        }

                        if(leftStartLength > 0 && endPos+leftStartLength<=textLen){
                            endPos = endPos+leftStartLength;
                        } else if(leftEndLength > 0 && startPos-leftEndLength>=0){
                            startPos =  startPos-leftEndLength;
                        }

                        String snippet = bodyText.substring(startPos, endPos);
                        DebugSet.d("TAG", "found keyword : " + keyword + "[" + keyPos + "] : " + snippet);

                        int searchKeywordIndex = index - startPos;
                        if(searchKeywordIndex >= snippet.length())
                            searchKeywordIndex = endPos - snippet.length();
                        if(searchKeywordIndex < 0)
                            searchKeywordIndex = 0;

                        int spineIndex = contents.getContentsPage()-1;
                        double currentPercent = contents.getContentsPage() / mSpine.getSpineInfos().size();

                        SearchResult sr = new SearchResult();
                        sr.chapterFile = contents.getContentsFilePath();
                        sr.keyword = keyword;
                        sr.text = snippet;
                        sr.pageOffset = keyPos;
                        sr.percent = contents.getContentsPage();
                        sr.spineIndex = spineIndex;
                        sr.currentKeywordIndex = searchKeywordIndex;
                        mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_SEARCH_RESULT,sr));
                        keyPos ++;
                        index = bodyText.indexOf(keyword, index+keyword.length());
                    }
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            mViewerHandler.sendMessage(mViewerHandler.obtainMessage(Defines.FIXEDLAYOUT_SEARCH_END));
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    };
}
