package com.flk.epubviewersample.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.ebook.epub.viewer.BookHelper;
import com.flk.epubviewersample.R;

import java.io.InputStream;

public class SettingsDialog {

    Context mContext;
    View mRootView;
    PopupWindow mPopup;

    private class UserFontDesc {
        LinearLayout font;
        TextView check;
        
        String fontName;
        String faceName;
        String fontPath;
        
        public UserFontDesc(int llRes, int tvRes, String fontName, String faceName, String fontPath) {
            font = (LinearLayout)mRootView.findViewById(llRes);
            font.setOnClickListener(new OptionItemClick());
            check = (TextView)mRootView.findViewById(tvRes);
            
            this.fontName = fontName;
            this.faceName = faceName;
            this.fontPath = fontPath;
        }
    }
    
    UserFontDesc fonts[];
    ImageView colors[];
    int[] lines;
    int[] paras;
    Rect margins[];
    String styles[];
    String weights[];
    String listStyles[];
        
    LinearLayout userStyles;
    
//    TextView selectedStyle;
    
    TextView lineSpace[];
    TextView paraSpace[];
    TextView marginSpace[];
    TextView fontStyle[];
    TextView fontWeight[];
    TextView listStyle[];
    
    TextView fontSize;
    TextView tvLineSpace;
    TextView tvPara;
    
    CheckBox chkIndent;
    CheckBox chkVolume;
    
    Integer __fontSize=100;
    UserFontDesc __font;
    Integer __color;

    Integer __lineSpacing;
    Integer __paraSpacing;
    Rect __margin;
    boolean __indent;
    boolean volume;
    
//    Integer __indent;
    String __style;
    String __weight;
    String __listStyle;
    
    boolean __settingsChanged = false;
    
    boolean __inited = false;
    
    public interface OnSettingsListener {
         void onCancel();
         void onClose(boolean changed);
         void onFontBigger(int value);
         void onFontSmaller(int value);
         void onFontChange(String fontName, String faceName, String fontPath);
         void onBackgroundColorChange(int color, boolean nightMode);
         void onLineSpacing(int value);
         void onParagraphSpacing(int value);
         void onMargin(int left, int top, int right, int bottom);
         void onIndent(boolean onoff);
         void onVolume(boolean useVolumeKey);
         void onInitBook();
    }
    
    OnSettingsListener mOnSettingsListener=null;
    
    public void setOnSettingsListener(OnSettingsListener l) {
        mOnSettingsListener = l;
    }
    
    public SettingsDialog(Context context) {
        init(context);
    }
    
    void init(Context context) {
        mContext = context;
        
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.option_popup, null);
        mRootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        ((Button)mRootView.findViewById(R.id.smaller)).setOnClickListener(new OptionItemClick());
        ((Button)mRootView.findViewById(R.id.bigger)).setOnClickListener(new OptionItemClick());
        ((Button)mRootView.findViewById(R.id.org)).setOnClickListener(new OptionItemClick());
        
        fontSize = (TextView)mRootView.findViewById(R.id.fontSize);
        if(fontSize!=null)
        	fontSize.setText(__fontSize + "% ");
        
        tvLineSpace = (TextView)mRootView.findViewById(R.id.tv_lineSpace);
        if(BookHelper.lineSpace!=null)
        	tvLineSpace.setText(BookHelper.lineSpace + "% ");
        
        tvPara = (TextView)mRootView.findViewById(R.id.tv_para);
        if(BookHelper.paraSpace!=null)
        	tvLineSpace.setText(BookHelper.paraSpace + "% ");
        
        if(BookHelper.indent!=null)
        	((TextView)mRootView.findViewById(R.id.tv_indent)).setText("");
        
        lines = new int[] { 100, 125, 150, 175, 200 };
        paras = new int[] { 0, 2, 4 };
        styles = new String[] { "normal", "italic", "oblique" };
        weights = new String[] { "normal", "bold", "300", "700" };
        listStyles = new String[] {"Cjk-Ideographic", "Hebrew", "Hiragana", "Hiragana-Iroha", "Katakana", "Katakana-iroha"};
        
