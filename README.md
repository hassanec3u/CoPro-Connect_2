# CoPro Connect

Application de gestion de copropriété : backend Spring Boot + frontend Angular.

## Démarrage

1. **MongoDB** : `net start MongoDB` (ou `mongod --dbpath C:\data\db`)
2. **Lancer l'app** : double-clic sur `start-all.bat`

   Ou manuellement :
   - Backend : `cd server_springboot` puis `mvn spring-boot:run`
   - Frontend : `cd frontend_Angular` puis `npm install` puis `npm start`
3. **Ouvrir** : http://localhost:4200 — Identifiants : **admin** / **admin123**

## Problèmes courants

| Problème | Solution |
|----------|----------|
| MongoDB | `net start MongoDB` ou `mongod --dbpath C:\data\db` |
| Maven manquant | `cd server_springboot` puis `.\generate-maven-wrapper.bat` |
| `ng` non reconnu | `cd frontend_Angular` puis `npm install` |
| Port occupé | `netstat -ano` + `findstr :4000` puis `taskkill /PID <PID> /F` |
| CORS | Vérifier `cors.allowed-origins=http://localhost:4200` dans `application.properties` |

**Logs** : `server_springboot/logs/copro-connect.log` — **Console** : F12 dans le navigateur.

## Config

- **Frontend** : `frontend_Angular/src/environments/environment.ts` → `apiUrl: 'http://localhost:4000/api'`
- **Backend** : `server_springboot/src/main/resources/application.properties` → port 4000, MongoDB, JWT, CORS

## Structure

```
coPro2/
├── frontend_Angular/    # Angular
├── server_springboot/   # Spring Boot
├── start-all.bat        # Tout démarrer
└── start-backend.bat    # Backend seul
```
