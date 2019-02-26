package com.flk.epubviewersample.activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flk.epubviewersample.R;
import com.flk.epubviewersample.view.ZoomInOutImageView;

import java.util.ArrayList;

/**
 * @author  syw
 */
public class ImageViewer extends Activity {

    /**
     */
    public static ImageViewer mThis=null;
    
    ZoomInOutImageView mImage;
    
    ArrayList<String> mImageList = new ArrayList<String>();
    
    int mIndex = 0;
    
    Bitmap mBitmap;
    TextView mLinkBack;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        
        setContentView(R.layout.imageviewer);
        
        mImage = (ZoomInOutImageView)findViewById(R.id.imageView);
        mLinkBack = (TextView)findViewById(R.id.linkBack);
        
        mLinkBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        Button prev = (Button)findViewById(R.id.btnPrev);
        Button next = (Button)findViewById(R.id.btnNext);
        prev.setOnClickListener(new OnClickCallback());
        next.setOnClickListener(new OnClickCallback());

        
        String fileName = null;
        Intent i = getIntent();
        if( i != null ) {
            fileName = i.getStringExtra("IMAGE_FILENAME");
            
            mImageList = i.getStringArrayListExtra("IMAGE_LIST");
        }
        
        if( fileName == null ) {
            finish();
            return;
        }

        mIndex = mImageList.indexOf(fileName);
        
        setImage(fileName);
    }
    
    private void setImage(String filePath) {
    	try {
        	
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inSampleSize = 1;
            mBitmap = BitmapFactory.decodeFile(filePath, bo);
            
            mImage.setImageBitmap(mBitmap);

        }
        catch(Exception e) {
            e.printStackTrace();
            
            finish();
        }
    }
    
    class OnClickCallback implements OnClickListener {
        @Override
        public void onClick(View v) {
            
            switch(v.getId()) {
                case R.id.btnPrev: {
                	mIndex -= 1;
                	if( mIndex < 0 )
                		Toast.makeText(ImageViewer.this, "더이상 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                	else
                		setImage(mImageList.get(mIndex));
                    break;
                }
                
                case R.id.btnNext: {
                	mIndex += 1;
                	if( mIndex < mImageList.size() )
                		setImage(mImageList.get(mIndex));
                	else
                		Toast.makeText(ImageViewer.this, "더이상 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                	
                    break;
                }
                
            }
        }
    }

}
