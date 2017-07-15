package com.mntechnique.oauth2authenticator.auth;

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

    protected OAuth2API(String serverURL) {
        this.serverURL = serverURL;
        this.accessTokenEndpoint = serverURL + AccountGeneral.TOKEN_ENDPOINT;
        this.authorizationBaseUrl = serverURL + AccountGeneral.AUTH_ENDPOINT;
    }

    public static OAuth2API instance(String serverUrl) {
        return new OAuth2API(serverUrl);
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
