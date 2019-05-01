package com.example.mapstest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
    Random rnd = new Random();
    ImageButton buttonMaps;
    Button weatherButton;
    TextView RandomText;
    String[] phrases = new String[]{
      "Running again?", "It's good time for run!",
      "Begin your day with run!", "Here you are, let's run new distance together!",
      "Run Johnny Run!", "Feel yourself as a mazerunner ;)",
      "Just run it!", "What about evening run?"
    };
    final int random = rnd.nextInt(phrases.length);
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        buttonMaps = findViewById(R.id.imageButton1);
        buttonMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!ifLocationEnabled() || !isPermissionGranted()) {

                        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                        else
                            requestLocation();
                        if (!ifLocationEnabled()) {
                            showAlert(1);
                        }
                    }else {
                        Intent buttonIntent = new Intent(MainActivity.this, MapsActivity.class);
                        startActivity(buttonIntent);
                    }
            }
        });

        weatherButton = findViewById(R.id.button2);
        weatherButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent buttonIntent = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(buttonIntent);
                }
        });
        RandomText = findViewById(R.id.Randomtext);
        RandomText.setText(phrases[random]);
    }


    private void showAlert(final int status){
        String message, title, btnText;
        if (status==1){
            message = "Enable NETWORK_PROVIDER please";
            title = "Enable Location";
            btnText = "Location Settings";
        }else{
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title).setMessage(message).setPositiveButton(btnText,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (status==1){
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }else{
                    requestPermissions(PERMISSIONS,PERMISSION_ALL);
                }
            }
        }).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        dialog.show();
    }

    private boolean requestLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        return ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean ifLocationEnabled(){
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isPermissionGranted(){
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(MainActivity.this, "Permission was granted", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(MainActivity.this, "Permission wasn't granted", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
