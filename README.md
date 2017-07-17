# OAuth 2 Account Authenticator Library

Add this library to any project to add Account Authenticator based on OAuth 2.0

## Steps 

### Add to jitpack to gradle/maven 

https://jitpack.io/#mntechnique/OAuth2Authenticator

### Override string.xml with your details

```

<resources>
    <string name="clientId">your_client_id</string>
    <string name="app_name">Your App Name</string>
    <string name="authEndpoint">/api/method/frappe.integrations.oauth2.authorize</string>
    <string name="tokenEndpoint">/api/method/frappe.integrations.oauth2.get_token</string>
    <string name="openIDEndpoint">/api/method/frappe.integrations.oauth2.openid_profile</string>
    <string name="redirectURI">oauth://oauth2authenticator</string>
    <string name="serverURL">http://test.mntechnique.com</string>
    <string name="oauth2Scope">openid all</string>
    <string name="clientSecret">your_client_secret</string>
    <string name="allowMultipleAccounts">1</string>
</resources>

```

Note : `allowMultipleAccounts` if set to 0, only one account can be added to account manager.

### Override colors, styles and mipmap

Override mipmap/ic_launcher and mipmap/ic_launcher_round to change account icons.

### Accessing resource

Initialize AuthRequest with Oauth 2.0 client details.

```
final AuthRequest authRequest = new AuthRequest(
        getApplicationContext(),
        oauth2Scope,clientId,clientSecret, serverURL,
        redirectURI, authEndpoint, tokenEndpoint);

```

Set Callback and request

```
AuthReqCallback callback = new AuthReqCallback() {
    @Override
    public void onSuccessResponse(String s) {
        Log.d("CallbackSuccess", s);
        //Can handle UI thread here
    }

    @Override
    public void onErrorResponse(String e) {
        Log.d("CallbackError", e);
    }
};

OAuthRequest request = new OAuthRequest(Verb.GET, serverURL + openIDEndpoint);
```

Get Accounts and make authenticated request with an account

```
Account[] accounts = mAccountManager.getAccountsByType(BuildConfig.APPLICATION_ID);

if (accounts.length == 1) {
    authRequest.makeAuthenticatedRequest(accounts[0],request, callback);
} else if (Build.VERSION.SDK_INT >= 23) {
    Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{BuildConfig.APPLICATION_ID}, null, null  , null, null);
    startActivityForResult(intent, 1);
}
```
