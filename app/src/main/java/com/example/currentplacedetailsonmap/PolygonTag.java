package com.example.currentplacedetailsonmap;

public class PolygonTag {
    private String polygonName = "Area";
    private String userName;

    public void setPolygonName(String name) {
        polygonName = name;
    }

    public void setUserName(String name) {
        userName = name;
    }

    public String getPolygonName() {
        return polygonName;
    }

    public String getUserName() {
        return userName;
    }
}
