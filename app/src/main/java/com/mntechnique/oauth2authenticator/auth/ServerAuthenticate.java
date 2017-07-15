package com.mntechnique.oauth2authenticator.auth;

import com.github.scribejava.core.model.OAuth2AccessToken;

import org.json.JSONObject;

/**
 * User: Frappe
 * Date: 3/27/13
 * Time: 2:35 AM
 */
public interface ServerAuthenticate {
    //public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
    public String userSignIn(JSONObject authMethod, String CLIENT_ID, String REDIRECT_URI) throws Exception;
    public JSONObject getOpenIDProfile(String accessToken, String OPENID_PROFILE_URL);
}