        if( BookHelper.getDevice(mContext) == BookHelper.TABLET) {
            if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
                margins = new Rect[] { new Rect(40,80,40,80), new Rect(60,100,60,100), new Rect(80,120,80,120) };
            else 
                margins = new Rect[] { new Rect(60,75,60,75), new Rect(80,95,80,95), new Rect(100,115,100,115) };
        } else {
            if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
                margins = new Rect[] { new Rect(10,10,10,10), new Rect(20,20,20,20), new Rect(30,30,30,30) };
            else
                margins = new Rect[] { new Rect(10,10,10,10), new Rect(20,20,20,20), new Rect(40,40,40,40) };
        }
        
        fonts = new UserFontDesc[3];
        fonts[0] = new UserFontDesc(R.id.fontDefault, R.id.checkDefault, "기본글꼴", "", "");
        fonts[1] = new UserFontDesc(R.id.fontMJ, R.id.checkMJ, "나눔명조", "NanumMyeongjo", "file:///android_asset/fonts/NanumMyeongjo.ttf");
        fonts[2] = new UserFontDesc(R.id.fontGT, R.id.checkGT, "나눔고딕", "NanumGothic", "file:///android_asset/fonts/NanumGothic.ttf" );

        colors = new ImageView[8];
        colors[0] = (ImageView)mRootView.findViewById(R.id.iv_white);
        colors[0].setOnClickListener(new OptionItemClick());
        colors[0].setTag(Integer.valueOf(0XFFFFFF));
        colors[1] = (ImageView)mRootView.findViewById(R.id.iv_black);
        colors[1].setOnClickListener(new OptionItemClick());
        colors[1].setTag(new Integer(0x2b2b2b));
        colors[2] = (ImageView)mRootView.findViewById(R.id.iv_grey);
        colors[2].setOnClickListener(new OptionItemClick());
        colors[2].setTag(new Integer(0xd1d1d1));
        colors[3] = (ImageView)mRootView.findViewById(R.id.iv_blue);
        colors[3].setOnClickListener(new OptionItemClick());
        colors[3].setTag(new Integer(0xf4e6e9));
        colors[4] = (ImageView)mRootView.findViewById(R.id.iv_violet);
        colors[4].setOnClickListener(new OptionItemClick());
        colors[4].setTag(new Integer(0xe4e6f2));
        colors[5] = (ImageView)mRootView.findViewById(R.id.iv_beige);
        colors[5].setOnClickListener(new OptionItemClick());
        colors[5].setTag(new Integer(0xddead6));
        colors[6] = (ImageView)mRootView.findViewById(R.id.iv_brown);
        colors[6].setOnClickListener(new OptionItemClick());
        colors[6].setTag(new Integer(0xeee6ca));
        colors[7] = (ImageView)mRootView.findViewById(R.id.iv_navy);
        colors[7].setOnClickListener(new OptionItemClick());
        colors[7].setTag(new Integer(0x584a3d));
        
        lineSpace = new TextView[5];
        lineSpace[0] = (TextView)mRootView.findViewById(R.id.lineSpace1);
        lineSpace[0].setOnClickListener(new OptionItemClick());
        lineSpace[1] = (TextView)mRootView.findViewById(R.id.lineSpace2);
        lineSpace[1].setOnClickListener(new OptionItemClick());
        lineSpace[2] = (TextView)mRootView.findViewById(R.id.lineSpace3);
        lineSpace[2].setOnClickListener(new OptionItemClick());
        lineSpace[3] = (TextView)mRootView.findViewById(R.id.lineSpace4);
        lineSpace[3].setOnClickListener(new OptionItemClick());
        lineSpace[4] = (TextView)mRootView.findViewById(R.id.lineSpace5);
        lineSpace[4].setOnClickListener(new OptionItemClick());
        
