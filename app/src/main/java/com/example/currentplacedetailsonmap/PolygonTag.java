package com.example.currentplacedetailsonmap;

/**
 * A class used to represent each Polygon.
 */
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
