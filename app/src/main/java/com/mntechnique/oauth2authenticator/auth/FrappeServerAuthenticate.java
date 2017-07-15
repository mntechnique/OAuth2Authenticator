package com.mntechnique.oauth2authenticator.auth;

import android.util.Log;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Handles the comminication with Parse.com
 *
 * User: revant
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class FrappeServerAuthenticate implements ServerAuthenticate{
    String authtoken;
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
            e.printStackTrace();
        }
        return oAuth2AccessToken.getAccessToken();
    }

    @Override
    public JSONObject getOpenIDProfile(String accessToken, String OPENID_PROFILE_URL){
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(accessToken);
        final OAuthRequest request = new OAuthRequest(Verb.GET, AccountGeneral.SERVER_URL + AccountGeneral.OPENID_PROFILE_ENDPOINT);
        AccountGeneral.oauth20Service.signRequest(oAuth2AccessToken, request);
        Response response = null;
        try {
            response = AccountGeneral.oauth20Service.execute(request);
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            Log.d("getOIDEndp", response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject openIDProfile = null;
        try {
            openIDProfile = new JSONObject(response.getBody());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return openIDProfile;
    }

    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    private class User implements Serializable {
        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        private String objectId;
        public String sessionToken;
        private String gravatarId;


        private String avatarUrl;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
