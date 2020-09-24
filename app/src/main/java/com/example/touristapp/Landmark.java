package com.example.touristapp;

import com.google.android.gms.maps.model.LatLng;

public class Landmark {
    private String _name;
    private String _description;
    private int _logoID;
    private LatLng _latLng;

    public void setLatLng(LatLng latLng) {
        this._latLng = latLng;
    }

    public LatLng getLatLng() {
        return _latLng;
    }

    public Landmark(String name, String description, int logoID, Double lat, Double longg) {
        this._name = name;
        this._description = description;
        this._logoID = logoID;
        this._latLng = new LatLng(lat, longg);
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public void setLogoID(int logoID) {
        this._logoID = logoID;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public int getLogoID() {
        return _logoID;
    }
}
