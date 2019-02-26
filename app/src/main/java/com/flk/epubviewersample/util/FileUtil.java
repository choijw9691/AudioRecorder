package com.flk.epubviewersample.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

public class FileUtil {
	
	private final static String CLASSTAG = "FileUtil";
	
	public static void fileCopy(File in, File out) throws IOException
	{
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try
		{
			// magic number for Andorid, 8Mb - 32Kb)
			int maxCount = (8 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size)
			{
				position += inChannel.transferTo(position, maxCount, outChannel);
			}
		}
		catch (IOException e)
		{
			LogUtil.d(CLASSTAG, "copy Exception "+ e.getMessage());
		}
		finally
		{
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : convertInputStreamToByteArray
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : InputStream을 Byte Array로 변환하여 리턴한다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 6:06:49
	 * </PRE>
	 *   @return byte[]
	 *   @param istream
	 *   @return
	 *   @throws Exception
	 */
	public static byte[] convertInputStreamToByteArray(InputStream istream) throws Exception
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int num = 0;
			while( (num = istream.read(buf)) != -1)
			{
				bout.write(buf, 0, num);
			}
			byte[] ret = bout.toByteArray();
			bout.close();
			return ret;
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	/**
	 * 
	 * <PRE>
	 * 1. MethodName : getJSONObjectFromFile
	 * 2. ClassName  : SyncDataManager
	 * 3. Comment   : 파일을 읽어 JSONObject로 리턴한다.
	 * 4. 작성자    : ekwjd0119
	 * 5. 작성일    : 2012. 11. 19. 오후 5:48:55
	 * </PRE>
	 *   @return JSONObject
	 *   @param filePath
	 *   @return
	 */
	public static JSONObject getJSONObjectFromFile(String filePath){
		try {
			File dataFile = new File( filePath );
			if( !dataFile.exists() ) {
				LogUtil.d( CLASSTAG , " getJSONObjectFromFile() filePath not exist filePath :" + filePath );
				return null;
			}
			FileInputStream input = new FileInputStream( dataFile );
			
			byte[] inputByte = convertInputStreamToByteArray(input);
			
			String str = new String( inputByte, 0, inputByte.length, "utf-8" );
//			LogUtil.d( CLASSTAG , "getJSONObjectFromFile() str : " + str );
			
			if( str.equals( "" ) )
				return null;
			
			JSONObject jo = new JSONObject(str);
//			JSONObject jo = (JSONObject)new JSONTokener(str).nextValue();
			
			
			if( jo.length() <= 0 )
				return null;
			
			
			return jo;
			
		} catch( JSONException e ) {
			e.printStackTrace();
			return null;
		} catch( FileNotFoundException e ) {
			e.printStackTrace();
			return null;
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
//	public static String getBase64encode(String content){
//		return Base64.encodeToString(content.getBytes(), 0);
//	}
//	public static String getBase64decode(String content){
//		return new String(Base64.decode(content, 0));
//	}

//	public static String getURLEncode(String content) throws UnsupportedEncodingException{
//		return URLEncoder.encode(content, "utf-8");
//	}
//	
//	public static String getURLDecode(String content) throws UnsupportedEncodingException{
//		return URLDecoder.decode(content, "utf-8");
//	}
}