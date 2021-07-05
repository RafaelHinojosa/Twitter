package com.codepath.apps.restclienttemplate;

import android.app.AuthenticationRequiredException;
import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;

import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.FlickrApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;

/*
 *
 * This is the object responsible for communicating with a REST API.
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes:
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 *
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 *
 */
public class TwitterClient extends OAuthBaseClient {
    public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
    public static final String REST_URL = "https://api.twitter.com/1.1"; // Base API URL
    public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;       // Secret key
    public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET; // Secret key

    // Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
    public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

    // See https://developer.chrome.com/multidevice/android/intents
    public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

    // Construct a Twitter client
    public TwitterClient(Context context) {
        super(context, REST_API_INSTANCE,
                REST_URL,
                REST_CONSUMER_KEY,
                REST_CONSUMER_SECRET,
                null,  // OAuth2 scope, null for OAuth1
                String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
                        context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
    }

    // Client gets 25 tweets (as in count) through Twitter's API
    public void getHomeTimeline(JsonHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("since_id", 1);
        client.get(apiUrl, params, handler);
    }

    // Client post a Tweet with the content as "tweetContent"
    public void publishTweet(String tweetContent, JsonHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("status", tweetContent);
        client.post(apiUrl, params, "", handler);
    }

    // User replies to a tweet
    public void replyTweet(String replyContent, String tweetId, JsonHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", replyContent);                 // Content of the new tweet
        params.put("in_reply_to_status_id", tweetId);       // Replying TweetId
        params.put("auto_populate_reply_metadata", true);   // Flag confirming that this is a reply
        client.post(apiUrl, params, "", handler);
        // Handler indicates success or failure in the post request
    }

    // User likes/dislikes a tweet
    public void likeTweet(String id_str, boolean setFavorite, JsonHttpResponseHandler handler) {
        String apiUrl;
        RequestParams params = new RequestParams();
        params.put("id", id_str);

        // User likes a tweet
        if(setFavorite) {
            apiUrl = getApiUrl("favorites/create.json");
        }
        // User no longer likes a tweet
        else {
            apiUrl = getApiUrl("favorites/destroy.json");
        }
        client.post(apiUrl, params, "", handler);
    }

    // User retweets/unretweets a tweet
    public void retweet(String id_str, boolean retweet, JsonHttpResponseHandler handler) {
        String apiUrl;
        // User retweets
        if(retweet) {
            apiUrl = getApiUrl("statuses/retweet/" + id_str + ".json");
        }
        // User UNretweets
        else {
            apiUrl = getApiUrl("statuses/unretweet/" + id_str + ".json");
        }
        client.post(apiUrl, handler);
    }

    // Algo for get/post requests
    /* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
     * 	  i.e getApiUrl("statuses/home_timeline.json");
     * 2. Define the parameters to pass to the request (query or body)
     *    i.e RequestParams params = new RequestParams("foo", "bar");
     * 3. Define the request method and make a call to the client
     *    i.e client.get(apiUrl, params, handler);
     *    i.e client.post(apiUrl, params, handler);
     */
}
