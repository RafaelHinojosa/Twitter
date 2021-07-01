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

    TwitterClient client;

    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        client = TwitterApp.getRestClient(this);

        tvUserReplied = findViewById(R.id.tvUserReplied);
        etReply = findViewById(R.id.etReply);
        btnReplyPublish = findViewById(R.id.btnReplyPublish);
        tvCharCounter = findViewById(R.id.tvCharCounter);
        tvCharCounter.setCounterEnabled(true);
        tvCharCounter.setCounterMaxLength(MAX_TWEET_LENGTH);

        // Unwrap information from the replied tweet
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweetId"));

        tvUserReplied.setText(tweet.user.screenName);


        // Click listener to reply the tweet
        btnReplyPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetId = tweet.id_str;
                String tweetContent = etReply.getText().toString();

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

                // API Call to reply the tweet
                client.replyTweet(tweetContent, tweetId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Replied Tweet: " + tweet.body);

                            // Pass the data
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();           // Close the activity (done with publishing)
                            // Will return to TimelineActivity.onActivityResult
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to reply to tweet", throwable);
                    }
                });
            }
        });

    }
}