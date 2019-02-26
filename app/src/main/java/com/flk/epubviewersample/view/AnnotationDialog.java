package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.ViewerContainer;
import com.flk.epubviewersample.R;
import com.flk.epubviewersample.activity.BaseBookActivity;

import java.util.ArrayList;


public class AnnotationDialog extends AlertDialog implements OnItemClickListener {

    public static AnnotationDialog mThis = null;
    public String mVersion;
    Context mContext;
    /**
     */
    BaseBookActivity mParent;
    /**
     */
    ViewerContainer mViewer;
    
    LinearLayout mItemContainer;

    View mRootView;
    public ListView mList;
    AnnoAdapter mAdapter;
    
    ArrayList<Highlight> mItems= new ArrayList<Highlight>();
    String[] colorArray = {"#FFC000", "#C4ED61", "#B0CBF5", "#FFA8D2", "#D9B0EA" };

    public interface OnAnnotationItem {
        public abstract void onSelect(Highlight high);
        public abstract void onShow();
    }
    
    OnAnnotationItem mOnAnnotationItem = null;
    public void setOnAnnotationItem(OnAnnotationItem l) {
        mOnAnnotationItem = l;
    }
    
    public class AnnoAdapter extends BaseAdapter implements ListAdapter {
        LayoutInflater inflator;
        int __selectedPos = -1; 
                
        AnnoAdapter() {
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
            return mItems.size();
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
            
            Highlight item = mItems.get(position);
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
                TextView snippet = (TextView)v.findViewById(R.id.tvSnippet);
                TextView page = (TextView)v.findViewById(R.id.tvPage);

                iv.setBackgroundColor(BookHelper.getColorInt(item.colorIndex));
                
                String text = item.text;
                if( text.length() > 30 ) {
                    text = text.substring(0,30);
                    text += "...";
                }
                chapter.setText(text);
                
//                dateTime.setText(BookHelper.getDate(item.uniqueID, "yy/MM/dd hh:mm:ss"));
                dateTime.setText(item.creationTime);
                snippet.setText( "메모 : "+ Uri.decode(item.memo) );
                if( item.memo.length() > 0 )
                	snippet.setVisibility(View.VISIBLE);
                else 
                	snippet.setVisibility(View.GONE);
                
				page.setText("" + Math.round(item.percentInBook) + "%");
            }
            
            return v;
        }
        
    }

    
    public AnnotationDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public AnnotationDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public AnnotationDialog(Context context) {
        super(context, android.R.style.Theme_Light);
        init(context);
    }
    
    public AnnotationDialog(Context context, ArrayList<Highlight> items, String version, ViewerContainer viewer) {
        super(context, android.R.style.Theme_Translucent);
        init(context);
        mVersion = version;
        mItems = items;
        mViewer = viewer;
    }
    
    void init(Context context) {
        mContext = context;
        mParent = (BaseBookActivity)mContext;
        
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.note_dialog, null);
        this.setView(mRootView);
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mThis = this;
        
        mList = (ListView)mRootView.findViewById(android.R.id.list);
        mList.setOnItemClickListener(this);
        mList.setSelected(true);

        mAdapter = new AnnoAdapter();
        mList.setAdapter(mAdapter);

        Button sync = (Button)mRootView.findViewById(R.id.btnSync);
        Button merge = (Button)mRootView.findViewById(R.id.btnMerge);
        Button show = (Button)mRootView.findViewById(R.id.btnShow);
        sync.setOnClickListener(new ClickCallback());
        merge.setOnClickListener(new ClickCallback());
        show.setOnClickListener(new ClickCallback());
    }

    
    public void updateHighlights(ArrayList<Highlight> highlights) {
        mItems = highlights;
        mAdapter.notifyDataSetChanged();
    }
    
    
    class ClickCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            
            switch(v.getId()) {
                case R.id.btnSync: {
//                	SyncDataManager manager = new SyncDataManager();
//                	String result = manager.mergeAnnotation(
//                			BookHelper.getFileString("server_annotation.flk"), 
//                			BookHelper.getFileString(""), 
//                			"");
                    break;
                }
                case R.id.btnMerge: {
//                	AnnotationManager hm = new AnnotationManager();
//                    hm.mergeHighlight( BookHelper.getFileString("annotation_bak.flk"), 
//                    		BookHelper.getFileString(BookHelper.annotationFileName),
//                    		BookHelper.getFileString(BookHelper.annotationFileName),
////                    		BookHelper.getFileString("merge_annotation.flk"),
//                    		BookHelper.getFileString(BookHelper.annotationHistoryFileName) );
                	
//    				mViewer.mergeBookmarkData("test");
                    break;
                }
                case R.id.btnShow: {
                	mOnAnnotationItem.onShow();
                	mAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        
        if( mOnAnnotationItem != null ) {
            Highlight high = mItems.get(pos);
            mOnAnnotationItem.onSelect(high);
            
            mAdapter.setSelectedPosition(pos);
            dismiss();
        }
        
    }

}
