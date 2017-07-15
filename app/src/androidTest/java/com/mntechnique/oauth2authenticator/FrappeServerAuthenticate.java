package com.mntechnique.oauth2authenticator;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Handles the comminication with Parse.com
 *
 * User: revant
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class FrappeServerAuthenticate implements ServerAuthenticate{
    String authtoken;
    @Override
    public String userSignIn(String TOKEN_URL, final JSONObject authCode, final String CLIENT_ID, final String REDIRECT_URI) throws Exception {
        HashMap<String,String> params=new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("redirect_uri", REDIRECT_URI);
        try {
            if (authCode.get("type")=="refresh"){
                String refresh_token = (String) authCode.get("refresh_token");
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh_token);
            }
            else if(authCode.get("type")=="code"){
                String code = (String) authCode.get("code");
                params.put("grant_type", "authorization_code");
                params.put("code", code);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            URL url=new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(FrappeUtils.postDataStr(params));

            writer.flush();
            writer.close();
            os.close();
            InputStream inputStream = conn.getInputStream();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
            String is = stringWriter.toString();
            int responseCode=conn.getResponseCode();
            conn.disconnect();
            if (responseCode == HttpURLConnection.HTTP_OK)
                return is;
            else
                return "";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return authtoken;
    }

    @Override
    public JSONObject getOpenIDProfile(String accessToken, String OPENID_PROFILE_URL){
        JSONObject openIDProfile = new JSONObject();
        HashMap<String,String> params=new HashMap<>();
        try{
            URL url=new URL(OPENID_PROFILE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            InputStream inputStream = conn.getInputStream();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
            String is = stringWriter.toString();
            int responseCode=conn.getResponseCode();
            conn.disconnect();
            if (responseCode == HttpURLConnection.HTTP_OK)
                openIDProfile = new JSONObject(is);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return openIDProfile;
    }
    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    private class User implements Serializable {
        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        private String objectId;
        public String sessionToken;
        private String gravatarId;


        private String avatarUrl;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
