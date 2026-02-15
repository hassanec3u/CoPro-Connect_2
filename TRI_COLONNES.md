# 🔄 Tri par Colonnes - Documentation

## ✅ Fonctionnalité Implémentée

Les en-têtes des colonnes du tableau sont maintenant **cliquables** pour trier les résidents.

---

## 📊 Colonnes Triables

| Colonne | Champ MongoDB | Tri Disponible |
|---------|---------------|----------------|
| **Lot No.** | `lotId` | ✅ Oui |
| **Bât/Appt** | `batiment` | ✅ Oui |
| Étage | `etage` | ❌ Non |
| Cave | `caveId` | ❌ Non |
| **Propriétaire** | `proprietaireNom` | ✅ Oui |
| Occupants | - | ❌ Non (liste) |
| Comptes Happix | - | ❌ Non (liste) |
| Actions | - | ❌ Non |

**Note** : Seules **3 colonnes** sont triables : Lot No., Bât/Appt, et Propriétaire.

---

## 🎯 Comportement

### 1️⃣ **Premier Clic**
- Trie la colonne en **ordre croissant** (A→Z, 0→9)
- Affiche l'icône **▲**

### 2️⃣ **Deuxième Clic**
- Inverse le tri en **ordre décroissant** (Z→A, 9→0)
- Affiche l'icône **▼**

### 3️⃣ **Tri par défaut**
- Si aucun tri n'est actif, affiche **⇅** (neutre)
- Tri par défaut au chargement : `batiment ASC, porte ASC`

---

## 🔗 Appel API

### Format de la requête
```http
GET /api/residents?page=0&size=10&sort=proprietaireNom,asc
```

### Paramètres
| Paramètre | Type | Exemple | Description |
|-----------|------|---------|-------------|
| `page` | `int` | `0` | Numéro de page (0-indexé) |
| `size` | `int` | `10` | Nombre d'éléments par page |
| `search` | `string` | `"Dupont"` | Recherche globale |
| `batiment` | `string` | `"B1"` | Filtre par bâtiment |
| `statutLot` | `string` | `"Propriétaire Bailleur"` | Filtre par statut |
| **`sort`** | `string` | `"lotId,desc"` | **Tri : champ,direction** |

### Directions de tri
- `asc` : Ordre croissant (par défaut)
- `desc` : Ordre décroissant

---

## 💻 Exemples d'utilisation

### Trier par lot (croissant)
```http
GET /api/residents?page=0&size=10&sort=lotId,asc
```

### Trier par propriétaire (décroissant)
```http
GET /api/residents?page=0&size=10&sort=proprietaireNom,desc
```

### Trier + Filtrer par statut
```http
GET /api/residents?page=0&size=10&statutLot=Propriétaire%20Bailleur&sort=batiment,asc
```

---

## 🎨 Interface Visuelle

### Apparence des en-têtes

```
┌──────────────────────────────────────┐
│ Lot No. ⇅  │ Bât/Appt ▲  │ Étage ⇅  │
├──────────────────────────────────────┤
│  Données triées...                   │
└──────────────────────────────────────┘
```

### États visuels
- **Neutre** : `⇅` (gris, colonne non triée)
- **Ascendant** : `▲` (bleu, tri actif)
- **Descendant** : `▼` (bleu, tri actif)

### Effets au survol
- Fond en dégradé bleu-violet (`#E0E7FF`, `#DDD6FE`)
- Curseur en forme de pointeur (`cursor: pointer`)
- Icône de tri en couleur primaire

---

## 🧩 Architecture

### Frontend (Angular)

**Composant** : `dashboard-page.component.ts`
```typescript
sortField = 'batiment';
sortDirection: 'asc' | 'desc' = 'asc';

handleSort(field: string): void {
  if (this.sortField === field) {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
  } else {
    this.sortField = field;
    this.sortDirection = 'asc';
  }
  this.currentPage = 1;
  this.loadData();
}

getSortIcon(field: string): string {
  if (this.sortField !== field) return '⇅';
  return this.sortDirection === 'asc' ? '▲' : '▼';
}
```

**Template** : `dashboard-page.component.html`
```html
<th class="sortable" (click)="handleSort('lotId')">
  Lot No. <span class="sort-icon">{{ getSortIcon('lotId') }}</span>
</th>
```

