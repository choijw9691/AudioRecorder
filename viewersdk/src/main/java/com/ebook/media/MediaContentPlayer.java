package com.ebook.media;

public abstract class MediaContentPlayer {
	public abstract void play(String id);
	public abstract void play(String id, double startTime);
	public abstract void pause(String id);
	public abstract void stop(String id);
	public abstract void loop(String id, boolean loop);
}
