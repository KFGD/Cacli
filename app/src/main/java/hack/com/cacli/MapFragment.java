package hack.com.cacli;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private NMapContext mMapContext;
    private static final String CLIENT_ID = "HTSOdNC5nUu2HRqBtirR";
    private Geocoder mGeocoder;
    private GPSModule gpsModule;
    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance(){
        MapFragment mapFragment = new MapFragment();

        return mapFragment;
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
        mGeocoder = new Geocoder(getActivity(), Locale.KOREA);
        mMapContext.onCreate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final NMapView mapView = (NMapView)getView().findViewById(R.id.mapView);
        initMapView(mapView);

        final NMapViewerResourceProvider mMapViewerResourceProvider = new NMapViewerResourceProvider(getActivity());
        final NMapOverlayManager mapOverlayManager = new NMapOverlayManager(getActivity(), mapView, mMapViewerResourceProvider);

        gpsModule = new GPSModule(getActivity(), new GPSModule.OnSuccessListener() {
            @Override
            public void success(Location location) {
                if(location == null) {
                    Toast.makeText(getActivity(), "GPS을 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                //set event listener to the overlay
                poiDataOverlay.setOnStateChangeListener(new NMapPOIdataOverlay.OnStateChangeListener() {
                    @Override
                    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

                    }

                    @Override
                    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
                        NGeoPoint point = nMapPOIitem.getPoint();
                        try {
                            List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                            if(addressList != null && addressList.size() > 0){
                                Toast.makeText(getActivity(), String.format(Locale.KOREA,"현재 위치의 주소는 %s", addressList.get(0).getAddressLine(0).toString()), Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
