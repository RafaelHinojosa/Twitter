package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.codepath.apps.restclienttemplate.databinding.ActivityDetailsBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityLoginBinding;
import com.codepath.apps.restclienttemplate.models.SampleModel;
import com.codepath.apps.restclienttemplate.models.SampleModelDao;
import com.codepath.oauth.OAuthLoginActionBarActivity;

// Activity to connect to Twitter and Log In (OAuth)
public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {
	String TAG = "LoginActitivity";

	SampleModelDao sampleModelDao;

	// Creates the Login Activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Binding
		ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
		View view = binding.getRoot();
		setContentView(view);

		final SampleModel sampleModel = new SampleModel();
		sampleModel.setName("CodePath");

		sampleModelDao = ((TwitterApp) getApplicationContext()).getMyDatabase().sampleModelDao();

		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				sampleModelDao.insertModel(sampleModel);
			}
		});
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
	@Override
	public void onLoginSuccess() {
		Log.d(TAG, "On login Success");
		Intent i = new Intent(this, TimelineActivity.class);
		startActivity(i);
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
		e.printStackTrace();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to login
	public void loginToRest(View view) {
		getClient().connect();
	}

}
