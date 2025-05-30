<p align="center">
  <a href="https://scm-manager.org/">
    <img alt="SCM-Manager" src="https://download.scm-manager.org/images/logo/scm-manager_logo.png" width="500" />
  </a>
</p>
<h1 align="center">
  scm-cas-plugin
</h1>

The central authentication service plugin helps with authentication for SCM-Manager using CAS.

## Usage

Find out how this plugin can be used on the [user documentation page](https://scm-manager.org/plugins/scm-cas-plugin/docs).

## Build and testing

The plugin can be compiled and packaged with the following tasks:

* clean - `gradle clean` - deletes the build directory
* run - `gradle run` - starts an SCM-Manager with the plugin pre-installed and with livereload for the ui
* build - `gradle build` - executes all checks, tests and builds the smp inclusive javadoc and source jar
* test - `gradle test` - run all java tests
* ui-test - `gradle ui-test` - run all ui tests
* check - `gradle check` - executes all registered checks and tests (java and ui)
* fix - `gradle fix` - fixes all fixable findings of the check task
* smp - `gradle smp` - Builds the smp file, without the execution of checks and tests

For the development and testing the `run` task of the plugin can be used:

* run - `gradle run` - starts scm-manager with the plugin pre-installed.

If the plugin was started with `gradle run`, the default browser of the os should be automatically opened.
If the browser does not start automatically, start it manually and go to [http://localhost:8081/scm](http://localhost:8081/scm).

In this mode each change to web files (src/main/js or src/main/webapp), should trigger reload of the browser with the made changes.

## Setup Test Environment

Add the following hostnames to your local `/etc/hosts`, pointing to your ip address (**not 127.0.0.1**):

* cas.hitchhiker.com
* scm.hitchhiker.com

Replace `%%myip%%` in the docker-compose file with the one you added to your /etc/hosts.

Start cas server with docker-compose:

```bash
docker-compose up
```

The server should now run on https://cas.hitchhicker.com:8443/cas the default credential are `trillian` with password `secret`.

To enabled cas authentication for the test server use the following curl command:

```bash
curl -u "scmadmin:scmadmin" -H "Content-Type: application/vnd.scmm-casconfig+json;v=2" -XPUT -d '{ "casUrl": "https://cas.hitchhiker.com:8443/cas", "displayNameAttribute": "displayName", "mailAttribute": "mail", "groupAttribute": "groups", "enabled": true }' http://scm.hitchhiker.com:8081/scm/api/v2/cas/configuration
```

To test the cas login, start the scm-cas-plugin as described above.

Not you should be able to login, by accessing http://scm.hitchhiker.com:8081/scm

For testing the rest client, you could use the following command:

```bash
curl -u trillian:secret http://scm.hitchhiker.com:8081/scm/api/v2/me
```

## Directory & File structure

A quick look at the files and directories you'll see in an SCM-Manager project.

    .
    ├── node_modules/
    ├── src/
    |   ├── main/
    |   |   ├── java/
    |   |   ├── js/
    |   |   └── resources/
    |   └── test/
    |       ├── java/
    |       └── resources/
    ├── .editorconfig
    ├── .gitignore
    ├── build.gradle
    ├── CHANGELOG.md
    ├── gradle.properties
    ├── gradlew
    ├── LICENSE.txt
    ├── package.json
    ├── README.md
    ├── settings.gradle
    ├── tsconfig.json
    └── yarn.lock

1.  **`node_modules/`**: This directory contains all modules of code that your project depends on (npm packages) are automatically installed.

2.  **`src/`**: This directory will contain all code related to what you see or not. `src` is a convention for “source code”.
    1. **`main/`**
        1. **`java/`**: This directory contains the Java code.
        2. **`js/`**: This directory contains the JavaScript code for the web ui, inclusive unit tests: suffixed with `.test.ts`
        3. **`resources/`**: This directory contains the classpath resources.
    2. **`test/`**
        1. **`java/`**: This directory contains the Java unit tests.
        2. **`resources/`**: This directory contains classpath resources for unit tests.

3.  **`.editorconfig`**: This is a configuration file for your editor using [EditorConfig](https://editorconfig.org/). The file specifies a style that IDEs use for code.

4.  **`.gitignore`**: This file tells git which files it should not track / not maintain a version history for.

5.  **`build.gradle`**: Gradle build configuration, which also includes things like metadata.

6.  **`CHANGELOG.md`**: All notable changes to this project will be documented in this file.

7.  **`gradle.properties`**: Defines the module version.

8.  **`gradlew`**: Bundled gradle wrapper if you don't have gradle installed.

9.  **`LICENSE.txt`**: This project is licensed under AGPLv3.

10.  **`package.json`**: Here you can find the dependency/build configuration and dependencies for the frontend.

11.  **`README.md`**: This file, containing useful reference information about the project.

12.  **`settings.gradle`**: Gradle settings configuration.

13. **`tsconfig.json`** This is the typescript configuration file.

14. **`yarn.lock`**: This is the ui dependency configuration.

## Need help?

Looking for more guidance? Full documentation lives on our [homepage](https://scm-manager.org/docs/) or the dedicated pages for our [plugins](https://scm-manager.org/plugins/). Do you have further ideas or need support?

- **Community Support** - Contact the SCM-Manager support team for questions about SCM-Manager, to report bugs or to request features through the official channels. [Find more about this here](https://scm-manager.org/support/).

- **Enterprise Support** - Do you require support with the integration of SCM-Manager into your processes, with the customization of the tool or simply a service level agreement (SLA)? **Contact our development partner Cloudogu! Their team is looking forward to discussing your individual requirements with you and will be more than happy to give you a quote.** [Request Enterprise Support](https://cloudogu.com/en/scm-manager-enterprise/).
