# Guide Rapide - Validation CoPro Connect

## 🎯 Architecture en 3 Niveaux

```
╔════════════════════════════════════════════════════════════════╗
║                    1. COUCHE API (Controller)                   ║
║  📋 ResidentController.java                                    ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │ @PostMapping                                             │  ║
║  │ createResident(@Valid Resident resident)                 │  ║
║  │   ├─ @Valid → Valide annotations Jakarta                │  ║
║  │   ├─ residentValidator.validateForCreation()            │  ║
║  │   └─ residentService.createResident()                   │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                                                                 ║
║  ✅ Responsabilités:                                            ║
║     • Valider les annotations (@NotBlank, @Email, etc.)        ║
║     • Appeler le validator pour les règles métier             ║
║     • Gérer les réponses HTTP                                  ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║                   2. VALIDATOR (Validation Métier)              ║
║  🔍 ResidentValidator.java                                     ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │ validateForCreation(Resident resident)                   │  ║
║  │   └─ Vérifie doublon de lotId                           │  ║
║  │                                                           │  ║
║  │ validateForUpdate(String id, Resident details)          │  ║
║  │   ├─ Vérifie existence du résident                      │  ║
║  │   └─ Vérifie doublon de lotId (sauf pour le même)      │  ║
║  │                                                           │  ║
║  │ validateId(String id)                                    │  ║
║  │   └─ Vérifie que l'ID n'est pas vide                   │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                                                                 ║
║  ✅ Responsabilités:                                            ║
║     • Vérifier les doublons                                    ║
║     • Vérifier l'existence des entités                        ║
║     • Valider la cohérence métier                             ║
╚════════════════════════════════════════════════════════════════╝
                              ↓
╔════════════════════════════════════════════════════════════════╗
║                    3. SERVICE (Logique Métier)                  ║
║  ⚙️ ResidentService.java                                       ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │ @Transactional                                           │  ║
║  │ createResident(Resident resident)                        │  ║
║  │   ├─ normalizeResidentData()                            │  ║
║  │   │   ├─ Trim des espaces                               │  ║
║  │   │   └─ Lowercase pour emails                          │  ║
║  │   └─ residentRepository.save()                          │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                                                                 ║
║  ✅ Responsabilités:                                            ║
║     • Normaliser les données                                   ║
║     • Gérer les transactions                                   ║
║     • Appeler le repository                                    ║
║     ❌ PAS de validation                                       ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 📝 Types de Validation

### 1. Validation des Annotations (Automatique)

```java
// Dans Resident.java
@NotBlank(message = "Le numéro de lot est obligatoire")
@Size(min = 1, max = 20)
private String lotId;

@Email(message = "L'adresse email n'est pas valide")
private String proprietaireEmail;
```

**Déclenché par** : `@Valid` dans le controller  
**Exception levée** : `MethodArgumentNotValidException`  
**Code HTTP** : 400 BAD REQUEST

---

### 2. Validation Métier (Manuelle via Validator)

```java
// Dans ResidentValidator.java
public void validateForCreation(Resident resident) {
    // Vérifie que le lotId n'existe pas déjà
    if (existingResident.isPresent()) {
        throw new DuplicateResidentException(resident.getLotId());
    }
}
```

**Déclenché par** : Appel explicite dans le controller  
**Exception levée** : `DuplicateResidentException`  
**Code HTTP** : 409 CONFLICT

---

### 3. Validation des Paramètres (Automatique)

```java
// Dans ResidentController.java
@GetMapping
public ResponseEntity<?> getAllResidents(
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
)
```

**Déclenché par** : `@Validated` sur la classe controller  
**Exception levée** : `ConstraintViolationException`  
**Code HTTP** : 400 BAD REQUEST

---

## 🔄 Flux d'une Requête POST

```
1. HTTP POST /api/residents
   Body: { "lotId": "A101", "batiment": "A", ... }
   
2. ResidentController.createResident()
   │
   ├─ @Valid valide les annotations
   │  ├─ @NotBlank sur lotId ✅
   │  ├─ @Email sur proprietaireEmail ✅
   │  └─ @Size sur tous les champs ✅
   │
   ├─ residentValidator.validateForCreation()
   │  └─ Vérifie doublon de lotId ✅
   │
   └─ residentService.createResident()
      ├─ Normalise les données (trim, lowercase)
      └─ Sauvegarde en base de données
      
