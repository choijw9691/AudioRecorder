package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.flk.epubviewersample.R;

/**
 * @author  syw
 */
public class ChapterSelector extends AlertDialog implements OnItemClickListener {
    ChapterSelector mThis = null;
    Context mContext;
    View mRootView;
    ListView mChapterList;
    UnModifiableArrayList<ChapterInfo> mChapters = new UnModifiableArrayList<>();
    ChapterInfo currentChapterInfo;
    OnSelectChapter mOnSelectChapter = null;
    public interface OnSelectChapter {
        void onSelect(ChapterSelector sender, int position, ChapterInfo chapter);
    }

    public ChapterSelector(Context context) {
        super(context);
        init(context);
    }
    
    public ChapterSelector(Context context, UnModifiableArrayList<ChapterInfo> chapters, ChapterInfo current) {
        super(context);
        mChapters = chapters;
        currentChapterInfo = current;
        init(context);
    }
    
    void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.select_dialog, null);
        this.setView(mRootView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        mChapterList = mRootView.findViewById(android.R.id.list);
        mChapterList.setOnItemClickListener(this);
        ChapterAdapter adapter = new ChapterAdapter();
        mChapterList.setAdapter(adapter);
    }

    public void setOnSelectChapter(OnSelectChapter l) {
        mOnSelectChapter = l;
    }

    public class ChapterAdapter extends BaseAdapter implements ListAdapter {

        LayoutInflater inflator;

        ChapterAdapter() {
            inflator = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mChapters.size();
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
            v.findViewById(R.id.tvSnippet).setVisibility(View.GONE);
            ChapterInfo item = mChapters.get(position);
            if( item != null ) {
                TextView chapterTitle = v.findViewById(R.id.tvChapter);
                String label = "";
                for( int i = 0 ; i < item.getChapterDepth() ; i++ ){
                	label = "  " + label;
                }
                label += item.getChapterName();
                chapterTitle.setText(label);
                chapterTitle.setTextColor(Color.BLACK);
                if( item.getChapterFilePath().equals(currentChapterInfo.getChapterFilePath())) {
                    if( currentChapterInfo.getChapterId().isEmpty()){
                        chapterTitle.setTextColor(Color.RED);
                    } else if ( !currentChapterInfo.getChapterId().isEmpty() && item.getChapterId().equals(currentChapterInfo.getChapterId())) {
                        chapterTitle.setTextColor(Color.RED);
                    }
                }
            }
            return v;
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        if( mOnSelectChapter != null ) {
        	ChapterInfo chapter = mChapters.get(pos);
            if( chapter.getChapterFilePath().length() > 0 ){
            	mOnSelectChapter.onSelect(this, pos, chapter);
            	dismiss();
            }
        }
    }
}
