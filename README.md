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

Set Callbacks and request

```
// callback to handle Request Response
final AuthReqCallback responseCallback = new AuthReqCallback() {
    @Override
    public void onSuccessResponse(String s) {
        try {
            JSONObject openID = new JSONObject(s);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            TextView tvSub = (TextView) findViewById(R.id.tvSub);
            TextView tvName = (TextView) findViewById(R.id.tvName);
            TextView tvGivenName = (TextView) findViewById(R.id.tvGivenName);
            TextView tvFamName = (TextView) findViewById(R.id.tvFamName);
            TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
            LinearLayout llOpenID = (LinearLayout) findViewById(R.id.llOpenID);

            tvSub.setText(openID.getString("sub"));
            tvName.setText(openID.getString("name"));
            tvGivenName.setText(openID.getString("given_name"));
            tvFamName.setText(openID.getString("family_name"));
            tvEmail.setText(openID.getString("email"));

            progressBar.setVisibility(View.GONE);
            llOpenID.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(String s) {

    }
};

// callback to handle Bearer Token
AuthReqCallback accessTokenCallback = new AuthReqCallback() {
    @Override
    public void onSuccessResponse(String s) {
        Log.d("CallbackSuccess", s);
        JSONObject bearerToken = new JSONObject();
        try {
            bearerToken = new JSONObject(s);
        } catch (JSONException e) {
            bearerToken = new JSONObject();
        }

        if(bearerToken.length() > 0){
            try {
                authRequest.makeRequest(bearerToken.getString("access_token"), request, responseCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onErrorResponse(String s) {
        Log.d("CallbackError", s);
    }
};

OAuthRequest request = new OAuthRequest(Verb.GET, serverURL + openIDEndpoint);
```

Get Accounts and make authenticated request with an account

```
Account[] accounts = mAccountManager.getAccountsByType(BuildConfig.APPLICATION_ID);

if (accounts.length == 1) {
    getToken(accounts[0], accessTokenCallback);
} else if (Build.VERSION.SDK_INT >= 23) {
    Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{BuildConfig.APPLICATION_ID}, null, null  , null, null);
    startActivityForResult(intent, 1);
}
```

getAuthToken with failure notification in notification bar 


```
void getAuthToken(final Account account, final AuthReqCallback callback) {
    mAccountManager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
            true, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bundle = future.getResult();
                        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                        callback.onSuccessResponse(authToken);
                        Log.d("bearerToken", authToken);
                        mAccountManager.invalidateAuthToken(account.type, authToken);
                    } catch (Exception e) {
                        Log.d("error", e.toString());
                    }
                }
            }, null);
}
```

getAuthToken with login activity intent 

```
void getAuthToken(final Account account, final AuthReqCallback callback) {
    mAccountManager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
            null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bundle = future.getResult();
                        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                        callback.onSuccessResponse(authToken);
                        Log.d("bearerToken", authToken);
                        mAccountManager.invalidateAuthToken(account.type, authToken);
                    } catch (Exception e) {
                        Log.d("error", e.toString());
                    }
                }
            }, null);
}
```
