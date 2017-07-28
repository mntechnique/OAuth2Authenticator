package com.mntechnique.oauth2authenticator.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mntechnique.oauth2authenticator.R;

/**
 * Created by revant on 28/7/17.
 */

public class RetrieveAuthTokenTask extends AsyncTask<String, Void, Void> {

    private Exception exception;
    private AuthReqCallback callback;
    private Context context;
    private String authToken;

    public RetrieveAuthTokenTask(Context context, AuthReqCallback postExecuteCallback) {
        this.callback = postExecuteCallback;
        this.context = context;
    }

    protected Void doInBackground(String... urls) {
        Account[] accounts = null;
        final AccountManager am = AccountManager.get(context);
        accounts = am.getAccountsByType(context.getResources().getString(R.string.package_name));
        if (accounts.length == 1) {
            final Account account = accounts[0];
            am.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, true, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bundle = future.getResult();
                        authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                        callback.onSuccessResponse(authToken);
                        am.invalidateAuthToken(account.type, authToken);
                    } catch (Exception e) {
                        Log.d("error", e.getMessage());
                        callback.onErrorResponse(e.toString());
                    }
                }
            }, null);
        } else {
            Log.d("Accounts", "NOT 1 account found!");
        }
        return null;
    }
}
