package com.example.asd_1.tagged;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by asd-1 on 6/14/2017.
 */

public class Post {
    String imageURL;
    String title;
    String description;
    ArrayList<String> tags = new ArrayList<>();
    int index;
    MainActivity main;
    Bitmap image;

    public Post (MainActivity main, JSONObject jsonObject, int index) {
        this.index = index;
        this.main = main;

        try {
            imageURL = jsonObject.getString("url");
            title = jsonObject.getString("name");
            description = jsonObject.getString("description");
            JSONArray jsonTags = jsonObject.getJSONArray("tags");
            for (int i = 0; i < jsonTags.length(); i++) {
                tags.add(jsonTags.getString(i));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

    }

}
