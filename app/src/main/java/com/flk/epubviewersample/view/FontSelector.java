package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
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

import com.flk.epubviewersample.R;

import java.util.ArrayList;

/**
 * @author  syw
 */
public class FontSelector extends AlertDialog 
    implements View.OnClickListener, OnItemClickListener
{
    /**
     */
    FontSelector mThis = null;
    
    Context mContext;
    
    View mRootView;
    ListView mFontList;
    
    
    class FontItem {
        String name;
        Typeface face;
        String path;
    }
    
    ArrayList<FontItem> mFace = new ArrayList<FontItem>();

    
    public static interface OnChangeFont {
        public abstract void onGetFont(String name, Typeface face, String path);
    }
    
    /**
     */
    OnChangeFont mOnChangeFont = null;
    public void setOnChangeFont(OnChangeFont l) {
        mOnChangeFont = l;
    }
    
    
    public class FontAdapter extends BaseAdapter implements ListAdapter {
        LayoutInflater inflator;
        
        FontAdapter() {
            inflator = LayoutInflater.from(mContext);
        }
        
        @Override
        public int getCount() {
            return mFace.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return Integer.valueOf(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return (long)position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            
            if( convertView == null ) {
                v = inflator.inflate(R.layout.item_fontselector, null);
            } else {
                v = convertView;
            }
            
            FontItem item = mFace.get(position);
            
            if( item != null ) {
                TextView fontName = (TextView)v.findViewById(R.id.fontName);
                fontName.setText(item.name);
                
                TextView text = (TextView)v.findViewById(R.id.text);
                text.setTypeface(item.face);
            }
            
            return v;
        }
        
    }
    

    public FontSelector(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public FontSelector(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public FontSelector(Context context) {
        super(context);
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
        
        mFontList = (ListView)mRootView.findViewById(android.R.id.list);
        mFontList.setOnItemClickListener(this);

        try {
            FontItem fi = new FontItem();
            fi.face = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumGothic.ttf");
            fi.name = "나눔고딕";
            fi.path = "file:///android_asset/fonts/NanumGothic.ttf";
            
            mFace.add(fi);
            fi = new FontItem();
            fi.face = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumMyeongjo.ttf");
            fi.name = "나눔명조";
            fi.path = "file:///android_asset/fonts/NanumMyeongjo.ttf";
            mFace.add(fi);
            
            fi = new FontItem();
            fi.face = Typeface.createFromAsset(getContext().getAssets(), "fonts/UnBatang.ttf");
            fi.name = "은글꼴 바탕체";
            fi.path = "file:///android_asset/fonts/UnBatang.ttf";
            mFace.add(fi);
    
            fi = new FontItem();
            fi.face = Typeface.createFromAsset(getContext().getAssets(), "fonts/UnDotum.ttf");
            fi.name = "은글꼴 돋움체";
            fi.path = "file:///android_asset/fonts/UnDotum.ttf";
            mFace.add(fi);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        FontAdapter fontAdapter = new FontAdapter();
        mFontList.setAdapter(fontAdapter);
        
    }
    
    
    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        
        if( mOnChangeFont != null ) {
            
            FontItem item = mFace.get(pos);
            mOnChangeFont.onGetFont(item.name, item.face, item.path);
            
            dismiss();
        }
    }

    
    
}
