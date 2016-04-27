package com.triumphant.akash.buskarma_v2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.triumphant.akash.buskarma_v2.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String routeSelection;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      //  setSupportActionBar(toolbar);
        button = (Button) findViewById(R.id.button_selection);
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner_bus);
        // Spinner Drop down elements
        List<String> categories1 = new ArrayList<String>();
        categories1.add("Select the bus");
        categories1.add("Bus # 1");
        categories1.add("Bus # 2");
        categories1.add("Bus # 3");
        categories1.add("Bus # 4");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories1);

        // Drop down layout style - list view with radio button
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner1.setAdapter(dataAdapter1);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                /*if (selectedItem.equals("Comet Cruiser 883 West")) {
                    // do your stuff
                    routeSelection = "WEST";
                } else if (selectedItem.equals("Comet Cruiser 883 East")) {
                    routeSelection = "EAST";
                }*/
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        Spinner spinner = (Spinner) findViewById(R.id.spinner_bus_route);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select the bus route");
        categories.add("Comet Cruiser 883 West");
        categories.add("Comet Cruiser 883 East");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Comet Cruiser 883 West")) {
                    // do your stuff
                    routeSelection = "WEST";

                } else if (selectedItem.equals("Comet Cruiser 883 East")) {
                    routeSelection = "EAST";
                }
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toMapsActivity();
            }
        });

    }

    public void toMapsActivity(){
        Intent intent = new Intent(this.getApplicationContext(), MapsActivity.class).putExtra("selection", routeSelection);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            toSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toSettingsActivity(){
        Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

}
