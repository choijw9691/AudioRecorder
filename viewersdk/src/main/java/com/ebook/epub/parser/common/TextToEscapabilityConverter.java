package com.ebook.epub.parser.common;

public class TextToEscapabilityConverter implements IConverter<EscapabilityOption>{
	
	private final String glossary 	= "glossary";
	private final String glossdef 	= "glossdef";
	private final String glossterm 	= "glossterm";
	
	@Override
	public EscapabilityOption convert(Object obj) {
		
		String epubType = (String) obj;
		
		if(epubType.equalsIgnoreCase(glossary)){
			return EscapabilityOption.Glossary;
		} else if(epubType.equalsIgnoreCase(glossdef)){
			return EscapabilityOption.Glossdef;
		} else if(epubType.equalsIgnoreCase(glossterm)){
			return EscapabilityOption.GlossTerm;
		} else{
			return EscapabilityOption.None;
		}
	}
}
