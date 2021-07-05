package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

// Parcelable class that contains the attributes of a tweet and some methods for it
@Parcel
public class Tweet {

    // Tweet attributes
    public String body;
    public String createdAt;
    public String favorited;
    public User user;
    public String media_url_https;
    public String id_str;

    // For the parcel library, we MUST implement an empty constructor and write @Parcel on top of the class
    public Tweet() {}

    // Returns a tweet created with the
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.favorited = jsonObject.getString("favorited");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.id_str = jsonObject.getString("id_str");

        // Follow the [] and {}. [] for JSONArray, {} for JSONObject
        // https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/object-model/entities
        // Getting the media url if there is one (posted image in a tweet)
        if(!jsonObject.isNull("extended_entities")) {
            tweet.media_url_https = jsonObject
                    .getJSONObject("extended_entities")
                    .getJSONArray("media")
                    .getJSONObject(0)
                    .getString("media_url_https");
        }
        else {
            tweet.media_url_https = "";
        }

        return tweet;
    }

    // Puts all the Tweets obtained in JSON format to an ArrayList
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i=0; i<jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }

    // Returns the tweet id string
    public String getTweetId() {
        return id_str;
    }

}
