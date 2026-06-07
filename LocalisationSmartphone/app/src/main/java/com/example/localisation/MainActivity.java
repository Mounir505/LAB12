package com.example.localisation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvInfo;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private String deviceId;

    // Remplacer impérativement par votre adresse IP locale calculée au début !
    private static final String INSERT_URL = "http://192.168.1.50/localisation/createPosition.php";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tvInfo);
        requestQueue = Volley.newRequestQueue(this);

        // Identifiant unique de remplacement pour l'IMEI sécurisé
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Demander explicitement les permissions à l'utilisateur au runtime
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permissions déjà accordées, on démarre le suivi GPS
            startTrackingLocation();
        }
    }

    private void startTrackingLocation() {
        try {
            // Mise à jour toutes les 60 secondes OU si déplacement de 150 mètres
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    60000,
                    150,
                    locationListener
            );
        } catch (SecurityException e) {
            tvInfo.setText("Erreur d'autorisation d'accès au GPS");
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            String displayMsg = String.format(Locale.FRANCE,
                    getString(R.string.new_location),
                    String.valueOf(latitude),
                    String.valueOf(longitude));

            tvInfo.setText(displayMsg);

            // Appel de la méthode d'envoi réseau
            addPositionToServer(latitude, longitude);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, String.format(getString(R.string.provider_enabled), provider), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, String.format(getString(R.string.provider_disabled), provider), Toast.LENGTH_SHORT).show();
        }
    };

    private void addPositionToServer(final double lat, final double lon) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                INSERT_URL,
                response -> {
                    try {
                        // Traitement de la réponse JSON Premium renvoyée par le serveur
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Réponse serveur enregistrée", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Échec de connexion au serveur distant", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDate = sdf.format(new Date());

                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date_position", currentDate);
                params.put("imei", deviceId); // Envoi de l'identifiant unique simulé/sécurisé

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingLocation();
            } else {
                tvInfo.setText("Permission GPS refusée. L'application ne peut pas fonctionner.");
            }
        }
    }
}