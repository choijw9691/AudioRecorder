package com.ebook.epub.viewer.data;
import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubFile;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlItem;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.viewer.DebugSet;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * EPUB에 포함된 ImageList 관리 클래스
 * 
 * @author djHeo
 *
 */
public class ImageReader {
	private EpubFile epubFile;
	private ArrayList<String> imageList = new ArrayList<String>();
	
	public UnModifiableArrayList<String> getPublicationImageList() {
		return new UnModifiableArrayList<>(imageList);
	}
	
	public ImageReader(EpubFile epub) throws XmlPackageException, XmlContainerException, EpubFileSystemException {
		this.epubFile = epub;
		readImageOfContents();
	}
	
	public void readImageOfContents() throws XmlPackageException, XmlContainerException, EpubFileSystemException {
		
		Iterator<XmlItem> images = epubFile.getPublicationImages();
		
		while (images.hasNext()) {
			XmlItem xmlItem = (XmlItem) images.next();
			
			String fullPath = epubFile.getPublicationPath() + "/" + xmlItem.getHRef();
			DebugSet.d("TAG", "image :" + fullPath);
			imageList.add(fullPath);
		}
	}
}
