html\_content = """  README - Laboratoire de Localisation en Temps Réel

Documentation du Laboratoire : Application de Localisation en Temps Réel
========================================================================

**Projet :** LocalisationTempsReel

**Package global :** `com.example.localisationtempsreel`

**Environnement cible :** Android Studio (Gradle build system)

* * *

1\. Présentation Générale et Objectifs du Laboratoire
-----------------------------------------------------

Ce laboratoire a pour but de concevoir et d'implémenter une application Android native capable de suivre la position géographique d'un utilisateur en temps réel, de l'afficher de manière interactive sur une carte Google Maps, et d'assurer une communication réseau bidirectionnelle pour la centralisation des coordonnées de localisation.

L'application s'articule autour de trois axes techniques majeurs :

*   **La géolocalisation matérielle :** Exploitation des capteurs GPS et des réseaux cellulaires/Wi-Fi via les services Google Play pour obtenir une précision métrique.
*   **L'affichage cartographique :** Intégration du SDK Google Maps pour restituer visuellement les déplacements sous forme de marqueurs dynamiques.
*   **La synchronisation réseau :** Utilisation de la bibliothèque HTTP Volley pour acheminer les coordonnées vers un serveur distant et récupérer les positions des autres terminaux connectés.

* * *

2\. Architecture Logique et Structure des Fichiers
--------------------------------------------------

Le projet respecte l'architecture standard recommandée pour le développement de projets Android modernes de petite à moyenne envergure. Voici le détail de l'arborescence et le rôle de chaque composant :

### 2.1 Architecture du Code Source (Java/Kotlin)

*   `MainActivity` : Point d'entrée principal de l'application. Elle gère le cycle de vie de l'application, l'initialisation des composants de carte (Maps API), les demandes d'autorisation d'accès aux capteurs à l'exécution (Runtime Permissions), ainsi que les boucles de mise à jour de la position.

### 2.2 Architecture des Ressources Apprenties (Res)

*   `/layout/activity_main.xml` : Fichier de structure d'interface utilisateur (UI). Il utilise un conteneur racine de type `ConstraintLayout` pour garantir des performances d'affichage optimales et un agencement fluide sur différents formats d'écrans. Il englobe un en-tête d'informations, la vue fragmentée dédiée à la carte (`SupportMapFragment`), et une zone d'action basse pour interagir avec le serveur.

* * *

3\. Configuration des Dépendances et Écosystème Logiciel
--------------------------------------------------------

L'application s'appuie sur plusieurs bibliothèques externes définies au sein du gestionnaire de dépendances Gradle (`build.gradle.kts :app`). Le tableau ci-dessous dresse l'inventaire exhaustif des modules intégrés :

Groupe & Artefact

Version Recommandée

Rôle Technique et Utilité

`androidx.constraintlayout:constraintlayout`

2.1.4

Permet le positionnement relatif et flexible des éléments de l'UI sans imbrications complexes de layouts.

`com.google.android.gms:play-services-maps`

18.2.0

Fournit les fragments et utilitaires requis pour instancier, afficher et contrôler des cartes interactives Google Maps.

`com.google.android.gms:play-services-location`

21.2.0

API du fournisseur d'emplacement fusionné (Fused Location Provider API) pour acquérir les coordonnées avec optimisation de la batterie.

`com.android.volley:volley`

1.2.1

Gestionnaire de requêtes HTTP asynchrones. Utilisé pour les opérations de type POST/GET vers les API Web du backend.

`androidx.appcompat:appcompat`

1.6.1

Assure la rétrocompatibilité des fonctionnalités récentes d'Android sur les anciennes versions du système d'exploitation.

`com.google.android.material:material`

1.11.0

Fournit les composants graphiques respectant les directives de design de Material 3 (Boutons surélevés, cartes, etc.).

* * *

4\. Guide d'Installation, de Configuration et de Déploiement
------------------------------------------------------------

### 4.1 Génération et Intégration de la Clé API Google Maps

Pour faire fonctionner la carte, une clé API valide issue de la Google Cloud Console est impérative :

1.  Rendez-vous sur la console Google Cloud et créez ou sélectionnez un projet existant.
2.  Activez l'API nommée **"Maps SDK for Android"**.
3.  Accédez à la section "Identifiants", puis générez une clé d'API (API Key). Restreignez-la si nécessaire à votre application via l'empreinte SHA-1 de débogage de votre machine.
4.  Ouvrez le fichier `AndroidManifest.xml` de votre projet Android et insérez la balise suivante à l'intérieur du bloc `<application>` :

<meta-data
    android:name="com.google.android.geo.API\_KEY"
    android:value="VOTRE\_CLE\_API\_ICI" />
    

### 4.2 Configuration des Permissions d'Accès Système

Le suivi en temps réel exige des autorisations strictes qui doivent être déclarées au préalable dans le fichier `AndroidManifest.xml` :

<!-- Autorisation d'accès à la position ultra-précise (GPS) -->
<uses-permission android:name="android.permission.ACCESS\_FINE\_LOCATION" />
<!-- Autorisation d'accès à la position approximative (Réseau) -->
<uses-permission android:name="android.permission.ACCESS\_COARSE\_LOCATION" />
<!-- Autorisation d'accès au réseau Internet pour charger les cartes et interroger les API -->
<uses-permission android:name="android.permission.INTERNET" />
    

_Note importante :_ Depuis Android 6.0 (API Level 23), les permissions de localisation doivent également faire l'objet d'une demande dynamique à l'écran via du code Kotlin/Java pendant l'exécution de l'application.

* * *

5\. LIVRABLE REQUIS : Vidéo de Démonstration (Vidéo-Démo)
---------------------------------------------------------

Dans le cadre de l'évaluation de ce laboratoire, chaque étudiant ou équipe doit impérativement soumettre une vidéo explicative et démonstrative. Cette vidéo fait office de preuve de fonctionnement autonome du projet.

### 5.1 Plan de Présentation Obligatoire (Structure de la Vidéo)

La vidéo doit rigoureusement suivre le plan de démonstration suivant :

1.  **Introduction et Identification :**  
    Présentation orale de votre identité (Nom, Prénom, Numéro d'étudiant) face caméra ou par introduction vocale distincte. Rappel rapide de l'intitulé du lab.
2.  **Parcours et Explication du Code Source (1 minute 30 secondes) :**  
    Montrez brièvement votre structure de code dans Android Studio. Expliquez comment la classe `MainActivity` s'abonne aux mises à jour de position du `FusedLocationProviderClient`, comment les permissions sont gérées, et comment la requête `Volley` structure l'envoi des coordonnées GPS.
3.  **Démonstration Runtime sur Émulateur ou Appareil Réel (2 minutes) :**  
    Exécutez l'application en direct. Montrez les étapes suivantes :
    *   Lancement de l'application et apparition de la boîte de dialogue demandant l'autorisation d'accès à la localisation (si première exécution).
    *   Chargement réussi des fonds de carte de Google Maps.
    *   Génération d'un changement de coordonnées (via les options de simulation de trajectoire de l'émulateur Android ou en marchant si testé sur appareil physique).
    *   Mise à jour automatique de la caméra de la carte centrée sur le nouveau marqueur utilisateur.
    *   Clic sur le bouton "Rafraîchir les positions" avec démonstration visuelle ou via les logs de la console (Logcat) prouvant qu'une requête réseau Volley a bien été émise et reçue par le serveur.



https://github.com/user-attachments/assets/a3325baf-1d25-4e98-acb8-454236a0a03b


