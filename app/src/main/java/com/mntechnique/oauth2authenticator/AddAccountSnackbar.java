package com.mntechnique.oauth2authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mntechnique.oauth2authenticator.auth.AuthenticatorActivity;

/**
 * Created by revant on 28/7/17.
 */

public class AddAccountSnackbar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAddAccountSnackBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        showAddAccountSnackBar();
    }

    public void showAddAccountSnackBar(){
        AccountManager mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(getResources().getString(R.string.package_name));
        if (accounts.length == 0){
            Snackbar.make(findViewById(android.R.id.content), R.string.please_add_account, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(AddAccountSnackbar.this, AuthenticatorActivity.class);
                            startActivity(intent);
                        }
                    }).show();
        }
    }
}
