# Documentation de Validation des Données

## Vue d'ensemble

Ce document décrit toutes les validations mises en place dans l'application CoPro Connect pour garantir l'intégrité des données.

## Modèles avec Validation

### 1. Resident (Résident)

#### Champs obligatoires
- **lotId** : Numéro de lot (1-20 caractères)
- **batiment** : Bâtiment (1-10 caractères)
- **etage** : Étage (1-5 caractères)
- **porte** : Numéro de porte (1-10 caractères)
- **proprietaireNom** : Nom du propriétaire (2-100 caractères)

#### Champs optionnels validés
- **caveId** : Identifiant de cave (max 20 caractères)
- **statutLot** : Doit être "Propriétaire", "Locataire" ou "Vacant"
- **proprietaireMobile** : Format téléphone (+33 ou 06..., 10-20 caractères)
- **proprietaireEmail** : Format email valide (max 100 caractères)

#### Listes validées
- **occupants** : Liste d'objets Occupant (validation imbriquée)
- **happixAccounts** : Liste d'objets HappixAccount (validation imbriquée)

---

### 2. Occupant

#### Champs obligatoires
- **nom** : Nom de l'occupant (2-100 caractères)

#### Champs optionnels validés
- **mobile** : Format téléphone (10-20 caractères)
- **email** : Format email valide (max 100 caractères)

---

### 3. HappixAccount (Compte Happix)

#### Champs obligatoires
- **nom** : Nom du compte (2-100 caractères)

#### Champs optionnels validés
- **mobile** : Format téléphone (10-20 caractères)
- **email** : Format email valide (max 100 caractères)
- **nomBorne** : Nom de borne (max 50 caractères)
- **type** : Doit être "resident" ou "autorisé"
- **relation** : Description de la relation (max 50 caractères)

---

## Validations métier (ResidentService)

### Création de résident (`createResident`)
1. ✅ Vérification que le `lotId` n'existe pas déjà (évite les doublons)
2. ✅ Normalisation des données (trim, lowercase pour emails)
3. ✅ Génération automatique de l'ID MongoDB si non fourni

### Mise à jour de résident (`updateResident`)
1. ✅ Vérification que l'ID existe
2. ✅ Vérification que le nouveau `lotId` n'est pas déjà utilisé par un autre résident
3. ✅ Normalisation des données

### Suppression de résident (`deleteResident`)
1. ✅ Vérification que l'ID n'est pas vide
2. ✅ Vérification que le résident existe

### Recherche par bâtiment/statut
1. ✅ Vérification que les paramètres ne sont pas vides

---

## Validations au niveau Controller

### Pagination (`getAllResidents`)
- **page** : Minimum 0
- **size** : Entre 1 et 100
- Validation supplémentaire pour empêcher size > 100

---

## Exceptions personnalisées

### 1. `ResidentNotFoundException`
- **Code HTTP** : 404 (NOT_FOUND)
- **Usage** : Quand un résident n'est pas trouvé par son ID

### 2. `DuplicateResidentException`
- **Code HTTP** : 409 (CONFLICT)
- **Usage** : Quand on essaie de créer un résident avec un lotId existant

### 3. `ValidationException`
- **Code HTTP** : 400 (BAD_REQUEST)
- **Usage** : Erreurs de validation métier

### 4. `MethodArgumentNotValidException`
- **Code HTTP** : 400 (BAD_REQUEST)
- **Usage** : Erreurs de validation des annotations Jakarta (@Valid, @NotBlank, etc.)
- **Format de réponse** :
```json
{
  "message": "Erreur de validation",
  "errors": {
    "lotId": "Le numéro de lot est obligatoire",
    "proprietaireEmail": "L'adresse email n'est pas valide"
  },
  "status": 400
}
```

### 5. `ConstraintViolationException`
- **Code HTTP** : 400 (BAD_REQUEST)
- **Usage** : Erreurs de validation des paramètres (@Min, @Max, etc.)

---

## Normalisation automatique des données

Le service `ResidentService` normalise automatiquement les données avant sauvegarde :

1. **Trim des espaces** : Tous les champs texte
2. **Lowercase pour emails** : Uniformisation des adresses email
3. **Normalisation récursive** : Application sur les occupants et comptes Happix

---

## Format des numéros de téléphone acceptés

Regex : `^(\\+?[0-9\\s.-]{10,20})?$`

Exemples valides :
- `0612345678`
- `06 12 34 56 78`
- `+33 6 12 34 56 78`
- `+33-6-12-34-56-78`
- Champ vide (optionnel)

---

## Format des emails

- Validation standard Jakarta `@Email`
- Conversion automatique en lowercase
- Longueur maximum : 100 caractères

---

## Amélioration du tri

Le tri par **bâtiment ASC** puis **porte ASC** est maintenant appliqué même avec des filtres de recherche.

---

## Tests recommandés

Pour tester les validations, essayez :

1. ✅ Créer un résident avec un email invalide
2. ✅ Créer un résident sans nom de propriétaire
3. ✅ Créer deux résidents avec le même lotId
4. ✅ Mettre à jour un résident avec un lotId existant
5. ✅ Rechercher avec une pagination size > 100
6. ✅ Créer un résident avec un statutLot invalide

---

## Exemple de requête valide

```json
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

---

## Notes importantes

- Les validations sont appliquées automatiquement grâce à `@Valid` dans les controllers
- Les messages d'erreur sont en français pour une meilleure UX
- La transaction `@Transactional` garantit l'intégrité des données lors des opérations de modification
- Les emails sont automatiquement convertis en minuscules pour éviter les doublons
