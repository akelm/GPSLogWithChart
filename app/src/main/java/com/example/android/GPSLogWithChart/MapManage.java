package com.example.android.GPSLogWithChart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingVariationHue;
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList;

import java.util.ArrayList;
import java.util.List;

public class MapManage {
    public static Polyline line = null;
    public static ArrayList<Float> velocities = null;
    public static float maxV = -Float.MAX_VALUE;
    public static float minV = Float.MAX_VALUE;
    public static Marker selectedMarker;
    static PolychromaticPaintList paintList = null;
    static ColorMappingVariationHue colorMapVar = null;
    static MapView map = null;
    static ScaleBarOverlay mScaleBarOverlay = null;
    private static Paint paint = null;
    private static MapManage instance = null;
    private static Activity activity = null;

    // singleton
    private MapManage() {

    }

    public static void displayMap() {

        map = activity.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        map.setMultiTouchControls(true);

        final DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);

        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);

        IMapController mapController = map.getController();
        mapController.setZoom(18.);
        // todo change to current/last locale
        GeoPoint startPoint = new GeoPoint(52.22058123, 20.98443718);
        mapController.setCenter(startPoint);

        line = new Polyline();

        paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeCap(Paint.Cap.ROUND);

        line.setOnClickListener((polyline, mapView, eventPos) -> {
            BoundingBox bBox = mapView.getBoundingBox();
            List<GeoPoint> lgp = line.getActualPoints();
            double dist = Double.MAX_VALUE;
            int ind = -1;
            for (int i = 0; i < lgp.size(); i++) {
                GeoPoint gp = lgp.get(i);
                if (bBox.contains(gp)) {
                    double newDist = eventPos.distanceToAsDouble(gp);
                    if (newDist < dist) {
                        dist = newDist;
                        ind = i;
                    }
                }
            }

            if (ind > -1) {
                selectedMarker.setPosition(lgp.get(ind));
                selectedMarker.setAlpha(0.5f);
                map.invalidate();
                ChartManage.setSelected(ind);
            }

            return false;
        });

        map.getOverlayManager().add(line);

        velocities = new ArrayList<>();

        selectedMarker = new Marker(map);
        selectedMarker.setPosition(startPoint);
        selectedMarker.setDraggable(true);
        selectedMarker.setOnMarkerClickListener((marker, mapView) -> true);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setAlpha(0.5f);
        selectedMarker.setVisible(false);

        selectedMarker.setIcon(activity.getDrawable(R.drawable.ic_place_black_24dp));
        selectedMarker.getIcon().setTint(activity.getColor(R.color.colorAccent));
        map.getOverlays().add(selectedMarker);
        map.invalidate();
    }

    public static void setup(Activity act) {
        activity = act;
        Context context = activity.getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static MapManage getInstance() {
        if (instance == null) {
            instance = new MapManage();
        }
        return instance;
    }

    public static void updateVelocities(float vel) {
        velocities.add(vel);
        if (vel > maxV) {
            maxV = vel;
        }
        if (vel < minV) {
            minV = vel;
        }
    }

    public static void setMapOffset(int x, int y) {
        map.setMapCenterOffset(x, y);
        map.invalidate();
    }

    public static void setData(List<TrackEntity> tracks) {
        clearMap();
        if (tracks != null && tracks.size() > 0) {
            for (TrackEntity tr : tracks) {
                addTrack(tr);
            }
        } else {
            clearMap();
        }

    }

    public static void clearMap() {
        line.getActualPoints().clear();
        velocities.clear();
        selectedMarker.setVisible(false);
        maxV = -Float.MAX_VALUE;
        minV = Float.MAX_VALUE;
        updatePaintlist();
        map.invalidate();
    }

    public static void addTrack(TrackEntity track) {
        if (track != null) {
            Location l = new Location("");
            l.setLongitude(track.getLon());
            l.setLatitude(track.getLat());
            l.setAltitude(0);
            l.setSpeed(track.getVel());
            l.setTime(track.getTime());
            updateVelocities(track.getVel());
            updateLine(l);
        }
    }

    static void updatePaintlist() {
        colorMapVar = new MyColorList(
                minV, maxV, (float) 250, (float) 0, (float) 1, (float) 0.5);

        paintList = new PolychromaticPaintList(
                paint, colorMapVar, true);
        line.getOutlinePaintLists().clear();
        line.getOutlinePaintLists().add(paintList);
    }

    public static void updateLine(Location loc) {
        loc.setAltitude(0);
        GeoPoint newPoint = new GeoPoint(loc);
        IMapController mapController = map.getController();

        mapController.setCenter(newPoint);

        line.addPoint(newPoint);
        updatePaintlist();

        map.invalidate();

    }

    public static void setSelected(int ind) {
        GeoPoint gp = line.getActualPoints().get(ind);
        selectedMarker.setPosition(gp);
        selectedMarker.setAlpha(0.5f);
        map.invalidate();
    }

    public static class MyColorList extends ColorMappingVariationHue {

        MyColorList(
                float scalarStart, float scalarEnd, float hueStart, float hueEnd, float saturation, float luminance) {
            super(scalarStart, scalarEnd, hueStart, hueEnd, saturation, luminance);
        }

        @Override
        public int getColorForIndex(int pSegmentIndex) {
            double velo = velocities.get(pSegmentIndex);
            return computeColor((float) velo);
        }
    }

}
