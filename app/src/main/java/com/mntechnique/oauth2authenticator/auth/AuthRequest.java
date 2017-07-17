package com.mntechnique.oauth2authenticator.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by revant on 17/7/17.
 */

public class AuthRequest {

    OAuth20Service oauth20Service;
    Context mContext;
    String responseBody = "{}";

    public AuthRequest(Context context, String oauth2Scope, String clientId, String clientSecret, String serverURL,
                String redirectURI, String authEndpoint, String tokenEndpoint) {
        this.mContext = context;
        this.oauth20Service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(oauth2Scope)
                .callback(redirectURI)
                .build(OAuth2API.instance(serverURL, authEndpoint, tokenEndpoint));
    }

    public void makeAuthenticatedRequest(final Account account, final OAuthRequest request, final AuthReqCallback callback){
        final AccountManager accountManager = AccountManager.get(mContext);
        accountManager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(final AccountManagerFuture<Bundle> future) {
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new AsyncTask<Void, Void, String>(){
                            @Override
                            protected String doInBackground(Void... params) {
                                String authToken;
                                String out = "";
                                try {
                                    Bundle bundle = future.getResult();
                                    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                    JSONObject bearerToken = new JSONObject(authToken);
                                    OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(bearerToken.getString("access_token"));
                                    oauth20Service.signRequest(oAuth2AccessToken, request);
                                    Response response = null;
                                    response = oauth20Service.execute(request);
                                    return response.getBody();
                                    //accountManager.invalidateAuthToken(account.type, authToken);
                                } catch (JSONException e) {
                                    Log.d("JSONerror", e.getMessage());
                                    callback.onErrorResponse(e.getMessage());
                                } catch (IOException e) {
                                    Log.d("OAuth2Authenticator", e.getMessage());
                                    callback.onErrorResponse(e.getMessage());
                                } catch (Exception e) {
                                    Log.d("OAuth2Authenticator", e.toString());
                                    callback.onErrorResponse(e.toString());
                                    e.printStackTrace();
                                }
                                return out;
                            }
                            @Override
                            protected void onPostExecute(String result){
                                callback.onSuccessResponse(result);
                            }
                        }.execute();
                    }
                });
//                new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                    }
//                }).start();
            }
        },null);
    }
}