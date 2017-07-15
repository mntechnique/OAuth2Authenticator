package com.mntechnique.oauth2authenticator.auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.mntechnique.oauth2authenticator.BuildConfig;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;

    /**
     * Account name
     */
    public static String ACCOUNT_NAME = "OAuth2Authenticator";//Resources.getSystem().getString(R.string.app_name);

    /**
     * OAuth 2 Scope
     */
    public static String OAUTH2_SCOPE = "openid all";//Resources.getSystem().getString(R.string.oauth2Scope);

    /**
     * OAuth 2 Client Details
     */
    public static String CLIENT_ID = "f9b89210ae";//Resources.getSystem().getString(R.string.clientId);
    public static String CLIENT_SECRET = "2fa756a823";//Resources.getSystem().getString(R.string.clientSecret);

    /**
     * Auth token types
     */
    public static String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an account";
    public static String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an account";

    /**
     * URLs
     */
    public static String SERVER_URL = "http://test.mntechnique.com";//Resources.getSystem().getString(R.string.serverURL);
    public static String REDIRECT_URI = "oauth://oauth2authenticator";//Resources.getSystem().getString(R.string.redirectURI);

    /**
     * Endpoints
     */
    public static String AUTH_ENDPOINT = "/api/method/frappe.integrations.oauth2.authorize";//Resources.getSystem().getString(R.string.authEndpoint);
    public static String TOKEN_ENDPOINT = "/api/method/frappe.integrations.oauth2.get_token";//Resources.getSystem().getString(R.string.tokenEndpoint);
    public static String OPENID_PROFILE_ENDPOINT = "/api/method/frappe.integrations.oauth2.openid_profile";//Resources.getSystem().getString(R.string.openIDEndpoint);

    public static final OAuth20Service oauth20Service = new ServiceBuilder(AccountGeneral.CLIENT_ID)
            .apiSecret(AccountGeneral.CLIENT_SECRET)
            .scope(AccountGeneral.OAUTH2_SCOPE)
            .callback(AccountGeneral.REDIRECT_URI)
            .build(OAuth2API.instance(AccountGeneral.SERVER_URL));

    public static final ServerAuthenticate sServerAuthenticate = new FrappeServerAuthenticate();
    public static long expiresIn = 3300;
}
