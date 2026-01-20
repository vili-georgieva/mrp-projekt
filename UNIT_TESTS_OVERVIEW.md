# Unit Test Übersicht - MRP Projekt

## Zusammenfassung
- **Gesamt Tests**: 10 Unit Tests
- **Test Framework**: JUnit Jupiter 5.10.0
- **Mocking Framework**: Mockito 5.5.0
- **Test-Fokus**: Presentation Layer (Controller) + Business Logic Layer (Service)

## Test-Struktur

### 1. UserControllerTest (4 Tests) - Presentation Layer
Testet HTTP-Request-Handling für User-Endpoints

#### Tests:
1. **handleRegisterTest** - Erfolgreiche User-Registrierung gibt 201 Created zurück
2. **handleRegisterWithExistingUsernameTest** - Registrierung mit existierendem Username gibt 400 Bad Request
3. **handleLoginTest** - Erfolgreicher Login gibt 200 OK mit Token zurück
4. **handleLoginWithInvalidCredentialsTest** - Login mit falschen Credentials gibt 401 Unauthorized

### 2. UserServiceTest (6 Tests) - Business Logic Layer
Testet User-Management-Logik (Registrierung, Login, Token-Validierung)

#### Tests:
5. **registerTest** - Erfolgreiche Registrierung erstellt User mit gehashtem Password
6. **registerWithExistingUsernameTest** - Registrierung mit existierendem Username wirft IllegalArgumentException
7. **registerWithEmptyUsernameTest** - Registrierung mit leerem Username wirft IllegalArgumentException
8. **loginTest** - Login mit korrekten Credentials gibt UUID-Token zurück (nicht mehr "username-mrpToken")
9. **validateTokenTest** - Token-Validierung mit gültigem Token gibt User zurück
10. **validateTokenWithInvalidTokenTest** - Token-Validierung mit ungültigem Token gibt leeres Optional

### 3. MediaServiceTest (5 Tests) - Business Logic Layer
Testet Media-Management-Logik (Create, Update, Delete)

#### Tests:
11. **createMediaTest** - Erfolgreiche Media-Erstellung setzt Creator und gibt ID zurück
12. **createMediaWithEmptyTitleTest** - Media-Erstellung mit leerem Titel wirft IllegalArgumentException
13. **updateMediaTest** - Erfolgreiche Media-Update (nur eigene Media)
14. **updateMediaByDifferentUserTest** - Update von fremder Media wirft IllegalArgumentException
15. **deleteMediaTest** - Erfolgreiche Media-Löschung (nur eigene Media)
16. **deleteMediaByDifferentUserTest** - Löschung von fremder Media wirft IllegalArgumentException
17. **getAllMediaTest** - getAllMedia gibt Liste von Media-Einträgen zurück

### 4. RatingServiceTest (2 Tests) - Business Logic Layer
Testet Rating-Logik (Validierung)

#### Tests:
18. **createRatingWithInvalidStarsTest** - Rating mit Sternen < 1 wirft IllegalArgumentException
19. **createRatingWithTooManyStarsTest** - Rating mit Sternen > 5 wirft IllegalArgumentException

## Test-Verteilung

### Nach Layer:
- **Presentation Layer (Controller)**: 4 Tests (21%)
- **Business Logic Layer (Service)**: 15 Tests (79%)
- **Data Access Layer (Repository)**: 0 Tests (wie gefordert)

### Nach Komponente:
- **User-Komponente**: 10 Tests (53%)
- **Media-Komponente**: 7 Tests (37%)
- **Rating-Komponente**: 2 Tests (10%)

## Test-Qualität

### Alle Tests verwenden:
✅ **Mockito** für Dependency Mocking
✅ **@ExtendWith(MockitoExtension.class)** für JUnit 5 Integration
✅ **Arrange-Act-Assert Pattern** für klare Test-Struktur
✅ **Deutsche Kommentare** wie gefordert
✅ **Naming Convention**: KlasseTest / funktionTest

### Tests decken ab:
✅ Happy Path (erfolgreiche Operationen)
✅ Edge Cases (leere Werte, ungültige Eingaben)
✅ Security (nur eigene Ressourcen bearbeiten)
✅ Authentication (Token-Validierung)
✅ Authorization (Berechtigungsprüfung)

## Ausführung

```bash
# Alle Tests ausführen
mvn test

# Nur spezifische Test-Klasse
mvn test -Dtest=UserServiceTest

# Mit Coverage Report
mvn test jacoco:report
```

## Wichtige Änderungen

### Token-System verbessert:
- ❌ **Alt**: `username + "-mrpToken"` (z.B. "john-mrpToken")
- ✅ **Neu**: UUID-basiert (z.B. "a3f8d9e2-4b5c-11ec-9f2e3d4c-8b7a6f5e4d3c")
- ✅ **Bonus**: SHA-256 Password-Hashing

### Test-Validierung:
Tests prüfen dass das neue Token-System funktioniert (siehe `loginTest` in UserServiceTest).
