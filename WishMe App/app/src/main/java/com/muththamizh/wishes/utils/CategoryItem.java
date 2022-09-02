package com.muththamizh.wishes.utils;

public class CategoryItem {
    private String Title;
    private String Image;
    private int viewType=1;

    public CategoryItem() {
    }

    public CategoryItem(String title, String image) {
        Title = title;
        Image = image;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public CategoryItem setViewType(int viewType) {
        this.viewType = viewType;
        return this;
    }

    public int getViewType() {
        return viewType;
    }
}
