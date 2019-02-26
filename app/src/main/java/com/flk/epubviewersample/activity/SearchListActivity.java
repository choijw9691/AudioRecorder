package com.flk.epubviewersample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import java.util.ArrayList;

public class SearchListActivity extends Activity  implements AdapterView.OnItemClickListener {

    public Context mContext;

    private ListView mSearchList;
    private EditText mKeyword;
    private SearchAdapter mAdapter;
    private ProgressBar mProgress;
    private String __oldKeyword="";

    private ViewerContainer mViewer;

    ArrayList<SearchResult> mSearchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        mViewer = BaseBookActivity.mViewer;

        mContext = getApplicationContext();

        mSearchList = (ListView)findViewById(android.R.id.list);
        mSearchList.setOnItemClickListener(this);
        mSearchList.setSelected(true);
        mAdapter = new SearchAdapter();
        mSearchList.setAdapter(mAdapter);

        mKeyword = (EditText)findViewById(R.id.edKeyword);
        mProgress = (ProgressBar)findViewById(R.id.progressBar1);

        Button doSearch = (Button)findViewById(R.id.btnSearch);
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

        mViewer.setOnSearchResult(new ViewerContainer.OnSearchResult() {
            @Override
            public void onStart() {
                showProgress(true);
            }

            @Override
            public void onEnd() {
                showProgress(false);
            }

            @Override
            public void onFound( SearchResult sr) {
                addResult(sr);
            }
        });
    }

    public void clearResult() {
        mSearchResults.clear();

        SearchAdapter adapter = (SearchAdapter)mSearchList.getAdapter();
        adapter.notifyDataSetChanged();
    }

    public void addResult(SearchResult sr) {

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchResult result = mSearchResults.get(position);
        mAdapter.setSelectedPosition(position);
        Intent intent = new Intent();
        intent.putExtra("SEARCH_DATA", result);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
            return false;
        }

        return super.dispatchKeyEvent(event);
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

                SpannableString sText = new SpannableString(text);
                sText.setSpan(new BackgroundColorSpan(Color.YELLOW), item.currentKeywordIndex, item.currentKeywordIndex+__oldKeyword.length(), 0);
                sText.setSpan(new ForegroundColorSpan(Color.BLACK), item.currentKeywordIndex, item.currentKeywordIndex+__oldKeyword.length(), 0);

                result.setText( sText);

                chapter.setTextSize(10);
                chapter.setTextColor(Color.WHITE);
                chapter.setTypeface(null, Typeface.BOLD_ITALIC);
                chapter.setText(item.chapterName + ", index : " + item.spineIndex);

//                    page.setText("" + Math.round(item.percent) + "%");
                if(mViewer.getDocumentVersion().equals("3.0")){
                    page.setText("" + (int)item.percent + "p");
                }else{
                    page.setText("" + Math.round(item.percent) + "%");
                }
//                page.setText("p." + item.page + "(" + item.percent + ":" + percentPage + ")");
//                } else {
//                    page.setText("p.--");
//                }
            }
            return v;
        }

    }
}
