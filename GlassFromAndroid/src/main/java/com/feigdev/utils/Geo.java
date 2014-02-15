package com.feigdev.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import com.google.api.services.mirror.model.Location;

import java.io.IOException;
import java.util.List;

/**
 * Created by ejf3 on 2/9/14.
 */
public class Geo {
    private static final String TAG = "Geo";

    public static Location geoCode(Context context, String address){
        Geocoder coder = new Geocoder(context);

        if (address.contains("http://goo.gl")){
            address = address.substring(0, address.indexOf("http://goo.gl"));
        }

        address = address.replace("\n", ",");

        List<Address> addrs;
        try {
            addrs = coder.getFromLocationName(address,5);
            if (addrs == null || addrs.size() < 1) {
                Lg.d(TAG, "no location found");
                return null;
            }
            Address location = addrs.get(0);
            location.getLatitude();
            location.getLongitude();

            Location l = new Location();
            l.setAddress(address);
            l.setDisplayName(address);
            l.setLatitude(location.getLatitude());
            l.setLongitude(location.getLongitude());

            Lg.d(TAG, "location: " + l);

            return l;
        } catch (IOException e) {
            e.printStackTrace();
            Lg.e(TAG, "failed to build location", e);
        }
        return null;
    }
}
