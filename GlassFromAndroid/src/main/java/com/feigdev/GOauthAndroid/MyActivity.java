package com.feigdev.GOauthAndroid;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.feigdev.goauth.AuthHelper;
import com.feigdev.goauth.AuthPreferences;
import com.feigdev.mirror.MirrorMenuBuilder;
import com.feigdev.mirror.TimelineCardHelper;
import com.feigdev.utils.Geo;
import com.feigdev.utils.Lg;
import com.feigdev.utils.StringParser;
import com.google.api.services.mirror.model.Location;

/**
 * The code here is heavily based on the code presented in the following blog:
 * http://blog.tomtasche.at/2013/05/google-oauth-on-android-using.html
 */

public class MyActivity extends Activity {
    private static final String TAG = "MyActivity";
    private static final int AUTHORIZATION_CODE = 1993;
    private static final int ACCOUNT_CODE = 1601;
    /**
     * change this depending on the scope needed for the things you do in
     * doCoolAuthenticatedStuff()
     */
    private final String SCOPE = "https://www.googleapis.com/auth/glass.timeline";
    private AuthPreferences authPreferences;
    private AuthHelper authHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        authPreferences = new AuthPreferences(this);
        authHelper = new AuthHelper(this);

        final Context c = this;

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // login
                if (authPreferences.getUser() != null
                        && authPreferences.getToken() != null) {
                    Toast.makeText(c, R.string.already_authenticated, Toast.LENGTH_SHORT);
                    return;
                }

                authHelper.chooseAccount();
            }
        });
        findViewById(R.id.notify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // notify
                Lg.d(TAG, "RunTimeline using token: " + authPreferences.getToken());
                new SendMirrorNotification().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Lg.d(TAG, "onActivityResult");

        if (resultCode == RESULT_OK) {
            Lg.d(TAG, "RESULT_OK");
            if (requestCode == AUTHORIZATION_CODE) {
                Lg.d(TAG, "AUTHORIZATION_CODE");
                authHelper.requestTokenFromActivity();
            } else if (requestCode == ACCOUNT_CODE) {
                Lg.d(TAG, "ACCOUNT_CODE");
                String accountName = data
                        .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authPreferences.setUser(accountName);
                authHelper.requestTokenFromActivity();
            }
        }
    }

    private class SendMirrorNotification extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String content = "599 Fairchild Dr\n" +
                    "Mountain View, CA 94043\n" +
                    "http://goo.gl/maps/46CzF";
            String title = "Hacker Dojo";

            MirrorMenuBuilder menuPayload = new MirrorMenuBuilder();
            String url = StringParser.getUrl(content);
            menuPayload.addNavigateAction()
                    .addOpenUriAction(url)
                    .addReadAloudAction(content)
                    .addDeleteAction();

            Location location = Geo.geoCode(getApplicationContext(), content);

            TimelineCardHelper.insertTimeline(authPreferences, "19283",
                    title, content, location, menuPayload, new Runnable() {
                @Override
                public void run() {
                    Lg.w(TAG, "failed because token expired, try again");
                    authHelper.requestTokenFromActivity();
                }
            });
            return null;
        }
    }
}