        paraSpace = new TextView[3];
        paraSpace[0] = (TextView)mRootView.findViewById(R.id.paraSpace1);
        paraSpace[0].setOnClickListener(new OptionItemClick());
        paraSpace[1] = (TextView)mRootView.findViewById(R.id.paraSpace2);
        paraSpace[1].setOnClickListener(new OptionItemClick());
        paraSpace[2] = (TextView)mRootView.findViewById(R.id.paraSpace3);
        paraSpace[2].setOnClickListener(new OptionItemClick());
        
        marginSpace = new TextView[3];
        marginSpace[0] = (TextView)mRootView.findViewById(R.id.marginSpace1);
        marginSpace[0].setOnClickListener(new OptionItemClick());
        marginSpace[1] = (TextView)mRootView.findViewById(R.id.marginSpace2);
        marginSpace[1].setOnClickListener(new OptionItemClick());
        marginSpace[2] = (TextView)mRootView.findViewById(R.id.marginSpace3);
        marginSpace[2].setOnClickListener(new OptionItemClick());
        
        chkIndent = (CheckBox)mRootView.findViewById(R.id.chkIndent);
        chkIndent.setOnClickListener(new OptionItemClick());
        
        chkVolume = (CheckBox)mRootView.findViewById(R.id.cb_volume);
        chkVolume.setOnClickListener(new OptionItemClick());
        
        int cx = mRootView.getMeasuredWidth();
        int cy = mRootView.getMeasuredHeight();
        
        mPopup = new PopupWindow(mRootView, cx, cy, false);
//        mPopup.setOutsideTouchable(true);
        
