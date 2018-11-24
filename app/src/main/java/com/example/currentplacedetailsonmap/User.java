package com.example.currentplacedetailsonmap;

import com.google.android.gms.maps.model.Polyline;

import org.json.JSONObject;

public class User {
    String user_id;
    String name;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("user_id", this.user_id);
            jsonObject.put("name", this.name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
