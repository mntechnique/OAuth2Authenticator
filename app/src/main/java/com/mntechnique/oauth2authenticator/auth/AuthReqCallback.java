package com.mntechnique.oauth2authenticator.auth;

/**
 * Created by revant on 17/7/17.
 */

public interface AuthReqCallback {
    void onSuccessResponse(String result);
    void onErrorResponse(String error);
}
