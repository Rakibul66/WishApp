package com.muththamizh.wishes.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constant {
    //Enter You Privacy Policy Link Here
    public static final String PRIVACY_POLICY_LINK = "https://www.muththamizh.in/p/privacy-policy-for-our-android-apps.html";

    // Enter Share Text Here Whenever someone share your Application this text is include with share link
    public static final String SHARE_TEXT = "Get All Type Festival Wishes. Download Muththamizh Wishes App. ";




    // Do Not Change These Fileds...
    public static final String DATABASE_NAME = "BestWishes";
    public static final String CHILD_DATABASE = "Category";
    public static final String CALLED_NAME = "Name";
    public static final String NAME = "name";
    public static final String LOGO = "logo";
    public static final String IMAGE = "image";
    public static final String TEXT_WISHES = "TextWishes";
    public static final String TEXT = "TEXT";
    public static final DatabaseReference dataBaseReference = FirebaseDatabase.getInstance("https://wishes-app-61823-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(DATABASE_NAME);
}
