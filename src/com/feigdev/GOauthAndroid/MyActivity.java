package com.feigdev.GOauthAndroid;

import android.accounts.*;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.TimelineItem;

import java.io.IOException;

/**
 * The code here is heavily based on the code presented in the following blog:
 * http://blog.tomtasche.at/2013/05/google-oauth-on-android-using.html
 */

public class MyActivity extends Activity {
    private static final String MAIN = "Main";
    private static final int AUTHORIZATION_CODE = 1993;
    private static final int ACCOUNT_CODE = 1601;
    /**
     * change this depending on the scope needed for the things you do in
     * doCoolAuthenticatedStuff()
     */
    private final String SCOPE = "https://www.googleapis.com/auth/glass.timeline";
    private AuthPreferences authPreferences;
    private AccountManager accountManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(MAIN, "onCreate");
        super.onCreate(savedInstanceState);

        accountManager = AccountManager.get(this);

        authPreferences = new AuthPreferences(this);
        if (authPreferences.getUser() != null
                && authPreferences.getToken() != null) {
            doCoolAuthenticatedStuff();
        } else {
            chooseAccount();
        }
    }

    private void doCoolAuthenticatedStuff() {
        new RunTimeLine().execute();
    }

    private class RunTimeLine extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(MAIN, "doCoolAuthenticatedStuff with token: " + authPreferences.getToken());
            Mirror service = new Mirror.Builder(new NetHttpTransport(), new AndroidJsonFactory(), null)
                    .setApplicationName("GOauthAndroid").build();

            TimelineItem timelineItem = new TimelineItem();
            timelineItem.setText("Hello from android");
            try {
                service.timeline().insert(timelineItem).setOauthToken(authPreferences.getToken()).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void chooseAccount() {
        Log.d(MAIN, "chooseAccount");
        // use https://github.com/frakbot/Android-AccountChooser for
        // compatibility with older devices
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, ACCOUNT_CODE);
    }

    private void requestToken() {
        Log.d(MAIN, "requestToken");
        Account userAccount = null;
        String user = authPreferences.getUser();
        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                Log.d(MAIN, "found user " + account.name + " " + account.type + " " + account.toString());

                break;
            }
        }

        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this,
                new OnTokenAcquired(), null);
    }

    /**
     * call this method if your token expired, or you want to request a new
     * token for whatever reason. call requestToken() again afterwards in order
     * to get a new token.
     */
    private void invalidateToken() {
        Log.d(MAIN, "invalidateToken");
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google",
                authPreferences.getToken());

        authPreferences.setToken(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(MAIN, "onActivityResult");

        if (resultCode == RESULT_OK) {
            Log.d(MAIN, "RESULT_OK");
            if (requestCode == AUTHORIZATION_CODE) {
                Log.d(MAIN, "AUTHORIZATION_CODE");
                requestToken();
            } else if (requestCode == ACCOUNT_CODE) {
                Log.d(MAIN, "ACCOUNT_CODE");
                String accountName = data
                        .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authPreferences.setUser(accountName);

                // invalidate old tokens which might be cached. we want a fresh
                // one, which is guaranteed to work
                invalidateToken();

                requestToken();
            }
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Log.d(MAIN, "OnTokenAcquired");
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    authPreferences.setToken(token);

                    doCoolAuthenticatedStuff();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
