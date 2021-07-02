package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {

    public static final String TAG = "ReplyActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    TextInputLayout tvCharCounter;
    TextView tvUserReplied;
    EditText etReply;
    Button btnReplyPublish;

    TwitterClient client;       // Used to get to Client.replyTweet()

    Tweet tweet;

    // When the users clicks on the reply button, this activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);        // layout of the act. is in activity_reply.xml

        client = TwitterApp.getRestClient(this);

        tvUserReplied = findViewById(R.id.tvUserReplied);
        etReply = findViewById(R.id.etReply);
        btnReplyPublish = findViewById(R.id.btnReplyPublish);
        tvCharCounter = findViewById(R.id.tvCharCounter);
        tvCharCounter.setCounterEnabled(true);
        tvCharCounter.setCounterMaxLength(MAX_TWEET_LENGTH);

        // Unwrap information from the replied tweet (came from TweetsAdapter)
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweetId"));

        // Puts the username of the user's tweet you are replying of
        tvUserReplied.setText(tweet.user.screenName);

        // Click listener to make a http request and reply to the tweet
        btnReplyPublish.setOnClickListener(new View.OnClickListener() {
            // When reply publish button is clicked
            @Override
            public void onClick(View v) {
                String tweetId = tweet.id_str;
                String tweetContent = etReply.getText().toString();

                // Check if the tweet's number of characters are in bounds [1 - 280] characters
                if(tweetContent.isEmpty()) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tweet content is in bounds
                Toast.makeText(ReplyActivity.this, tweetContent, Toast.LENGTH_SHORT).show();

                // API Call to reply the tweet. Calls to Client.replyTweet()
                client.replyTweet(tweetContent, tweetId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Replied Tweet: " + tweet.body);

                            // Pass the data of the new tweet through the intent and sets the result as ok
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();           // Close the activity (done with publishing)
                            // Will return to TimelineActivity.onActivityResult
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // This executes if the status of the call is not success
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to reply to tweet", throwable);
                    }
                });
            }
        });

    }
}