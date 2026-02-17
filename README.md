# CoPro Connect

Application de gestion de copropriété : backend Spring Boot (API REST, MongoDB, JWT, MFA par email) et frontend Angular.

---

## Sommaire

- [Prérequis](#prérequis)
- [Démarrage](#démarrage)
- [Initialisation de la base (script Python)](#initialisation-de-la-base-script-python)
- [Configuration](#configuration)
- [Structure du projet](#structure-du-projet)
- [Dépannage](#dépannage)

---

## Prérequis

- **Java 17+** (backend)
- **Node.js 18+** et **npm** (frontend)
- **MongoDB** (local ou distant)
- **Python 3.8+** et **pip** (uniquement pour le script d’initialisation des données)

---

## Démarrage

1. **Démarrer MongoDB**  
   - Windows : `net start MongoDB`  
   - Ou : `mongod --dbpath C:\data\db`  
   - Vérifier qu’une instance écoute (ex. port 27017).

2. **Initialiser les données** (première fois ou base vide)  
   Voir la section [Initialisation de la base](#initialisation-de-la-base-script-python) ci‑dessous.

3. **Lancer l’application**
   - **Option simple** : double‑clic sur `start-all.bat`
   - **Manuel** :
     - Backend : `cd server_springboot` puis `mvn spring-boot:run`
     - Frontend : `cd frontend_Angular` puis `npm install` puis `npm start`

4. **Ouvrir** : http://localhost:4200  
   Identifiants par défaut : **admin** / **admin123** (MFA par email si configurée).

---

## Initialisation de la base (script Python)

Les données (utilisateur admin, résidents) ne sont **pas** créées au démarrage du serveur. Il faut exécuter une fois le script Python dans `server_springboot/scripts/`.

### Rôle du script

| Donnée        | Description |
|----------------|-------------|
| **Utilisateur admin** | Créé dans la collection `users` (mot de passe BCrypt, compatible Spring Security et MFA). |
| **Résidents** | Importés depuis `residentData.json` dans la collection `residents`, uniquement si elle est vide. |

Le script est **idempotent** : on peut le relancer sans créer de doublons.

### Utilisation (recommandé : tout automatique)

Sous **Windows**, un lanceur crée le venv et installe les dépendances si besoin, puis exécute le script :

```batch
cd server_springboot\scripts
run_init.bat
```

Avec des options :

```batch
run_init.bat --admin-only
run_init.bat --residents-only
```

Aucune commande manuelle (venv, pip) n’est nécessaire.

### Utilisation manuelle (venv + pip)

Si vous préférez contrôler l’environnement vous‑même :

```bash
cd server_springboot/scripts
python -m venv .venv
.\.venv\Scripts\activate    # Windows
pip install -r requirements.txt
python init_data.py
```

Options : `--admin-only`, `--residents-only`.

### Variables d’environnement

| Variable         | Description                    | Défaut |
|------------------|--------------------------------|--------|
| `MONGODB_URI`    | URI MongoDB                    | `mongodb://localhost:27017/copro-connect` |
| `RESIDENTS_JSON`| Chemin vers `residentData.json`| `../src/main/resources/residentData.json` |

Exemple (autre base) :  
`$env:MONGODB_URI="mongodb://localhost:27017/copro-connect-dev"; python init_data.py` (PowerShell).

### Compte admin créé

- **Username** : `admin`  
- **Password** : `admin123`  
- **Email** : `admin@copro-connect.fr` (pour la MFA)

---

## Configuration

- **Frontend** : `frontend_Angular/src/environments/environment.ts` → `apiUrl: 'http://localhost:4000/api'`
- **Backend** : `server_springboot/src/main/resources/application.properties`  
  - Port 4000, URI MongoDB, JWT, CORS, SMTP (MFA), export PDF.

---

## Structure du projet

```
coPro2/
├── frontend_Angular/           # Application Angular
├── server_springboot/         # API Spring Boot
│   ├── scripts/               # Script d’init. base (Python)
│   │   ├── run_init.bat       # Lanceur Windows (venv + deps + script)
│   │   ├── init_data.py
│   │   └── requirements.txt
│   └── src/main/resources/
│       ├── application.properties
│       └── residentData.json  # Données résidents (import optionnel)
├── start-all.bat
├── start-backend.bat
└── README.md                  # Ce fichier
```

---

## Dépannage

| Problème | Solution |
|----------|----------|
| MongoDB ne démarre pas | `net start MongoDB` ou `mongod --dbpath C:\data\db` |
| Maven manquant | `cd server_springboot` puis `.\generate-maven-wrapper.bat` |
| `ng` non reconnu | `cd frontend_Angular` puis `npm install` |
| Port 4000 ou 4200 occupé | `netstat -ano` + `findstr :4000` puis `taskkill /PID <PID> /F` |
| CORS | Vérifier `cors.allowed-origins` dans `application.properties` |
| Script init : `ModuleNotFoundError` | Activer le venv puis `pip install -r requirements.txt` |
| Script init : timeout MongoDB | Vérifier que MongoDB tourne et `MONGODB_URI` |

**Logs backend** : `server_springboot/logs/copro-connect.log`  
**Console navigateur** : F12.
