package com.mntechnique.oauth2authenticator.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.mntechnique.oauth2authenticator.R;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.mntechnique.oauth2authenticator.auth.AccountGeneral.ACCOUNT_NAME;
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

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.VISIBLE);
        webView.clearCache(true);
        CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().sync();
        Log.d("oauth2serv", AccountGeneral.oauth20Service.getAuthorizationUrl());
        webView.loadUrl(AccountGeneral.oauth20Service.getAuthorizationUrl());
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
                if (url.contains("?code=") && authComplete != true) {
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
                                accessToken = AccountGeneral.oauth20Service.getAccessToken(authCode);
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
                                JSONObject openIDProfile = sServerAuthenticate.getOpenIDProfile(bearerToken.getString("access_token") ,AccountGeneral.OPENID_PROFILE_ENDPOINT);
                                data.putString(AccountManager.KEY_ACCOUNT_NAME, openIDProfile.get("email").toString());
                                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                                data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                                data.putString(PARAM_USER_PASS, AccountGeneral.CLIENT_SECRET);
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
                                webView.setVisibility(View.GONE);
                                finishLogin(intent);
                            }
                        }
                    }.execute();
                }else if(url.contains("redirect_uri=oauth%3A%2F%2Ffoauth2authenticator") && authComplete != true) {
                    Toast.makeText(getBaseContext(), "Allow or Deny Access to Resources", Toast.LENGTH_LONG).show();
                }else if(url.contains("error=access_denied")){
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                }
            }
        });
    }

    private void finishLogin(Intent intent) {
        Log.d(AccountGeneral.ACCOUNT_NAME, TAG + "> finishLogin");

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
                mAccountManager.setUserData(account, "refreshToken", bearerToken.getString("refresh_token"));
                mAccountManager.setUserData(account, "accessToken", bearerToken.getString("access_token"));
                mAccountManager.setUserData(account, "redirectURI", AccountGeneral.REDIRECT_URI);
                mAccountManager.setUserData(account, "frappeServer", AccountGeneral.SERVER_URL);
                mAccountManager.setUserData(account, "clientId", AccountGeneral.CLIENT_ID);
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

}
