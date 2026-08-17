# Citizens Report SN 🇸🇳

**Citizens Report SN** est une application mobile citoyenne innovante conçue pour renforcer la collaboration entre les habitants et les autorités locales au Sénégal. Elle permet aux citoyens de signaler des incidents urbains (nids-de-poule, éclairage défaillant, déchets, problèmes de sécurité) en temps réel, facilitant ainsi une intervention rapide des services municipaux.

---

## I - Core Features

### - Citizen
- **Signalement Intelligent** : Prise de photo, localisation GPS automatique et classification automatique de la catégorie via IA.
- **Fil d'Actualité** : Consultation des incidents signalés dans la zone.
- **Suivi en Temps Réel** : Notifications push lors du changement de statut d'un rapport (Reçu -> En cours -> Résolu).
- **Profil & Historique** : Gestion des informations personnelles et historique des signalements.

### - Admin
- **Tableau de Bord** : Vue d'ensemble des incidents sur une carte interactive et via des statistiques.
- **Gestion des Incidents** : (Fonctionnalités futures) Assignation aux départements, changement de priorité et mise à jour des statuts.
- **Gestion des Départements** : (Fonctionnalités futures) Organisation des services intervenants.

### - IA (TensorFlow Lite)
- **Auto-Classification** : Analyse d'image sur l'appareil pour suggérer la catégorie de l'incident (Routes, Déchets, Sécurité, etc.).

### - Gamification
- **Système de Points** : Gain de points pour chaque signalement validé.
- **Badges Citoyens** : Attribution de badges (Nouveau, Engagé, Actif, d'Or) selon le score.

---

## II - Tech Stack

- **Android** : Kotlin, MVVM Architecture.
- **Base de données Locale** : Room (Offline-first support).
- **Réseau** : Retrofit & OkHttp.
- **Authentification** : JWT (JSON Web Token).
- **Localisation** : Google Maps SDK & Fused Location Provider.
- **IA** : TensorFlow Lite (Modèle MobileNet custom) (Changement dû à un bug).
- **Backend** : Server Laravel, MySQL.
- **Notifications** : Firebase Cloud Messaging (FCM).

---

## III - Installation & Setup

### - Projet Android (Mobile)

1. **Clonage du projet** :
   ```bash
   git clone https://github.com/mamadou-adiouma/citizens-reportsn.git
   ```
2. **Configuration des clés** :
   - Créez un fichier `local.properties` à la racine du projet :
     ```properties
     MAPS_API_KEY=CLE_GOOGLE_MAPS
     ```
   - Placez le fichier (envoyé dans le rendu) `google-services.json` dans le dossier `app/`.
3. **URL API** :
   - Modifiez l'URL de base dans `RetrofitClient.kt` pour pointer vers votre IP locale (machine) - `ipconfig` pour avoir l'IP :
     `private const val BASE_URL = "http://192.168.x.x:8000/api/"`

### - Backend (Laravel)

1. **Installation des dépendances** :
   ```bash
      git clone https://github.com/mamadou-adiouma/citizens-report-backend.git


   composer install
   ```
2. **Environnement** :
   - Copiez `.env.example` et le renommer `.env`.
   - Configurez la DB MySQL.
   - Générez la clé d'application : `php artisan key:generate`.
3. **JWT Setup** :
   ```bash
   php artisan jwt:secret
   ```
4. **Migrations** :
   ```bash
   php artisan migrate --seed
   ```
5. **Firebase** :
   - Ajoutez vos identifiants Firebase dans le fichier de configuration `config/firebase.php` (ou .env selon votre package).

---

## IV - Développement Local

1. **Lancer le serveur Laravel** :
   ```bash
   php artisan serve --host=0.0.0.0
   ```
2. **IP Address** : Assurez-vous que le téléphone Android et le serveur sont sur le même réseau Wi-Fi. Utilisez l'adresse IP de votre machine au lieu de `localhost` dans l'application mobile.

---

## V - Licence
Distribué sous la licence MIT. Voir `LICENSE` pour plus d'informations.

____________________________________________________________________________________________________
                                      `BON DEVELOPPEMENT ! 🚀`
   