package com.example.sarpa.autopay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

/**
 * Created by woonhyoungcheong on 2018. 4. 1..
 */

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapPOIItem mCustomMarker;
    private MapPoint my_MARKER_POINT;

    public static Handler mHandler;

    private double latitude;
    private double longitude;

    GPSTracker gps = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);





        mapView = new MapView(this);
        mapView.setDaumMapApiKey("16a32ab2274442b11c08bbff4f066741");

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        createCustomMarker(mapView);
    }

    private void createCustomMarker(MapView mapView) {
        if (gps == null) {
            gps = new GPSTracker(MapActivity.this, mHandler);
        } else {
            gps.Update();
        }

        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = 35.573239;
            longitude = 129.241236;
            Toast.makeText(getApplicationContext(), "현재 위치 - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            my_MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        mCustomMarker = new MapPOIItem();
        String name = "Custom Marker";
        mCustomMarker.setItemName(name);
        mCustomMarker.setTag(1);
        mCustomMarker.setMapPoint(my_MARKER_POINT);

        mCustomMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        mCustomMarker.setCustomImageResourceId(R.drawable.custom_marker_red);
        mCustomMarker.setCustomImageAutoscale(false);
        mCustomMarker.setCustomImageAnchor(0.5f, 1.0f);



        mapView.addPOIItem(mCustomMarker);
        mapView.selectPOIItem(mCustomMarker, true);
        mapView.setMapCenterPoint(my_MARKER_POINT, true);

        mapView.setPOIItemEventListener(new MapView.POIItemEventListener() {
            @Override
            public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                ((MainActivity)MainActivity.mContext).change(1);
            }

            @Override
            public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                ((MainActivity)MainActivity.mContext).change(1);
            }

            @Override
            public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                ((MainActivity)MainActivity.mContext).change(1);
            }

            @Override
            public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

            }
        });
    }
}