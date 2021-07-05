package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

// Activity that allows user compose tweets
public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    TextInputLayout tvCharCounter;
    EditText etCompose;
    Button btnTweet;

    TwitterClient client;

    // When the users clicks on the compose icon on the menu, this activity starts (is created)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);          // layout found in activity_compose.xml

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etReply);
        btnTweet = findViewById(R.id.btnReplyPublish);
        tvCharCounter = findViewById(R.id.tvCharCounter);
        tvCharCounter.setCounterEnabled(true);
        tvCharCounter.setCounterMaxLength(MAX_TWEET_LENGTH);

        // Button's onClickListener to publish the tweet
        btnTweet.setOnClickListener(new View.OnClickListener() {
            // Makes asynchronous call to the Twitter API to compose a tweet
            @Override
            public void onClick(View v) {
                String tweetContent = etCompose.getText().toString();

                // Checks that the tweet's body(content) is in bounds [1 - 280]
                if(tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tweet content is in bounds
                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_SHORT).show();
                // API Call to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    // If the call status (handler) is success, it is obtained to be in the timeline
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            // Pass the new tweet through an intent
                            Intent intent = new Intent();
                            // This tweet will be received in TimelineActivity.onActivityResult()
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // The result of the intent is ok
                            setResult(RESULT_OK, intent);
                            finish();           // Close the activity (done with publishing)
                            // Will return to TimelineActivity.onActivityResult
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Sends a message if the call status was not success
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });
    }
}