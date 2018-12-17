package com.example.currentplacedetailsonmap;


import java.util.ArrayList;

public class MyPolygon {
    public Tag tag;
    public int color;
    public String _id;
    public ArrayList<Pos> pos;

    public class Tag {
        public String polygon_name;
        public String user_name;
    }

    public class Pos {
        public double latitude;
        public double longitude;
    }
}
