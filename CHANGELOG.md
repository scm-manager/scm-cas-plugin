# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
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
