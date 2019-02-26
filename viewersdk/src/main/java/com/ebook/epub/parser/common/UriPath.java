package com.ebook.epub.parser.common;

public class UriPath {
	
	private static final String CHAR_SHARP = "#";
	private static final String CHAR_SLASH = "/";
	
	public static String getUriDirectoryName(String path){
		
		path = replacePath(path);
		
		if(path == null){
			return "";
		}
			
		int lastIndex = path.lastIndexOf(CHAR_SLASH);
		if(lastIndex==-1){
			return "";
		}
		
		return path.substring(0, lastIndex-1);
	}
	
	public static String getUriFileName(String path){
		
		replacePath(path);
		
		if(path == null){
			return "";
		}
		
		int lastIndex = path.lastIndexOf(CHAR_SLASH);
		
		if(lastIndex!=-1){
			return path.substring(lastIndex, path.lastIndexOf(CHAR_SHARP) != -1 ? path.lastIndexOf(CHAR_SHARP) : path.length());
		} else {
			return path.substring(0,  path.lastIndexOf(CHAR_SHARP) != -1 ? path.lastIndexOf(CHAR_SHARP) : path.length());
		}
	}
	
	public static String getUriFilePath(String path){
		
		if(path == null){
			return "";
		}
		
		return path.substring(0,  path.lastIndexOf(CHAR_SHARP) != -1 ? path.lastIndexOf(CHAR_SHARP) : path.length());
	}

	public static String getUriFragmentValue(String path){
		
		if(path == null){
			return "";
		}
		
		int lastIndex = path.lastIndexOf(CHAR_SHARP);
		if(lastIndex==-1){
			return "";
		}
		
		return path.substring(lastIndex, path.length()).replace("#","");
	}
	
	private static String replacePath(String path){
		return path.replaceAll("../", "");
	}
}
