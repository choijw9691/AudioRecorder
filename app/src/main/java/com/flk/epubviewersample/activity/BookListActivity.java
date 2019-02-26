package com.flk.epubviewersample.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.DebugSet;
import com.flk.epubviewersample.R;
import com.flk.epubviewersample.view.FileList;

public class BookListActivity extends Activity {

    public static BookListActivity mThis = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        mThis = this;

        checkPermissionForthisActivity();

        init();

    }

    private void init(){
        FileList fileList = new FileList(this);
        fileList.setOnPathChangedListener(new FileList.OnPathChangedListener() {

            @Override
            public void onChanged(String path) {

                TextView tv = (TextView)findViewById(R.id.currentDir);
                tv.setText(path);
            }

        });

        fileList.setOnFileSelected(new FileList.OnFileSelectedListener() {

            @Override
            public void onSelected(String path, String fileName) {
                // TODO Auto-generated method stub
                if( BookHelper.getBookType(path + fileName) == 2 ) {

//                	SyncDataManager manager = new SyncDataManager();
//                	String result = manager.mergeAnnotation(
//                			path + BookHelper.getOnlyFilename(fileName) + "/server_annotation.flk",
//                			path + BookHelper.getOnlyFilename(fileName),
//                			"");
//
//
//					try {
//						if( result != null )
//							FileUtil.fileCopy(new File(result), new File(path + BookHelper.getOnlyFilename(fileName) + "/server_annotation.flk"));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
////                	Log.d("TAG", "result : " + result);
//
//                	//주석 병합 Testl
//                    HighlightManager hm = new HighlightManager();
//                    hm.mergeHighlight( path + BookHelper.getOnlyFilename(fileName) + "/annotation_bak.flk",
//                    		path + BookHelper.getOnlyFilename(fileName) + "/annotation.flk",
//                    		path + BookHelper.getOnlyFilename(fileName) + "/annotation.flk",
//                    		path + BookHelper.getOnlyFilename(fileName) + "/annotationhistory.flk" );


                    BaseBookActivity.mThis = null;
                    Intent intent = new Intent(mThis, BaseBookActivity.class);
                    intent.putExtra("BOOK_PATH", path);
                    intent.putExtra("BOOK_FILE", fileName);
                    mThis.startActivityForResult(intent, 1);

                }
                else {
                    Toast.makeText(mThis, fileName + " 은(는) 지원하지 않는 형식의 파일입니다.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });


        LinearLayout container = (LinearLayout)findViewById(R.id.listcontainer);
        container.addView(fileList);

        fileList.setPath(Environment.getExternalStorageDirectory().toString());
        fileList.setFocusable(true);
        fileList.setFocusableInTouchMode(true);
    }

    private static final int PERMISSION_REQUEST = 1000;
    private void checkPermissionForthisActivity() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionReadStorage == PackageManager.PERMISSION_DENIED || permissionWriteStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        } else {
            Toast.makeText(getApplicationContext(), "read/write storage permission authorized", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:

                if(verifyPermission(grantResults)){
                    Toast.makeText(getApplicationContext(), "read/write storage permission authorized", Toast.LENGTH_SHORT).show();
                    init();
                } else{
                    showRequestPermissionDialog();
                }
                break;
        }
    }

    private boolean verifyPermission(int[] grantresults){
        if(grantresults.length<1){
            return false;
        }

        for(int result : grantresults){
            if(result!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void showRequestPermissionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
        builder.setMessage("권한 설정 필요");
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package : "+getPackageName()));
                    startActivity(intent);
                } catch (ActivityNotFoundException exception){
                    exception.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == 1 ){
            printMemory("finish");
        }
    }



    private void printMemory(String tag) {
        DebugSet.i("Memory",
                tag + " : "
                        + "---------------------------------------------------------- ");
        DebugSet.i("Memory",
                tag + " : " + " max " + ((Runtime.getRuntime().maxMemory())));
        DebugSet.i("Memory",
                tag + " : " + " total " + (Runtime.getRuntime().totalMemory()));
        DebugSet.i("Memory",
                tag + " : " + " free " + (Runtime.getRuntime().freeMemory()));
        DebugSet.i("Memory", tag
                + " : "
                + " total - free "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        DebugSet.i("Memory",
                tag
                        + " : "
                        + "---------------------------------------------------------- ");
        DebugSet.i("Memory", tag + " : " + " getNativeHeapSize "
                + Debug.getNativeHeapSize());
        DebugSet.i("Memory", tag + " : " + " getNativeHeapFreeSize "
                + Debug.getNativeHeapFreeSize());
        DebugSet.i(
                "Memory",
                tag + " : " + " getNativeHeapAllocatedSize "
                        + Debug.getNativeHeapAllocatedSize());
        DebugSet.i("Memory",
                tag
                        + " : "
                        + " --------------------------------------------------------- ");
    }
}
