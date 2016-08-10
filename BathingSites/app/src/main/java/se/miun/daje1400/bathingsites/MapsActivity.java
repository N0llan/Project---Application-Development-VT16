package se.miun.daje1400.bathingsites;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private LocationManager locationManager;
    private GoogleMap mMap;
    private final static int MY_PERMISSION_FINE_LOCATION = 123;
    private Database database;
    private Location currentLocation;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        database = new Database(getApplicationContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        setTrackFollow();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mapFragment.getMapAsync(this);

        //Listener to redraw bathingsites in the new radius
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch(key){
                    case "Show bathsites in radius (km)": mapChange();
                        break;
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void setTrackFollow(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //Sätter att den ska följa efter användaren
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 100, this); //Varje 10sekunder, och 100m
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.removeUpdates(this);
        }
    }

    //Override onResume for redraw when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        setTrackFollow();
        if (mMap != null){
            mapChange();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings: getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).addToBackStack(null).commit();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
    //Move camera when user moves. Also calls redraw function
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mapChange();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,10);
        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Checks if app has the correct permissions
    public void checkGPSpermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        } else { //Kollar permission till API 23. Dock så finns inte fullt stöd för API 23 i denna app
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_FINE_LOCATION);
        }

    }


    //Reads bathingsites from database. Adds a marker for each bathingsite in radius.
    public void readData(){
        database.open();
        List<String[]> bathSites = database.getAllBathingSites();
        database.close();
        for (int i = 0; i<bathSites.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            if (bathSites.get(i)[4] != null && bathSites.get(i)[3] != null && !bathSites.get(i)[3].equals("") && !bathSites.get(i)[4].equals("")){
                markerOptions.position(new LatLng(Double.parseDouble(bathSites.get(i)[4]),Double.parseDouble(bathSites.get(i)[3])));
                if (inRadius(markerOptions)){
                    String markerInfo = new String();
                    if (bathSites.get(i)[1] != null&&!bathSites.get(i)[1].equals("")){
                        markerInfo += bathSites.get(i)[1];
                    }
                    if (bathSites.get(i)[2] != null&&!bathSites.get(i)[2].equals("")){
                        markerInfo += "\n"+ getResources().getString(R.string.address)+": " + bathSites.get(i)[2];
                    }
                    if (bathSites.get(i)[5] != null&&!bathSites.get(i)[5].equals("")){
                        markerInfo += "\n"+getResources().getString(R.string.score) +": " + bathSites.get(i)[5] + "/5";
                    }
                    if (bathSites.get(i)[6] != null&&!bathSites.get(i)[6].equals("")){
                        markerInfo += "\n" + getResources().getString(R.string.with_temp)+ ": " + bathSites.get(i)[6]+ MainActivity.degreesSymbol;
                    }
                    if (bathSites.get(i)[7] != null&&!bathSites.get(i)[7].equals("")){
                        markerInfo += " " +getResources().getString(R.string.on_date)+ " " + bathSites.get(i)[7];
                    }
                    mMap.addMarker(markerOptions.title(bathSites.get(i)[0]).snippet(markerInfo)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.badplatsskylt));
                }
            }
        }
    }
    //Checks if bathingsite is in radius from user
    public boolean inRadius(MarkerOptions markerOptions){
        float[] dist = new float [2];
        float radius = Float.parseFloat(sharedPreferences.getString(getResources().getString(R.string.radius_distance),getResources().getString(R.string.radius_default_value)));
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (currentLocation != null){
            Location.distanceBetween(markerOptions.getPosition().latitude,markerOptions.getPosition().longitude, currentLocation.getLatitude(),currentLocation.getLongitude(),dist);
            if (dist[0] > radius*1000){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //called when map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null){
            mapChange();
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                CameraPosition.Builder position = new CameraPosition.Builder();
                position.target(new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()));
                position.zoom(11);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position.build()));
            } else {
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {     //zoomar in på Sverige
                        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(55.00199,11.10694),new LatLng(69.063141,24.16707));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,10));
                        mMap.setOnCameraChangeListener(null);              //Gör så att den inte zoomar in mer än en gång
                    }
                });
            }
        }
    }
    //Handles calls to redraw functions and permissioncheck.
    public void mapChange(){
        mMap.clear();
        setInfoAdapter();
        checkGPSpermission();   //Kollar GPS permission
        readData();             //Läser in data och fixar markers
    }

    public void setInfoAdapter(){
        //överlagring för att kunna skriva flera rader i marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getBaseContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getBaseContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getBaseContext());
                snippet.setTextColor(Color.BLUE);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });
    }
}
