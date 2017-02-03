package hack.com.cacli;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nhn.android.maps.maplib.NGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private MapFragment mapFragment;
    private TextView tv_pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = MapFragment.getInstance(this);
        tv_pos = (TextView)findViewById(R.id.tv_pos);
        Button btn_search_active = (Button)findViewById(R.id.btn_search_active);
        btn_search_active.setTypeface(Typeface.createFromAsset(getAssets(), "YoonGothic740.ttf"));
        btn_search_active.setText("주소 검색");
        btn_search_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.linear_normal).setVisibility(View.GONE);
                findViewById(R.id.linear_search).setVisibility(View.VISIBLE);
            }
        });


        final ImageButton btn_search = (ImageButton) findViewById(R.id.btn_search);
        final EditText edtxt_search = (EditText)findViewById(R.id.edtxt_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("내 주변");
        tv_title.setTypeface(Typeface.createFromAsset(getAssets(), "YoonGothic760.ttf"));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btn_search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("info","search button clicked");
                if(edtxt_search.getText().toString() == null){
                    Toast.makeText(MapActivity.this, "주소를 입력해 주세요." , Toast.LENGTH_SHORT).show();
                    return;
                }

                final NGeoPoint point = mapFragment.SearchMapByAddress(edtxt_search.getText().toString());
                InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtxt_search.getWindowToken(), 0);


                JSONArray jsonArr = null;

                try {
                    jsonArr = new AsyncTask<Void, Void, JSONArray>(){
                        @Override
                        protected JSONArray doInBackground(Void... voids) {
                            JSONArray jsonArr = mapFragment.POST("http://ec2-52-79-164-115.ap-northeast-2.compute.amazonaws.com/connect_client", point.getLongitude(), point.getLatitude());
                            Log.i("info","json array : " + jsonArr.toString());
                            return jsonArr;
                        }
                    }.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                /*//this is for test
                String str = "[{\"longitude\":126.977971,\"latitude\":37.565667,\"location\":\"서울\"}]";
                try {
                    jsonArr = new JSONArray(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                mapFragment.parseJsonArrayIntoMap(jsonArr, null);

                findViewById(R.id.linear_normal).setVisibility(View.VISIBLE);
                findViewById(R.id.linear_search).setVisibility(View.GONE);
            }
        });

        mapFragment.setArguments(new Bundle());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.frame_map, mapFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.i("info", "call Option");

        if(id == R.drawable.ic_back){
            Log.i("info","back");
            finish();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                drawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
            return true;
        }

        return true;
        //return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.i("info", "call item");

        if (id == R.id.nav_star) {
            startActivity(new Intent(this, StarActivity.class));
        } else if (id == R.id.nav_error) {
            startActivity(new Intent(this, ErrorActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(Gravity.RIGHT);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 100:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                }
            }break;
        }
    }

    public void callBottomSheet(String title, final boolean isStar){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "", Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)snackbar.getView();
        //layout.setMinimumHeight((int)getResources().getDimension(R.dimen.snackbar_height));
        layout.removeAllViews();
        layout.setBackgroundColor(Color.WHITE);

        View snackView = LayoutInflater.from(this).inflate(R.layout.view_bottom_sheet, null);
        TextView tv_title = (TextView)snackView.findViewById(R.id.tv_title);
        tv_title.setText(title);
        final ImageButton ib_star = (ImageButton)snackView.findViewById(R.id.ib_star);
        ib_star.setImageResource(isStar ? R.drawable.ic_star : R.drawable.ic_unstar);
        ib_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_star.setImageResource(R.drawable.ic_star);
            }
        });
        layout.addView(snackView);
        snackbar.show();
    }

    public void setCurrentPositionTextView(String pos){
        tv_pos.setText(pos);
    }
}
