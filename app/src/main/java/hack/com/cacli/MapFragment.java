package hack.com.cacli;


import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private NMapContext mMapContext;
    private static final String CLIENT_ID = "HTSOdNC5nUu2HRqBtirR";

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapContext = new NMapContext(super.getActivity());
        mMapContext.onCreate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final NMapView mapView = (NMapView)getView().findViewById(R.id.mapView);
        initMapView(mapView);

        final NMapViewerResourceProvider mMapViewerResourceProvider = new NMapViewerResourceProvider(getActivity());
        final NMapOverlayManager mapOverlayManager = new NMapOverlayManager(getActivity(), mapView, mMapViewerResourceProvider);

        GPSModule gpsModule = new GPSModule(getActivity(), new GPSModule.OnSuccessListener() {
            @Override
            public void success(Location location) {
                Log.i("info", String.format(Locale.KOREA, "위도 : %s 경도 : %s", String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude())));
                NGeoPoint point = new NGeoPoint(location.getLongitude(), location.getLatitude());
                mapView.getMapController().setMapCenter(point, 13);


                int markerId = NMapPOIflagType.PIN;

                // set POI data
                NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
                poiData.beginPOIdata(1);
                poiData.addPOIitem(location.getLongitude(), location.getLatitude(), "Pizza 777-111", markerId, 0);
                poiData.endPOIdata();

                // create POI data overlay
                NMapPOIdataOverlay poiDataOverlay = mapOverlayManager.createPOIdataOverlay(poiData, null);
// show all POI data
                poiDataOverlay.showAllPOIdata(0);

            }
        });
        gpsModule.getCurrentLocation();
    }

    private void initMapView(NMapView mapView) {
        mapView.setClientId(CLIENT_ID);
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();
        mMapContext.setupMapView(mapView);
    }


    @Override
    public void onStart() {
        super.onStart();
        mMapContext.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapContext.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapContext.onPause();
    }

    @Override
    public void onStop() {
        mMapContext.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mMapContext.onDestroy();
        super.onDestroy();
    }
}
