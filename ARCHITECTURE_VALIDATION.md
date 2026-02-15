# Architecture de Validation - CoPro Connect

## 🏗️ Architecture en Couches

L'application suit une architecture en couches avec **la validation au niveau de la couche API (Controller)** :

```
┌─────────────────────────────────────────────────────────────┐
│                     COUCHE API (Controller)                  │
│  - Validation des annotations Jakarta (@Valid, @NotBlank)   │
│  - Validation métier via ResidentValidator                  │
│  - Validation des paramètres (@Min, @Max)                   │
│  - Gestion des requêtes/réponses HTTP                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   COUCHE SERVICE (Service)                   │
│  - Logique métier (calculs, transformation)                 │
│  - Normalisation des données                                │
│  - Transactions                                              │
│  - Pas de validation métier ici                             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                COUCHE DONNÉES (Repository)                   │
│  - Accès à MongoDB                                          │
│  - Requêtes personnalisées                                  │
│  - Agrégations et statistiques                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Niveaux de Validation

### 1. Validation des Modèles (Annotations Jakarta)

**Où :** Classes du package `model`  
**Quand :** Automatiquement via `@Valid` dans le controller  
**Quoi :** Format, longueur, présence des champs obligatoires

#### Exemple dans `Resident.java`
```java
@NotBlank(message = "Le numéro de lot est obligatoire")
@Size(min = 1, max = 20, message = "Le numéro de lot doit contenir entre 1 et 20 caractères")
private String lotId;

@Email(message = "L'adresse email n'est pas valide")
@Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
private String proprietaireEmail;
```

---

### 2. Validation Métier (ResidentValidator)

**Où :** `validator/ResidentValidator.java`  
**Quand :** Appelé explicitement par le controller avant d'appeler le service  
**Quoi :** Règles métier (doublons, cohérence des données)

#### Méthodes du Validator

```java
// Vérifie qu'un résident peut être créé (pas de doublon de lotId)
void validateForCreation(Resident resident)

// Vérifie qu'un résident peut être mis à jour
void validateForUpdate(String id, Resident residentDetails)

// Vérifie qu'un ID est valide
void validateId(String id)

// Vérifie qu'un paramètre de recherche est valide
void validateSearchParameter(String paramName, String paramValue)
```

---

### 3. Validation des Paramètres (Annotations de contraintes)

**Où :** Paramètres des méthodes du controller  
**Quand :** Automatiquement via `@Validated` sur la classe controller  
**Quoi :** Pagination, limites, formats de paramètres

#### Exemple dans `ResidentController.java`
```java
@GetMapping
public ResponseEntity<PagedResidentsResponse> getAllResidents(
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
    // ...
)
```

---

## 🔄 Flux de Validation

### Exemple : Création d'un Résident

```
1. Requête HTTP POST /api/residents
   ↓
2. CONTROLLER : ResidentController.createResident()
   ├─ @Valid valide les annotations du modèle Resident
   │  └─ Si échec → MethodArgumentNotValidException (400)
   ├─ residentValidator.validateForCreation(resident)
   │  └─ Vérifie les doublons de lotId
   │     └─ Si échec → DuplicateResidentException (409)
   ↓
3. SERVICE : ResidentService.createResident()
   ├─ Normalise les données (trim, lowercase)
   ├─ Génère l'ID si nécessaire
   └─ Sauvegarde en base
   ↓
4. REPOSITORY : ResidentRepository.save()
   └─ Insertion MongoDB
   ↓
5. Retour au client avec le résident créé (201)
```

---

## 📁 Structure des Fichiers

```
src/main/java/com/copro/connect/
│
├── controller/
│   └── ResidentController.java       # Validation API + Appels au validator
│
├── validator/
│   └── ResidentValidator.java        # Validation métier centralisée
│
├── service/
│   └── ResidentService.java          # Logique métier (PAS de validation)
│
├── repository/
│   ├── ResidentRepository.java
│   └── ResidentRepositoryCustomImpl.java
│
├── model/
│   ├── Resident.java                 # Annotations de validation
│   ├── Occupant.java
│   └── HappixAccount.java
│
└── exception/
    ├── GlobalExceptionHandler.java   # Gestion centralisée des exceptions
    ├── ResidentNotFoundException.java
    ├── DuplicateResidentException.java
    └── ValidationException.java
