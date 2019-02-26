package com.ebook.epub.viewer;

import android.content.Context;
import android.content.res.Configuration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BookSettings {

	private String LOGTAG = "Booksettings";

	private Context mContext;

	private Integer mFontSize;

	private String mFontName;
	private String mFaceName;
	private String mFontPath;

	private Integer mLineSpace;

	private Integer mParaSpace;

	private Integer mIndent;

	private Integer mMarginLeft;
	private Integer mMarginTop;
	private Integer mMarginRight;
	private Integer mMarginBottom;

	private String mStyleFilePath;

	/**
	 * 스타일 저장 파일을 읽어 값을 읽는다.
	 * 
	 * @param context Context : 
	 * @param stylePath String : 오픈할 파일 경로 
	 */
	public void readStyleFile(Context context, String stylePath){
		mContext = context;
		mStyleFilePath = stylePath;

		File styleFile = new File(mStyleFilePath);

		/* 
		 	파일이 없으면 기본값 세팅.
			파일이 있으면 값을 읽어 세팅.
		 */
		if( !styleFile.exists()) {

			mFontName = "";
			mFaceName = "";
			mFontPath = "";

			mLineSpace = 130;

			mParaSpace = 2;

			mIndent = 0;

			if( BookHelper.getDevice(mContext) == BookHelper.PHONE ) {

				mMarginLeft = 23;
				mMarginTop = 23;
				mMarginRight = 23;
				mMarginBottom = 40;

				mFontSize = 100;

			} else {

				if( BookHelper.getOrientation(mContext) == Configuration.ORIENTATION_PORTRAIT ) {
					mMarginLeft = 60;
					mMarginTop = 100;
					mMarginRight = 60;
					mMarginBottom = 120;

				} else {
					mMarginLeft = 80;
					mMarginTop = 95;
					mMarginRight = 80;
					mMarginBottom = 120;
				}

				mFontSize = 130;
			}

		} else {

			JSONObject object = getJSONObjectFromFile(mStyleFilePath);

			if( object == null ) return;

				try {
					if( object.isNull("fontSize") ) {
						mFontSize = 100;
					} else {
						mFontSize = object.getInt("fontSize");
					}

					if( object.isNull("fontName") ) {
						mFontName = "";
						mFaceName = "";
						mFontPath = "";
					} else {
						mFontName = object.getString("fontName");
						mFaceName = object.getString("faceName");
						mFontPath = object.getString("fontPath");
					}

					if( object.isNull("lineSpace") ) {
						mLineSpace = 130;
					} else {
						mLineSpace = object.getInt("lineSpace");
					}

					if( object.isNull("paraSpace") ) {
						mParaSpace = 2;
					} else {
						mParaSpace = object.getInt("paraSpace");
					}

					if( !object.isNull("marginLeft" ) ) {
						mMarginLeft = object.getInt("marginLeft");
						mMarginTop = object.getInt("marginTop");
						mMarginRight = object.getInt("marginRight");
						mMarginBottom = object.getInt("marginBottom");
					}

					if( object.isNull("indent") ) {
						mIndent = 0;
					} else {
						mIndent = object.getInt("indent");
					}
				} catch (JSONException e) {
					DebugSet.d(LOGTAG, "readStyleFile JSONException : " + e.getMessage());
				}

			DebugSet.d(LOGTAG, "load style ..................... " + object.toString());
		}
	}

	/**
	 * 세팅한 스타일 설정값으로 파일을 저장한다.
	 */
	public void writeStyleFile(){

		try {

			DebugSet.d(LOGTAG, "save style ................ " + mStyleFilePath);

			File styleFile = new File(mStyleFilePath);
			if( !styleFile.exists()) {
				styleFile.createNewFile();
			}

			FileOutputStream output = new FileOutputStream(styleFile);

			JSONObject object = new JSONObject();

			object.put("fontSize", mFontSize);

			object.put("fontName", mFontName);
			object.put("faceName", mFaceName);
			object.put("fontPath", mFontPath);

			object.put("lineSpace", mLineSpace);

			object.put("paraSpace", mParaSpace);

			object.put("marginLeft", mMarginLeft);
			object.put("marginTop", mMarginTop);
			object.put("marginRight", mMarginRight);
			object.put("marginBottom", mMarginBottom);

			object.put("indent", mIndent);
			
			object.put("useDefault", false);

			DebugSet.d(LOGTAG, "json array ................. " + object.toString(1));

			output.write(object.toString(1).getBytes());

			output.close();

		} catch (IOException e) {
			DebugSet.e(LOGTAG, "writeStyleFile IOException : " + e.getMessage());
		} catch (JSONException e) {
			DebugSet.e(LOGTAG, "writeStyleFile JSONException : " + e.getMessage());
		}
	}

	/**
	 * 폰트 사이즈 세팅
	 * 
	 * @param fontSize int : 폰트 사이즈 % 값
	 */
	public void setFontSize(Integer fontSize){
		mFontSize = fontSize;
	}

	/**
	 * 폰트 종류 세팅
	 * 
	 * @param fontName String : 화면에 보여지는 폰트명
	 * @param faceName String : 폰트 family에 정의된 face 명
	 * @param fontPath String : 폰트 face에 정의된 path
	 */
	public void setFont(String fontName, String faceName, String fontPath){
		mFontName = fontName;
		mFaceName = faceName;
		mFontPath = fontPath;
	}

	/**
	 * 줄간격 세팅
	 *
	 * @param lineSpace int : 줄간격 설정 값
	 */
	public void setLineSpace(Integer lineSpace){
		mLineSpace = lineSpace;
	}

	/**
	 * 문단 간격 세팅
	 *
	 * @param paraSpace int : 문단 간격 설정 값 
	 */
	public void setParaSpace(Integer paraSpace){
		mParaSpace = paraSpace;
	}

	/**
	 * 들여쓰기 여부 세팅
	 *
	 * @param indent boolean : 들여쓰기 여부
	 */
	public void setIndent(Integer indent){
		mIndent = indent;
	}

	/**
	 * 여백 값 세팅
	 * 
	 * @param marginLeft int : 좌측 여백
	 * @param marginTop int : 상단 여백
	 * @param marginRight int : 우측 여백
	 * @param marginBottom int : 하단 여백
	 */
	public void setMargin(Integer marginLeft, Integer marginTop, Integer marginRight, Integer marginBottom){
		mMarginLeft = marginLeft;
		mMarginTop = marginTop;
		mMarginRight = marginRight;
		mMarginBottom = marginBottom;
	}


	/**
	 * 스타일 설정 전체를 파일로 저장한다.
	 *
	 * @param context Context : 
	 * @param stylePath String : 저장할 파일 경로 
	 * @param fontSize int : 폰트 사이즈 % 값
	 * @param fontName String : 화면에 보여지는 폰트명
	 * @param faceName String : 폰트 family에 정의된 face 명
	 * @param fontPath String : 폰트 face에 정의된 path
	 * @param lineSpace int : 줄간격 설정 값
	 * @param paraSpace int : 문단 간격 설정 값
	 * @param indent boolean : 들여쓰기 여부
	 * @param marginLeft int : 좌측 여백
	 * @param marginTop int : 상단 여백
	 * @param marginRight int : 우측 여백
	 * @param marginBottom int : 하단 여백
	 */
	public void saveAllStyle(Context context, String stylePath, Integer fontSize, String fontName, String faceName, String fontPath, 
			Integer lineSpace, Integer paraSpace, Integer indent, Integer marginLeft, Integer marginTop, Integer marginRight, Integer marginBottom ){


		mContext = context;
		mStyleFilePath = stylePath;
		// 통으로 쓰는거라 read 생략.

		setFontSize(fontSize);
		setFont(fontName, faceName, fontPath);
		setLineSpace(lineSpace);
		setParaSpace(paraSpace);
		setIndent(indent);
		setMargin(marginLeft, marginTop, marginRight, marginBottom);

		writeStyleFile();

	}

	/**
	 * getJSONObjectFromFile
	 *
	 * @param filePath String : 읽을 파일 위치정보
	 * @return JSONObject JSONObject : 파일에서 읽은 JSONObject
	 */
	private JSONObject getJSONObjectFromFile(String filePath) {

		try {
			File dataFile = new File( filePath );
			if( !dataFile.exists() ) {
				DebugSet.d( LOGTAG, " getJSONObjectFromFile() !DataFile.exists()" );
				return null;
			}

			InputStream input = new FileInputStream( dataFile );

			String inputByte = BookHelper.inputStream2String(input);
			input.close();

			return new JSONObject(inputByte);

		} catch( Exception e ) {
			e.printStackTrace();
		}

		return null;
	}

}
