package com.example.localisationtempsreel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // ATTENTION : Remplacez par l'IP exacte de votre serveur hôte XAMPP
    private final String insertUrl = "http://192.168.1.50/localisation/createPosition.php";
    private final String showUrl = "http://192.168.1.50/localisation/showPositions.php";

    private GoogleMap googleMapInstance;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private RequestQueue requestQueue;

    private TextView tvStatus;
    private Button btnFetchPositions;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des composants graphiques
        tvStatus = findViewById(R.id.tvStatus);
        btnFetchPositions = findViewById(R.id.btnFetchPositions);

        // Initialisation de la file d'attente des requêtes HTTP Volley
        requestQueue = Volley.newRequestQueue(this);

        // Récupération d'un identifiant unique matériel robuste (Alternative à l'IMEI obsolète sur les versions d'Android récentes)
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialisation du client de géolocalisation de Google Play Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Chargement asynchrone du Fragment de la carte Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Action de rafraîchissement manuel des marqueurs
        btnFetchPositions.setOnClickListener(v -> loadPositionsFromServer());

        // Configuration du tracking en continu (Fréquence : toutes les 10 secondes)
        configureLocationUpdates();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMapInstance = googleMap;

        // Paramétrage UI de base de la Map pour un rendu professionnel
        this.googleMapInstance.getUiSettings().setZoomControlsEnabled(true);
        this.googleMapInstance.getUiSettings().setCompassEnabled(true);

        // Une fois la carte prête, vérifier les permissions pour démarrer le cycle complet
        checkLocationPermissions();
    }

    /**
     * Configuration des paramètres de l'écouteur GPS en temps réel.
     */
    private void configureLocationUpdates() {
        // Paramétrage haute précision et rafraîchissement régulier
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        // Callback déclenché à chaque changement de coordonnée GPS capté par le téléphone
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateUIData(location);
                        sendPositionToServer(location);
                    }
                }
            }
        };
    }

    /**
     * Met à jour les informations textuelles et recentre la caméra sur l'appareil.
     */
    private void updateUIData(Location location) {
        String info = String.format(Locale.FRANCE, "Lat: %.5f | Lon: %.5f", location.getLatitude(), location.getLongitude());
        tvStatus.setText("Statut : Connecté | " + info);

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Déplace en douceur la caméra vers la position actuelle de l'utilisateur
        googleMapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
    }

    /**
     * Envoie de manière sécurisée et asynchrone la coordonnée vers le backend PHP en méthode POST.
     */
    private void sendPositionToServer(Location location) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, insertUrl,
                response -> Log.d(TAG, "Position synchronisée avec succès: " + response),
                error -> Log.e(TAG, "Erreur d'insertion réseau: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(location.getLatitude()));
                params.put("longitude", String.valueOf(location.getLongitude()));
                params.put("date", currentDate);
                params.put("imei", deviceId);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Récupère l'ensemble des positions de la base de données via showPositions.php et dessine les marqueurs.
     */
    private void loadPositionsFromServer() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, showUrl, null,
                response -> {
                    try {
                        // Nettoyer la carte avant de ré-afficher pour éviter les doublons de marqueurs graphiques
                        googleMapInstance.clear();

                        JSONArray positionsArray = response.getJSONArray("positions");
                        for (int i = 0; i < positionsArray.length(); i++) {
                            JSONObject posObj = positionsArray.getJSONObject(i);

                            double lat = posObj.getDouble("latitude");
                            double lon = posObj.getDouble("longitude");
                            String dateStr = posObj.getString("date");
                            String device = posObj.getString("imei");

                            LatLng point = new LatLng(lat, lon);
                            googleMapInstance.addMarker(new MarkerOptions()
                                    .position(point)
                                    .title("Appareil: " + device)
                                    .snippet("Date: " + dateStr));
                        }
                        Toast.makeText(MainActivity.this, "Marqueurs mis à jour (" + positionsArray.length() + ")", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur lors du traitement JSON: ", e);
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Erreur de chargement des points", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Gestion centralisée et moderne des permissions d'exécution pour Android 6.0 à Android 14+.
     */
    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions déjà accordées, on lance le tracking
            startLocationTracking();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationTracking() {
        googleMapInstance.setMyLocationEnabled(true); // Affiche le point bleu natif de Google sur la carte
        fusedLocationClient.requestLocationUpdates(
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build(),
                locationCallback,
                Looper.getMainLooper()
        );
        // Premier chargement automatique des marqueurs historiques au démarrage
        loadPositionsFromServer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            } else {
                Toast.makeText(this, "Permission GPS requise pour le fonctionnement du LAB", Toast.LENGTH_LONG).show();
                tvStatus.setText("Statut : Autorisation GPS refusée.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Bonne pratique de gestion de batterie : suspendre le tracking quand l'app passe en tâche de fond
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}