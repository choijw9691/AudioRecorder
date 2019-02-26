package com.ebook.epub.viewer;

public class ReadpositionData {

	private String type;
	private String version;
	private String deviceModel;
	private String deviceOsVersion;
	private String file;
	private String path;
	private double percent;
	private double totalPercent=-1;
	private long time;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getDeviceModel() {
		return deviceModel;
	}
	
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	
	public String getDeviceOsVersion() {
		return deviceOsVersion;
	}
	
	public void setDeviceOsVersion(String deviceOsVersion) {
		this.deviceOsVersion = deviceOsVersion;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public double getPercent() {
		return percent;
	}
	
	public void setPercent(double percent) {
		this.percent = percent;
	}
	
	public double getTotalPercent() {
		return totalPercent;
	}
	
	public void setTotalPercent(double totalPercent) {
		this.totalPercent = totalPercent;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	
}
