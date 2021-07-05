package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
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
import com.codepath.apps.restclienttemplate.databinding.ActivityLoginBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

// TweetsAdapter contains a list of tweets as in item_tweet.xml and manages each tweet data
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    String TAG = "TweetsAdapter";
    Context context;
    List<Tweet> tweets;

    // Pass in the context and the list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context  = context;
        this.tweets = tweets;
    }

    // For each row, inflate the Layout for a tweet (1 at a time)
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

    // Gets number of tweets in Tweets list
    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a clickable ViewHolder containing a specific tweet
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvName;
        TextView tvBody;
        TextView tvCreatedAt;
        ImageView ivMediaTimeLine;
        ImageView ivReply;
        ImageView ivRetweet;
        ImageView ivLike;

        // Associate variables with existing ids in item_tweet.xml
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivMediaTimeLine = itemView.findViewById(R.id.ivMediaTimeLine);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
            itemView.setOnClickListener(this);
        }

        // Bind method changes the data of the components
        public void bind(final Tweet tweet) {
            String userName = "@" + tweet.user.screenName;
            tvScreenName.setText(userName);
            tvName.setText(tweet.user.name);
            tvBody.setText(tweet.body);
            tvCreatedAt.setText(getTimeAgo(tweet.createdAt));
            // Profile picture
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .transform(new RoundedCornersTransformation(200, 10))
                    .into(ivProfileImage);
            // Set image published if there is one
            Log.d(TAG, tweet.media_url_https);
            if(tweet.media_url_https != null) {
                ivMediaTimeLine.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.media_url_https)
                        //.transform(new RoundedCornersTransformation(40, 0))
                        .into(ivMediaTimeLine);
            }
            else {
                // Does not take space
                ivMediaTimeLine.setVisibility(View.GONE);
            }

            // Reply button
            // Usually set the onClickListener on the bind method
            ivReply.setOnClickListener(new Button.OnClickListener() {
                // Activities when reply button is clicked
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ReplyActivity.class);
                    // putExtra(name to identify this intent, what I am putting into the intent)
                    intent.putExtra("tweetId", Parcels.wrap(tweet));
                    // StartActivityForResult is for activities so we make the context an activity
                    // We'll expect a result in TimelineActivity.onActivityResult
                    ((TimelineActivity) context).startActivityForResult(intent, TimelineActivity.REPLY_REQUEST_CODE);
                }
            });
            // Color retweet button if tweet is retweeted
            if(tweet.retweeted.equals("true")) {
                ivRetweet.setSelected(true);
                Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweet);
            }
            else {
                ivRetweet.setSelected(false);
                Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
            }
            // Color the Favorite heart if the tweet is liked
            if(tweet.favorited.equals("true")) {
                ivLike.setSelected(true);
                Glide.with(context).load(R.drawable.ic_vector_heart).into(ivLike);
            }
            else {
                ivLike.setSelected(false);
                Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivLike);
            }

            // Retweet Button ClickListener
            ivRetweet.setOnClickListener(new View.OnClickListener() {
                // Retweets the tweet where the button is
                @Override
                public void onClick(View view) {
                    // If not retweeted, retweet
                    if(!ivRetweet.isSelected()) {
                        ivRetweet.setSelected(true);
                        // Set a "bold" image
                        Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                        TimelineActivity.changeRetweetStatus(tweet.id_str, true);
                    }
                    // If retweeted, unretweet
                    else {
                        ivRetweet.setSelected(false);
                        // Set a lightweight image
                        Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                        TimelineActivity.changeRetweetStatus(tweet.id_str, false);
                    }
                }
            });
            // Like Button ClickListener
            ivLike.setOnClickListener(new View.OnClickListener() {
                // Likes or unlikes a tweet
                @Override
                public void onClick(View view) {
                    // If tweet is not liked, like it
                    if(!ivLike.isSelected()) {
                        ivLike.setSelected(true);
                        // Set a lightweight image
                        Glide.with(context).load(R.drawable.ic_vector_heart).into(ivLike);
                        TimelineActivity.changeFavoriteStatus(tweet.id_str, true);
                    }
                    // If tweet is liked, unlike it
                    else {
                        ivLike.setSelected(false);
                        // Set a lightweight image
                        Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivLike);
                        TimelineActivity.changeFavoriteStatus(tweet.id_str, false);
                    }
                }
            });
        }

        // OnClick on the ViewHolder (Tweet) to see its details
        @Override
        public void onClick(View view) {
            // Get the clicked tweet
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                // Make intent to pass the tweet to Details Activity
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("tweetDetails", Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }

    // Returns how many time has passed since the post of a twitter, based on the actual time and the Tweet's creation
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

    // Clear all the elements of the recycler view
    public void clear() {
        tweets.clear();
        // When changing data, notify the adapter
        notifyDataSetChanged();
    }

    // Add a list of tweets to the recycler view
    public void addAll(List<Tweet> newList) {
        tweets.addAll(newList);
        notifyDataSetChanged();
    }
}
