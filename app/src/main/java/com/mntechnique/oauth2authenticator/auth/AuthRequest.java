package com.mntechnique.oauth2authenticator.auth;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;

import com.mntechnique.oauth2authenticator.auth.AuthReqCallback;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by revant on 17/7/17.
 */

public class AuthRequest {

    OAuth20Service oauth20Service;
    Context mContext;

    public AuthRequest(Context context, String oauth2Scope, String clientId, String clientSecret, String serverURL,
                String redirectURI, String authEndpoint, String tokenEndpoint) {
        this.mContext = context;
        this.oauth20Service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(oauth2Scope)
                .callback(redirectURI)
                .build(OAuth20API.instance(serverURL, authEndpoint, tokenEndpoint));
    }

    public void makeRequest(String accessToken, final OAuthRequest request, final AuthReqCallback callback){
        final OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(accessToken);
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, String>(){
                    @Override
                    protected String doInBackground(Void... params) {
                        oauth20Service.signRequest(oAuth2AccessToken, request);
                        Response response = null;
                        String out = null;
                        try {
                            response = oauth20Service.execute(request);
                            out = response.getBody();
                        } catch (InterruptedException e) {
                            Log.e("OAuth2Authenticator", e.getMessage(), e);
                        } catch (ExecutionException e) {
                            Log.e("OAuth2Authenticator", e.getMessage(), e);
                        } catch (IOException e) {
                            Log.e("OAuth2Authenticator", e.getMessage(), e);
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
    }
}
