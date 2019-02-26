package com.ebook.epub.viewer;

/**
@class ReadingStyle
@brief 보기스타일 정보 class
 */
public class ReadingStyle {

	public Integer paragraphSpace;
	public Integer lineSpace;
	public Integer fontSize;
	public String fontPath;
	public String fontFace;
//	public Integer backgroundColor;
	public Integer textIndent;
	public Integer topMargin;
	public Integer bottomMargin;
	public Integer rightMargin;
	public Integer leftMargin;
	
//	public ReadingStyle(Integer paragraphSpace, Integer lineSpace,
//			Integer fontSize, String fontPath, String fontFace,
//			Integer backgroundColor, Integer textIndent, Integer topMargin,
//			Integer bottomMargin, Integer rightMargin, Integer leftMargin) {
	/**
	@breif ReadingStyle 생성자
	@param ReadingStyle 설정 정보들( 원본 : null ) 
	@return 생성된 ReadingStyle 객체
	 */
	public ReadingStyle(Integer paragraphSpace, Integer lineSpace,
			Integer fontSize, String fontPath, String fontFace,
			Integer textIndent, Integer topMargin, Integer bottomMargin, 
			Integer rightMargin, Integer leftMargin) {
		super();
		this.paragraphSpace = paragraphSpace;
		this.lineSpace = lineSpace;
		this.fontSize = fontSize;
		this.fontPath = fontPath;
		this.fontFace = fontFace;
//		this.backgroundColor = backgroundColor;
		this.textIndent = textIndent;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.rightMargin = rightMargin;
		this.leftMargin = leftMargin;
	}
	
	public Integer getParagraphSpace() {
		return paragraphSpace;
	}
	public void setParagraphSpace(Integer paragraphSpace) {
		this.paragraphSpace = paragraphSpace;
	}
	public Integer getLineSpace() {
		return lineSpace;
	}
	public void setLineSpace(Integer lineSpace) {
		this.lineSpace = lineSpace;
	}
	public Integer getFontSize() {
		return fontSize;
	}
	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontPath() {
		return fontPath;
	}
	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}
	public String getFontFace() {
		return fontFace;
	}
	public void setFontFace(String fontFace) {
		this.fontFace = fontFace;
	}
//	public Integer getBackgroundColor() {
//		return backgroundColor;
//	}
//	public void setBackgroundColor(Integer backgroundColor) {
//		this.backgroundColor = backgroundColor;
//	}
	public Integer getTextIndent() {
		return textIndent;
	}
	public void setTextIndent(Integer textIndent) {
		this.textIndent = textIndent;
	}
	public Integer getTopMargin() {
		return topMargin;
	}
	public void setTopMargin(Integer topMargin) {
		this.topMargin = topMargin;
	}
	public Integer getBottomMargin() {
		return bottomMargin;
	}
	public void setBottomMargin(Integer bottomMargin) {
		this.bottomMargin = bottomMargin;
	}
	public Integer getRightMargin() {
		return rightMargin;
	}
	public void setRightMargin(Integer rightMargin) {
		this.rightMargin = rightMargin;
	}
	public Integer getLeftMargin() {
		return leftMargin;
	}
	public void setLeftMargin(Integer leftMargin) {
		this.leftMargin = leftMargin;
	}
}
