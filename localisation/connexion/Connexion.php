<?php
class Connexion {
    private $connexion;

    public function __construct() {
        $host = 'localhost';
        $dbname = 'localisation';
        $login = 'root';
        $password = ''; // Laissez vide si XAMPP/WAMP par défaut

        try {
            $dsn = "mysql:host=$host;dbname=$dbname;charset=utf8mb4";
            $this->connexion = new PDO($dsn, $login, $password, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false // Sécurité supplémentaire contre l'injection SQL
            ]);
        } catch (PDOException $e) {
            // En production, on log l'erreur. Ici on l'affiche pour le debug.
            die(json_encode(["error" => "Erreur de connexion à la base : " . $e->getMessage()]));
        }
    }

    function getConnexion() {
        return $this->connexion;
    }
}
