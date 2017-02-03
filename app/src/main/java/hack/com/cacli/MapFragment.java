package hack.com.cacli;


import android.location.Address;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.Geocoder;
import android.widget.Toast;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements NMapPOIdataOverlay.OnStateChangeListener{

    private NMapContext mMapContext;
    private static final String CLIENT_ID = "HTSOdNC5nUu2HRqBtirR";
    private Geocoder mGeocoder;
    private MapActivity mapActivity;
    private MapOverlayController mapOverlayController;
    private GPSModule gpsModule;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance(MapActivity mapActivity){
        MapFragment mapFragment = new MapFragment();
        mapFragment.mapActivity = mapActivity;

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
            public void success(final Location location) {
                if(location == null) {
                    Toast.makeText(getActivity(), "GPS을 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("info", String.format(Locale.KOREA, "위도 : %s 경도 : %s", String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude())));
                NGeoPoint point = new NGeoPoint(location.getLongitude(), location.getLatitude());
                mapView.getMapController().setMapCenter(point, 13);

                JSONArray jsonArray = null;
                try {
                    jsonArray = new AsyncTask<Void, Void, JSONArray>(){
                        @Override
                        protected JSONArray doInBackground(Void... voids) {
                            JSONArray jsonArr = POST("http://hmkcode.appspot.com/jsonservlet", location.getLongitude(), location.getLatitude());
                            Log.i("info","json array : " + jsonArr.toString());
                            return jsonArr;
                        }
                    }.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                parseJsonArrayIntoMap(jsonArray, point);
                try {
                    List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                    if(addressList != null && addressList.size() > 0){
                        mapActivity.setCurrentPositionTextView(addressList.get(0).getAddressLine(0).substring(0, 20));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        gpsModule.getCurrentLocation();
    }

    public void parseJsonArrayIntoMap(JSONArray jsonArray, NGeoPoint location){
        final NMapViewerResourceProvider mMapViewerResourceProvider = new NMapViewerResourceProvider(getActivity());
        final NMapView mapView = (NMapView)getView().findViewById(R.id.mapView);
        NMapOverlayManager mapOverlayManager = new NMapOverlayManager(getActivity(), mapView, mMapViewerResourceProvider);

        if(mapOverlayController == null)
            mapOverlayController = new MapOverlayController(mMapViewerResourceProvider, mapOverlayManager);
        mapOverlayController.clearOverlay();
        List<OverlayItem> overlayItems = new ArrayList<>();
        if(location != null)
            overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude(), NMapPOIflagType.TO, "유저"));

        if(jsonArray != null){
            for(int i=0;i<jsonArray.length();i++){
                try{
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Log.i("info", "jsonobject : " + jsonObject.getString("longitude"));
                    overlayItems.add(new OverlayItem(jsonObject.getDouble("longitude"), jsonObject.getDouble("latitude"), NMapPOIflagType.FROM, jsonObject.getString("location")));
                }
                catch (org.json.JSONException e){
                    Log.i("info","json exception");
                }
            }
        }

        mapOverlayController.initOverlayItemList(overlayItems);
        mapOverlayController.displayOverlayItemList(MapFragment.this);
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

    public NGeoPoint SearchMapByAddress(String address){
        final NMapView mapView = (NMapView)getView().findViewById(R.id.mapView);
        NGeoPoint point = findGeoPoint(address);

        if(point != null){
            mapView.getMapController().setMapCenter(point,13);
            return point;
        }
        else{
            Toast.makeText(getActivity(), "찾을 수 없는 주소입니다." , Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    /**
     * 주소로부터 위치정보 취득
     * @param address 주소
     */
    private NGeoPoint findGeoPoint(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        Address addr;
        NGeoPoint location = null;

        Log.i("info","findGeoPoint");

        try {
            List<Address> listAddress = geocoder.getFromLocationName(address, 1);
            if (listAddress.size() > 0) { // 주소값이 존재 하면
                addr = listAddress.get(0); // Address형태로
                int lat = (int) ((addr.getLatitude()) * 1E6);
                int lng = (int) ((addr.getLongitude()) * 1E6);
                location = new NGeoPoint(lng, lat);

                Log.i("info", "주소로부터 취득한 위도 : " + lat + ", 경도 : " + lng);
            }
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return location;
    }

    public static String POST(String urlString){

        StringBuilder result = new StringBuilder();

        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.connect();
            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line = null;
            while((line = reader.readLine())!= null){
                result.append(line);
            }
            Log.i("info", result.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(httpURLConnection != null)
                httpURLConnection.disconnect();
        }

        return null;
    }

    public static JSONArray POST(String url, double longitude, double latitude){
        String result = "";
        Log.i("info","post to server");

        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection)urlCon.openConnection();

            String json = "";

            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("longitude", longitude);
            jsonObject.accumulate("latitude", latitude);

            // convert JSONObject to JSON to String
            json = jsonObject.toString();
            Log.i("info","json : " + json);

            // Set some headers to inform server about the type of the content
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);

            httpCon.connect();

            OutputStream os = httpCon.getOutputStream();
            OutputStreamWriter wr = new OutputStreamWriter(os,"UTF-8");
            wr.write(json);
            wr.flush();
            wr.close();
            os.close();

            Log.i("info","send");

            InputStream is = new URL(url).openStream();
            // receive response as inputStream
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                JSONArray jsonArr = new JSONArray(jsonText);
                return jsonArr;
            }
            catch (IOException e) {
                Log.i("info","inputstream error");
                e.printStackTrace();
            }
            finally {
                is.close();
                httpCon.disconnect();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mapOverlayController.clearCalloutOverlay();
            }
        });

        view.findViewById(R.id.btn_compass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapOverlayController.clearOverlay();
                gpsModule.getCurrentLocation();
            }
        });
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

    @Override
    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
        if(nMapPOIitem == null)return;

        NGeoPoint point = nMapPOIitem.getPoint();

        if(point == null)return;

        if("유저".equals(nMapPOIitem.getTag()))return;

        try {
            List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if(addressList != null && addressList.size() > 0){
                mapActivity.callBottomSheet(nMapPOIitem.getTag().toString(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }

    public class OverlayItem{
        private final double longitude;
        private final double latitude;
        private final int type;
        private final String tag;

        public OverlayItem(double longitude, double latitude, int type, String tag) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = type;
            this.tag = tag;
        }
    }

    public class MapOverlayController{
        private final NMapViewerResourceProvider mMapViewerResourceProvider;
        private final NMapOverlayManager mMapOverlayManager;
        private final NMapPOIdata mPOIData;

        public MapOverlayController(NMapViewerResourceProvider mapViewerResourceProvider, NMapOverlayManager mMapOverlayManager){
            this.mMapViewerResourceProvider = mapViewerResourceProvider;
            this.mMapOverlayManager = mMapOverlayManager;
            this.mPOIData = new NMapPOIdata(2, mMapViewerResourceProvider);
        }

        public void initOverlayItemList(List<OverlayItem> itemList){
            mPOIData.beginPOIdata(itemList.size());
            for(OverlayItem item : itemList){
                mPOIData.addPOIitem(item.longitude, item.latitude, null, item.type, item.tag);
            }
            mPOIData.endPOIdata();
        }

        public void displayOverlayItemList(NMapPOIdataOverlay.OnStateChangeListener onStateChangeListener){
            NMapPOIdataOverlay poIdataOverlay = mMapOverlayManager.createPOIdataOverlay(mPOIData, null);
            poIdataOverlay.setOnStateChangeListener(onStateChangeListener);
        }

        public void clearOverlay(){
            mMapOverlayManager.clearOverlays();
        }

        public void clearCalloutOverlay(){
            mMapOverlayManager.clearCalloutOverlay();
        }
    }
}
