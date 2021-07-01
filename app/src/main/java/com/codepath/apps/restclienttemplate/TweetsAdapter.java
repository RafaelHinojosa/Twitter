package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private final int REQUEST_CODE = 25;
    String TAG = "TweetsAdapter";
    Context context;
    List<Tweet> tweets;

    // Pass in the context and the list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context  = context;
        this.tweets = tweets;
    }

    // For each row, inflate the Layout for a tweet
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvName;
        TextView tvBody;
        TextView tvCreatedAt;
        ImageView ivMediaTimeLine;
        Button btnReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivMediaTimeLine = itemView.findViewById(R.id.ivMediaTimeLine);
            btnReply = itemView.findViewById(R.id.btnReply);
        }
        // usually put the onclick on the bind method
        public void bind(final Tweet tweet) {
            tvScreenName.setText(tweet.user.screenName);
            tvName.setText(tweet.user.name);
            tvBody.setText(tweet.body);
            tvCreatedAt.setText(getTimeAgo(tweet.createdAt));
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .into(ivProfileImage);
            // Set image published if there is one
            Log.d(TAG, tweet.media_url_https);
            if(tweet.media_url_https != null) {
                ivMediaTimeLine.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.media_url_https)
                        .into(ivMediaTimeLine);
            }
            else {
                // Does not take space
                ivMediaTimeLine.setVisibility(View.GONE);
            }

            // Reply button
            btnReply.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ReplyActivity.class);
                    // putExtra(nameId con el que lo voy a recibir, lo que le voy a mandar(el tweet))
                    // I put the tweetId as the name because it will be a unique identifier for the tweets.
                    intent.putExtra("tweetId", Parcels.wrap(tweet));

                    // startActivityForResult is for activities so we make the context an activity
                    ((TimelineActivity) context).startActivityForResult(intent, TimelineActivity.REPLY_REQUEST_CODE);
                }
            });
        }
    }

    public String getTimeAgo(String createdAt) {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 60 * HOUR_MILLIS;

        // Format Example "Wed Oct 10 20:19:24 +0000 2018"
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sdf.setLenient(true);

        try {
            long time = sdf.parse(createdAt).getTime();
            long now = System.currentTimeMillis();

            Log.d(TAG, "time = " + time);
            Log.d(TAG, "now = " + now);

            final long diff = now - time;

            if(diff < MINUTE_MILLIS) {
                return "just now";
            }
            else if(diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            }
            else if(diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            }
            else if(diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            }
            else if(diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            }
            else if(diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            }
            else {
                return diff / DAY_MILLIS + "d";
            }

        } catch (ParseException e) {
            Log.i(TAG, "getTimeAgo failed");
            e.printStackTrace();
        }


        return "";
    }

    // Clear all the elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list to the recycler
    public void addAll(List<Tweet> newList) {
        tweets.addAll(newList);
        notifyDataSetChanged();
    }
}
