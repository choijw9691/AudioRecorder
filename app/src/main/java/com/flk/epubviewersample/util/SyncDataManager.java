package com.flk.epubviewersample.util;

import com.ebook.epub.viewer.DebugSet;
import com.flk.epubviewersample.data.Define;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class SyncDataManager {
	
	private final static String CLASSTAG = "SyncDataManager";
	private final static String VER_1 = "1.0";
	private final static String VER_2 = "2.0";
	
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : checkSyncData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 신규데이터 인지 아닌지 구별하여 기존 데이터이면 신규 데이터로 포맷변환한 JSONObject를 리턴하고
	 * 				    신규 데이터일 경우 데이터 그대로 리턴한다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:06:42
	 * </PRE>
	 *   @return JSONObject
	 *   @param dataFilePath
	 */
	public JSONObject checkSyncData(String dataFilePath){
		try {
		
		boolean isNew = false;
		JSONObject dataObj = FileUtil.getJSONObjectFromFile( dataFilePath );
//		Log.d( CLASSTAG, dataObj.toString(1) );
		if( dataObj == null )
			return null;
		//Define.ANNOTATION_KEY의 유무로 기존 데이터인지 신규 데이터인지 판단.
		if( dataObj.has( Define.ANNOTATION_KEY ) )
			isNew = false;
		else
			isNew = true;
		
		if( isNew )
			return dataObj;
		
		JSONObject  newData = new JSONObject();
		JSONArray 	newArray = convertBookmarkList( dataObj.getJSONObject( Define.ANNOTATION_KEY ).getJSONArray( Define.BOOKMARK_KEY_BOOKMARKS ) );
		
		newData.put( Define.COMMON_DATA_TYPE, Define.NEW_BOOMARK_DATA_TYPE );
		newData.put( Define.NEW_BOOKMARK_VERSION_KEY, "2.0" );
		newData.put( Define.NEW_BOOKMARK_LIST_KEY, newArray );
		
		return newData;
		
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : convertBookmarkList
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 기존 포맷의 JSONArray를 전달하면 신규 포맷의 JSONArray를 리턴한다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:09:54
	 * </PRE>
	 *   @return JSONArray
	 *   @param preArray
	 *   @return
	 *   @throws JSONException
	 */
	private JSONArray convertBookmarkList(JSONArray preArray) throws JSONException {
		JSONArray newArray = new JSONArray();
		
		for( int i = 0; i < preArray.length(); i++ ){
			JSONObject newItem = new JSONObject();
			JSONObject preItem = preArray.getJSONObject( i );
			preItem = validatePreBookmak( preItem );
			if( preItem == null )
				continue;
			
			newItem.put( Define.NEW_BOOKMARK_ID_KEY, preItem.getLong( Define.BOOKMARK_KEY_ID ) );
			newItem.put( Define.NEW_BOOKMARK_PERCENT_KEY, preItem.getDouble( Define.BOOKMARK_KEY_PERCETNINCHAPTER ) );
			newItem.put( Define.NEW_BOOKMARK_CHAPTER_NAME_KEY, preItem.getJSONObject( Define.BOOKMARK_KEY_CONTENT ).getString( Define.BOOKMARK_KEY_CONTENT_DATA ) );
			newItem.put( Define.NEW_BOOKMARK_TIME_KEY, preItem.getString( Define.BOOKMARK_KEY_CREATETIME ) );
			newItem.put( Define.NEW_BOOKMARK_COLOR_KEY, preItem.getInt( Define.BOOKMARK_KEY_COLOR ) );
			newItem.put( Define.NEW_BOOKMARK_PATH_KEY, "" );
			newItem.put( Define.NEW_BOOKMARK_FILE_KEY, "" );
			newItem.put( Define.NEW_BOOKMARK_TYPE_KEY, "" );
			newItem.put( Define.NEW_BOOKMARK_TEXT_KEY, "" );
			newItem.put( Define.NEW_BOOKMARK_EXTRA1_KEY, preItem.getInt( Define.BOOKMARK_KEY_CHAPTERINDEX) );
			newItem.put( Define.NEW_BOOKMARK_EXTRA2_KEY, "" );
			newItem.put( Define.NEW_BOOKMARK_EXTRA3_KEY, "" );
			
			newArray.put( newItem );
		}
		
		
		return newArray;
	}
	
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : validatePreBookmak
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 북마크 버전1.0 Data 유효성 검사 메소드
	 * 				    필수 요소가 없는 경우는 null을 리턴하고,
	 * 				    기타 요소가 없는 경우는 채워서 리턴한다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2013. 1. 4. 오전 10:10:19
	 * </PRE>
	 *   @return JSONObject
	 *   @param preItem
	 *   @return
	 */
	private JSONObject validatePreBookmak(JSONObject preItem){
		try {
			Date d = null;
			//ID 값이 기존 yyyymmddhhmmss 형식이었던 것을 time 값으로 변경 처리.
			if( preItem.has( Define.BOOKMARK_KEY_ID ) && preItem.getString( Define.BOOKMARK_KEY_ID ).trim().length() > 0 ){
				String id;
				id = preItem.getString( Define.BOOKMARK_KEY_ID );
				
				//IOS
				if( id.length() == 10 ){
					preItem.put( Define.BOOKMARK_KEY_ID, id );
//					d = new Date( id );
				} else {
					SimpleDateFormat format = new SimpleDateFormat( "yyyymmddhhmmss" );
					TimeZone tz = TimeZone.getTimeZone("Asia/Seoul"); 
					format.setTimeZone(tz);
					d = format.parse( id );
					long time = d.getTime() / 1000;
					preItem.put( Define.BOOKMARK_KEY_ID, String.valueOf( time ) );
				}
				
			} else { 
				//convert 시점의 time 값을 넣어줌. 
				d = new Date();
				long time = d.getTime() / 1000;
				preItem.put( Define.BOOKMARK_KEY_ID, String.valueOf( time ) );
			}
			if( !preItem.has( Define.BOOKMARK_KEY_CHAPTERINDEX ) || preItem.getString( Define.BOOKMARK_KEY_CHAPTERINDEX ).trim().length() <= 0 )
				return null;
			if( !preItem.has( Define.BOOKMARK_KEY_CONTENT )
					|| !preItem.getJSONObject( Define.BOOKMARK_KEY_CONTENT ).has( Define.BOOKMARK_KEY_CONTENT_DATA ) 
					|| preItem.getJSONObject( Define.BOOKMARK_KEY_CONTENT ).getString( Define.BOOKMARK_KEY_CONTENT_DATA ).trim().length() <= 0 )
				return null;
			if( !preItem.has( Define.BOOKMARK_KEY_PERCETNINCHAPTER ) || preItem.getString( Define.BOOKMARK_KEY_PERCETNINCHAPTER ).trim().length() <= 0 )
				return null;
			
//			if( !preItem.has( Define.BOOKMARK_KEY_COLOR ) || preItem.getString( Define.BOOKMARK_KEY_COLOR ).trim().length() <= 0 )
			preItem.put( Define.BOOKMARK_KEY_COLOR, Define.BOOKMARK_KEY_COLOR_DEFAULT );
			
			if( !preItem.has( Define.BOOKMARK_KEY_CREATETIME ) || preItem.getString( Define.BOOKMARK_KEY_CREATETIME ).trim().length() <= 0 ){
				SimpleDateFormat format = new SimpleDateFormat( "yyyy-mm-dd hh:mm:ss" );
				TimeZone tz = TimeZone.getTimeZone("Asia/Seoul"); 
				format.setTimeZone(tz);
				preItem.put( Define.BOOKMARK_KEY_CREATETIME, format.format( d ) );
			} else {
				// 일시 마이그레이션 추가
				String createTime = preItem.getString( Define.BOOKMARK_KEY_CREATETIME );	        
		        if(createTime.indexOf(":") == -1){
		        	try {
		        		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		        		TimeZone tz = TimeZone.getTimeZone("Asia/Seoul"); 
		        		sdf1.setTimeZone(tz);
		        		Date date = sdf1.parse(createTime);

		        		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        		sdf2.setTimeZone(tz);
		        		createTime = sdf2.format(date);
		        		
		        		preItem.put( Define.BOOKMARK_KEY_CREATETIME, createTime );

		        	} catch (ParseException e) {
		        		e.printStackTrace();
		        	}
		        }
			}
			
		} catch( JSONException e ) {
			return null;
		} catch( ParseException e ) {
			e.printStackTrace();
			return null;
		}
		return preItem;
	}
	//사용안함.
	//추후 버전체크 
	private String isNewVersion(JSONObject serverData) throws JSONException{
		if( serverData.has( Define.ANNOTATION_KEY ) )
			return VER_1;
		else if( serverData.has( Define.COMMON_DATA_TYPE ) && serverData.getString( Define.COMMON_DATA_TYPE ).equals( Define.NEW_BOOMARK_DATA_TYPE ) ){
			if( serverData.has( Define.NEW_BOOKMARK_VERSION_KEY ) ){
				String version = serverData.getString( Define.NEW_BOOKMARK_VERSION_KEY );
				return version;
			}
		}
		return "";
	}
	
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : setSyncReadPositionData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 서버에서 받은 ReadPosition String 데이터를 로컬 파일로 저장해준다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:17:45
	 * </PRE>
	 *   @return void
	 *   @param serverReadPositionData : 서버에서 받은 String data
	 *   @param localDataDirPath : readPosition 파일이 저장될 컨텐츠 Rootpath
	 *   @param filePreFixName : readPosition 파일 앞에 붙을 preFixname , 빈 값을 보낼 경우 prefix 없이 저장.
	 */
//	public void setSyncReadPositionData(String serverReadPositionData, String localDataDirPath, String filePreFixName){
//		try {
//			String preName = "/";
//			if( filePreFixName != null && filePreFixName.trim().length() > 0 )
//				preName = "/"+filePreFixName+"_";
//			
//			if( serverReadPositionData == null || serverReadPositionData.equals( "" ))
//				return;
//			
//			
//			if( !serverReadPositionData.contains( "{" ) ){
//				//퍼센트 값 저장 방법
//				LogUtil.d( CLASSTAG, "!serverData.contains( \"{\" )" );
//				FileOutputStream outputStream = new FileOutputStream( new File( localDataDirPath + preName + Define.READ_POSITION_PERCENTAGE_FILE_NAME ) );
//				outputStream.write( serverReadPositionData.getBytes() );
//				outputStream.close();
//				return;
//			}
//			
//			JSONObject serverObject = new JSONObject( serverReadPositionData );
//			File localFile = new File( localDataDirPath + preName + Define.READ_POSITION_FILE_NAME);
//			
//			if( localFile.exists() ){
//				localFile.delete();
//			}
//			localFile.createNewFile();
//			
//			FileOutputStream outputStream = new FileOutputStream( localFile );
//			outputStream.write( serverObject.toString(0).getBytes() );
//			outputStream.close();
//			
//		} catch( JSONException e ) {
//			e.printStackTrace();
//		} catch( IOException e ) {
//			e.printStackTrace();
//		}
//	}
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : getReadPositionData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : ReadPosition 파일 내용을 String으로 전달한다. 마지막 읽은 위치를 서버에 전송할 때 사용.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:24:22
	 * </PRE>
	 *   @return String
	 *   @param localReadPositionFilePath
	 *   @return
	 */
	public String getReadPositionData(String localReadPositionFilePath){
		JSONObject localReadPositionData = FileUtil.getJSONObjectFromFile( localReadPositionFilePath );
		if(localReadPositionData != null && localReadPositionData.toString().trim().length() > 0)
			return localReadPositionData.toString();
		else
			return null;
	}
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : mergeBookmarkData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 신규 포맷 형식의 데이터 merge (기존포맷형식의 서버데이터는 신규로 변환하여 merge한다.)
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:25:39
	 * </PRE>
	 *   @return String
	 *   @param serverDataFilePath
	 *   @param localDataDirPath
	 *   @param filePreFixName
	 *   @return
	 */
	public String mergeBookmarkData(String serverDataFilePath, String localDataDirPath, String filePreFixName){
		try {
			String preName = "/";
			if( filePreFixName != null && filePreFixName.trim().length() > 0 )
				preName = "/"+filePreFixName+"_";
			
			//서버데이터가 기존포맷일 경우 신규 포맷으로 변환하여 merge 진행
			JSONObject serverData = checkSyncData( serverDataFilePath );
			
			String localBookmarkFilePath = localDataDirPath + preName + Define.BOOKMARK_FILE_NAME;
			String localHistoryFilePath = localDataDirPath + preName + Define.BOOKMARK_HISTORY_FILE_NAME;
			File localFile = new File( localBookmarkFilePath );
			File historyFile = new File( localHistoryFilePath );
			
			if( serverData == null ){
				if( localFile.exists() && FileUtil.getJSONObjectFromFile( localBookmarkFilePath ) != null ){
					if( FileUtil.getJSONObjectFromFile( localHistoryFilePath ) != null ){
						historyFile.delete();
						return localBookmarkFilePath;
					} else {
						localFile.delete();
						return null;
					}
				} else 
					return null;
			} else {
				if( !localFile.exists() || FileUtil.getJSONObjectFromFile( localBookmarkFilePath ) == null || FileUtil.getJSONObjectFromFile( localHistoryFilePath ) == null ){
					FileUtil.fileCopy( new File( serverDataFilePath ), localFile );
					return null;
				}
			}
			
			String data = serverData.toString(1);
			LogUtil.d( CLASSTAG, "server : " + data );
			
			File localBakFile = new File( localDataDirPath + preName +Define.BOOKMARK_BACKUP_FILE_NAME );
			if( !localBakFile.exists() )
				localBakFile.createNewFile();
			File historyBakFile = new File( localDataDirPath + preName +Define.BOOKMARK_HISTORY_BACKUP_FILE_NAME );
			if( !historyBakFile.exists() )
				historyBakFile.createNewFile();
			
			FileUtil.fileCopy( localFile, localBakFile );
			FileUtil.fileCopy( historyFile, historyBakFile );
			
			JSONArray serverArray = serverData.getJSONArray( Define.NEW_BOOKMARK_LIST_KEY );
			JSONArray localArray = FileUtil.getJSONObjectFromFile( localBookmarkFilePath ).getJSONArray( Define.NEW_BOOKMARK_LIST_KEY );
			JSONArray mergeArray = new JSONArray();
			JSONObject historyObject = FileUtil.getJSONObjectFromFile( localHistoryFilePath );
			
			for( int i=0; i < serverArray.length(); i++ ) {
				if( historyObject.has( serverArray.getJSONObject( i ).getString( Define.NEW_BOOKMARK_ID_KEY ) ) ){
					continue;
				}
				mergeArray.put( serverArray.getJSONObject( i ) );
			}
			for( int i=0; i < localArray.length(); i++ ){
				if( historyObject.has( localArray.getJSONObject( i ).getString( Define.NEW_BOOKMARK_ID_KEY ) ) ){
					if( Integer.parseInt(historyObject.getString( localArray.getJSONObject( i ).getString( Define.NEW_BOOKMARK_ID_KEY ) )) == Define.HISTORY_ADD )
						mergeArray.put( localArray.getJSONObject( i ) );
				}
			}
			
			JSONObject mergeObject = FileUtil.getJSONObjectFromFile( localBookmarkFilePath );
			mergeObject.put( Define.NEW_BOOKMARK_LIST_KEY, mergeArray );
			
			LogUtil.d( CLASSTAG, "merge : " + mergeObject.toString(1) );
			
			FileOutputStream outputStream = new FileOutputStream( localFile );
			outputStream.write( mergeObject.toString(0).getBytes() );
			outputStream.close();
			
			historyFile.delete();
			
			return localBookmarkFilePath;
		} catch(JSONException e){
			e.printStackTrace();
			return null;
		} catch( IOException e ) {
			e.printStackTrace();
			return null;
		}
	} 
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : revertBookmarkData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:34:55
	 * </PRE>
	 *   @return void
	 *   @param localDataDirPath
	 */
	public void revertBookmarkData(String localDataDirPath, String filePreFixName){
		try {
			String preName = "/";
			if( filePreFixName != null && filePreFixName.trim().length() > 0 )
				preName = "/"+filePreFixName+"_";
			
			File localBakFile = new File( localDataDirPath + preName + Define.BOOKMARK_BACKUP_FILE_NAME );
			if( !localBakFile.exists() )
				return;
			File historyBakFile = new File( localDataDirPath + preName + Define.BOOKMARK_HISTORY_BACKUP_FILE_NAME );
			if( !historyBakFile.exists() )
				return;
			
			File localFile = new File( localDataDirPath + preName +Define.BOOKMARK_FILE_NAME );
			if( !localFile.exists() )
				localFile.createNewFile();
			File historyFile = new File( localDataDirPath + preName +Define.BOOKMARK_HISTORY_FILE_NAME );
			if( !historyFile.exists() )
				historyFile.createNewFile();
			
			FileUtil.fileCopy( localBakFile, localFile );
			FileUtil.fileCopy( historyBakFile, historyFile );
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * <PRE>
	 *  마지막 읽은 위치 동기화 처리.
	 *  자동 동기화를 하지않는 경우도 serverData를 null로 입력하여 호출한다.
	 * </PRE>
	 * 
	 * @param serverData  서버에서 내려받은 데이터
	 * @param xPathFilePath xPathFile파일경로.
	 * @param percentFilePath percentFile파일경로.
	 * @return boolean 성공유무
	 */
	public Boolean setSyncReadPositionData(String serverData, String xPathFilePath, String percentFilePath){
		
		boolean result = true;
		
		if(xPathFilePath == null || xPathFilePath.trim().length() < 16) return false;
		if(percentFilePath == null) percentFilePath = "";
		
		File xPathFile = new File(xPathFilePath);
		File percentFile = new File(percentFilePath);
		
		try {
			
			if(xPathFile.exists()) { LogUtil.d( CLASSTAG, "## xPathFile is exists" ); }
			if(percentFile.exists()) { LogUtil.d( CLASSTAG, "## percentFile is exists" ); }
			
			if (serverData == null || serverData.trim().length() == 0) {
				LogUtil.d( CLASSTAG, "## serverData null" );
				
				if(!xPathFile.exists()) {

					if(percentFile.exists()){
						JSONObject jObject = FileUtil.getJSONObjectFromFile(percentFilePath);
						result = xPathFileWriter(xPathFilePath, jObject.getJSONObject(Define.OLD_READ_POSITION_MAIN_KEY), null, CHECK_TYPE_OLD_LOCAL);
					} 
				}
				
			} else {
				LogUtil.d( CLASSTAG, "## serverData not null" );
				if(serverData.indexOf("}") == -1) {
					
					if(!xPathFile.exists()) {
						if(percentFile.exists()) {
							JSONObject mainObject = FileUtil.getJSONObjectFromFile(percentFilePath);
							JSONObject jObject = mainObject.getJSONObject(Define.OLD_READ_POSITION_MAIN_KEY);
							
							if (serverData.equals(jObject.getString(Define.OLD_READ_POSITION_PERCENT_KEY))) {
								result = xPathFileWriter(xPathFilePath, jObject, null, CHECK_TYPE_OLD_LOCAL);
							} else {
								result = xPathFileWriter(xPathFilePath, null, serverData, CHECK_TYPE_OLD_SERVER);
							}
							
						} else{
							result = xPathFileWriter(xPathFilePath, null, serverData, CHECK_TYPE_OLD_SERVER);
						}
					}

				} else {
					JSONObject server = new JSONObject( serverData );
					if( server.has( Define.NEW_READ_POSITION_PATH_KEY ) ){
						//2013.04.10
						//djheo
						result = xPathFileWriter(xPathFilePath, mergeReadPositionData( serverData, xPathFilePath ), null, CHECK_TYPE_NEW);
					}
					else
						result = pageFileWriter( xPathFilePath, serverData );
				}
			}
			
			LogUtil.d( CLASSTAG, "## mergeReadPosition return : " + result );
			return result;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	private final int CHECK_TYPE_OLD_SERVER = 100;
	private final int CHECK_TYPE_OLD_LOCAL 	= 200;
	private final int CHECK_TYPE_NEW 		= 300;
	
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : pageFileWriter
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : PDF 마지막 읽은 위치 File 처리 메소드
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2013. 1. 7. 오후 4:41:12
	 * </PRE>
	 *   @return Boolean
	 *   @param filePath
	 *   @param serverData
	 *   @return
	 */
	private Boolean pageFileWriter(String filePath, String serverData){
		try {
			File localFile = new File( filePath );
			if( localFile.exists() ){
				localFile.delete();
			}
			
			localFile.createNewFile();
			
			FileOutputStream outputStream = new FileOutputStream( localFile );
			outputStream.write( serverData.getBytes() );
			outputStream.close();
			
			return true;
		} catch( IOException e ) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : mergeReadPositionData
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : time 키를 이용하여 서버와 로컬데이터 중 최신 데이터를 리턴한다.
	 * 4. 작성자    : djheo
	 * 5. 작성일    : 2013. 4. 10. 오후 2:44:25
	 * </PRE>
	 *   @return JSONObject
	 *   @param serverStr
	 *   @param localFilePath
	 *   @return
	 */
	private JSONObject mergeReadPositionData(String serverStr, String localFilePath){
		
		JSONObject lastesData = new JSONObject();
		
		try {
			JSONObject serverData = new JSONObject( serverStr );
			JSONObject localData = FileUtil.getJSONObjectFromFile(localFilePath);
			
			if( localData == null ) { LogUtil.d( CLASSTAG, "localFile not exist.." ); return serverData; }
			
			long serverTime;
			long localTime;
			
			if( serverData.has( Define.NEW_READ_POSITION_TIME_KEY ) && !serverData.isNull( Define.NEW_READ_POSITION_TIME_KEY ) ) {
				serverTime = serverData.getLong( Define.NEW_READ_POSITION_TIME_KEY );
				
				if( localData.has( Define.NEW_READ_POSITION_TIME_KEY ) && !localData.isNull( Define.NEW_READ_POSITION_TIME_KEY ) ) {
					localTime = localData.getLong( Define.NEW_READ_POSITION_TIME_KEY );
					if( serverTime > localTime ) {
						LogUtil.d( CLASSTAG, "server yes, local yes, lastes server" );
						lastesData = serverData;
					} else {
						LogUtil.d( CLASSTAG, "server yes, local yes, lastes local" );
						lastesData = localData;
					}
				} else {
					LogUtil.d( CLASSTAG, "server yes, local no, lastes server" );
					lastesData = serverData;
				}
			} else {
				
				if( localData.has( Define.NEW_READ_POSITION_TIME_KEY ) && !localData.isNull( Define.NEW_READ_POSITION_TIME_KEY ) ) {
					LogUtil.d( CLASSTAG, "server no, local yes, lastes local" );
					lastesData = localData;
				} else {
					LogUtil.d( CLASSTAG, "server no, local no, lastes server" );
					lastesData = serverData;
				}
			}
		} catch( JSONException e ) {
			e.printStackTrace();
		}
		return lastesData;
	}

	/**
	 * <PRE>
	 *  xPathFile 파일을 write 한다.
	 * </PRE>
	 * 
	 * @param xPathFilePath write할 파일경로
	 * @param jObject write할 data
	 * @param serverData write할 data
	 * @param checkType write Type
	 * @return boolean 성공유무
	 */
	private Boolean xPathFileWriter(String xPathFilePath, JSONObject jObject, String serverData, int checkType){
		
		String file = null, path = null, percent = null, chapter_index = null, chapter_percent = null;
		long time = -1;
		
		File xPathFile = new File(xPathFilePath);
		
		try {
			
			switch (checkType) {
			case CHECK_TYPE_OLD_SERVER:
				LogUtil.d( CLASSTAG, "## xPathFileWriter CHECK_TYPE_OLD_SERVER" );
				
				percent = serverData;
				
				break;
				
			case CHECK_TYPE_OLD_LOCAL:
				LogUtil.d( CLASSTAG, "## xPathFileWriter CHECK_TYPE_OLD_LOCAL" );
				
				percent 		= jObject.getString(Define.OLD_READ_POSITION_PERCENT_KEY);
				chapter_index 	= jObject.getString(Define.OLD_READ_POSITION_CHAPTER_INDEX_KEY);
				chapter_percent = jObject.getString(Define.OLD_READ_POSITION_CHAPTER_PERCENT_KEY);
				
				break;
				
			case CHECK_TYPE_NEW:
				LogUtil.d( CLASSTAG, "## xPathFileWriter CHECK_TYPE_NEW" );
				
				file = jObject.getString(Define.NEW_READ_POSITION_FILE_KEY);
				path = jObject.getString(Define.NEW_READ_POSITION_PATH_KEY);
				chapter_percent = jObject.getString(Define.NEW_READ_POSITION_CHAPTER_PERCENT_KEY);
				
				break;
			}
			
			if(xPathFile.exists()){
				xPathFile.delete();
			}
			
			//2013.04.10
			//djheo
			if( jObject.has( Define.NEW_READ_POSITION_TIME_KEY ) && !jObject.isNull( Define.NEW_READ_POSITION_TIME_KEY ) )
				time = jObject.getLong( Define.NEW_READ_POSITION_TIME_KEY );
			
			xPathFile.createNewFile();
	
			FileOutputStream fos = new FileOutputStream( xPathFile );
			
			JSONObject newData = new JSONObject();
			
			if(file != null && file.trim().length() > 0) 
				newData.put(Define.NEW_READ_POSITION_FILE_KEY, file); 
//			if(path != null && path.trim().length() > 0) 
				newData.put(Define.NEW_READ_POSITION_PATH_KEY, path);
			if(percent != null && percent.trim().length() > 0) 
				newData.put(Define.NEW_READ_POSITION_PERCENT_KEY, percent);
			if(chapter_index != null && chapter_index.trim().length() > 0) 
				newData.put(Define.NEW_READ_POSITION_CHAPTER_INDEX_KEY, chapter_index);
			if(chapter_percent != null && chapter_percent.trim().length() > 0) 
				newData.put(Define.NEW_READ_POSITION_CHAPTER_PERCENT_KEY, chapter_percent);
			if( time > 0 )
				newData.put( Define.NEW_READ_POSITION_TIME_KEY, time );
			
			fos.write(newData.toString(1).getBytes());
			
			fos.close();
			
			LogUtil.d( CLASSTAG, "## xPathFileWriter OK " + newData.toString(1) );
			return true;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public String mergeAnnotation(String serverDataFilePath, String localDataDirPath, String filePreFixName){
		try {
			
			String preName = "/";
			if( filePreFixName != null && filePreFixName.trim().length() > 0 )
				preName = "/"+filePreFixName+"_";
			
			String localAnnotationFilePath = localDataDirPath + preName + Define.ANNOTATION_FILE_NAME;
			String localHistoryFilePath = localDataDirPath + preName + Define.ANNOTATION_HISTORY_FILE_NAME;
			
			JSONObject serverData = FileUtil.getJSONObjectFromFile( serverDataFilePath );
			JSONObject localData = FileUtil.getJSONObjectFromFile( localAnnotationFilePath );
			JSONObject historyData = FileUtil.getJSONObjectFromFile( localHistoryFilePath );
			
			JSONArray serverArray;
			JSONArray localArray;
			JSONArray mergeArray = new JSONArray();
			if( serverData != null ){
				serverArray = serverData.getJSONArray(Define.ANNOTATION_LIST_KEY);
			} else {
				return localData != null ? localAnnotationFilePath : null;
			}
			
			if( localData != null ){
				localArray = localData.getJSONArray(Define.ANNOTATION_LIST_KEY);
			} else {
				File localFile = new File(localAnnotationFilePath);
				if( !localFile.exists() ) localFile.createNewFile();
				FileUtil.fileCopy(new File(serverDataFilePath), localFile);
				return null;
			}
			
			/////////Annotation 및 history 파일 백업 //////////
			File backupAnnotationFile = new File(localAnnotationFilePath.substring(0, localAnnotationFilePath.lastIndexOf("/")+1) + Define.ANNOTATION_BACKUP_FILE_NAME);
			File backupAnnotationHistoryFile = new File(localHistoryFilePath.substring(0, localHistoryFilePath.lastIndexOf("/")+1) + Define.ANNOTATION_HISTORY_BACKUP_FILE_NAME);
			
			if( !backupAnnotationFile.exists() )
				backupAnnotationFile.createNewFile();
			if( !backupAnnotationHistoryFile.exists() )
				backupAnnotationHistoryFile.createNewFile();
			
			FileUtil.fileCopy(new File(localAnnotationFilePath), backupAnnotationFile);
			FileUtil.fileCopy(new File(localHistoryFilePath), backupAnnotationHistoryFile);
			//////////////////////////////////////////////
			
			if( historyData != null ){
				for( int i = 0; i < serverArray.length(); i++ ){
					if( historyData.has(serverArray.getJSONObject(i).getString(Define.ANNOTATION_ID_KEY)) )
						continue;
					mergeArray.put(serverArray.getJSONObject(i));
				}
				
				for( int j = 0; j < localArray.length(); j++ ){
					if( historyData.has(localArray.getJSONObject(j).getString(Define.ANNOTATION_ID_KEY)) ){
						int type = Integer.parseInt(historyData.getString(localArray.getJSONObject(j).getString(Define.ANNOTATION_ID_KEY)).substring(0, 1));
						if( type % 2 == 1 )
							mergeArray.put(localArray.getJSONObject(j));
					}
				}
			} else {
				FileUtil.fileCopy(new File(serverDataFilePath), new File(localAnnotationFilePath));
				return null;
			}
			
			JSONObject mergeObject = FileUtil.getJSONObjectFromFile( localAnnotationFilePath );
			mergeObject.put( Define.ANNOTATION_LIST_KEY, mergeArray );
			
			DebugSet.d("TAG", "merge : " + mergeObject.toString(1));
			
			FileOutputStream outputStream = new FileOutputStream( localAnnotationFilePath );
			outputStream.write( mergeObject.toString(0).getBytes() );
			
			
			historyData = filterAnnotationHistory(serverArray, historyData);
			
			if( historyData != null ){
				FileOutputStream historyOutputStream = new FileOutputStream(localHistoryFilePath);
				historyOutputStream.write(historyData.toString(1).getBytes());
				historyOutputStream.close();
			} else {
				new File(localHistoryFilePath).delete();
			}
			
			outputStream.close();
			
			return localAnnotationFilePath;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 히스토리 데이터 중 서버데이터에 있는 아이디만 걸러서 전달
	 * @param serverArray
	 * @param history
	 * @return
	 */
	private JSONObject filterAnnotationHistory(JSONArray serverArray, JSONObject history) {
		
		try {
		
			JSONObject filterHistory = new JSONObject();
			
			Iterator<String> keys = history.keys();
			
			while (keys.hasNext()) {
				String id = (String) keys.next();
				if( Integer.parseInt(history.getString(id).substring(0, 1)) % 2 == 0 ) {
					for (int i = 0; i < serverArray.length(); i++) {
						if (serverArray.getJSONObject(i).getString(Define.ANNOTATION_ID_KEY).equals(id)) {
							filterHistory.put(id, history.getString(id));
							break;
						}
					}
				} else {
					filterHistory.put(id, history.getString(id));
				}
				
			}

			return filterHistory;
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void revertAnnotationData(String localDataDirPath, String filePreFixName){
		try {
			String preName = "/";
			if( filePreFixName != null && filePreFixName.trim().length() > 0 )
				preName = "/"+filePreFixName+"_";
			
			File localBakFile = new File( localDataDirPath + preName + Define.ANNOTATION_BACKUP_FILE_NAME );
			if( !localBakFile.exists() )
				return;
			File historyBakFile = new File( localDataDirPath + preName + Define.ANNOTATION_HISTORY_BACKUP_FILE_NAME );
			if( !historyBakFile.exists() )
				return;
			
			File localFile = new File( localDataDirPath + preName +Define.ANNOTATION_FILE_NAME );
			if( !localFile.exists() )
				localFile.createNewFile();
			File historyFile = new File( localDataDirPath + preName +Define.BOOKMARK_HISTORY_FILE_NAME );
			if( !historyFile.exists() )
				historyFile.createNewFile();
			
			FileUtil.fileCopy( localBakFile, localFile );
			FileUtil.fileCopy( historyBakFile, historyFile );
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
