package com.mntechnique.oauth2authenticator.auth;

import android.app.Application;
import android.content.res.Resources;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.mntechnique.oauth2authenticator.BuildConfig;
import com.mntechnique.oauth2authenticator.R;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "OAuth2Authenticator";//Resources.getSystem().getString(R.string.app_name);

    /**
     * OAuth 2 Scope
     */
    public static final String OAUTH2_SCOPE = "openid all";//Resources.getSystem().getString(R.string.oauth2Scope);

    /**
     * OAuth 2 Client Details
     */
    public static final String CLIENT_ID = "f9b89210ae";//Resources.getSystem().getString(R.string.clientId);
    public static final String CLIENT_SECRET = "2fa756a823";//Resources.getSystem().getString(R.string.clientSecret);

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an account";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an account";

    /**
     * URLs
     */
    public static final String SERVER_URL = "http://test.mntechnique.com";//Resources.getSystem().getString(R.string.serverURL);
    public static final String REDIRECT_URI = "oauth://oauth2authenticator";//Resources.getSystem().getString(R.string.redirectURI);

    /**
     * Endpoints
     */
    public static final String AUTH_ENDPOINT = "/api/method/frappe.integrations.oauth2.authorize";//Resources.getSystem().getString(R.string.authEndpoint);
    public static final String TOKEN_ENDPOINT = "/api/method/frappe.integrations.oauth2.get_token";//Resources.getSystem().getString(R.string.tokenEndpoint);
    public static final String OPENID_PROFILE_ENDPOINT = "/api/method/frappe.integrations.oauth2.openid_profile";//Resources.getSystem().getString(R.string.openIDEndpoint);

    public static final OAuth20Service oauth20Service = new ServiceBuilder(AccountGeneral.CLIENT_ID)
            .apiSecret(AccountGeneral.CLIENT_SECRET)
            .scope(AccountGeneral.OAUTH2_SCOPE)
            .callback(AccountGeneral.REDIRECT_URI)
            .build(OAuth2API.instance(AccountGeneral.SERVER_URL));

    public static final ServerAuthenticate sServerAuthenticate = new FrappeServerAuthenticate();
}
