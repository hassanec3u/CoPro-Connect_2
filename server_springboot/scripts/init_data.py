#!/usr/bin/env python3
"""
MongoDB data initialization script (external to the Java application).

Usage:
  python init_data.py              # Admin + test user + residents (if empty)
  python init_data.py --admin-only     # Only the admin and test users
  python init_data.py --residents-only # Only the residents (if empty)

Environment variables:
  MONGODB_URI     - MongoDB URI (default: mongodb://localhost:27017/copro-connect)
  RESIDENTS_JSON  - Path to residentData.json
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
# Must correspond to Java fields of the Resident, HappixAccount, etc. models
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
    """Converts a JSON document (snake_case) to a MongoDB document (camelCase)."""
    result = {}
    for key, value in doc.items():
        new_key = RESIDENT_FIELD_MAP.get(key, key)

        if new_key == "happixAccounts" and isinstance(value, list):
            value = [convert_happix(h) for h in value]

        result[new_key] = value
    return result


def convert_happix(doc):
    """Converts a Happix account JSON (snake_case) to a MongoDB document (camelCase)."""
    result = {}
    for key, value in doc.items():
        result[HAPPIX_FIELD_MAP.get(key, key)] = value
    return result


def get_db():
    uri = os.environ.get("MONGODB_URI", DEFAULT_URI)
    client = MongoClient(uri)
    return client.get_default_database()


def _hash_password(plain: str) -> str:
    """Hashes a password with BCrypt (prefix 2a for Spring Security compatibility)."""
    salt = bcrypt.gensalt(rounds=10, prefix=b"2a")
    return bcrypt.hashpw(plain.encode("utf-8"), salt).decode("utf-8")


def _create_user(db, *, username: str, password: str, name: str, email: str, role: str, mfa_enabled: bool):
    users = db["users"]
    if users.find_one({"username": username}):
        print(f"ℹ️  User '{username}' already exists.")
        return False
    now = datetime.utcnow()
    users.insert_one({
        "username": username,
        "password": _hash_password(password),
        "name": name,
        "email": email,
        "role": role,
        "mfaEnabled": mfa_enabled,
        "createdAt": now,
        "updatedAt": now,
    })
    print(f"✅ User '{username}' created.")
    print(f"   Username: {username}")
    print(f"   Password: {password}")
    print(f"   Email:    {email}")
    print(f"   Role:     {role}")
    return True


def ensure_admin_user(db):
    _create_user(
        db,
        username="admin",
        password="admin123",
        name="Administrator",
        email="abouchekou@gmail.com",
        role="ADMIN",
        mfa_enabled=False,
    )


def ensure_test_user(db):
    """Creates a non-admin test user to validate permissions."""
    _create_user(
        db,
        username="user",
        password="user123",
        name="Test User",
        email="testuser@example.com",
        role="USER",
        mfa_enabled=False,
    )


def ensure_residents_data(db):
    residents = db["residents"]
    count = residents.count_documents({})
    if count > 0:
        print(f"ℹ️  Database already contains {count} residents. Import skipped.")
        return
    json_path = os.environ.get("RESIDENTS_JSON", str(DEFAULT_RESIDENTS_JSON.resolve()))
    if not os.path.isabs(json_path):
        json_path = str((SCRIPT_DIR / json_path).resolve())
    if not os.path.isfile(json_path):
        print("⚠️  residentData.json file not found:", json_path)
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
    print(f"✅ {len(normalized)} residents imported from residentData.json.")


def main():
    parser = argparse.ArgumentParser(description="MongoDB data initialization")
    parser.add_argument("--admin-only", action="store_true", help="Create only the admin user")
    parser.add_argument("--residents-only", action="store_true", help="Import only the residents")
    args = parser.parse_args()

    try:
        db = get_db()
        print("MongoDB connection OK.\n")

        if not args.residents_only:
            ensure_admin_user(db)
            ensure_test_user(db)
        if not args.admin_only:
            ensure_residents_data(db)

        print("\nInitialization completed.")
    except Exception as e:
        print("Error:", e, file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
