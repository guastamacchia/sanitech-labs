# Keycloak realm import (sanitech)

Il realm di sviluppo è definito in `.infra/keycloak/realm-export/sanitech-realm.json` ed è importato automaticamente
dal container Keycloak (vedi `.infra/keycloak/Dockerfile`).

## Import del realm

1. Ricostruire l'immagine Keycloak (include il JSON del realm).
2. Avviare lo stack backend.

Esempio con gli script del repo:

```bash
bash .script/backend/up.sh
```

Keycloak verrà avviato con `--import-realm` e importerà automaticamente il realm `sanitech`.

## Rotazione del secret del client `svc-directory`

Il client `svc-directory` è un client confidential con service account abilitato (grant `client_credentials`).
Per rigenerare il secret:

1. Accedere alla console admin di Keycloak.
2. Selezionare il realm `sanitech`.
3. Clients → `svc-directory` → tab **Credentials** → **Regenerate**.
4. Aggiornare il valore nei secret/variabili d'ambiente (es. `KEYCLOAK_ADMIN_CLIENT_SECRET` o il valore
   usato per `sanitech.keycloak.admin.client-secret` nei servizi backend).
5. Se si vuole persistente anche nel JSON, aggiornare il campo `secret` in
   `.infra/keycloak/realm-export/sanitech-realm.json` e ricostruire l'immagine Keycloak.