```

---

## 🎯 Responsabilités par Couche

### Controller (API Layer) ✅
- ✅ Valider les annotations Jakarta via `@Valid`
- ✅ Appeler le `ResidentValidator` pour les règles métier
- ✅ Valider les paramètres de requête via annotations
- ✅ Gérer les réponses HTTP (codes de statut)
- ✅ Logger les requêtes entrantes

### Validator ✅
- ✅ Vérifier les doublons de `lotId`
- ✅ Vérifier l'existence des entités
- ✅ Valider la cohérence des données métier
- ✅ Lancer des exceptions personnalisées

### Service ✅
- ✅ Normaliser les données (trim, lowercase)
- ✅ Gérer les transactions (`@Transactional`)
- ✅ Implémenter la logique métier
- ✅ Appeler les repositories
- ❌ **PAS de validation** (déléguée au validator/controller)

### Repository ✅
- ✅ Accès aux données MongoDB
- ✅ Requêtes personnalisées et filtres
- ✅ Agrégations et statistiques
- ❌ **PAS de logique métier**

---

## 🔍 Exemples de Code

### Controller avec Validation

```java
@PostMapping
public ResponseEntity<Resident> createResident(@Valid @RequestBody Resident resident) {
    log.info("POST /api/residents - Creating new resident");
    
    // Validation métier au niveau API
    residentValidator.validateForCreation(resident);
    
    // Appel au service (qui ne fait PLUS de validation)
    Resident createdResident = residentService.createResident(resident);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdResident);
}
```

### Validator

```java
@Component
public class ResidentValidator {
    private final ResidentRepository residentRepository;
    
    public void validateForCreation(Resident resident) {
        // Vérifier que le lot ID n'existe pas déjà
        Optional<Resident> existingResident = residentRepository.findAll().stream()
            .filter(r -> r.getLotId().equalsIgnoreCase(resident.getLotId().trim()))
            .findFirst();
        
        if (existingResident.isPresent()) {
            throw new DuplicateResidentException(resident.getLotId());
        }
    }
}
```

### Service (simplifié)

```java
@Transactional
public Resident createResident(Resident resident) {
    log.info("Creating new resident: {}", resident.getLotId());
    
    // UNIQUEMENT normalisation et sauvegarde
    normalizeResidentData(resident);
    
    if (resident.getId() == null || resident.getId().isEmpty()) {
        resident.setId(null);
    }
    
    return residentRepository.save(resident);
}
```

---

## 🚨 Gestion des Erreurs

Toutes les erreurs de validation sont interceptées par `GlobalExceptionHandler` :

| Exception | Code HTTP | Description |
|-----------|-----------|-------------|
| `MethodArgumentNotValidException` | 400 | Validation des annotations du modèle |
| `ConstraintViolationException` | 400 | Validation des paramètres |
| `ValidationException` | 400 | Erreur de validation métier |
| `DuplicateResidentException` | 409 | Doublon de lotId |
| `ResidentNotFoundException` | 404 | Résident introuvable |

---

## ✅ Avantages de cette Architecture

1. **Séparation des préoccupations** : Chaque couche a un rôle clair
2. **Validation au bon niveau** : Les erreurs sont détectées tôt (au niveau API)
3. **Service léger** : Le service se concentre sur la logique métier
4. **Testabilité** : Le validator peut être testé indépendamment
5. **Réutilisabilité** : Le validator peut être utilisé par plusieurs controllers
6. **Maintenabilité** : Les règles métier sont centralisées dans le validator
7. **Codes HTTP corrects** : Validation 400, Doublon 409, Not Found 404

---

## 📊 Comparaison Avant/Après

### ❌ Avant (Validation dans le Service)
```
Controller → Service (validation + logique) → Repository
```
**Problème :** Mélange de validation et logique métier dans le service

### ✅ Après (Validation dans la Couche API)
```
Controller (validation) → Service (logique) → Repository
```
**Avantage :** Séparation claire, validation au niveau API

---

## 🧪 Tests Recommandés

1. **Tests Unitaires du Validator**
   - Tester `validateForCreation()` avec doublons
   - Tester `validateForUpdate()` avec différents scénarios
   - Tester `validateId()` avec IDs invalides

2. **Tests d'Intégration du Controller**
   - POST avec données invalides → 400
   - POST avec doublon → 409
   - PUT avec ID inexistant → 404
   - GET avec pagination invalide → 400

3. **Tests du Service**
   - Vérifier la normalisation des données
   - Vérifier les transactions
   - Pas besoin de tester la validation (déjà testée dans le validator)

---

**Date de mise à jour** : 24 janvier 2026  
**Version** : 2.0.0 (Architecture avec Validator)  
**Statut** : ✅ Production Ready
