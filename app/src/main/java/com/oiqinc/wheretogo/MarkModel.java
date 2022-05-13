package com.oiqinc.wheretogo;

import com.yandex.mapkit.geometry.Point;

public class MarkModel {

    private String text;

    private Point point;
    private String markname;
    private String markowner;
    private String bitmaps;
    private String id;

    public MarkModel(String text, Point point, String markname, String markowner, String bitmaps, String id) {
        this.text = text;
        this.point = point;
        this.markname = markname;
        this.markowner = markowner;
        this.bitmaps = bitmaps;
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public String getMarkname() {
        return markname;
    }

    public void setMarkname(String markname) {
        this.markname = markname;
    }

    public String getMarkowner() {
        return markowner;
    }

    public void setMarkowner(String markowner) {
        this.markowner = markowner;
    }

    public String getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(String bitmaps) {
        this.bitmaps = bitmaps;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