3. HTTP 201 CREATED
```

---

## 🚨 Gestion des Erreurs

| Erreur | Code | Exemple |
|--------|------|---------|
| Champ obligatoire manquant | 400 | `{"errors": {"lotId": "Le numéro de lot est obligatoire"}}` |
| Email invalide | 400 | `{"errors": {"proprietaireEmail": "L'adresse email n'est pas valide"}}` |
| Doublon de lotId | 409 | `{"message": "Un résident existe déjà avec le lot ID: A101"}` |
| Résident introuvable | 404 | `{"message": "Résident introuvable avec l'id: xyz"}` |
| Pagination invalide | 400 | `{"errors": {"size": "doit être inférieur ou égal à 100"}}` |

---

## ✅ Checklist de Validation

Quand vous créez/modifiez un endpoint :

- [ ] Ajouter `@Valid` sur le `@RequestBody` si c'est un objet
- [ ] Ajouter `@Min/@Max` sur les paramètres numériques
- [ ] Appeler le validator approprié au début de la méthode
- [ ] S'assurer que le service ne fait PAS de validation
- [ ] Ajouter un test pour chaque cas d'erreur

---

## 📋 Exemple Complet

### Modèle avec Annotations

```java
@Document(collection = "residents")
public class Resident {
    @NotBlank(message = "Le numéro de lot est obligatoire")
    @Size(min = 1, max = 20)
    private String lotId;
    
    @Email(message = "L'adresse email n'est pas valide")
    private String proprietaireEmail;
    
    @Valid  // Validation imbriquée
    private List<Occupant> occupants;
}
```

### Controller avec Validation

```java
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
@Validated  // Active la validation des paramètres
public class ResidentController {
    
    private final ResidentService residentService;
    private final ResidentValidator residentValidator;
    
    @PostMapping
    public ResponseEntity<Resident> createResident(
            @Valid @RequestBody Resident resident) {  // Valide les annotations
        
        // Validation métier
        residentValidator.validateForCreation(resident);
        
        // Logique métier (sans validation)
        Resident created = residentService.createResident(resident);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### Validator

```java
@Component
@RequiredArgsConstructor
public class ResidentValidator {
    
    private final ResidentRepository residentRepository;
    
    public void validateForCreation(Resident resident) {
        // Vérifier doublon
        boolean exists = residentRepository.findAll().stream()
            .anyMatch(r -> r.getLotId().equalsIgnoreCase(resident.getLotId()));
        
        if (exists) {
            throw new DuplicateResidentException(resident.getLotId());
        }
    }
}
```

### Service (Simple et Propre)

```java
@Service
@RequiredArgsConstructor
public class ResidentService {
    
    private final ResidentRepository residentRepository;
    
    @Transactional
    public Resident createResident(Resident resident) {
        // Uniquement normalisation et sauvegarde
        normalizeResidentData(resident);
        return residentRepository.save(resident);
    }
    
    private void normalizeResidentData(Resident resident) {
        if (resident.getLotId() != null) {
            resident.setLotId(resident.getLotId().trim());
        }
        if (resident.getProprietaireEmail() != null) {
            resident.setProprietaireEmail(
                resident.getProprietaireEmail().trim().toLowerCase()
            );
        }
    }
}
```

---

## 🎓 Règles d'Or

1. ✅ **Validation au niveau API** : Controller + Validator
2. ✅ **Service sans validation** : Uniquement logique métier
3. ✅ **Exceptions spécifiques** : Pas de RuntimeException générique
4. ✅ **Codes HTTP appropriés** : 400, 404, 409
5. ✅ **Messages en français** : Pour l'UX
6. ✅ **Logs détaillés** : Pour le débogage
7. ✅ **Transactions** : Sur les méthodes de modification

---

## 🧪 Tests Unitaires

### Tester le Validator

```java
@Test
void validateForCreation_shouldThrowException_whenDuplicate() {
    // Given
    Resident existing = new Resident();
    existing.setLotId("A101");
    when(repository.findAll()).thenReturn(List.of(existing));
    
    Resident newResident = new Resident();
    newResident.setLotId("A101");
    
    // When & Then
    assertThrows(DuplicateResidentException.class, 
        () -> validator.validateForCreation(newResident));
}
```

### Tester le Controller

```java
@Test
void createResident_shouldReturn400_whenEmailInvalid() {
    mockMvc.perform(post("/api/residents")
        .contentType(APPLICATION_JSON)
        .content("{\"lotId\":\"A101\",\"proprietaireEmail\":\"invalid\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.proprietaireEmail").exists());
}
```

---

**Version** : 2.0.0  
**Date** : 24 janvier 2026  
**Statut** : ✅ Production Ready
