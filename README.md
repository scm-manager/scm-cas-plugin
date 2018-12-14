# scm-cas-plugin

CAS Authentication plugin for version 2.x of SCM-Manager

# Setup Test Environment

Add the the the following hostnames to your local `/etc/hosts`, pointing to your ip address (**not 127.0.0.1**):

* cas.hitchhiker.com
* scm.hitchhiker.com

Start cas server with docker-compose:

```bash
docker-compose up
```

The server should now run on https://cas.hitchhicker.com:8443/cas the default credential are `trillian` with password `secret`.

To test the cas login start the scm-cas-plugin, by running:

```bash
mvn clean run
```

Not you should be able to login, by accessing http://scm.hitchhiker.com:8081/scm

For testing the rest client, you could use the following command:

```bash
curl -u trillian:secret http://scm.hitchhiker.com:8081/scm/api/v2/me
```
