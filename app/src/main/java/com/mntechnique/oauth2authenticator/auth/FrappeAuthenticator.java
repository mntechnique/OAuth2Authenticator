package com.mntechnique.oauth2authenticator.auth;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static com.mntechnique.oauth2authenticator.auth.AccountGeneral.*;

/**
 * Created with IntelliJ IDEA.
 * User: Frappe
 * Date: 19/03/13
 * Time: 18:58
 */
public class FrappeAuthenticator extends AbstractAccountAuthenticator {

    private String TAG = "Frappe Authenticator";
    private final Context mContext;
    String authToken;

    public FrappeAuthenticator(Context context) {
        super(context);

        // I hate you! Google - set mContext as protected!
        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d("frappe", TAG + "> addAccount");

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d("frappe", TAG + "> getAuthToken");

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            Bundle result = new Bundle();
            result = getBundle("invalid_token_type",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        authToken = am.getUserData(account, "authtoken");
        String accessToken = am.getUserData(account, "accessToken");
        String refreshToken = am.getUserData(account, "refreshToken");

        String serverURL = am.getUserData(account, "serverURL");
        String CLIENT_ID = am.getUserData(account, "clientId");
        String REDIRECT_URI = am.getUserData(account, "redirectURI");
        String authEndpoint = am.getUserData(account, "authEndpoint");
        String tokenEndpoint = am.getUserData(account, "tokenEndpoint");
        String openIDEndpoint = am.getUserData(account, "openIDEndpoint");
        String clientSecret = am.getUserData(account, "clientSecret");
        String oauth2Scope = am.getUserData(account, "oauth2Scope");
        String expiresIn = am.getUserData(account, "expiresIn");
        String tokenExpiryTime = am.getUserData(account, "tokenExpiryTime");

        Log.d("OAuth2Authenticator", TAG + "> at isnull - " + accessToken);
        Log.d("OAuth2Authenticator", TAG + "> expiryTime - " + tokenExpiryTime);

        Long currentTime = System.currentTimeMillis()/1000;
        Long tokenExpiry = Long.parseLong(tokenExpiryTime);

        //Initiate Scribe Java Auth Service
        AccountGeneral accountGeneral = new AccountGeneral(
                oauth2Scope, CLIENT_ID, clientSecret, serverURL,
                REDIRECT_URI, authEndpoint, tokenEndpoint
        );

        Log.d("OAuth2Authenticator", accountGeneral.toString());

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(accessToken) || currentTime > tokenExpiry) {
            Log.d("OAuth2Authenticator", "return new or expired token");
            Bundle result = new Bundle();
            try {
                refreshBearerToken(am, account, refreshToken, CLIENT_ID, REDIRECT_URI, expiresIn);
                result = getBundle("valid",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
            } catch (Exception e) {
                result = getBundle("new_intent",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
                e.printStackTrace();
            }
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            JSONObject bearerToken = new JSONObject();
            JSONObject openIDProfile = new JSONObject();
            String access_token;
            try {
                bearerToken = new JSONObject(authToken);
                access_token = bearerToken.getString("access_token");
                openIDProfile = sServerAuthenticate.getOpenIDProfile(access_token, serverURL, openIDEndpoint);
                Bundle result = new Bundle();
                Log.d("OAuth2Aauthenticator", "check openid");
                if (openIDProfile.length() != 0 && openIDProfile.getString("email") == account.name){
                    Log.d("OAuth2Aauthenticator", "OpenID correct");
                    result = getBundle("valid",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
                } else {
                    Log.d("OAuth2Aauthenticator", "OpenID missing");
                    refreshBearerToken(am, account, refreshToken, CLIENT_ID, REDIRECT_URI, expiresIn);
                    result = getBundle("valid",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
                }
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                Bundle result = getBundle("new_intent",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
                e.printStackTrace();
                return result;
            }
        }
        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        Bundle result = getBundle("new_intent",AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,account,response);
        return result;
    }

    public Bundle getBundle(String bundleType, String authTokenType, Account account, AccountAuthenticatorResponse response){
        // bundleType - invalid_token_type, new_intent, valid
        Bundle result = new Bundle();
        if (bundleType == "invalid_token_type"){
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
        }else if(bundleType == "new_intent"){
            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
            intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
            intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
            result.putParcelable(AccountManager.KEY_INTENT, intent);
        }else if(bundleType == "valid"){
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        }
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    public void refreshBearerToken (AccountManager am, Account account, String refreshToken,
                                    String CLIENT_ID, String REDIRECT_URI, String expiresIn) throws Exception {
        Log.d("OAuth2Aauthenticator", "refreshing token");
        JSONObject authMethod = new JSONObject();

        Long tsLong = (System.currentTimeMillis()/1000)+ Long.parseLong(expiresIn);
        String refreshedTokenExpiryTime = tsLong.toString();

        am.setUserData(account, "tokenExpiryTime", refreshedTokenExpiryTime);
        authMethod.put("type", "refresh");
        authMethod.put("refresh_token", refreshToken);
        final String authToken = sServerAuthenticate.userSignIn(authMethod,CLIENT_ID,REDIRECT_URI);
        am.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, authToken);
        JSONObject bearerToken = new JSONObject(authToken);
        am.setUserData(account, "authtoken", authToken);
        am.setUserData(account, "refreshToken", bearerToken.getString("refresh_token"));
        am.setUserData(account, "accessToken", bearerToken.getString("access_token"));
    }
}
