package com.example.currentplacedetailsonmap;

import java.util.ArrayList;

public class MyPolyline {
    public Tag tag;
    public ArrayList<Pos> pos;
    public int status;

    public class Tag {
        public String user_name;
    }

    public class Pos {
        public double latitude;
        public double longitude;
    }
}
