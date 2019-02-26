package com.ebook.epub.parser.ocf;

import com.ebook.epub.parser.common.ExceptionParameter;
import com.ebook.epub.parser.common.FileInfo;
import com.ebook.epub.viewer.EpubFileUtil;
import com.ebook.epub.viewer.ViewerErrorInfo;

/**
@class EpubContainer
@brief  
 */
public class EpubContainer {
	
	private String rootPath;
	private String containerPath;
	private String metaInfPath;
	private String mimeTypePath;
	
	private String encryptionFilePath;
	
	static int dircount = 0;
	static int filecount = 0;
	static int chaptercount = 0;
	static int itemcount = 0;
	
	public EpubContainer(String path) throws EpubFileSystemException {
		this.rootPath = path;

		metaInfPath = setMetaInfPath();
		containerPath = setContainerPath();
		mimeTypePath = setMimeTypeFilePath();
		encryptionFilePath = setEncryptionFilePath();
	}
	
	public String getContainerPath() {
		return containerPath;
	}

	public String getMetaInfPath() {
		return metaInfPath;
	}

	public String getMimeTypePath() {
		return mimeTypePath;
	}

	public String getEncryptionFilePath(){
		return encryptionFilePath;
		
	}
	private String setContainerPath() throws EpubFileSystemException {
		
		FileInfo container = EpubFileUtil.getResourceFile(getMetaInfPath(), EpubFileSystem.CONTAINER);
		
		if( container == null )
			throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_FILE_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_FILE_NOT_FOUND, ExceptionParameter.CONTAINER);
		
		return container.filePath;
		
	}
	
	private String setMetaInfPath() throws EpubFileSystemException {
		
		FileInfo metaInf = EpubFileUtil.getResourceDirectory(rootPath, EpubFileSystem.META_INF);
		
		if( metaInf == null )
			throw new EpubFileSystemException(ViewerErrorInfo.CODE_ERROR_EPUB_META_INF_DIRECTORY_NOT_FOUND, ViewerErrorInfo.MSG_ERROR_EPUB_META_INF_DIRECTORY_NOT_FOUND);
		
		return metaInf.filePath;
	}
	
	/**
	 * mimeType 파일 경로를 전달한다.
	 * @return
	 * @throws EpubFileSystemException
	 */
	private String setMimeTypeFilePath() {
		
		FileInfo mimeType = EpubFileUtil.getResourceFile(rootPath, EpubFileSystem.MIMETYPE);
		
		if( mimeType == null )
			return "";
//			throw new EpubFileSystemException("mimeType"); //필수아님
		
		return mimeType.filePath;
	}
	
	private String setEncryptionFilePath(){
		
		FileInfo encryptionFilePath = EpubFileUtil.getResourceFile(getMetaInfPath(), EpubFileSystem.ENCRYPTION);
		
		if( encryptionFilePath == null )
			return "";

		return encryptionFilePath.filePath;
	}
}
