package com.example.asd_1.tagged;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;

/**
 * Created by asd-1 on 6/14/2017.
 */

public class FeedListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Post> postArray;
    private static LayoutInflater inflater = null;
    int displayWidth;

    public FeedListAdapter(Context context, ArrayList<Post> postArray, int displayWidth) {
        super();
        this.context = context;
        this.postArray = postArray;
        this.displayWidth = displayWidth;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateResults(ArrayList<Post> postArray) {
       this.postArray = postArray;
        //Triggers the list update
        notifyDataSetChanged();
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return postArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.main_row_layout, null);


        ImageView itemImage = (ImageView) view.findViewById(R.id.itemImage);


        itemImage.setImageResource(R.drawable.taggedplaceholder);

        Glide.with(context)
                .load(postArray.get(position).imageURL)
                .transition(new DrawableTransitionOptions().dontTransition())
                .into(itemImage);

        TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
        itemTitle.setText(postArray.get(position).title);

        TextView itemTags = (TextView) view.findViewById(R.id.itemTags);
        String tagsText = new String();
        ArrayList<String> tags = postArray.get(position).tags;
        for (String tag : tags) {
            tagsText += '#';
            tagsText += tag;
            tagsText += ' ';
        }
        itemTags.setText(tagsText);


        return view;
    }

    public void loadImage(int position, ImageView imageView) {

    }

}
