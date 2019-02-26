package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebook.epub.viewer.SearchResult;
import com.ebook.epub.viewer.ViewerContainer;
import com.flk.epubviewersample.R;
import com.flk.epubviewersample.activity.BaseBookActivity;

import java.util.ArrayList;

/**
 * @author  syw
 */
public class SearchDialog extends AlertDialog implements OnItemClickListener {
    /**
     */
    public static SearchDialog mThis = null;
    
    Context mContext;
    /**
     */
    BaseBookActivity mParent;
    /**
     */
//    EPubViewer mViewer;
    ViewerContainer mViewer;
    
    LinearLayout mItemContainer;

    View mRootView;
    public ListView mSearchList;
    EditText mKeyword;
    SearchAdapter mAdapter;
    
    ProgressBar mProgress;
    
    String __oldKeyword=""; 
    
    ArrayList<SearchResult> mSearchResults = new ArrayList<SearchResult>();
    
    public static interface OnSelectKeyword {
        public abstract void onSelect(SearchDialog sender, int position, SearchResult result);
    }
    
    public void setOnSelectKeyword(OnSelectKeyword l) {
        mOnSelectKeyword = l;
    }
    
    /**
     */
    OnSelectKeyword mOnSelectKeyword = null;

    public SearchDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public SearchDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public SearchDialog(Context context) {
        super(context);
        init(context);
    }
    
    public SearchDialog(Context context, ViewerContainer viewer) {
        super(context);
        init(context);
        
        mViewer = viewer;
    }
    
    void init(Context context) {
        mContext = context;
        mParent = (BaseBookActivity)mContext;
        
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.search_dialog, null);
        this.setView(mRootView);
    }

    public class SearchAdapter extends BaseAdapter implements ListAdapter {
        LayoutInflater inflator;
        int __selectedPos = -1; 
                
        SearchAdapter() {
            inflator = LayoutInflater.from(mContext);
        }
        
        public void setSelectedPosition(int pos) {
            __selectedPos = pos;
            notifyDataSetChanged();
        }
        public int getSelectedPosition() {
            return __selectedPos;
        }

        @Override
        public int getCount() {
            return mSearchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        @Override
        public long getItemId(int position) {
            return (long)position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            
            if( convertView == null ) {
                v = inflator.inflate(R.layout.item_list, null);
            } else {
                v = convertView;
            }
            
            
            SearchResult item = mSearchResults.get(position);
            if( item != null ) {
                
                LinearLayout container = (LinearLayout)v.findViewById(R.id.itemContainer);
                if(__selectedPos!=-1 && __selectedPos==position) {
                    container.setBackgroundColor(0xc0ff80);
                }
                else {
                    container.setBackgroundColor(0x00000000);
                }

                ImageView iv = (ImageView)v.findViewById(R.id.ivImage);
                TextView chapter = (TextView)v.findViewById(R.id.tvChapter);
                TextView dateTime = (TextView)v.findViewById(R.id.tvDate);
                TextView result = (TextView)v.findViewById(R.id.tvSnippet);
                TextView page = (TextView)v.findViewById(R.id.tvPage);
                
                iv.setVisibility(View.GONE);
                dateTime.setVisibility(View.GONE);
                
                String text = item.text;
//                int index = text.toLowerCase().indexOf(__oldKeyword.toLowerCase());
//                if(index != -1) {
//                    
//                    if( index != 0 ) {
//                        text = "..." + text;
//                        index+=3;
//                    }
                    
//                    if( !text.trim().endsWith(".") ) {
//                        text = text + "...";
//                    }
                    
                    SpannableString sText = new SpannableString(text);
                    sText.setSpan(new BackgroundColorSpan(Color.YELLOW), item.currentKeywordIndex, item.currentKeywordIndex+__oldKeyword.length(), 0);
                    sText.setSpan(new ForegroundColorSpan(Color.BLACK), item.currentKeywordIndex, item.currentKeywordIndex+__oldKeyword.length(), 0);
                    
                    result.setText( sText);
//                }
//					
                chapter.setTextSize(10);
                chapter.setTextColor(Color.WHITE);
                chapter.setTypeface(null,Typeface.BOLD_ITALIC);
                chapter.setText(item.chapterName + ", index : " + item.spineIndex);
                
                BaseBookActivity activity = (BaseBookActivity)mContext;
                
//                DebugSet.d("TAG", "searchResult spineIndex : " + item.spineIndex);
//                if( !activity.mViewer.isPaging() ) {
//                    int percentPage = 0;
//                    
//                    for(Spine sp: BookHelper.mBook.getSpines()) {
//                        String spFile = BookHelper.getFilename(sp.fileName).toLowerCase();
//                        String chFile = BookHelper.getFilename(item.chapterFile).toLowerCase();
//                        
//                        if( spFile.equals(chFile) ) {
//                            int pageInChapter = Math.round((float)((item.percent * (double)sp.nPage ) / 100));
//                            percentPage += pageInChapter;
//                            break;
//                        }
//                        
//                        percentPage += sp.nPage;
//                    }
    
//                    page.setText("" + Math.round(item.percent) + "%");
                if(mViewer.getDocumentVersion().equals("3.0")){
                	page.setText("" + (int)item.percent + "p");
                }else{
                	page.setText("" + Math.round(item.percent) + "%");
                }    
                
//                    page.setText("p." + item.page + "(" + item.percent + ":" + percentPage + ")");
//                }
//                else {
//                    page.setText("p.--"); 
//                }
            }
            
            return v;
        }
        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mThis = this;
        
        mSearchList = (ListView)mRootView.findViewById(android.R.id.list);
        mSearchList.setOnItemClickListener(this);
        mSearchList.setSelected(true);

        mAdapter = new SearchAdapter();
        mSearchList.setAdapter(mAdapter);

        mKeyword = (EditText)mRootView.findViewById(R.id.edKeyword);
        
        mProgress = (ProgressBar)mRootView.findViewById(R.id.progressBar1);
        
        
        Button doSearch = (Button)mRootView.findViewById(R.id.btnSearch);
        doSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String keyword = mKeyword.getText().toString().trim();
                
                if(mViewer != null && keyword.length() > 1 ) {

                    clearResult();
                    
                    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
                    imm.hideSoftInputFromWindow(mKeyword.getWindowToken(), 0); 

                    mViewer.searchText(keyword);
                    __oldKeyword = keyword;
                }
                
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        
        if( mOnSelectKeyword != null ) {
            SearchResult result = mSearchResults.get(pos);
            mOnSelectKeyword.onSelect(this, pos, result);
            
            mAdapter.setSelectedPosition(pos);
            
            dismiss();
        }
    }

    public void clearResult() {
        mSearchResults.clear();
        
        SearchAdapter adapter = (SearchAdapter)mSearchList.getAdapter();
        adapter.notifyDataSetChanged();
    }
    
    public void addResult(SearchResult sr) {
        
        Log.d("DEBUG", "SearchResult Add : " + sr.text);
        
        mSearchResults.add(sr);
        
        SearchAdapter adapter = (SearchAdapter)mSearchList.getAdapter();
        adapter.notifyDataSetChanged();
        
//        mSearchList.setSelection(mSearchResults.size()-1);
    }
    
    public void showProgress(boolean bShow) {
        if( bShow ) {
            mProgress.setVisibility(View.VISIBLE);
        }
        else {
            mProgress.setVisibility(View.INVISIBLE);
        }
    }

}
