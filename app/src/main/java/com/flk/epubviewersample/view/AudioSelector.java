package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ebook.epub.viewer.data.AudioNavigationInfo;
import com.flk.epubviewersample.R;

import java.util.ArrayList;

public class AudioSelector extends AlertDialog implements AdapterView.OnItemClickListener {

    private Context mContext;

    private View mRootView;
    private  ListView mAudioListView;
    public ArrayList<AudioNavigationInfo> mAudioList = new ArrayList<>();
    private OnSelectAudio mOnSelectAudio = null;

    public static interface OnSelectAudio {
        void onSelect(int position, AudioNavigationInfo audioInfo);
    }
    public void setOnSelectAudio(OnSelectAudio l) {
        mOnSelectAudio = l;
    }

    public AudioSelector(Context context, ArrayList<AudioNavigationInfo> audioList) {
        super(context);
        init(context);
        mAudioList = audioList;
    }

    private void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.select_dialog, null);
        this.setView(mRootView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("PLAY_LIST");
        mAudioListView = (ListView)mRootView.findViewById(android.R.id.list);
        mAudioListView.setOnItemClickListener(this);
        AudioAdapter adapter = new AudioAdapter();
        mAudioListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if( mOnSelectAudio != null ) {
            AudioNavigationInfo audio = mAudioList.get(position);
            mOnSelectAudio.onSelect( position, audio);
            dismiss();
        }
    }

    public class AudioAdapter extends BaseAdapter implements ListAdapter {
        LayoutInflater inflator;

        AudioAdapter() {
            inflator = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mAudioList.size();
        }

        @Override
        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View v;

            if( view == null ) {
                v = inflator.inflate(R.layout.item_list, null);
            } else {
                v = view;
            }

            AudioNavigationInfo item = mAudioList.get(position);
            if( item != null ) {
                ImageView iv = (ImageView)v.findViewById(R.id.ivImage);
                TextView title = (TextView)v.findViewById(R.id.tvChapter);
                TextView dateTime = (TextView)v.findViewById(R.id.tvDate);
                TextView snippet = (TextView)v.findViewById(R.id.tvSnippet);
                TextView page = (TextView)v.findViewById(R.id.tvPage);

                iv.setVisibility(View.GONE);
                dateTime.setVisibility(View.GONE);
                snippet.setVisibility(View.GONE);
                page.setVisibility(View.GONE);

                title.setText(item.title);
            }

            return v;
        }
    }
}

