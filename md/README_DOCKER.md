# Docker Setup für Media Ratings Platform

## Überblick

Dieses Projekt verwendet Docker Compose, um PostgreSQL als Datenbank-Container zu betreiben. Das macht die Entwicklung einfacher, da keine lokale PostgreSQL-Installation erforderlich ist.

## Voraussetzungen

- Docker installiert (mindestens Version 20.x)
- Docker Compose installiert (mindestens Version 2.x)

Installation prüfen:
```bash
docker --version
docker-compose --version
```

## Schnellstart

### 1. PostgreSQL Container starten

```bash
# Im Projekt-Verzeichnis
docker-compose up -d
```

**Was passiert hier?**
- `-d` startet den Container im Hintergrund (detached mode)
- Docker lädt das PostgreSQL Image (postgres:16-alpine) herunter
- Container wird mit Name `mrp_postgres` erstellt
- Datenbank `mrp_db` wird automatisch erstellt
- Das `schema.sql` Script wird automatisch ausgeführt (alle Tabellen werden erstellt)

### 2. Status prüfen

```bash
# Container Status anzeigen
docker-compose ps

# Logs anschauen
docker-compose logs -f postgres
```

### 3. Anwendung starten

```bash
# Maven Build und Start
mvn clean compile exec:java -Dexec.mainClass="org.example.Main"
```

Die Anwendung verbindet sich automatisch mit dem PostgreSQL Container auf `localhost:5432`.

### 4. Container stoppen

```bash
# Container stoppen (Daten bleiben erhalten)
docker-compose stop

# Container stoppen und entfernen (Daten bleiben erhalten im Volume)
docker-compose down

# Container, Volumes UND Daten löschen (VORSICHT!)
docker-compose down -v
```

## Docker Compose Konfiguration erklärt

### Service Definition

```yaml
postgres:
  image: postgres:16-alpine      # Verwendet PostgreSQL 16 (Alpine Linux = klein & schnell)
  container_name: mrp_postgres   # Fester Container-Name
  restart: unless-stopped        # Automatischer Neustart bei Absturz
```

### Umgebungsvariablen

```yaml
environment:
  POSTGRES_DB: mrp_db           # Name der Datenbank
  POSTGRES_USER: postgres       # Datenbank-Benutzer
  POSTGRES_PASSWORD: postgres   # Passwort (NICHT in Production!)
```

### Port Mapping

```yaml
ports:
  - "5432:5432"   # Host:Container - Macht PostgreSQL auf localhost:5432 erreichbar
```

### Volumes (Datenpersistenz)

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data                          # Persistente Daten
  - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro  # Schema beim Start
```

**Wichtig:** 
- `postgres_data` ist ein Docker Volume - Daten bleiben erhalten, auch wenn Container gelöscht wird
- `schema.sql` wird nur beim ERSTEN Start ausgeführt (wenn Datenbank leer ist)

### Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres -d mrp_db"]
  interval: 10s   # Prüfung alle 10 Sekunden
  timeout: 5s     # Timeout nach 5 Sekunden
  retries: 5      # 5 Versuche bis "unhealthy"
```

Prüft, ob PostgreSQL bereit ist, Verbindungen zu akzeptieren.

## Nützliche Befehle

### Container Management

```bash
# Container starten
docker-compose up -d

# Container neustarten
docker-compose restart

# Container stoppen
docker-compose stop

# Container Logs live anschauen
docker-compose logs -f postgres

# Container Shell öffnen
docker-compose exec postgres bash
```

### Datenbank-Zugriff

```bash
# PostgreSQL CLI (psql) öffnen
docker-compose exec postgres psql -U postgres -d mrp_db

# SQL File direkt ausführen
docker-compose exec -T postgres psql -U postgres -d mrp_db < src/main/resources/schema.sql
```

### Datenbank Management

```bash
# Tabellen anzeigen
docker-compose exec postgres psql -U postgres -d mrp_db -c "\dt"

# Alle Daten löschen und neu starten
docker-compose down -v
docker-compose up -d

# Backup erstellen
docker-compose exec postgres pg_dump -U postgres mrp_db > backup.sql

# Backup wiederherstellen
docker-compose exec -T postgres psql -U postgres -d mrp_db < backup.sql
```

