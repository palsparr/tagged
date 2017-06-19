package com.example.asd_1.tagged;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by asd-1 on 6/15/2017.
 */

public class JsonTask {

    MainActivity main;
    String url;
    ArrayList<String> allTags = new ArrayList<>();

    public JsonTask(MainActivity main, String url){
        this.main = main;
        this.url = url;
    }

    public void execute() {
        new Async().execute(url);
    }

    private class Async extends AsyncTask<String, Void, ArrayList<Post>> {
        @Override
        protected ArrayList<Post> doInBackground (String...params){
            ArrayList<Post> postArray = new ArrayList<>();
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();



                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder stringBuilder = new StringBuilder();
                String responseString;

                while ((responseString = reader.readLine()) != null) {
                    stringBuilder.append(responseString);
                }

                try {
                    JSONObject response = new JSONObject(stringBuilder.toString());
                    JSONArray responseArray = response.getJSONArray("files");

                    for (int i = 0; i < responseArray.length(); i++) {
                        JSONObject jsonObject = responseArray.getJSONObject(i);


                        Post post = new Post(main, jsonObject, i);
                        postArray.add(post);

                        for (String tag : post.tags) {
                            if (!allTags.contains(tag)) {
                                allTags.add(tag);
                            }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            return postArray;
    }



    protected void onPostExecute(ArrayList<Post> result) {
        super.onPostExecute(result);
        main.postArray = result;
        main.updateFeedView();
        main.allTagsList = allTags;
        main.fillFilterView();
        if (main.swipeRefreshLayout.isRefreshing()) {
            main.swipeRefreshLayout.setRefreshing(false);
        }
    }
}
}
