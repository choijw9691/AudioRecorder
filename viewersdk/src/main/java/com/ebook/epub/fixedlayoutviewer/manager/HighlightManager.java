package com.ebook.epub.fixedlayoutviewer.manager;

import com.ebook.epub.viewer.AnnotationHistory;
import com.ebook.epub.viewer.Highlight;

import java.util.ArrayList;

public class HighlightManager {

    public static ArrayList<Highlight> highlightData;

    private static AnnotationHistory highlightHistory = new AnnotationHistory();

    public HighlightManager() {
        highlightData = new ArrayList<>();
    }

    public static ArrayList<Highlight> getHighlightList() {
        return highlightData;
    }

    public static void setHighlightList(ArrayList<Highlight> highlight) {
        highlightData = highlight;
    }

    public AnnotationHistory getHighlightHistory() {
        return highlightHistory;
    }

    public void setHighlightHistory(AnnotationHistory __hlHistory) {
        highlightHistory = __hlHistory;
    }
}
