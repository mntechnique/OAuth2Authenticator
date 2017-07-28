package com.mntechnique.oauth2authenticator.auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

public class AccountGeneral {

    /**
     * Auth token types
     */
    public static String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an account";
    public static String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an account";
    public static final ServerAuthenticate sServerAuthenticate = new OAuth20ServerAuthenticate();

    public static OAuth20Service oauth20Service = null;

    AccountGeneral(String oauth2Scope, String clientId, String clientSecret, String serverURL,
                   String redirectURI, String authEndpoint, String tokenEndpoint){
        oauth20Service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(oauth2Scope)
                .callback(redirectURI)
                .build(OAuth20API.instance(serverURL, authEndpoint, tokenEndpoint));
    }
}
