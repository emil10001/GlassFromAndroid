package com.feigdev.goauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.feigdev.utils.Lg;

/**
 * Created by ejf3 on 2/8/14.
 */
public class AuthHelper {
    private static final String TAG = "AuthHelper";
    public static final int AUTHORIZATION_CODE = 1993;
    public static final int ACCOUNT_CODE = 1601;
    public static final String SCOPE = "https://www.googleapis.com/auth/glass.timeline https://www.googleapis.com/auth/plus.me";
    private static long lastTokenRequest = 0;

    private AuthPreferences authPreferences;
    private AccountManager accountManager;
    private Activity activity;
    private Handler handler;

    public AuthHelper(Context context, Handler handler){
        accountManager = AccountManager.get(context);
        authPreferences = new AuthPreferences(context);
        this.handler = handler;
    }

    public AuthHelper(Activity activity){
        accountManager = AccountManager.get(activity);
        authPreferences = new AuthPreferences(activity);
        this.activity = activity;
    }

    public void chooseAccount() {
        Lg.d(TAG, "chooseAccount");
        if (null == activity){
            Lg.e(TAG, "activity NPE");
            return;
        }

        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        activity.startActivityForResult(intent, ACCOUNT_CODE);
    }

    /**
     * call this method if your token expired, or you want to request a new
     * token for whatever reason. call requestToken() again afterwards in order
     * to get a new token.
     */
    private void invalidateToken() {
        Lg.d(TAG, "invalidateToken");
        accountManager.invalidateAuthToken("com.google",
                authPreferences.getToken());

        authPreferences.setToken(null);
    }

    private Account prepareRequestToken(){
        Lg.d(TAG, "prepareRequestToken");
        if (null != authPreferences.getToken()){
            invalidateToken();
        }

        Account userAccount = null;
        String user = authPreferences.getUser();
        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                Lg.d(TAG, "found user " + account.name + " " + account.type + " " + account.toString());
                break;
            }
        }
        return userAccount;
    }

    public void requestTokenFromActivity() {
        Lg.d(TAG, "requestTokenFromActivity");
        Account userAccount = prepareRequestToken();
        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, activity,
                new OnTokenAcquired(), null);
    }

    public void requestTokenFromBackgroundService(){
        Lg.d(TAG, "requestTokenFromBackgroundService");
        if (null == handler){
            Lg.e(TAG, "requestTokenFromBackgroundService null handler");
            return;
        }

        if ((System.currentTimeMillis() - lastTokenRequest) < 10 * 1000)
            return;
        lastTokenRequest = System.currentTimeMillis();

        final Account userAccount = prepareRequestToken();
        handler.post(new Runnable() {
            @Override
            public void run() {
                accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE,
                        null, true, new OnTokenAcquired(), null);
            }
        });

    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        // to get a token on the server, follow the instructions from here:
        // https://developers.google.com/+/mobile/android/sign-in

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Lg.d(TAG, "OnTokenAcquired");
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);

                if (launch != null && null != activity) {
                    activity.startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    authPreferences.setToken(token);

                    for (String key : bundle.keySet()) {
                        Lg.d(TAG, "bundle." + key + ": " + bundle.get(key).toString());
                    }

                    Lg.d(TAG, "authSuccess for token: " + authPreferences.getToken());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
