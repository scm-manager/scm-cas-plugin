# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.5.0 - 2024-08-01
### Added
- Cache for successful CAS authentications

### Fixed
- Prevent CAS throttling for local users and api token logins

## 2.4.0 - 2021-10-07
### Added
- Fire logout event on cas logout ([#28](https://github.com/scm-manager/scm-cas-plugin/pull/28))

## 2.3.1 - 2021-07-30
### Fixed
- Login button link and styling ([#23](https://github.com/scm-manager/scm-cas-plugin/pull/23))

## 2.3.0 - 2021-06-16
### Added
- Supports for proxy tickets ([#21](https://github.com/scm-manager/scm-cas-plugin/pull/21))

### Changed
- Bump cas test environment version to Cas 6.4.0-RC4

## 2.2.3 - 2021-03-02
### Fixed
- User authentication check

## 2.2.2 - 2021-02-11
### Fixed
- Invalid email addresses will not lead to authentication failure ([#13](https://github.com/scm-manager/scm-cas-plugin/pull/13))

## 2.2.1 - 2020-11-20
### Changed
- Do not trace failed login as failed request ([#10](https://github.com/scm-manager/scm-cas-plugin/pull/10))

## 2.2.0 - 2020-11-09
### Changed
- Set span kind for http requests (for Trace Monitor)

## 2.1.2 - 2020-10-09
### Fixed
- Anonymous mode after token invalidation ([#9](ihttps://github.com/scm-manager/scm-cas-plugin/pull/9))

## 2.1.1 - 2020-09-08
### Fixed
- Invalidate access token cookie on cas redirect ([#8](https://github.com/scm-manager/scm-cas-plugin/pull/8))

## 2.1.0 - 2020-09-01
### Added
- Integrate full anonymous access mode ([#5](https://github.com/scm-manager/scm-cas-plugin/pull/5))

## 2.0.1 - 2020-08-13
### Fixed
- Do not redirect mercurial hook requests

## 2.0.0 - 2020-06-04
### Changed
- Changeover to MIT license ([#2](https://github.com/scm-manager/scm-cas-plugin/pull/2))
- Rebuild for api changes from core

## 2.0.0-rc5 - 2020-03-19
### Fixed
- Redirection with anonymous access enabled fixed

## 2.0.0-rc4 - 2020-03-19
### Fixed
- Hotfix for rc2: Redirection with anonymous access enabled fixed

## 2.0.0-rc3 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin. ([#1](https://github.com/scm-manager/scm-cas-plugin/pull/1))

## 2.0.0-rc2 - 2020-01-31
### Fixed
- Fix NPE on restart

