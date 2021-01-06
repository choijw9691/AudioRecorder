package com.ebook.epub.viewer;

import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.parser.ops.XmlChapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EpubFileUtil {

	/**
	 * 폴더의 부모 경로와 폴더명을 전달하면 폴더가 존재하는지 확인하여 전체 폴더 경로를 전달한다.
	 * @param path
	 * @param name
	 * @return
	 */
	public static FileInfo getResourceDirectory(String path, String name) {

		File fileByLowerCase = new File(path+"/"+name);
		File fileByUpperCase = new File(path+"/"+name.toUpperCase());
		
		if( fileByLowerCase.exists() ){
			if( fileByLowerCase.isDirectory() ){
				return new FileInfo(path + "/" + name, fileByLowerCase.length());
			}
		}
		
		if( fileByUpperCase.exists() ){
			if( fileByUpperCase.isDirectory() ){
				return new FileInfo(path + "/" + name.toUpperCase(), fileByUpperCase.length());
			}
		}
		
		return null;
	}

	/**
	 * 파일의 부모 경로와 파일명을 전달하면 파일이 존재하는지 확인하여 전체 파일 경로를 전달한다.
	 * @param path
	 * @param name
	 * @return
	 */
	public static FileInfo getResourceFile(String path, String name) {
		if(name.startsWith("/") || name.startsWith("../")){
			name= name.replace("../","/");
			name=name.substring(1, name.length());
			File file = new File(path);
			String parentPath = file.getParent();
			file = new File(parentPath + "/" + name);
			if( file!=null && file.exists() && file.isFile() ){
				return new FileInfo(parentPath + "/" + name, file.length());
			}
		} else {
			File file = new File(path + "/" + name);
			if( file!=null && file.exists() && file.isFile() ){
				return new FileInfo(path + "/" + name, file.length());
			}
		}
		return null;
	}

	public static FileInfo getResourceFile(String path, XmlChapter chapter) {
		String href = chapter.gethRef();
		String id = "";
		String fileName = href;
		String dirName = "";
		String parentPath = "";

		if (path.endsWith("/"))
			parentPath = path.substring(0, path.length()-1);
		else
			parentPath = path;
			
		//href에 '../'가 포함되어 있는 경우 보정
		//path에서 제일 끝 폴더는 제거
		if( fileName.indexOf("../") != -1 ){
			parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"));
			fileName = fileName.replace("../", "");
			fileName = fileName.replace("./", "");
		}

		if( fileName.lastIndexOf("#") != -1 ) {
			fileName = fileName.substring(0, fileName.lastIndexOf("#"));
			id = href.substring(href.lastIndexOf("#"));
		}
		if( fileName.lastIndexOf("/") != -1 ) {
			dirName = fileName.substring(0, fileName.lastIndexOf("/"));
			fileName = fileName.substring(fileName.lastIndexOf("/"));
		} 

		if( fileName.trim().length() <= 0 )
			return null;

        String fullPath = parentPath + "/" + dirName + fileName;
        		
		File file = new File(fullPath);

		if( file.exists() && file.isFile() )
			return new FileInfo(fullPath + id, file.length());

		return null; //파일이 존재하지 않으면 null 리턴
	}

	public static FileInfo getResourceFile(String path, XmlItem name) {

		String href = name.getHRef();
		String fileName = href;
		String dirName = "";

		if( fileName.trim().length() <= 0 )
			return null;

		if(path.endsWith("/")){
			File file = new File(path + dirName + fileName);
			if( file.exists() && file.isFile() )
				return new FileInfo(path + dirName + fileName, file.length());
		} else{
			File file = new File(path + "/" + dirName + fileName);
			if( file.exists() && file.isFile() )
				return new FileInfo(path + "/" + dirName + fileName, file.length());
		}

		return null; //파일이 존재하지 않으면 null 리턴
	}
	
	public static String getURLEncode(String content) throws UnsupportedEncodingException{
		return URLEncoder.encode(content, "utf-8");
	}
	
	public static String getURLDecode(String content) throws UnsupportedEncodingException{
		return URLDecoder.decode(content, "utf-8");
	}
	
	public static JSONObject getJSONObjectFromFile(String filePath) {

		try {
			File dataFile = new File( filePath );
			if( !dataFile.exists() ) {
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

    public static String readFile(String filePath) {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } finally {
            try {
                if (br!=null) br.close();
            } catch (Exception e) {}
        }
        return sb.toString();
    }
}
