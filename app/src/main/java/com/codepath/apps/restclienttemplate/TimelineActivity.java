package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityLoginBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;


// Activity for the Twitter timeline
public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    // Can be any number but must be UNIQUE
    public final int POST_REQUEST_CODE = 20;
    public static final int REPLY_REQUEST_CODE = 25;
    public static final int LIKE_REQUEST_CODE = 30;
    public static final int RETWEET_REQUEST_CODE = 40;

    List<Tweet> tweets;
    TweetsAdapter adapter;
    public static TwitterClient client;

    RecyclerView rvTweets;
    Button btnLogout;
    private SwipeRefreshLayout swipeContainer;

    // Creates a timeline
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding
        ActivityTimelineBinding binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);;

        // Is set as a RestClient
        client = TwitterApp.getRestClient(this);

        // Associates the variables with the components in activity_timeline.xml
        rvTweets = binding.rvTweets;
        btnLogout = binding.btnLogout;
        swipeContainer = binding.swipeContainer;

        // Initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        // Sets an Refresh Listener to the Swipe Container
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // Logout button setup
        btnLogout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.clearAccessToken();
                finish();
            }
        });

        populateHomeTimeLine();
    }

    // Creates the options menu where user can compose
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;    // so that the menu can be displayed
    }

    // Checks if a menu item was clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Compose (actionbar button) item has been selected
        if(item.getItemId() == R.id.compose) {
            // Intent to go from "this" to ComposeActivity to compose a tweet
            Intent intent = new Intent(this, ComposeActivity.class);
            // We will receive a result from the asynchronous call
            startActivityForResult(intent, POST_REQUEST_CODE);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method that makes data changes according to the http request done and the result of the call
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // If user composed a tweet
        if(requestCode == POST_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from intent (tweet)
            // This tweet was sent from ComposeActivity
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Modify data source of tweets (list of tweets) at the beginning of the list
            tweets.add(0, tweet);
            // Notify the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);     // So it appears at the top
        }
        // If user replied a tweet
        else if(requestCode == REPLY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from intent (tweet)
            // This tweet was sent from ReplyActivity
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Put the new tweet at the top of the data source of the tweets (list of tweets)
            tweets.add(0, tweet);
            // Notify the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);     // So it appears at the top
        }
        else {
            // If other request made, create an onActivityResult instance with the parameters received
            // We can add more ActivityResults according to the different http request that can be done
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Populates the home Timeline of the TimelineActivity
    private void populateHomeTimeLine() {
        // Gets a list of tweets and add them to the list of tweets if succeeded
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                }
                catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure" + response, throwable);

            }
        });
    }

    // Updates Home Timeline when refreshed
    public void fetchTimelineAsync(int page) {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;

                // Clear everything to replace it with the new tweets got from the http request
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Stop the refreshing circle and activity
                swipeContainer.setRefreshing(false);
            }

            // If the call status was not success
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Failed to update: " + throwable.toString());
            }
        });
    }

    // User retweets/unretweets a specific tweet
    public static void changeRetweetStatus(String id_str, boolean retweet) {
        // Not selected, then select and retweet
        client.retweet(id_str, retweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "Retweet status changed correctly");
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Could not change Retweet status");
            }
        });
    }

    // User likes/unlikes a tweet
    public static void changeFavoriteStatus(String id_str, boolean setFavorite) {
        client.likeTweet(id_str, setFavorite, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "Tweet Favorite Status Changed");
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Could not change the favorite status of the tweet");
            }
        });
    }
}