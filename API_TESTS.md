# Exemples de Requêtes API - Tests de Validation

Ce fichier contient des exemples de requêtes pour tester toutes les validations mises en place.

---

## 1. ✅ Création d'un résident valide

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A101",
  "batiment": "A",
  "etage": "1",
  "porte": "101",
  "caveId": "C12",
  "statutLot": "Propriétaire",
  "proprietaireNom": "Jean Dupont",
  "proprietaireMobile": "+33 6 12 34 56 78",
  "proprietaireEmail": "jean.dupont@example.com",
  "occupants": [
    {
      "nom": "Marie Dupont",
      "mobile": "0612345679",
      "email": "marie.dupont@example.com"
    }
  ],
  "happixAccounts": [
    {
      "nom": "Jean Dupont",
      "mobile": "+33612345678",
      "email": "jean.dupont@example.com",
      "nomBorne": "Borne Entrée",
      "type": "resident",
      "relation": "occupant"
    }
  ]
}
```

**Résultat attendu** : 201 CREATED

---

## 2. ❌ Champs obligatoires manquants

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "batiment": "A",
  "etage": "1"
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "lotId": "Le numéro de lot est obligatoire",
    "porte": "Le numéro de porte est obligatoire",
    "proprietaireNom": "Le nom du propriétaire est obligatoire"
  },
  "status": 400
}
```

---

## 3. ❌ Email invalide

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A102",
  "batiment": "A",
  "etage": "1",
  "porte": "102",
  "proprietaireNom": "Marie Martin",
  "proprietaireEmail": "email-invalide"
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "proprietaireEmail": "L'adresse email n'est pas valide"
  },
  "status": 400
}
```

---

## 4. ❌ Téléphone invalide

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A103",
  "batiment": "A",
  "etage": "1",
  "porte": "103",
  "proprietaireNom": "Pierre Durand",
  "proprietaireMobile": "123"
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "proprietaireMobile": "Le numéro de téléphone n'est pas valide"
  },
  "status": 400
}
```

---

## 5. ❌ Statut lot invalide

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A104",
  "batiment": "A",
  "etage": "1",
  "porte": "104",
  "proprietaireNom": "Sophie Bernard",
  "statutLot": "StatusInvalide"
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "statutLot": "Le statut doit être: Propriétaire, Locataire ou Vacant"
  },
  "status": 400
}
```

---

## 6. ❌ Champs trop longs

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A" + ("x" * 50),  # Plus de 20 caractères
  "batiment": "A",
  "etage": "1",
  "porte": "105",
  "proprietaireNom": "Paul"
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "lotId": "Le numéro de lot doit contenir entre 1 et 20 caractères"
  },
  "status": 400
}
```

---

## 7. ❌ Doublon de lotId (création)

```bash
# Première création
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A106",
  "batiment": "A",
  "etage": "1",
  "porte": "106",
  "proprietaireNom": "Luc Petit"
}

# Seconde tentative avec le même lotId
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A106",
  "batiment": "A",
  "etage": "2",
  "porte": "206",
  "proprietaireNom": "Anne Legrand"
}
```

**Résultat attendu** : 409 CONFLICT
```json
{
  "message": "Un résident existe déjà avec le lot ID: A106",
  "status": 409,
  "path": "/api/residents"
}
```

---

## 8. ❌ Occupant avec données invalides

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A107",
  "batiment": "A",
  "etage": "1",
  "porte": "107",
  "proprietaireNom": "Claire Moreau",
  "occupants": [
    {
      "nom": "X",  # Trop court (min 2 caractères)
      "email": "email-invalide"
    }
  ]
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "occupants[0].nom": "Le nom de l'occupant doit contenir entre 2 et 100 caractères",
    "occupants[0].email": "L'adresse email de l'occupant n'est pas valide"
  },
  "status": 400
}
```

---

## 9. ❌ Compte Happix avec type invalide

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "A108",
  "batiment": "A",
  "etage": "1",
  "porte": "108",
  "proprietaireNom": "Thomas Roux",
  "happixAccounts": [
    {
      "nom": "Thomas Roux",
      "type": "TypeInvalide"
    }
  ]
}
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation",
  "errors": {
    "happixAccounts[0].type": "Le type doit être: resident ou autorisé"
  },
  "status": 400
}
```

---

## 10. ✅ Mise à jour valide

```bash
PUT http://localhost:8080/api/residents/{id}
Content-Type: application/json

{
  "lotId": "A101",
  "batiment": "A",
  "etage": "1",
  "porte": "101",
  "caveId": "C12",
  "statutLot": "Locataire",  # Changement de statut
  "proprietaireNom": "Jean Dupont",
  "proprietaireMobile": "+33 6 12 34 56 78",
  "proprietaireEmail": "jean.dupont@example.com",
  "occupants": [],
  "happixAccounts": []
}
```

**Résultat attendu** : 200 OK

---

## 11. ❌ Mise à jour avec doublon de lotId

