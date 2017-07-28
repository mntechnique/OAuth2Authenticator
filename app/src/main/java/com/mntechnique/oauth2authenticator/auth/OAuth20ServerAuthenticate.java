package com.mntechnique.oauth2authenticator.auth;

import android.util.Log;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Handles the communication with OAuth 2 Provider
 *
 * User: revant
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class OAuth20ServerAuthenticate implements ServerAuthenticate {

    @Override
    public String userSignIn(final JSONObject authCode, final String CLIENT_ID, final String REDIRECT_URI) throws Exception {
        OAuth2AccessToken oAuth2AccessToken = null;
        HashMap<String,String> params=new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("redirect_uri", REDIRECT_URI);
        try {
            if (authCode.get("type")=="refresh"){
                String refresh_token = (String) authCode.get("refresh_token");
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh_token);
                oAuth2AccessToken = AccountGeneral.oauth20Service.refreshAccessToken(refresh_token);
            }
            else if(authCode.get("type")=="code"){
                String code = (String) authCode.get("code");
                params.put("grant_type", "authorization_code");
                params.put("code", code);
                oAuth2AccessToken = AccountGeneral.oauth20Service.getAccessToken(code);
            }
        } catch (JSONException e) {
            oAuth2AccessToken = null;
            e.printStackTrace();
        }
        return oAuth2AccessToken.getRawResponse();
    }

    @Override
    public JSONObject getOpenIDProfile(String accessToken, String serverURL, String openIDEndpoint){
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(accessToken);
        final OAuthRequest request = new OAuthRequest(Verb.GET, serverURL + openIDEndpoint);
        AccountGeneral.oauth20Service.signRequest(oAuth2AccessToken, request);
        Response response = null;
        JSONObject openIDProfile = new JSONObject();
        try {
            response = AccountGeneral.oauth20Service.execute(request);
            openIDProfile = new JSONObject(response.getBody());
        } catch (JSONException e) {
            openIDProfile = new JSONObject();
            Log.d("OAuth2Authenticator", e.getMessage());
        } catch (IOException e) {
            openIDProfile = new JSONObject();
            Log.d("OAuth2Authenticator", e.getMessage());
        } catch (Exception e){
            openIDProfile = new JSONObject();
            Log.d("OAuth2Authenticator", e.getMessage());
        }
        Log.d("OIDPJSON", openIDProfile.toString());
        return openIDProfile;
    }
}