        mPopup.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
//            	checkSettingChanged();
            }
        });
    }
    
    void checkSettingChanged(){
    	if( __inited ) {
            BookHelper.useDefault = true;
            __settingsChanged = true;

            BookHelper.fontName = "";
            BookHelper.faceName = "";
            BookHelper.fontPath = "";
            
            BookHelper.setStyleInit(__inited);
            return;
        }

        boolean b1=false,b2=false,b3=false,b4=false,b5=false,b6=false,b7=false,b8=false;
        
        b1 = BookHelper.setFontSize(__fontSize);

        if( __font != null )
            b2 = BookHelper.setFont(__font.fontName, __font.faceName, __font.fontPath);
        
        b3 = BookHelper.setBackgroundColor(__color);
        b5 = BookHelper.setLineSpace(__lineSpacing);
        b6 = BookHelper.setParaSpace(__paraSpacing);

        if( __margin != null )
            b7 = BookHelper.setMargin(__margin.left, __margin.top, __margin.right, __margin.bottom);
               
        if(__indent){
        	if(__indent){
        		b8 = BookHelper.setIndent(1);
        	} else{
        		b8 = BookHelper.setIndent(0);
        	}
        	
        } 
        
        if( b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8 ) {
            __settingsChanged = true;
            
            // useDefault는 페이지 스타일 값들의 컨트롤에 사용된다.
            BookHelper.useDefault = false;
        } else {
            __settingsChanged = false;
        }
        
        Log.d("TAG", "settings Changed : " + __settingsChanged );
    }
    
    
    int checkRange(int[] values, int d) {

        int index=0;
        int max = Integer.MAX_VALUE;
        for(int i=0; i<values.length; i++) {
            
            int min = Math.abs(values[i] - d);
            if( min < max ) {
                index = i;
                max = min;
            }
        }

        return index;
    }
    
    public void setDefaultValues() {
        
    	if(BookHelper.fontSize!=null){
    		__fontSize = BookHelper.getFontSize();
    		fontSize.setText(__fontSize + "% ");
    	}

        // font default
        for(UserFontDesc ufd: fonts) {
            
            if( BookHelper.faceName!=null && BookHelper.faceName.equals(ufd.faceName) ) {
                __font = ufd;
                
                clearChecked();
                ufd.check.setText("V");
                
                break;
            }
        }
        
        // color default
        __color = BookHelper.backgroundColor;
        for(ImageView iv: colors) {
            Integer color = (Integer)iv.getTag();
            if( color == __color ) {
                iv.setSelected(true);
            }
            else {
                iv.setSelected(false);
            }
        }
        
        __lineSpacing = BookHelper.getLineSpace();
//        int index  = checkRange( lines,  __lineSpacing);
//        int start = __lineSpacing - (index * 25);
//        for(int i=0; i<lines.length; i++) {
//            if( i == index ) {
//                lineSpace[i].setSelected(true);
//            }
//            else {
//                lineSpace[i].setSelected(false);
//            }
//            lines[i] = Math.max(start + (i*25), 100);
//            Log.d("TAG", "load line space >>>>>>>>>> " + lines[i]);
//        }
        
        if(BookHelper.paraSpace!=null){
            __paraSpacing = BookHelper.getParaSpace();
        }
        
//        index = checkRange(paras,  __paraSpacing);
//        for(int i=0; i<paras.length; i++) {
//            if( i == index ) {
//                paraSpace[i].setSelected(true);
//            }
//            else {
//                paraSpace[i].setSelected(false);
//            }
//        }
        
        // get margin
        for(int i=0; i<margins.length; i++) {
            if( BookHelper.leftMargin!=null && margins[i].left == BookHelper.leftMargin &&
                margins[i].top == BookHelper.topMargin &&
                margins[i].right == BookHelper.rightMargin &&
                margins[i].bottom == BookHelper.bottomMargin ) {
                marginSpace[i].setSelected(true);
                __margin = margins[i];
                
            } else {
                marginSpace[i].setSelected(false);
            }
        }
        
        // indent
        if(BookHelper.indent==null || BookHelper.indent==0){
        	chkIndent.setChecked(false);
        } else{
        	chkIndent.setChecked(true);
        }
    }

    public void setCurrentVolumeState(boolean useVolumeKey){
    	chkVolume.setChecked(useVolumeKey);
    }

    public void show(View parent, int x, int y) {
        
        if( mPopup != null ) {
            
            // setup current value
            setDefaultValues();
            
            int w = mPopup.getWidth();
            int h = mPopup.getHeight();
            
            Display dp = ((Activity)mContext).getWindowManager().getDefaultDisplay();
            int xx = dp.getWidth() - w;
            int yy = dp.getHeight() - h;
            
            mPopup.showAtLocation(parent, Gravity.NO_GRAVITY, xx, yy);
        }
    }
    public void hide() {
        if( mPopup != null )
            mPopup.dismiss();
    }
    
    public boolean isShowing() {
        if( mPopup != null )
            return mPopup.isShowing();
        return false;
    }

    void clearChecked() {
        for(UserFontDesc fs : fonts) {
            fs.check.setText("");
        }
    }
    
    public boolean checkSettingsChanged() {
    	checkSettingChanged();
        return __settingsChanged;
    }
    
    UserFontDesc getFontDesc(int id) {
        
        for(UserFontDesc fs : fonts) {
            if( fs.font.getId() == id ) {
                fs.check.setText("V");

                __font = fs;
                return fs;
            }
        }
        return null;
        
    }
    class OptionItemClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            
            switch(v.getId()) {
            	case R.id.org: {
	                if( mOnSettingsListener != null ) {
	                    mOnSettingsListener.onInitBook();
	                }
	                break;
	            }
                
                case R.id.smaller: {
                    if( mOnSettingsListener != null ) {
                        __fontSize -= 15;
                        mOnSettingsListener.onFontSmaller(__fontSize);
                    }
                    
                    fontSize.setText(__fontSize + "% ");
                    break;
                }
                case R.id.bigger: {
                    if( mOnSettingsListener != null ) {
                        __fontSize += 15;
                        mOnSettingsListener.onFontBigger(__fontSize);
                    }
                    
                    fontSize.setText(__fontSize + "% ");
                    break;
               }
                
                case R.id.fontDefault:
                case R.id.fontGT:
                case R.id.fontMJ:
                {
                    
                    String fontPath = "";
                    String faceName = "";
                    String fontName = "";
                    
                    clearChecked();
                    UserFontDesc ufd = getFontDesc(v.getId());
                    
                    if( ufd != null ) {
                        fontName = (v.getId() == R.id.fontDefault) ? "" : ufd.fontName;
                        faceName = ufd.faceName;
                        fontPath = ufd.fontPath;
                    }
                    
                    if( mOnSettingsListener != null ) {
                        mOnSettingsListener.onFontChange(fontName, faceName, fontPath);
                    }
                    
                    break;
                }
                    
                    
                case R.id.iv_white: 
                case R.id.iv_black: 
                case R.id.iv_grey: 
                case R.id.iv_blue: 
                case R.id.iv_violet: 
                case R.id.iv_beige: 
                case R.id.iv_brown: 
                case R.id.iv_navy: {
                    toggleSelected(colors, v);
                    
                    int color = (Integer)v.getTag();
                    boolean nightMode = v.getId()==R.id.iv_black || v.getId()==R.id.iv_navy ? true : false;
                    
                    __color = color;
                    if(nightMode) 
                        BookHelper.nightMode = 1;
                    else
                        BookHelper.nightMode = 0;
                    
                    if( mOnSettingsListener != null ) {
                        mOnSettingsListener.onBackgroundColorChange(color, nightMode);
                    }
                    
                    break;
                }

                case R.id.lineSpace1:
                case R.id.lineSpace2:
                case R.id.lineSpace3:
                case R.id.lineSpace4:
                case R.id.lineSpace5: {
                	
                    toggleSelected(lineSpace, v);
                
                    int index = v.getId() - R.id.lineSpace1;
                    
                    __lineSpacing = lines[index]; 
                            
                    if( mOnSettingsListener != null ) 
                        mOnSettingsListener.onLineSpacing(lines[index]);
                    
                    tvLineSpace.setText(__lineSpacing + "% ");
                    break;
                }
                
                case R.id.paraSpace1: 
                case R.id.paraSpace2: 
                case R.id.paraSpace3: {
                    toggleSelected(paraSpace, v);
                    
                    int index = v.getId() - R.id.paraSpace1;
                    
                    __paraSpacing = paras[index];
                    
                    if( mOnSettingsListener != null ) {
                        int height = __paraSpacing;
                        mOnSettingsListener.onParagraphSpacing(height);
                    }
                    break;
                }
                
                case R.id.marginSpace1:
                case R.id.marginSpace2:
                case R.id.marginSpace3: {
                    toggleSelected(marginSpace, v);
                    
                    int index = v.getId() - R.id.marginSpace1;
                    
                    Rect m = margins[index];
                    
                    __margin = m;
                            
                    if( mOnSettingsListener != null ) 
                        mOnSettingsListener.onMargin(m.left, m.top, m.right, m.bottom);
                    break;
                }
                
                case R.id.chkIndent: {
                    CheckBox chk = (CheckBox)v;
                    
                    __indent = chk.isChecked();
                    
                    if( mOnSettingsListener != null ) 
                        mOnSettingsListener.onIndent(chk.isChecked());
                    
                    break;
                }
                
                case R.id.cb_volume: {
                    CheckBox chk = (CheckBox)v;
                    
                    volume = chk.isChecked();
                    
                    if( mOnSettingsListener != null ) 
                        mOnSettingsListener.onVolume(volume);
                    
                    break;
                }
                
            }
        }
        
    }

    void toggleSelected(View[] views, View selected) {
        
        for(View v: views) {
            
            if(v.getId() == selected.getId() ) {
                if( !selected.isSelected() )
                    selected.setSelected(true);
            } 
            else {
                v.setSelected(false);
            }
                
        }
    }
    
    String readAsset(Context context, String fileName) {
        
        AssetManager am = context.getResources().getAssets();
        InputStream is = null;
        // 읽어들인 문자열이 담길 변수
        String result = null;

        try {
            is = am.open(fileName);
            int size = is.available();

            if (size > 0) {
                byte[] data = new byte[size];
                is.read(data);
                result = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (Exception e) {}
            }
        }

        am = null;
        return result;
        
    }
}
