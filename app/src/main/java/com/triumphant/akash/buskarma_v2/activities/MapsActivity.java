package com.triumphant.akash.buskarma_v2.activities;

import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Handler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.triumphant.akash.buskarma_v2.R;
import com.triumphant.akash.buskarma_v2.threads.ClientSocketReader;
import com.triumphant.akash.buskarma_v2.utilities.JSONParser;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
//sample comment
//    public String SERVER_IP = "54.174.186.244";
//    public int SERVER_PORT = 6970;
    public Marker busMarker;
    public String SERVER_IP = "192.168.0.20";
    public int SERVER_PORT = 7070;
    private GoogleMap mMap;
    private LatLng you;
    Marker m;
    MarkerOptions a;
    private final String LOG_TAG = "BusKarma";
    public Handler mHandler;
    public BufferedReader inFromServer;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng UTD = new LatLng(32.9843468, -96.7481245);
    private LatLng mcCallum = new LatLng(32.98797679, -96.77084923);
    private LatLng bush = new LatLng(33.003215, -96.703908);
    private volatile ArrayList<Polyline> polyLines;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        polyLines = new ArrayList<Polyline>();
        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner_bus_route);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(selectedItem.equals("Comet Cruiser 883 West"))
                {
                    // do your stuff
                    Toast.makeText(getApplicationContext(), "883 West selected", Toast.LENGTH_SHORT).show();
                    String url1 = makeURL(UTD.latitude, UTD.longitude, mcCallum.latitude, mcCallum.longitude);
                    String url2 = makeURL(mcCallum.latitude, mcCallum.longitude,UTD.latitude, UTD.longitude);
                    ArrayList<String> arrayList = new ArrayList<String>();
                    arrayList.add(url1);
                    arrayList.add(url2);
                    new connectAsyncTask(arrayList, "#ffa500").execute();

                }
                else if(selectedItem.equals("Comet Cruiser 883 East")){
                    Toast.makeText(getApplicationContext(), "883 East selected", Toast.LENGTH_SHORT).show();

                    String url1 = makeURL(UTD.latitude, UTD.longitude, bush.latitude, bush.longitude);
                    String url2 = makeURL(bush.latitude, bush.longitude,UTD.latitude, UTD.longitude);
                    ArrayList<String> arrayList = new ArrayList<String>();
                    arrayList.add(url1);
                    arrayList.add(url2);
                    new connectAsyncTask(arrayList, "#00B200").execute();
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        // Spinner click listener
      //  spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Comet Cruiser 883 West");
        categories.add("Comet Cruiser 883 East");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        String str = msg.getData().getString("msg");
                        if(str == null){
                            //Reader thread sent null to main thread means Server sent readerThread NUll
                            //means server went down
                            Toast.makeText(getApplicationContext(), "server went down \nrelaunch the aplication and try again", Toast.LENGTH_LONG).show();
                        }
                        String[] ll = str.split(",");
                        busMarker.setPosition(new LatLng(Double.parseDouble(ll[0]), Double.parseDouble(ll[1])));
                        Log.i(LOG_TAG, "*****************GOT THIS DATA FROM SERVER*****************"+str);

                    }
                };
            }
        });

        ManagerThread managerThread = new ManagerThread();
        managerThread.start();
//        try {
//            managerThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Connecting my GoogleApiClient to Location Services
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        //Disconnect the client
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng UTD = new LatLng(32.9843468, -96.7481245);
        you = new LatLng(32.9843468, -96.7481248);
        MarkerOptions bus = new MarkerOptions().position(UTD).title("Comet Cruiser").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        busMarker = mMap.addMarker(bus);

        a = new MarkerOptions().position(you).title("you").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_user));
        m = mMap.addMarker(a);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UTD, 13.0f));
        //mMap.
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection is suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
//Log.i(LOG_TAG, location.toString());
        String[] strings = location.toString().split(" ");
        String[] strings1 = strings[1].split(",");
        you = new LatLng(Double.parseDouble(strings1[0]), Double.parseDouble(strings1[1]));
        // Log.i(LOG_TAG, you.toString());
        if (m != null) {
            m.setPosition(you);
        }

//        if (!firstLocationFlag) {
//            String url = makeURL(32.9843468, -96.7481245, Double.parseDouble(strings1[0]), Double.parseDouble(strings1[1]));
//            firstLocationFlag = true;
//            new connectAsyncTask(url).execute();
//
//        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            // return true;
//            toSettings();
//        }
//
        return super.onOptionsItemSelected(item);
    }

//    public void toSettings() {
//        Intent intent = new Intent(getApplicationContext(), Settings.class);
//        startActivity(intent);
//    }


    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyCvPLLdtgZuDInzc-oeSznZEOe8InHq3ao");
        return urlString.toString();
    }


    private class connectAsyncTask extends AsyncTask<Void, Void, ArrayList<String>> {
        private ProgressDialog progressDialog;
        ArrayList<String> urls;
        String color;

        connectAsyncTask(ArrayList<String> urlPass, String clr) {
            urls = urlPass;
            color = clr;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        //the only method that runs in the background thread, (onPreExecute() and onPostExecute() runs in the main thread)
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> jsons = new ArrayList<String>();
            JSONParser jParser = new JSONParser();
            for(String url: urls) {
                String json = jParser.getJSONFromUrl(url);
                jsons.add(json);
            }
            return jsons;
        }

        //I can update the UI in this method based on some input I came up after doInbackground method(), because it's executed in main thread
        @Override
        protected void onPostExecute(ArrayList<String> results) {
            super.onPostExecute(results);
            progressDialog.hide();
            if (results != null) {
                drawPath(results, color);
            }
        }

        public void drawPath(ArrayList<String>  results, String color) {

            //first remove all the polylines from the map
            synchronized (this) {
                for (Polyline line : polyLines) {
                    line.remove();
                }

                polyLines.clear();
            }
            try {
                //Tranform the string into a json object
                for(String result: results) {
                    JSONObject json = new JSONObject(result);
                    JSONArray routeArray = json.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    String encodedString = overviewPolylines.getString("points");
                    List<LatLng> list = decodePoly(encodedString);
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                    .addAll(list)
                                    .width(12)
                                    .color(Color.parseColor(color))//Google maps blue color
                                    .geodesic(true)
                    );
                    polyLines.add(line);
                }
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
            }
            catch (JSONException e) {

            }
        }

    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    public class ManagerThread extends Thread{

        public void run() {
            Looper.prepare();
            Socket clientSocket = null;
            try {
                clientSocket = new Socket(SERVER_IP, SERVER_PORT);
               DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes("client\n");
               // ClientSocketWriter socketWriteThread = new ClientSocketWriter(outToServer);
              //  socketWriteThread.start();

                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ClientSocketReader socketReadThread = new ClientSocketReader(inFromServer, mHandler, getApplicationContext());
                socketReadThread.start();

               // socketWriteThread.join();
                socketReadThread.join();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "server is not up", Toast.LENGTH_LONG).show();
            }
        }
    }

        public void toDetailsActivity() {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

    }