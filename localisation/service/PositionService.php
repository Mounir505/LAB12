<?php
include_once __DIR__ . '/../dao/IDao.php';
include_once __DIR__ . '/../classe/Position.php';
include_once __DIR__ . '/../connexion/Connexion.php';

class PositionService implements IDao {
    private $connexion;

    public function __construct() {
        $db = new Connexion();
        $this->connexion = $db->getConnexion();
    }

    public function create($position) {
        $sql = "INSERT INTO position(latitude, longitude, date, imei) VALUES (?, ?, ?, ?)";
        $stmt = $this->connexion->prepare($sql);
        
        return $stmt->execute([
            $position->getLatitude(),
            $position->getLongitude(),
            $position->getDate(),
            $position->getImei()
        ]);
    }

    public function getAll() {
        $sql = "SELECT * FROM position ORDER BY date DESC"; // Récupère du plus récent au plus ancien
        $stmt = $this->connexion->prepare($sql);
        $stmt->execute();
        return $stmt->fetchAll();
    }

    // Méthodes non utilisées dans ce TP, mais requises par l'interface IDao
    public function update($obj) { return false; }
    public function delete($obj) { return false; }
    public function getById($id) { return null; }
}