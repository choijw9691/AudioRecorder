package com.flk.epubviewersample.view;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flk.epubviewersample.R;
import com.flk.epubviewersample.activity.BookListActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


/**
 * @author  syw
 */
public class FileList extends ListView {

	public FileList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public FileList(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public FileList(Context context) {
		super(context);

		init(context);
	}
	
	private void init(Context context) {
		_Context = context;
        this.setOnItemClickListener(_OnItemClick);
	}

	class FileItem {
	    boolean isDirectory;
	    String fileName;
	    public FileItem(boolean isDir, String name) {
	        isDirectory = isDir;
	        fileName = name;
	    }
	}
	
	private Context _Context = null;
	private ArrayList<FileItem> _List = new ArrayList<FileItem>();
	
    private ArrayList<String> _FolderList = new ArrayList<String>();
    private ArrayList<String> _FileList = new ArrayList<String>();
    FileListAdapter _Adapter = null;
	
	
	class FileListAdapter extends BaseAdapter {
        LayoutInflater inflator;
                
        FileListAdapter() {
            inflator = LayoutInflater.from(_Context);
        }

        @Override
        public int getCount() {
            return _List.size();
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
                v = inflator.inflate(R.layout.item_filelist, null);
            } else {
                v = convertView;
            }

            FileItem ft = _List.get(position);
            if( ft != null ) {
                ImageView iv = (ImageView)v.findViewById(R.id.imgFileType);
                TextView tv = (TextView)v.findViewById(R.id.tvFile);
                
                if(ft.isDirectory) {
                    if( ft.fileName.matches("<..>") )
                        iv.setImageResource(R.drawable.root2);
                    else
                        iv.setImageResource(R.drawable.folder);
                    
                } else {
                    if( ft.fileName.toLowerCase().endsWith(".epub") )
                        iv.setImageResource(R.drawable.aiepub);
                    else if( ft.fileName.toLowerCase().endsWith(".txt") )
                    	iv.setImageResource(R.drawable.aitxt);
                    else
                        iv.setImageResource(R.drawable.aiunknow);
                }
                
                tv.setText(ft.fileName);
            }

            return v;
        }
	    
	}
	
	// Property 
	private String _Path = "";
	
	// Event
	/**
     */
	private OnPathChangedListener _OnPathChangedListener = null;
	/**
     */
	private OnFileSelectedListener _OnFileSelectedListener = null;
	
	private boolean openPath(String path) {
		_FolderList.clear();
		_FileList.clear();
		
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) return false;
        
        for (int i=0; i<files.length; i++) {
        	if (files[i].isDirectory()) {
        		_FolderList.add("<" + files[i].getName() + ">");
        	} else {
        		_FileList.add(files[i].getName());
        	}
        }

        Collections.sort(_FolderList);
        Collections.sort(_FileList);
        
        _FolderList.add(0, "<..>");
        
        return true;
	}
	
	private void updateAdapter() {
		_List.clear();
		
		for(int i=0; i<_FolderList.size(); i++) {
		    FileItem ft = new FileItem(true, _FolderList.get(i));
		    _List.add(ft);
		}
		for(int i=0; i<_FileList.size(); i++) {
            FileItem ft = new FileItem(false, _FileList.get(i));
            _List.add(ft);
		}
        		
		//_Adapter = new ArrayAdapter<String>(_Context, android.R.layout.simple_list_item_1, _List);
		_Adapter = new FileListAdapter();
        setAdapter(_Adapter);
	}

	public void setPath(String value) {
		if (value.length() == 0) {
			value = "/";
		} else {
			String lastChar = value.substring(value.length()-1, value.length());
			if (lastChar.matches("/") == false) value = value + "/"; 
		}
		
		if (openPath(value)) {
			_Path = value;
			updateAdapter();	        
			if (_OnPathChangedListener != null) _OnPathChangedListener.onChanged(value);
		}
	}

	public String getPath() {
		return _Path;
	}
	
	public void setOnPathChangedListener(OnPathChangedListener value) {
		_OnPathChangedListener = value;
	}

	public OnPathChangedListener getOnPathChangedListener() {
		return _OnPathChangedListener;
	}

	public void setOnFileSelected(OnFileSelectedListener value) {
		_OnFileSelectedListener = value;
	}

	public OnFileSelectedListener getOnFileSelected() {
		return _OnFileSelectedListener;
	}
	
	public String DelteRight(String value, String border) {
		String list[] = value.split(border);

		String result = "";
		
		for (int i=0; i<list.length; i++) {
			result = result + list[i] + border; 
		}
		
		return result;
	}
	
	private String deleteLastFolder(String value) {
		String list[] = value.split("/");

		String result = "";
		
		for (int i=0; i<list.length-1; i++) {
			result = result + list[i] + "/"; 
		}
		
		return result;
	}
	
	private String getRealPathName(String newPath) {
		String path = newPath.substring(1, newPath.length()-1);
		
		if (path.matches("..")) {
			return deleteLastFolder(_Path);
		} else {
			return _Path + path + "/";
		}
	}
	
	private OnItemClickListener _OnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		    FileItem ft = _List.get(position);
			String fileName = ft.fileName;
			if (fileName.matches("<.*>")) {
				setPath(getRealPathName(fileName));
			}
			else {
				if (_OnFileSelectedListener != null) _OnFileSelectedListener.onSelected(_Path, fileName);
			}
		}
	};

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0 ) {

        	if(_Path.equals(Environment.getExternalStorageDirectory().toString()+ "/")){
        		BookListActivity.mThis.finish();
        	}else if(_Path.equals("/")){
				BookListActivity.mThis.finish();
        	}else{
        		setPath(getRealPathName("<..>"));        		
        	}
        	
            return true;
        }
            
        return super.onKeyDown(keyCode, event);
    }
	
	

	public interface OnFileSelectedListener {
		public void onSelected(String path, String fileName);
	}

	public interface OnPathChangedListener {
		public void onChanged(String path);
	}

}
