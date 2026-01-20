# Projekt Status - Zusammenfassung
Stand: 20. Januar 2026

## Was funktioniert

1. **Token-basierte Authentifizierung**: UUID-basierte Tokens (Format: `{UUID}-{UUID}`)
2. **Rating System**: Komplett implementiert (7 Endpoints)
3. **Favorites System**: Komplett implementiert (5 Endpoints)
4. **Media CRUD**: Komplett implementiert (5 Endpoints)
5. **User Management**: Registrierung und Login (2 Endpoints)
6. **Unit Tests**: 19 Tests vorhanden (1 weiterer für Minimum 20 empfohlen)
7. **Docker Setup**: docker-compose.yml fertig

**Gesamt: 19 Endpoints, 28+ Punkte**

## Was noch fehlt

1. **Search & Filter** (3 Punkte, ca. 2h)
2. **Leaderboard** (2 Punkte, ca. 1h)
3. **User Profile** (1 Punkt, ca. 1h)
4. **Dokumentation** (3.5 Punkte, ca. 2h)

**Potenzial: +9.5 Punkte in ca. 6h**

## Docker starten

```bash
# Docker Daemon aktivieren
sudo snap start docker.dockerd
sudo chmod 666 /var/run/docker.sock

# PostgreSQL Container starten
cd /home/m/Downloads/mrp-projekt-main
docker compose up -d

# Prüfen
docker ps
```

Falls Docker nicht funktioniert: PostgreSQL nativ installieren auf Port 5432.

## Application starten

```bash
cd /home/m/Downloads/mrp-projekt-main
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Server: http://localhost:8080

## Nächste Schritte (Empfehlung)

1. Search & Filter implementieren
2. Leaderboard implementieren
3. User Profile implementieren
4. 1 weiteren Unit Test schreiben
5. Dokumentation vervollständigen

Zeitaufwand: ca. 6-7 Stunden für alle Features
