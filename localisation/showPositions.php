<?php
header('Content-Type: application/json; charset=utf-8');

// L'application Android utilise POST dans le code Java fourni
if ($_SERVER["REQUEST_METHOD"] == "POST" || $_SERVER["REQUEST_METHOD"] == "GET") {
    include_once __DIR__ . '/service/PositionService.php';
    
    try {
        $service = new PositionService();
        $positions = $service->getAll();
        
        echo json_encode(["positions" => $positions]);
    } catch(Exception $e) {
        http_response_code(500);
        echo json_encode(["error" => "Erreur serveur : " . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(["error" => "Méthode non autorisée"]);
}