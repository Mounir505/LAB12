<?php
header('Content-Type: application/json; charset=utf-8');

// Vérification stricte de la méthode POST
if($_SERVER["REQUEST_METHOD"] != "POST"){
    http_response_code(405);
    echo json_encode(["ok" => false, "error" => "Méthode POST requise"]);
    exit;
}

include_once __DIR__ . '/service/PositionService.php';
include_once __DIR__ . '/classe/Position.php';

// Récupération des données envoyées par Volley
$latitude = isset($_POST['latitude']) ? $_POST['latitude'] : null;
$longitude = isset($_POST['longitude']) ? $_POST['longitude'] : null;
$date = isset($_POST['date']) ? $_POST['date'] : null;
$imei = isset($_POST['imei']) ? $_POST['imei'] : null;

$ip = $_SERVER['REMOTE_ADDR']; // Récupère l'IP du téléphone pour info

// Validation des données
if ($latitude === null || $longitude === null || $date === null || $imei === null) {
    http_response_code(400);
    echo json_encode(["ok" => false, "error" => "Paramètres manquants", "ip" => $ip]);
    exit;
}

try {
    $service = new PositionService();
    $position = new Position(null, $latitude, $longitude, $date, $imei);
    $service->create($position);
    
    echo json_encode(["ok" => true, "message" => "Position insérée", "ip" => $ip]);
} catch(Exception $e) {
    http_response_code(500);
    echo json_encode(["ok" => false, "error" => $e->getMessage(), "ip" => $ip]);
}