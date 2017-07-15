package com.mntechnique.oauth2authenticator.auth;

import org.json.JSONObject;

/**
 * User: Frappe
 * Date: 3/27/13
 * Time: 2:35 AM
 */
public interface ServerAuthenticate {
    public String userSignIn(JSONObject authMethod, String CLIENT_ID, String REDIRECT_URI) throws Exception;
    public JSONObject getOpenIDProfile(String accessToken, String serverURL, String openIDEndpoint);
}
