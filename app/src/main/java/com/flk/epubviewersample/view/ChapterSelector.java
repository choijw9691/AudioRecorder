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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.flk.epubviewersample.R;

/**
 * @author  syw
 */
public class ChapterSelector extends AlertDialog implements OnItemClickListener {
    /**
     */
    static ChapterSelector mThis = null;
    
    Context mContext;

    View mRootView;
    ListView mChapterList;

    UnModifiableArrayList<ChapterInfo> mChapters = new UnModifiableArrayList<ChapterInfo>();
    ChapterInfo currentChapterInfo;

    public ChapterSelector(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public ChapterSelector(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public ChapterSelector(Context context) {
        super(context);
        init(context);
    }
    
    public ChapterSelector(Context context, UnModifiableArrayList<ChapterInfo> chapters, ChapterInfo current) {
        super(context);
        init(context);

//        for(ChapterInfo ch: chapters) {
//            if( ch.isVisible())
//                mChapters.add(ch);
//        }
        
        mChapters = chapters;
        currentChapterInfo = current;
    }
    
    void init(Context context) {
        mContext = context;
        
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.select_dialog, null);
        this.setView(mRootView);
    }

    public static interface OnSelectChapter {
        public abstract void onSelect(ChapterSelector sender, int position, ChapterInfo chapter);
    }
    public void setOnSelectChapter(OnSelectChapter l) {
        mOnSelectChapter = l;
    }
    
    /**
     */
    OnSelectChapter mOnSelectChapter = null;

    
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

            ChapterInfo item = mChapters.get(position);
            if( item != null ) {
                
                ImageView iv = (ImageView)v.findViewById(R.id.ivImage);
                TextView chapterTitle = (TextView)v.findViewById(R.id.tvChapter);
                TextView dateTime = (TextView)v.findViewById(R.id.tvDate);
                TextView snippet = (TextView)v.findViewById(R.id.tvSnippet);
                TextView page = (TextView)v.findViewById(R.id.tvPage);
                
                iv.setVisibility(View.GONE);
                dateTime.setVisibility(View.GONE);
                snippet.setVisibility(View.GONE);
                
                String label = "";
                for( int i = 0 ; i < item.getChapterDepth() ; i++ ){
                	label = "  " + label;
                }
                
                label += item.getChapterName();
                chapterTitle.setText(label);
                if( item.getChapterFilePath().equals(currentChapterInfo.getChapterFilePath()) )
                	chapterTitle.setTextColor(Color.RED);
                else
                	chapterTitle.setTextColor(Color.BLACK);
                DebugSet.d("TAG", "depth : " + item.getChapterDepth());

//                int nPage=0;
//                for(Spine sp: BookHelper.mBook.getSpines() ) {
//                    String chFile = item.mFilename.toLowerCase();
//                    String spFile = sp.fileName.toLowerCase();
//                    
//                    if( spFile.equals(chFile) ) {
//                        break;
//                    }
//                    nPage += sp.nPage;
//                }
                
                page.setText("");
//                if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
//                    page.setText("p." + (nPage + item.mLandPageOffset + 1) );
//                else
//                    page.setText( "p." + (nPage + item.mPortPageOffset + 1) );

            }
            
            return v;
        }
        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mThis = this;
        
        mChapterList = (ListView)mRootView.findViewById(android.R.id.list);
        mChapterList.setOnItemClickListener(this);
        
        ChapterAdapter adapter = new ChapterAdapter();
        mChapterList.setAdapter(adapter);
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
