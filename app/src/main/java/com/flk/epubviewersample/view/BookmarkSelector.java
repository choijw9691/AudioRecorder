package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Bookmark;
import com.flk.epubviewersample.R;

import java.util.ArrayList;

/**
 * @author  syw
 */
public class BookmarkSelector extends AlertDialog 
    implements View.OnClickListener, OnItemClickListener {

    /**
     */
    BookmarkSelector mThis = null;
    Context mContext;
    
    View mRootView;
    ListView mList;
    
    private String mVersion;
    
    ArrayList<Bookmark> mBookmark = new ArrayList<Bookmark>();
    
    public static interface OnSelectBookmark {
        public abstract void onSelect(BookmarkSelector sender, Bookmark bmd);
    }
    
    /**
     */
    OnSelectBookmark mOnSelectBookmark = null;
    public void setOnSelectBookmark(OnSelectBookmark l) {
        mOnSelectBookmark = l;
    }
    
    public class BookmarkAdapter extends BaseAdapter implements ListAdapter {
        LayoutInflater inflator;
        
        BookmarkAdapter() {
            inflator = LayoutInflater.from(mContext);
        }
        
        @Override
        public int getCount() {
            return mBookmark.size();
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
            
            Bookmark item = mBookmark.get(position);
            
            if( item != null ) {
                
                ImageView iv = (ImageView)v.findViewById(R.id.ivImage);
                TextView chapterTitle = (TextView)v.findViewById(R.id.tvChapter);
                TextView dateTime = (TextView)v.findViewById(R.id.tvDate);
                TextView snippet = (TextView)v.findViewById(R.id.tvSnippet);
                TextView page = (TextView)v.findViewById(R.id.tvPage);
                
                Drawable dr = mContext.getResources().getDrawable(R.drawable.icon_bookmark_on);
                iv.setImageDrawable(dr);
                
                chapterTitle.setText(item.chapterName);

                String date = BookHelper.getDate(item.uniqueID, "yyyy.MM.dd hh:mm");
                dateTime.setText(date);
                
                String text = item.text;
                if( text.startsWith("file://")) {
                    int index = text.lastIndexOf("/");
                    if( index != -1 ) {
                        text = text.substring(index+1);
                    }
                }
                else {
                    text = text.replace("\n", ".");
                    if( text.length() > 30 ) {
                        text = text.substring(0, 30);
                        text += "...";
                    }
                }
                snippet.setText(text);

//                BaseBookActivity activity = (BaseBookActivity)mContext;
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
    
//                    page.setText("p." + item.page + "(" + item.percent + ":" + percentPage + ")");
//                    page.setText("" + Math.round(item.percentInBook) + "%");
//                if(mVersion.equals("3.0")){
//                	page.setText("" + (int)item.percent + "p");
//				}else{
					page.setText("" + Math.round(item.percentInBook) + "%");
//                }
//                }
//                else {
//                    page.setText("p.--"); 
//                }
            }
            
            return v;
        }
        
    }
    
    public BookmarkSelector(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public BookmarkSelector(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public BookmarkSelector(Context context) {
        super(context);
        init(context);
    }
    
    public BookmarkSelector(Context context, ArrayList<Bookmark> bms) {
        super(context);
        init(context);
        
        mBookmark = bms;
    }
    
    public BookmarkSelector(Context context, ArrayList<Bookmark> bms, String version) {
        super(context);
        init(context);
        mVersion = version;
        mBookmark = bms;
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
        
        mList = (ListView)mRootView.findViewById(android.R.id.list);
        mList.setOnItemClickListener(this);
        
        BookmarkAdapter adapter = new BookmarkAdapter();
        mList.setAdapter(adapter);
        
    }

    @Override
    public void onClick(View v) {
        
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        if( mOnSelectBookmark != null ) {
            Bookmark bookMark = mBookmark.get(pos);
            mOnSelectBookmark.onSelect(this, bookMark);
            
            dismiss();
        }
    }
    
    
    public void updateBookmark(ArrayList<Bookmark> bookMarks) {
        
        mBookmark.clear();
        mBookmark = bookMarks;

        ((BookmarkAdapter)mList.getAdapter()).notifyDataSetChanged();
    }
    
}