### Debugging

```bash
# Container Ressourcen-Nutzung
docker stats mrp_postgres

# Container Details anzeigen
docker inspect mrp_postgres

# Netzwerk prüfen
docker network inspect mrp_network

# Volume Details
docker volume inspect sem_projekt_postgres_data
```

## Troubleshooting

### Problem: Port 5432 bereits belegt

**Fehler:** `Bind for 0.0.0.0:5432 failed: port is already allocated`

**Lösung:**
```bash
# Prüfe, welcher Prozess Port 5432 verwendet
sudo lsof -i :5432

# Stoppe lokale PostgreSQL Installation
sudo systemctl stop postgresql

# Oder ändere Port in docker-compose.yml:
ports:
  - "5433:5432"  # Verwende Port 5433 auf Host

# Dann application.properties anpassen:
db.url=jdbc:postgresql://localhost:5433/mrp_db
```

### Problem: Schema wird nicht geladen

**Symptom:** Tabellen existieren nicht, obwohl Container läuft

**Lösung:**
```bash
# Container komplett neu erstellen
docker-compose down -v
docker-compose up -d

# Oder manuell Schema ausführen
docker-compose exec -T postgres psql -U postgres -d mrp_db < src/main/resources/schema.sql
```

### Problem: Verbindung zur Datenbank fehlschlägt

**Fehler:** `Connection refused` oder `Unknown host`

**Lösung:**
```bash
# 1. Prüfe ob Container läuft
docker-compose ps

# 2. Prüfe Health Status
docker-compose ps
# Status sollte "healthy" sein

# 3. Prüfe Logs
docker-compose logs postgres

# 4. Teste Verbindung manuell
docker-compose exec postgres psql -U postgres -d mrp_db -c "SELECT version();"
```

### Problem: Datenbank-Passwort wird nicht akzeptiert

**Lösung:**
```bash
# Volume löschen und neu erstellen (Daten gehen verloren!)
docker-compose down -v
docker-compose up -d
```

## Best Practices

### Development

1. **Verwende immer Docker Compose** für Entwicklung
2. **Prüfe Health Status** vor dem Starten der Anwendung:
   ```bash
   docker-compose ps
   ```
3. **Logs überwachen** bei Problemen:
   ```bash
   docker-compose logs -f
   ```

### Production (später)

1. **Ändere Standard-Passwort** - NIEMALS `postgres/postgres` in Production!
2. **Verwende .env File** für sensible Daten:
   ```bash
   cp .env.example .env
   # Editiere .env mit sicheren Passwörtern
   ```
3. **Backup Strategy** implementieren
4. **Resource Limits** setzen in docker-compose.yml:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1'
         memory: 1G
   ```

## Vorteile dieser Docker-Setup

✅ **Einfache Installation** - Nur `docker-compose up -d`  
✅ **Isolierung** - Keine Konflikte mit anderen Projekten  
✅ **Reproduzierbar** - Gleiche Umgebung für alle Entwickler  
✅ **Automatische Schema-Initialisierung** - `schema.sql` wird automatisch ausgeführt  
✅ **Datenpersistenz** - Daten bleiben erhalten über Container-Neustarts  
✅ **Health Monitoring** - Automatische Überprüfung ob DB bereit ist  
✅ **Einfaches Cleanup** - `docker-compose down -v` löscht alles  

## Integration mit bestehender Anwendung

Die Anwendung in `src/main/java/org/example/util/DatabaseConnection.java` verwendet bereits die richtigen Verbindungsparameter aus `application.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/mrp_db
db.username=postgres
db.password=postgres
```

Diese Werte passen perfekt zum Docker Container - keine Änderungen nötig! 🎉

## Nächste Schritte

1. ✅ Docker Setup ist fertig
2. 🔄 Container starten: `docker-compose up -d`
3. ✅ Schema wird automatisch geladen
4. 🚀 Anwendung starten: `mvn clean compile exec:java`
5. 🧪 Tests ausführen: `mvn test`

