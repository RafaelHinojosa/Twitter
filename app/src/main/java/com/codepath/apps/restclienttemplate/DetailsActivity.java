package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

// Activity class to see the details of a Tweet
public class DetailsActivity extends AppCompatActivity {

    public static final String TAG = "DetailsActivity";

    // Declare class' attributes
    ImageView ivProfileImage;
    TextView tvName;
    TextView tvScreenName;
    TextView tvReplyingTo;
    TextView tvBody;
    ImageView ivBodyImage;
    TextView tvHour;
    TextView tvDate;
    ImageView ivReply;
    ImageView ivRetweet;
    ImageView ivLike;
    ImageView ivShare;

    Tweet tweet;
    TwitterClient client;

    // Creates the view and associates variables to the view's components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Associate class' attributes to components in activity_details.xml
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvReplyingTo = findViewById(R.id.tvReplyingTo);
        tvBody = findViewById(R.id.tvBody);
        ivBodyImage = findViewById(R.id.ivBodyImage);
        tvHour = findViewById(R.id.tvHour);
        tvDate = findViewById(R.id.tvDate);
        ivReply = findViewById(R.id.ivReply);
        ivRetweet = findViewById(R.id.ivRetweet);
        ivLike = findViewById(R.id.ivLike);
        ivShare = findViewById(R.id.ivShare);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweetDetails"));
        bind(tweet);
    }

    // Sets data to the components in the view
    protected void bind(final Tweet tweet) {
        // Set text to TextViews
        tvName.setText(tweet.user.name);
        String screenName = "@" + tweet.user.screenName;
        tvScreenName.setText(screenName);
        // REPLYING TO....
        tvBody.setText(tweet.body);
        // HOUR
        // DATE

        // Set images
        // Profile Image
        Glide.with(this)
            .load(tweet.user.profileImageUrl)
            .centerCrop()
            .transform(new CircleCrop())
            .into(ivProfileImage);
        // Body Image (if exists)
        if(tweet.media_url_https != null) {
            ivBodyImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tweet.media_url_https)
                    .into(ivBodyImage);
        }
        else {
            ivBodyImage.setVisibility(View.GONE);
        }

        // Color the retweet button if the tweet is retweeted
        if(tweet.retweeted.equals("true")) {
            ivRetweet.setSelected(true);
            Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_retweet).into(ivRetweet);
        }
        else {
            ivRetweet.setSelected(false);
            Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
        }

        // Color the Favorite heart if the tweet is liked
        if(tweet.favorited.equals("true")) {
            ivLike.setSelected(true);
            Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_heart).into(ivLike);
        }
        else {
            ivLike.setSelected(false);
            Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_heart_stroke).into(ivLike);
        }

        // Set onClickListeners
        // Reply
        // Retweet
        ivRetweet.setOnClickListener(new View.OnClickListener() {
            // Retweets the tweet where the button is
            @Override
            public void onClick(View view) {
                // If not retweeted, retweet
                if(!ivRetweet.isSelected()) {
                    ivRetweet.setSelected(true);
                    // Set a "bold" image
                    Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                    TimelineActivity.changeRetweetStatus(tweet.id_str, true);
                }
                // If retweeted, unretweet
                else {
                    ivRetweet.setSelected(false);
                    // Set a lightweight image
                    Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                    TimelineActivity.changeRetweetStatus(tweet.id_str, false);
                }
            }
        });
        // Like
        // Like Button ClickListener
        ivLike.setOnClickListener(new View.OnClickListener() {
            // Likes or unlikes a tweet
            @Override
            public void onClick(View view) {
                // If tweet is not liked, like it
                if(!ivLike.isSelected()) {
                    ivLike.setSelected(true);
                    // Set a lightweight image
                    Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_heart).into(ivLike);
                    TimelineActivity.changeFavoriteStatus(tweet.id_str, true);
                }
                // If tweet is liked, unlike it
                else {
                    ivLike.setSelected(false);
                    // Set a lightweight image
                    Glide.with(DetailsActivity.this).load(R.drawable.ic_vector_heart_stroke).into(ivLike);
                    TimelineActivity.changeFavoriteStatus(tweet.id_str, false);
                }
            }
        });
        // Share
    }
}