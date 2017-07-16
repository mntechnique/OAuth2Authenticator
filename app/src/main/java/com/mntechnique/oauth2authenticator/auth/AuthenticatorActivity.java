package com.mntechnique.oauth2authenticator.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.mntechnique.oauth2authenticator.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import static com.mntechnique.oauth2authenticator.auth.AccountGeneral.sServerAuthenticate;

/**
 * The Authenticator activity.
 *
 * Called by the Authenticator and in charge of identifing the user.
 *
 * It sends back to the Authenticator the result.
 */

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_PASS = "USER_PASS";

    public String authCode;

    public String urlEncodedRedirectURI = "oauth%3A%2F%2Foauth2authenticator";

    private final int REQ_SIGNUP = 1;

    private final String TAG = this.getClass().getSimpleName();

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        mAccountManager = AccountManager.get(getBaseContext());

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

        // Init OAuth2 flow
        initOAuth2();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void initOAuth2() {
        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
        final WebView webView = (WebView) findViewById(R.id.webv);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        try {
            urlEncodedRedirectURI = URLEncoder.encode(getResources().getString(R.string.redirectURI), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().sync();
        final AccountGeneral accountGeneral = new AccountGeneral(
                getResources().getString(R.string.oauth2Scope),
                getResources().getString(R.string.clientId),
                getResources().getString(R.string.clientSecret),
                getResources().getString(R.string.serverURL),
                getResources().getString(R.string.redirectURI),
                getResources().getString(R.string.authEndpoint),
                getResources().getString(R.string.tokenEndpoint)
        );
        Log.d("authURL", accountGeneral.oauth20Service.getAuthorizationUrl());
        if (isConnected(getApplicationContext())) {
            webView.loadUrl(accountGeneral.oauth20Service.getAuthorizationUrl());
            webView.setWebViewClient(new WebViewClient() {

                boolean authComplete = false;
                Intent resultIntent = new Intent();

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon){
                    super.onPageStarted(view, url, favicon);

                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    if (url.contains("?code=") && authComplete != true) {
                        webView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        Uri uri = Uri.parse(url);
                        authCode = uri.getQueryParameter("code");
                        Log.i("", "CODE : " + authCode);
                        new AsyncTask<String, Void, Intent>() {

                            @Override
                            protected Intent doInBackground(String... params) {
                                Log.d("frappe", TAG + "> Started authenticating");

                                String authtoken = null;
                                Bundle data = new Bundle();
                                OAuth2AccessToken accessToken = null;

                                try {
                                    accessToken = accountGeneral.oauth20Service.getAccessToken(authCode);
                                } catch (IOException e) {
                                    Toast.makeText(getBaseContext(), "IOException", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    Toast.makeText(getBaseContext(), "InterruptedException", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    Toast.makeText(getBaseContext(), "ExecutionException", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                                Log.d("AccessToken", accessToken.getRawResponse());
                                authtoken = accessToken.getRawResponse();
                                try {
                                    JSONObject bearerToken = new JSONObject(accessToken.getRawResponse());
                                    JSONObject openIDProfile = sServerAuthenticate.getOpenIDProfile(bearerToken.getString("access_token"),
                                            getResources().getString(R.string.serverURL),
                                            getResources().getString(R.string.openIDEndpoint));
                                    data.putString(AccountManager.KEY_ACCOUNT_NAME, openIDProfile.get("email").toString());
                                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                                    data.putString(PARAM_USER_PASS, getResources().getString(R.string.clientSecret));
                                } catch (Exception e) {
                                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                                }

                                final Intent res = new Intent();
                                res.putExtras(data);
                                return res;
                            }

                            @Override
                            protected void onPostExecute(Intent intent) {
                                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                                    Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                } else {
                                    finishLogin(intent);
                                    finish();
                                }
                            }
                        }.execute();
                    } else if (url.contains("error=access_denied")){
                        Log.i("", "ACCESS_DENIED_HERE");
                        resultIntent.putExtra("code", authCode);
                        authComplete = true;
                    }
                }
            });
        }
        else {
            Snackbar.make(findViewById(android.R.id.content), "Internet Connection Error", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }).show();
        }
    }

    private void finishLogin(Intent intent) {
        Log.d(getResources().getString(R.string.app_name), TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String authtoken = null;

        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, true)) {
            Log.d("frappe", TAG + "> finishLogin > addAccountExplicitly");
            authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            System.out.println(authtoken);

            mAccountManager.setAuthToken(account, authtokenType, authtoken);

            mAccountManager.setUserData(account, "authtoken", authtoken);
            setAccountAuthenticatorResult(getIntent().getExtras());
            setResult(RESULT_OK,getIntent());

            JSONObject bearerToken;
            try {
                bearerToken = new JSONObject(authtoken);
                Long tokenExpiryTime= (System.currentTimeMillis()/1000) + Long.parseLong(getResources().getString(R.string.expiresIn));
                mAccountManager.setUserData(account, "authToken", authtoken);
                mAccountManager.setUserData(account, "refreshToken", bearerToken.getString("refresh_token"));
                mAccountManager.setUserData(account, "accessToken", bearerToken.getString("access_token"));
                mAccountManager.setUserData(account, "serverURL", getResources().getString(R.string.serverURL));
                mAccountManager.setUserData(account, "redirectURI", getResources().getString(R.string.redirectURI));
                mAccountManager.setUserData(account, "clientId", getResources().getString(R.string.clientId));
                mAccountManager.setUserData(account, "clientSecret", getResources().getString(R.string.clientSecret));
                mAccountManager.setUserData(account, "oauth2Scope", getResources().getString(R.string.oauth2Scope));
                mAccountManager.setUserData(account, "authEndpoint", getResources().getString(R.string.authEndpoint));
                mAccountManager.setUserData(account, "tokenEndpoint", getResources().getString(R.string.tokenEndpoint));
                mAccountManager.setUserData(account, "openIDEndpoint", getResources().getString(R.string.openIDEndpoint));
                mAccountManager.setUserData(account, "tokenExpiryTime", tokenExpiryTime.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("frappe", TAG + "> finishLogin > setPassword new "+authtoken);
        } else {
            Log.d("frappe", TAG + "> finishLogin > setPassword no new login"+authtoken);
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
    public static boolean isConnected(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
