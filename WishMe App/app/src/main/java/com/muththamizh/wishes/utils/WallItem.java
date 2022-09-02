package com.muththamizh.wishes.utils;

public class WallItem {
    private String Image;
    private int viewType = 1;

    public WallItem() {
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public WallItem setViewType(int viewType) {
        this.viewType = viewType;
        return this;
    }

    public int getViewType() {
        return viewType;
    }
}
