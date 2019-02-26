package com.ebook.mediaoverlay;

public interface OnMediaOverlayListener {
    void addMediaOverlayHighlighter(String currentFilePath, String id);
    void removeMediaOverlayHighlighter();
}
