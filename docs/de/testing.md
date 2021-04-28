---
title: Testen
subtitle: Testumgebung einrichten
---
Füge der lokalen `/etc/hosts` die folgenden Hostnamen hinzu, die auf die eigene IP-Adresse verweisen (**nicht 127.0.0.1**):

* cas.hitchhiker.com
* scm.hitchhiker.com

Ersetze `%%myip%%` in der Docker-Compose-Datei durch der, in der /etc/hosts geschriebenen.

Starte den CAS-Server mit Docker-Compose:

```bash
docker-compose up
```

Der Server sollte nun unter https://cas.hitchhicker.com:8443/cas mit den Standardanmeldeinformationen `trillian` und dem Passwort `secret` erreichbar sein.

Um die CAS-Authentifizierung für den Testserver zu aktivieren, verwende den folgenden curl-Befehl:

```bash
curl -u "scmadmin:scmadmin" -H "Content-Type: application/vnd.scmm-casconfig+json;v=2" -XPUT -d '{ "casUrl": "https://cas.hitchhiker.com:8443/cas", "displayNameAttribute": "displayName", "mailAttribute": "mail", "groupAttribute": "groups", "enabled": true }' http://scm.hitchhiker.com:8081/scm/api/v2/cas/configuration
```

Um die CAS-Anmeldung zu testen, starte das scm-cas-plugin:

```bash
mvn clean run
```

Man sollte sich nicht anmelden können, indem man auf http://scm.hitchhiker.com:8081/scm zugreift.

Zum Testen des restlichen Clients kann folgender Befehl verwendet werden:

```bash
curl -u trillian:secret http://scm.hitchhiker.com:8081/scm/api/v2/me
```
