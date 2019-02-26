package com.ebook.epub.viewer.data;

public class ReadingOrderInfo {

	private String spinePath = "";
	private double spinePercentage = 0.0;
	private double spineStartPercentage = 0.0;
	
	private boolean hasMediaOverlay = false;
	
	public ReadingOrderInfo(String spinePath, double spineStartPercent, double spinePercent, boolean hasMediaOverlay) {
		this.spinePath = spinePath;
		this.spineStartPercentage = spineStartPercent;
		this.spinePercentage = spinePercent;
		this.hasMediaOverlay = hasMediaOverlay;
	}
	public String getSpinePath() {
		return spinePath;
	}
	
	public void setSpinePath(String spinePath) {
		this.spinePath = spinePath;
	}
	
	public double getSpinePercentage() {
		return spinePercentage;
	}
	
	public void setSpinePercentage(double spinePercentage) {
		this.spinePercentage = spinePercentage;
	}
	
	public double getSpineStartPercentage() {
		return spineStartPercentage;
	}
	
	public void setSpineStartPercentage(double spineStartPercentage) {
		this.spineStartPercentage = spineStartPercentage;
	}

	public boolean isHasMediaOverlay() {
		return hasMediaOverlay;
	}

	public void setHasMediaOverlay(boolean hasMediaOverlay) {
		this.hasMediaOverlay = hasMediaOverlay;
	}
}
