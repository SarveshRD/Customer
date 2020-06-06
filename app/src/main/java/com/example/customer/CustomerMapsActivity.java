package com.example.customer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CustomerMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

   // private static final String TAG = "CustomerMapsActivity";
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private ProgressBar loadingBar_customer_act;
    String userId = "";
    DatabaseReference ref;
    GeoFire geoFire;
    Button saveData;
    View MapView;
    public static final int PERMISSION_REQUEST_CODE = 9001;
    public final int PLAY_SERVICES_ERROR_CODE = 9002;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;
    public LocationRequest mLocationRequest;
    double lats, longs = 0.0;
    private int radius = 1;
    private Boolean driverFound = false,CameraMoving=false;
    public String driverFoundID="iluhefuihuihrf4";
    public GeoQuery geoQuery;
    private Location mLocation;
    public SupportMapFragment supportMapFragment;
    private long temp_tot_customer_child = 1234;
    DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
    DatabaseReference save_ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders");
    private CameraUpdate cp;
    private CameraUpdate zoom;
    private TextView textview;
    private  EditText search_editText;
    private MyLocationReceiver mLocationReceiver;

    public void createMap() {
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.customer_map_container);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
        MapView = supportMapFragment.getView();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("this", "onCreate called");
        setContentView(R.layout.activity_customer_maps);



        mLocationReceiver = new MyLocationReceiver(this, Snackbar.make(findViewById(android.R.id.content), "Location service is not enabled", Snackbar.LENGTH_INDEFINITE));

        saveData = findViewById(R.id.save_data);
        loadingBar_customer_act = findViewById(R.id.progressBar_cut_act);
        textview =findViewById(R.id.text_view2);
        search_editText = findViewById(R.id.edit_text);

        Places.initialize(getApplicationContext(),"AIzaSyBTIoVSBSmAHYWYPNRtPaiJdKn2ZkrDoNI");
        search_editText.setFocusable(false);
        search_editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List <Place.Field>fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(getApplicationContext());

                startActivityForResult(intent,100);
            }

        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();


        ref = FirebaseDatabase.getInstance().getReference("CustomersAvailable");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        geoFire = new GeoFire(ref);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!checkLocationPermission()) { requestLocationPermission(); }

        else
            {
            get_CurrentLocation();
            mMap.setMyLocationEnabled(true);
            if(MapView != null && MapView.findViewById(Integer.parseInt("1"))!=null)
            {
                View locationbutton =((View) MapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                layoutParams.setMargins(0,0,30,30);
            }
            mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    saveData.setEnabled(false);
                     saveData.setText(R.string.GetLocation);
                }
            });

            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    mMap.clear();
                    saveData.setText(R.string.SaveLocation);
                    saveData.setEnabled(true);
                    LatLng latLng =mMap.getCameraPosition().target;
                    lats = latLng.latitude;
                    longs =latLng.longitude;
                    Geocoder geocoder =new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lats, longs, 1);
/*                       String add = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String zip = addresses.get(0).getPostalCode();
                        textview.setText(add + " " +state +" " + city +" " +zip );*/
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


                    saveData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                getClosestDriver();
                        }
                    });
                }

    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mLocationReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mLocationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("this", "onConnected called");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(120 * 1000);
        mLocationRequest.setFastestInterval(60 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {

                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        initGoogleMap();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(CustomerMapsActivity.this,
                                    REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("this", "onRequestPermissionsResult called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"Granted",Toast.LENGTH_LONG).show();
            initGoogleMap();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Failed to initialize map",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("onActivityResult()", Integer.toString(resultCode));
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        //one
        switch (requestCode)
        {

            case REQUEST_LOCATION:
                //two
                switch (resultCode) {

                    case Activity.RESULT_OK: {
                        Toast.makeText(CustomerMapsActivity.this, "Location  enabled by user ."
                                , Toast.LENGTH_LONG).show();
                        initGoogleMap();
                    }
                    break;

                    case Activity.RESULT_CANCELED:
                        {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(CustomerMapsActivity.this, "Location not enabl., user cancelled."
                                , Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }

                }
                //two

                break;

                //three
            case 100 :
                switch (requestCode)
                {

                    case RESULT_OK: {
                       Place place =Autocomplete.getPlaceFromIntent(data);

                        search_editText.setText(place.getAddress());

                       textview.setText(String.format("Locality  :%s ", place.getName()));
                        break;
                    }


                    case AutocompleteActivity.RESULT_ERROR:
                    {
                        // The user was asked to change settings, but chose not to
                        Status status =Autocomplete.getStatusFromIntent(data);

                        Toast.makeText(CustomerMapsActivity.this, status.getStatusMessage()
                                , Toast.LENGTH_LONG).show();
                        break;
                    }

                    default: {
                        break;
                    }

                }
                break;
                //three
        }
        //one
         }

    private void initGoogleMap() {
        if (isServicesOk()) {
            if (checkLocationPermission()) {
                Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show();
                createMap();
            } else {
                requestLocationPermission();
            }
        }
    }


    private boolean checkLocationPermission() {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

        int result = googleApi.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, new DialogInterface
                    .OnCancelListener() {
                @Override
                public void onCancel(DialogInterface task) {
                    Toast.makeText(CustomerMapsActivity.this, "Dialog is cancelled by User", Toast.LENGTH_SHORT)
                            .show();
                }
            });

            dialog.show();
        } else {
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private void requestLocationPermission() {
        if (!checkLocationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
                        .ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void  getClosestDriver()
    {

        loadingBar_customer_act.setVisibility(View.VISIBLE);
        search_editText.setEnabled(false);
        textview.setEnabled(false);
        saveData.setEnabled(false);

        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(lats, longs), radius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                temp_tot_customer_child = 0;
                if (!driverFound) {
                    driverFound = true;
                    driverFoundID = key;
                    save_ref.child(driverFoundID).child("myCustomers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot datas : dataSnapshot.getChildren()) {
                                temp_tot_customer_child = temp_tot_customer_child + 1;
                            }

                            if (temp_tot_customer_child >= 10) {
                                driverFound = (!driverFound);
                                driverFoundID = null;
                            }
                            else
                                {
                                    SharedPreferences sharedPreferences =getSharedPreferences("SharedPrefs",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("temp_tot_customer_child",String.valueOf(temp_tot_customer_child+1));
                                    editor.putString("lats",String.valueOf(lats));
                                    editor.putString("longs",String.valueOf(longs));
                                    editor.putString("driverFoundID",driverFoundID);
                                    editor.apply();
                                    loadingBar_customer_act.setVisibility(View.GONE);
                                    startActivity(new Intent(getApplicationContext(),Plan1.class));
                                    finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void get_CurrentLocation() {
        final LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                mMap.clear();
                LatLng lt = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                cp = CameraUpdateFactory.newLatLng(lt);
                zoom = CameraUpdateFactory.zoomTo(11);
                mMap.moveCamera(cp);
                mMap.animateCamera(zoom);
                lats = location.getLatitude();
                longs = location.getLongitude();
                Geocoder geocoder =new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                    String add = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String zip = addresses.get(0).getPostalCode();
                    textview.setText(String.format("%s %s %s %s",add,city,state,zip));
                }
                   catch (IOException e) {
                    e.printStackTrace();
                }

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
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = null;

        if (checkLocationPermission()) {
            locationManager.requestSingleUpdate(criteria, locationListener, looper);
        }
    }

}