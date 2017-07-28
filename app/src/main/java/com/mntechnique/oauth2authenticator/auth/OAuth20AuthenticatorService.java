package com.mntechnique.oauth2authenticator.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Frappe
 * Date: 19/03/13
 * Time: 19:10
 */
public class OAuth20AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        OAuth20Authenticator authenticator = new OAuth20Authenticator(this);
        return authenticator.getIBinder();
    }
}