---

### Backend (Spring Boot)

**Controller** : `ResidentController.java`
```java
@GetMapping
public ResponseEntity<PagedResidentsResponse> getAllResidents(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String sort) {
    // ...
}
```

**Service** : `ResidentService.java`
```java
private Pageable createPageable(int page, int size, String sort) {
    if (sort != null && !sort.trim().isEmpty()) {
        String[] sortParams = sort.split(",");
        String field = sortParams[0].trim();
        String direction = sortParams.length > 1 ? sortParams[1].trim() : "asc";
        
        Sort sortObj = direction.equalsIgnoreCase("desc") 
            ? Sort.by(field).descending() 
            : Sort.by(field).ascending();
        
        return PageRequest.of(page, size, sortObj);
    }
    // Tri par défaut
    return PageRequest.of(page, size, 
        Sort.by("batiment").ascending().and(Sort.by("porte").ascending()));
}
```

---

## 🧪 Tests

### 1. Trier par Numéro de Lot
- ✅ Cliquer sur "Lot No." → Ordre A01, A02, A03...
- ✅ Cliquer à nouveau → Ordre inversé ...A03, A02, A01

### 2. Trier par Bâtiment
- ✅ Cliquer sur "Bât/Appt" → Ordre B1, B2, B3...
- ✅ Vérifier que l'icône change (⇅ → ▲ → ▼)

### 3. Trier par Nom Propriétaire
- ✅ Cliquer sur "Propriétaire" → Ordre alphabétique
- ✅ Vérifier que l'icône change (⇅ → ▲ → ▼)

### 4. Tri + Filtre
- ✅ Filtrer par "Propriétaire Bailleur"
- ✅ Trier par "Bâtiment" → Seuls les bailleurs triés

### 5. Tri + Recherche
- ✅ Rechercher "Dupont"
- ✅ Trier par "Lot No." → Résultats triés

---

## 📝 Notes Techniques

### MongoDB
- Les champs triables doivent être en **camelCase** (ex: `proprietaireNom`)
- Le tri fonctionne sur tous les types : `string`, `number`, `date`

### Performance
- Le tri est effectué **côté serveur** (MongoDB)
- Aucun impact sur les performances frontend
- Compatible avec la pagination

### Limitations
- **Seulement 3 colonnes triables** : `lotId`, `batiment`, `proprietaireNom`
- **Pas de tri sur les listes** (`occupants`, `happixAccounts`)
- **Pas de tri sur "Étage" et "Cave"** (colonnes non triables)
- **Un seul champ à la fois** (pas de tri multi-colonnes)

---

## 🚀 Améliorations Futures

1. **Tri multi-colonnes** : `?sort=batiment,asc&sort=porte,asc`
2. **Mémorisation du tri** : Sauvegarder le choix dans `localStorage`
3. **Tri sur listes** : Compter le nombre d'occupants et trier
4. **Indicateur visuel renforcé** : Badge "Trié par..." dans la toolbar

---

## 🔍 Débogage

### Console du navigateur
Vérifiez les logs suivants :
```
🔄 Sort clicked: proprietaireNom
📡 Loading data with params: {sortField: "proprietaireNom", sortDirection: "asc"}
🌐 API Call: http://localhost:8080/api/residents?page=0&size=10&sort=proprietaireNom,asc
```

### Réseau
- **Onglet Network** → Requête `/api/residents?...&sort=...`
- **Code 200** : Tri réussi
- **Code 400** : Champ de tri invalide

---

## ✅ Résumé

| Fonctionnalité | Statut |
|----------------|--------|
| En-têtes cliquables | ✅ Fait |
| Icônes de tri (⇅/▲/▼) | ✅ Fait |
| Tri croissant/décroissant | ✅ Fait |
| Tri + Filtres | ✅ Fait |
| Tri + Recherche | ✅ Fait |
| Tri + Pagination | ✅ Fait |
| Backend Spring Boot | ✅ Fait |
| MongoDB camelCase | ✅ Fait |
| Styles responsive | ✅ Fait |

🎉 **Les en-têtes du tableau sont maintenant interactifs !**