```bash
# Supposons que A101 et A102 existent déjà
PUT http://localhost:8080/api/residents/{id_de_A102}
Content-Type: application/json

{
  "lotId": "A101",  # Essaie de changer A102 en A101 (déjà utilisé)
  "batiment": "A",
  "etage": "1",
  "porte": "102",
  "proprietaireNom": "Marie Martin"
}
```

**Résultat attendu** : 409 CONFLICT
```json
{
  "message": "Un résident existe déjà avec le lot ID: A101",
  "status": 409,
  "path": "/api/residents/{id}"
}
```

---

## 12. ❌ Résident introuvable

```bash
GET http://localhost:8080/api/residents/id-inexistant
```

**Résultat attendu** : 404 NOT FOUND
```json
{
  "message": "Résident introuvable avec l'id: id-inexistant",
  "status": 404,
  "path": "/api/residents/id-inexistant"
}
```

---

## 13. ❌ Pagination invalide

```bash
GET http://localhost:8080/api/residents?page=-1&size=200
```

**Résultat attendu** : 400 BAD REQUEST
```json
{
  "message": "Erreur de validation des paramètres",
  "errors": {
    "getAllResidents.page": "doit être supérieur ou égal à 0",
    "getAllResidents.size": "doit être inférieur ou égal à 100"
  },
  "status": 400
}
```

---

## 14. ✅ Recherche avec filtres

```bash
GET http://localhost:8080/api/residents?page=0&size=10&search=Dupont&batiment=A&statutLot=Propriétaire
```

**Résultat attendu** : 200 OK avec résultats triés par bâtiment puis porte

---

## 15. ✅ Suppression valide

```bash
DELETE http://localhost:8080/api/residents/{id}
```

**Résultat attendu** : 200 OK
```json
{
  "message": "Résident supprimé avec succès"
}
```

---

## 16. ❌ Suppression avec ID vide

```bash
DELETE http://localhost:8080/api/residents/
```

**Résultat attendu** : 404 NOT FOUND (route non trouvée)

---

## 17. ✅ Récupération des statistiques

```bash
GET http://localhost:8080/api/residents/statistics
```

**Résultat attendu** : 200 OK
```json
{
  "totalLots": 10,
  "totalBatiments": 2,
  "totalOccupants": 15,
  "totalHappix": 8,
  "statutCount": {
    "Propriétaire": 7,
    "Locataire": 3
  },
  "batimentCount": {
    "A": 6,
    "B": 4
  },
  "lotsAvecOccupants": 8,
  "lotsVides": 2,
  "moyenneOccupants": 1.5,
  "happixByType": {
    "resident": 6,
    "autorisé": 2
  }
}
```

---

## 18. ✅ Récupération par bâtiment

```bash
GET http://localhost:8080/api/residents?batiment=A
```

**Résultat attendu** : 200 OK avec résidents du bâtiment A triés

---

## 19. ✅ Normalisation automatique

```bash
POST http://localhost:8080/api/residents
Content-Type: application/json

{
  "lotId": "  A109  ",  # Espaces avant et après
  "batiment": " A ",
  "etage": " 1 ",
  "porte": " 109 ",
  "proprietaireNom": "  Émilie Blanc  ",
  "proprietaireEmail": "  EMILIE.BLANC@EXAMPLE.COM  "  # Majuscules + espaces
}
```

**Résultat attendu** : 201 CREATED
```json
{
  "lotId": "A109",  # Trimé
  "batiment": "A",
  "etage": "1",
  "porte": "109",
  "proprietaireNom": "Émilie Blanc",
  "proprietaireEmail": "emilie.blanc@example.com"  # Lowercase + trimé
}
```

---

## Notes pour les tests

1. Remplacez `{id}` par un ID MongoDB valide dans vos tests
2. Assurez-vous que le serveur tourne sur `http://localhost:8080`
3. Ajoutez le token JWT dans les headers si l'authentification est activée :
   ```
   Authorization: Bearer {votre_token_jwt}
   ```
4. Pour les tests avec des doublons, créez d'abord un résident, puis tentez de créer un doublon
5. Utilisez Postman, Insomnia, ou curl pour exécuter ces requêtes

---

## Commandes curl

### Création
```bash
curl -X POST http://localhost:8080/api/residents \
  -H "Content-Type: application/json" \
  -d '{"lotId":"A101","batiment":"A","etage":"1","porte":"101","proprietaireNom":"Jean Dupont"}'
```

### Récupération
```bash
curl -X GET http://localhost:8080/api/residents?page=0&size=10
```

### Mise à jour
```bash
curl -X PUT http://localhost:8080/api/residents/{id} \
  -H "Content-Type: application/json" \
  -d '{"lotId":"A101","batiment":"A","etage":"1","porte":"101","proprietaireNom":"Jean Dupont Modifié"}'
```

### Suppression
```bash
curl -X DELETE http://localhost:8080/api/residents/{id}
```
