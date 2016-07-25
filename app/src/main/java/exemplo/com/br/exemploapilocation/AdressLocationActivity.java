package exemplo.com.br.exemploapilocation;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;
import exemplo.com.br.exemploapilocation.domain.MessaageEB;
import exemplo.com.br.exemploapilocation.service.LocationIntentService;

/**
 * Created by ROGERIO on 24/07/2016.
 */
public class AdressLocationActivity extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String LOCATION = "location";
    public static final String TYPE = "type";
    public static final String ADDRESS = "address";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private EditText etAddress;
    private TextView tvAddressLocation;
    private Button btNameToCoord;
    private Button btCoordToName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_location);

        EventBus.getDefault().register(this);

        etAddress = (EditText) findViewById(R.id.et_address);
        tvAddressLocation = (TextView) findViewById(R.id.tv_address_location);
        btNameToCoord = (Button) findViewById(R.id.bt_name_to_coord);
        btCoordToName = (Button) findViewById(R.id.bt_coord_to_name);

        callConnection();
    }


    private synchronized void callConnection(){
        Log.i("LOG", "AddressLocationActivity.callConnection()");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public void callIntentService(int type, String address){
        Intent it = new Intent(this, LocationIntentService.class);
        it.putExtra(TYPE, type);
        it.putExtra(ADDRESS, address);
        it.putExtra(LOCATION, mLastLocation);
        startService(it);
    }

    // LISTERNERS
    @Override
    public void onConnected(Bundle bundle) {

        Log.i("LOG", "AddressLocationActivity.onConnected(" + bundle + ")");

        Location l = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if(l != null){
            mLastLocation = l;
            btNameToCoord.setEnabled(true);
            btCoordToName.setEnabled(true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i("LOG", "AddressLocationActivity.onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("LOG", "AddressLocationActivity.onConnectionFailed(" + connectionResult + ")");
    }



    public void getLocationListener(View view){
        int type;
        String address = null;

        if(view.getId() == R.id.bt_name_to_coord){
            type = 1;
            address = etAddress.getText().toString();
        }
        else{
            type = 2;
        }

        callIntentService(type, address);
    }



    public  void OnEvent(final MessaageEB m){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("LOG", m.getResultMessage());
                tvAddressLocation.setText("Data: "+m.getResultMessage());
            }
        });
    }


    /**
     * Created by ROGERIO on 24/07/2016.
     */
    public static class UpdateLocationActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,LocationListener {

        private GoogleApiClient mGoogleApiClient;
        private LocationRequest mLocationRequest;
        private TextView tvUpdateLocation;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_update_location);
            Log.i("LOG", "UpdateLocationActivity.onCreate()");

            tvUpdateLocation = (TextView) findViewById(R.id.tv_update_location);

            callConnection();
        }



        @Override
        public void onResume(){
            super.onResume();

            if(mGoogleApiClient !=null && mGoogleApiClient.isConnected()){
                startLocationUpdate();
            }
        }

        @Override
        public void onPause(){
            super.onPause();

            if(mGoogleApiClient != null){
                stopLocationUpdate();
            }
        }

        private synchronized void callConnection(){
            Log.i("LOG", "UpdateLocationActivity.callConnection()");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

        private void initLocationRequest(){
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setFastestInterval(2000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        private void startLocationUpdate(){
            initLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        private void stopLocationUpdate(){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }


        @Override
        public void onConnected(Bundle bundle)
        {

            Log.i("LOG", "UpdateLocationActivity.onConnected(" + bundle + ")");

            Location l = LocationServices
                    .FusedLocationApi
                    .getLastLocation(mGoogleApiClient); // PARA JÁ TER UMA COORDENADA PARA O UPDATE FEATURE UTILIZAR

            startLocationUpdate();

        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i("LOG", "UpdateLocationActivity.onConnectionSuspended(" + i + ")");

        }

        @Override
        public void onLocationChanged(Location location) {

            tvUpdateLocation.setText(Html.fromHtml("Location: " + location.getLatitude() + "<br />" +
                    "Longitude: " + location.getLongitude() + "<br />" +
                    "Bearing: " + location.getBearing() + "<br />" +
                    "Altitude: " + location.getAltitude() + "<br />" +
                    "Speed: " + location.getSpeed() + "<br />" +
                    "Provider: " + location.getProvider() + "<br />" +
                    "Accuracy: " + location.getAccuracy() + "<br />" +
                    "Speed: " + DateFormat.getTimeInstance().format(new Date()) + "<br />"));
        }



        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.i("LOG", "UpdateLocationActivity.onConnectionFailed(" + connectionResult + ")");

        }


     }
}
