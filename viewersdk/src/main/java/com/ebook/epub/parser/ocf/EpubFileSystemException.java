package com.ebook.epub.parser.ocf;

public class EpubFileSystemException extends Exception {

	private static final long serialVersionUID = 1L;
	private int mErrorCode = -1;

	public EpubFileSystemException(Throwable th){
		super(th);
	}
	
	
	public EpubFileSystemException(String msg){
		super(msg);
	}
	
	public EpubFileSystemException(int errorCode, String msg){
		super(msg);
		mErrorCode = errorCode;
	}
	
	public EpubFileSystemException(int errorCode, String msg, Object...params){
		super(makeMessage(msg, params));
		mErrorCode = errorCode;
	}
	
	public EpubFileSystemException(int errorCode, Throwable th, String msg, Object...params){
		super(makeMessage(msg, params), th);
		mErrorCode = errorCode;
	}
	
	
	public int getErrorCode(){
		return mErrorCode;
	}
	
	private static String makeMessage(String msg, Object... params){
		String resultMsg = "Msg = " + msg + ", Param = ";
		
		for (int i = 0; i < params.length; i++) {
			resultMsg += "'%s' ";
		}
		
		return String.format(resultMsg, params);
	}
}
