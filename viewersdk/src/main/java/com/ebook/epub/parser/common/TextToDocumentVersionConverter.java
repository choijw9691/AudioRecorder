package com.ebook.epub.parser.common;

public class TextToDocumentVersionConverter implements IConverter<DocumentVersions>{

	private final String version2 = "2.0";
	private final String version3 = "3.0";
	
	@Override
	public DocumentVersions convert(Object obj) {
		
		String version = (String) obj;
		
		if(version.equalsIgnoreCase(version2)){
			return DocumentVersions.Version2;
		} else if(version.equalsIgnoreCase(version3)){
			return DocumentVersions.Version3;
		}
		
		throw new NullPointerException();	// TODO ::: 
	}
}
