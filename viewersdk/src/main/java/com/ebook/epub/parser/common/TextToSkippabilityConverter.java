package com.ebook.epub.parser.common;

public class TextToSkippabilityConverter implements IConverter<SkippabilityOption>{
	
	private final String annotation = "annotation";
	private final String footnote 	= "footnote";
	private final String help 		= "help";
	private final String marginalia = "marginalia";
	private final String note 		= "note";
	private final String pagebreak 	= "pagebreak";
	private final String practice 	= "practice";
	private final String rearnote 	= "rearnote";
	private final String sidebar 	= "sidebar";
	
	@Override
	public SkippabilityOption convert(Object obj) {
		
		String epubType = (String) obj;
		
		if(epubType.equalsIgnoreCase(annotation)){
			return SkippabilityOption.Annotation;
		} else if(epubType.equalsIgnoreCase(footnote)){
			return SkippabilityOption.Footnote;
		} else if(epubType.equalsIgnoreCase(help)){
			return SkippabilityOption.Help;
		} else if(epubType.equalsIgnoreCase(marginalia)){
			return SkippabilityOption.Marginalia;
		} else if(epubType.equalsIgnoreCase(pagebreak)){
			return SkippabilityOption.Pagebreak;
		} else if(epubType.equalsIgnoreCase(practice)){
			return SkippabilityOption.Practice;
		} else if(epubType.equalsIgnoreCase(rearnote)){
			return SkippabilityOption.Rearnote;
		} else if(epubType.equalsIgnoreCase(sidebar)){
			return SkippabilityOption.Sidebar;
		} else{
			return SkippabilityOption.None;
		}
	}

}
