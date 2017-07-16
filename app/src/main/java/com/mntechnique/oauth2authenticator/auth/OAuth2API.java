package com.mntechnique.oauth2authenticator.auth;

import android.content.res.Resources;

import com.github.scribejava.apis.google.GoogleJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * Created by revant on 15/7/17.
 */

public class OAuth2API extends DefaultApi20 {

    private final String serverURL;
    private final String accessTokenEndpoint;
    private final String authorizationBaseUrl;

    protected OAuth2API(String serverURL, String authEndpoint, String tokenEndpoint) {
        this.serverURL = serverURL;
        this.authorizationBaseUrl = serverURL + authEndpoint;
        this.accessTokenEndpoint = serverURL + tokenEndpoint;
    }

    public static OAuth2API instance(String serverUrl, String authEndpoint, String tokenEndpoint) {
        return new OAuth2API(serverUrl, authEndpoint, tokenEndpoint);
    }

    public String getServerURL() {
        return serverURL;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return authorizationBaseUrl;
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return GoogleJsonTokenExtractor.instance();
    }
}
