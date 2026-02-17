#!/usr/bin/env python3
"""
Script d'initialisation des données MongoDB (externe à l'application Java).

Usage:
  python init_data.py              # Admin + résidents (si vides)
  python init_data.py --admin-only     # Uniquement l'utilisateur admin
  python init_data.py --residents-only # Uniquement les résidents (si vides)

Variables d'environnement:
  MONGODB_URI     - URI MongoDB (défaut: mongodb://localhost:27017/copro-connect)
  RESIDENTS_JSON  - Chemin vers residentData.json
"""

import argparse
import json
import os
import sys
from datetime import datetime
from pathlib import Path

import bcrypt
from pymongo import MongoClient


DEFAULT_URI = "mongodb://localhost:27017/copro-connect"
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_RESIDENTS_JSON = SCRIPT_DIR / ".." / "src" / "main" / "resources" / "residentData.json"

# Mapping JSON (snake_case) → MongoDB/Spring Data (camelCase)
# Doit correspondre aux champs Java du modèle Resident, HappixAccount, etc.
RESIDENT_FIELD_MAP = {
    "lot_id": "lotId",
    "cave_id": "caveId",
    "statut_lot": "statutLot",
    "proprietaire_nom": "proprietaireNom",
    "proprietaire_mobile": "proprietaireMobile",
    "proprietaire_email": "proprietaireEmail",
    "happix_accounts": "happixAccounts",
}

HAPPIX_FIELD_MAP = {
    "nom_borne": "nomBorne",
}


def convert_resident(doc):
    """Convertit un document JSON (snake_case) en document MongoDB (camelCase)."""
    result = {}
    for key, value in doc.items():
        new_key = RESIDENT_FIELD_MAP.get(key, key)

        if new_key == "happixAccounts" and isinstance(value, list):
            value = [convert_happix(h) for h in value]

        result[new_key] = value
    return result


def convert_happix(doc):
    """Convertit un compte Happix JSON (snake_case) en document MongoDB (camelCase)."""
    result = {}
    for key, value in doc.items():
        result[HAPPIX_FIELD_MAP.get(key, key)] = value
    return result


def get_db():
    uri = os.environ.get("MONGODB_URI", DEFAULT_URI)
    client = MongoClient(uri)
    return client.get_default_database()


def ensure_admin_user(db):
    users = db["users"]
    if users.find_one({"username": "admin"}):
        print("ℹ️  Utilisateur admin déjà présent.")
        return
    # Préfixe 2a pour compatibilité avec Spring Security BCryptPasswordEncoder
    salt = bcrypt.gensalt(rounds=10, prefix=b"2a")
    hashed = bcrypt.hashpw("admin123".encode("utf-8"), salt)
    users.insert_one({
        "username": "admin",
        "password": hashed.decode("utf-8"),
        "name": "Administrateur",
        "email": "admin@copro-connect.fr",
        "role": "ADMIN",
        "mfaEnabled": True,
        "createdAt": datetime.utcnow(),
        "updatedAt": datetime.utcnow(),
    })
    print("✅ Utilisateur admin créé.")
    print("   Username: admin")
    print("   Password: admin123")
    print("   Email: admin@copro-connect.fr")


def ensure_residents_data(db):
    residents = db["residents"]
    count = residents.count_documents({})
    if count > 0:
        print(f"ℹ️  La base contient déjà {count} résidents. Import ignoré.")
        return
    json_path = os.environ.get("RESIDENTS_JSON", str(DEFAULT_RESIDENTS_JSON.resolve()))
    if not os.path.isabs(json_path):
        json_path = str((SCRIPT_DIR / json_path).resolve())
    if not os.path.isfile(json_path):
        print("⚠️  Fichier residentData.json introuvable:", json_path)
        return
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    docs = data if isinstance(data, list) else [data]
    normalized = []
    for doc in docs:
        id_val = doc.pop("id", None)
        if id_val is None:
            continue
        converted = convert_resident(doc)
        converted["_id"] = str(id_val)
        normalized.append(converted)
    if not normalized:
        return
    residents.insert_many(normalized)
    print(f"✅ {len(normalized)} résidents importés depuis residentData.json.")


def main():
    parser = argparse.ArgumentParser(description="Initialisation des données MongoDB")
    parser.add_argument("--admin-only", action="store_true", help="Créer uniquement l'utilisateur admin")
    parser.add_argument("--residents-only", action="store_true", help="Importer uniquement les résidents")
    args = parser.parse_args()

    try:
        db = get_db()
        print("Connexion MongoDB OK.\n")

        if not args.residents_only:
            ensure_admin_user(db)
        if not args.admin_only:
            ensure_residents_data(db)

        print("\nInitialisation terminée.")
    except Exception as e:
        print("Erreur:", e, file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
