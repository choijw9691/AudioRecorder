package com.ebook.epub.viewer;

public class PopupData {

    public String highlightId = "";
    public int x;
    public int y;
    public BookHelper.ContextMenuType menuType;
    public int contentsPosition;

    public PopupData(String highlightId, int x, int y, BookHelper.ContextMenuType menuType) {
        this.highlightId = highlightId;
        this.x = x;
        this.y = y;
        this.menuType = menuType;
    }

    public PopupData(String highlightId, int x, int y, BookHelper.ContextMenuType menuType, int contentsPosition) {
        this.highlightId = highlightId;
        this.x = x;
        this.y = y;
        this.menuType = menuType;
        this.contentsPosition = contentsPosition;
    }
}