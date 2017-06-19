package com.example.asd_1.tagged;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView feedView;
    ArrayList<Post> postArray;
    ArrayList<Post> filteredPostArray;
    FeedListAdapter feedViewAdapter;
    View inspectPostView;
    ViewGroup mainLayout;
    FrameLayout contentView;

    ImageView inspectPostImage;
    ImageView fullScreenPostImage;

    LinearLayout filterView;

    ArrayList<String> allTagsList;

    SwipeRefreshLayout swipeRefreshLayout;

    int displayHeight;
    int displayWidth;

    RelativeLayout disconnectedLayout;
    String filter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inspectPostView = null;
        feedView = (ListView) findViewById(R.id.feedListView);

        mainLayout = (ViewGroup) findViewById(R.id.mainLayout);

        disconnectedLayout = (RelativeLayout) findViewById(R.id.disconnectedLayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        filterView = (LinearLayout) findViewById(R.id.tagsFilterContainer);

        contentView = (FrameLayout)this.getWindow().getDecorView().getRootView();


        postArray = new ArrayList<>();
        filteredPostArray = new ArrayList<>();

        feedViewAdapter = new FeedListAdapter(this, filteredPostArray, displayWidth);
        feedView.setAdapter(feedViewAdapter);

        //Set up a click listener for items in the Feed
        feedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inspectPostView = getLayoutInflater().inflate(R.layout.inspect_post, null);
                inspectPostView.setMinimumHeight(displayHeight);

                final Post post = filteredPostArray.get(position);

                inspectPostImage = (ImageView) inspectPostView.findViewById(R.id.inspectPostImage);

                Glide.with(getApplicationContext())
                        .load(post.imageURL)
                        .into(inspectPostImage)
                        .onLoadStarted(getResources().getDrawable(R.drawable.taggedplaceholder));


                inspectPostImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fullScreenPostImage = new ImageView(getApplicationContext());

                        Glide.with(getApplicationContext())
                                .load(post.imageURL)
                                .into(fullScreenPostImage)
                                .onLoadStarted(getResources().getDrawable(R.drawable.taggedplaceholder));

                        fullScreenPostImage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                        fullScreenPostImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        fullScreenPostImage.setBackgroundColor(Color.parseColor("#000000"));
                        contentView.addView(fullScreenPostImage);
                    }

                });


                TextView inspectPostTitle = (TextView) inspectPostView.findViewById(R.id.inspectPostTitle);
                inspectPostTitle.setText(post.title);

                TextView inspectPostDescription = (TextView) inspectPostView.findViewById(R.id.inspectPostDescription);
                inspectPostDescription.setText(post.description);

                TextView inspectPostTags = (TextView) inspectPostView.findViewById(R.id.inspectPostTags);
                String tagsText = new String();
                ArrayList<String> tags = post.tags;
                for (String tag : tags) {
                    tagsText += '#';
                    tagsText += tag;
                    tagsText += ' ';
                }
                inspectPostTags.setText(tagsText);

                mainLayout.addView(inspectPostView);
            }
        });


        allTagsList = new ArrayList<>();

        //Perform AsyncTask to download JSON data

        final JsonTask jsonTask = new JsonTask(this, "https://static.mobileinteraction.se/developertest/wordpressphotoawards.json");
        if (checkConnection()) {
            jsonTask.execute();
        } else {
            disconnectedLayout.setVisibility(View.VISIBLE);
        }


        //Handle refreshing of Feed

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                if (checkConnection()) {
                    jsonTask.execute();
                    if (disconnectedLayout.getVisibility() == View.VISIBLE) {
                        disconnectedLayout.setVisibility(View.INVISIBLE);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), "Unable to refresh", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {

        //Handle backbutton press
        if (fullScreenPostImage != null) {
            contentView.removeView(fullScreenPostImage);
            fullScreenPostImage = null;
        } else if (inspectPostView != null) {
            mainLayout.removeView(inspectPostView);
            inspectPostView = null;
        } else if (filter != null) {
            clearFilter();
        } else {
            super.onBackPressed();
        }

    }

    public void updateFeedView() {

        //Update items in Feed
        filteredPostArray.clear();
        filteredPostArray.addAll(postArray);
        filterFeed();
        feedViewAdapter.updateResults(filteredPostArray);

    }

    public void fillFilterView() {

        //Add tags to the filterView
        if (filter == null) {
            for (String tag : allTagsList) {
                final String filterTag = tag;
                View filterTagLayout = getLayoutInflater().inflate(R.layout.filter_tag_layout, null);
                TextView tagTextView = (TextView) filterTagLayout.findViewById(R.id.filterTagText);
                tagTextView.setText("#" + tag);

                filterTagLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!swipeRefreshLayout.isRefreshing()) {
                            if (filter == null) {
                                filter = filterTag;
                                updateFeedView();
                                filterView.removeAllViews();
                                View filterTagLayout = getLayoutInflater().inflate(R.layout.filter_tag_highlighted_layout, null);
                                TextView tagTextView = (TextView) filterTagLayout.findViewById(R.id.filterTagHighlightedText);
                                tagTextView.setText("#" + filter);
                                filterTagLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!swipeRefreshLayout.isRefreshing())
                                            clearFilter();
                                    }
                                });
                                filterView.addView(filterTagLayout);

                            }
                        }
                    }
                });
                filterView.addView(filterTagLayout);

            }
        }

    }

    public void clearFilter() {

        //Remove non-selected filters from filterView
        filterView.removeAllViews();
        filter = null;
        fillFilterView();
        updateFeedView();
    }
    public void filterFeed() {

        //Filter items in feed, only show items of selected tag
        if (filter != null) {
            ArrayList<Post> removePosts = new ArrayList<>();
            for (Post post : filteredPostArray) {
                if (!post.tags.contains(filter)) {
                    removePosts.add(post);
                }
            }
            filteredPostArray.removeAll(removePosts);
        }
    }

    public boolean checkConnection() {

        //Check conecction to network
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }
}
