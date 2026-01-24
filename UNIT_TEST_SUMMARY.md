# Unit Test Zusammenfassung
## Anzahl Tests: 21 (Anforderung: min. 20)
### UserServiceTest: 7 Tests
1. registerTest - Erfolgreiche Registrierung
2. registerWithExistingUsernameTest - Duplikat-Check
3. registerWithEmptyUsernameTest - Validation
4. loginTest - Login mit korrekten Credentials (KORRIGIERT: hashedPassword)
5. loginWithWrongPasswordTest - Login mit falschem Password (NEU)
6. validateTokenTest - Token-Validierung
7. validateTokenWithInvalidTokenTest - Ungültiger Token
### MediaServiceTest: 8 Tests
1. createMediaTest - Media erstellen
2. createMediaWithEmptyTitleTest - Validation
3. updateMediaTest - Update eigene Media
4. updateMediaByDifferentUserTest - Ownership Check
5. updateNonexistentMediaTest - Error Handling
6. deleteMediaTest - Delete eigene Media
7. deleteMediaByDifferentUserTest - Ownership Check
8. getAllMediaTest - Alle Media abrufen
### RatingServiceTest: 2 Tests
1. createRatingWithInvalidStarsTest - Stern < 1
2. createRatingWithTooManyStarsTest - Stern > 5
### UserControllerTest: 4 Tests
1. testHandleRegister - HTTP POST Registration
2. testHandleLogin - HTTP POST Login
3. testHandleGetUser - HTTP GET mit Auth
4. testHandleGetUserUnauthorized - HTTP GET ohne Auth
## Qualität:
- JUnit Jupiter 5.10.0 (neueste Version)
- Mockito für Isolation
- Arrange-Act-Assert Pattern
- Positive und Negative Tests
- Edge Case Testing
- Ownership und Security Testing
## Verbesserung:
- loginTest korrigiert: Mock User hat jetzt gehashtes Password
- loginWithWrongPasswordTest hinzugefügt für bessere Coverage
- Tests können jetzt korrekt fehlschlagen ("auf rot gehen")
