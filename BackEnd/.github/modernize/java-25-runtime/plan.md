# Modernization Plan: Java 25 Runtime for CTI_SENAI Backend

**Project**: CTI_SENAI — BackEnd

## Technical Framework

- **Language/runtime**: Java 21 currently; target Java 25
- **Framework**: Spring Boot 3.5.6; Spring Framework version is managed by
  Spring Boot
- **Build tool**: Maven Wrapper 3.9.16
- **Database**: PostgreSQL
- **Key dependencies**: Spring Web, Spring Security, Spring Data JPA,
  Spring Validation, JJWT

## Overview

Modernize the backend's Java runtime/build target from Java 21 to Java 25 while
preserving its existing behavior and keeping the work limited to runtime
compatibility. Verify and address the compatibility of the existing Spring Boot
3.5.6 / Spring Framework line with Java 25. Preserve those versions if
compatible; if compatibility requires changes, make only the changes necessary
for this runtime target. Align the backend container and the Java-version
references in developer-facing configuration, then run focused build and test
verification.

The scope is limited to the Java backend. It excludes unrelated assessment
findings, cloud-readiness changes, deployment work, and modernization of the
frontend or Python service.

## Target Evidence and Current-State Evidence

- The requested assessment is
  `BackEnd/.github/modernize/assessment/reports/report-20261112223134/report.json`.
  Its project metadata advertises the `openjdk25` capability, and its Java
  version rule identifies use of the latest LTS Java version as the finding
  addressed by this plan.
- Oracle's Java SE Support Roadmap lists Java SE 25 as an LTS release and says
  the next planned LTS is Java 29 in September 2027. Therefore Java 25 is the
  latest released LTS as of 2026-11-12:
  https://www.oracle.com/java/technologies/java-se-support-roadmap.html
- Repository evidence: `BackEnd/pom.xml` sets `java.version` to 21 and uses
  Spring Boot parent 3.5.6; `BackEnd/README.md` documents Java 21; and
  `deploy/Dockerfile.backend` uses Temurin 21 for both the Maven build and JRE
  runtime stages. The Maven Wrapper pins Maven 3.9.16.

## Migration Impact Summary

| Area | Current state | Target state | Scope |
|---|---|---|---|
| Backend Java | Java 21 | Java 25 | Maven compile/runtime compatibility |
| Spring stack | Boot 3.5.6 | Retain if compatible | Required Java 25 compatibility only |
| Backend container | Temurin 21 | Java 25 runtime | `deploy/Dockerfile.backend` |
| Verification | Existing Maven tests | Java 25 build and tests | Focused backend checks |

No Azure service migration, infrastructure provisioning, or deployment is
included.

## Execution Constraints

- Keep changes restricted to Java 25 runtime/build compatibility, the existing
  Spring stack as needed for that compatibility, backend container/developer
  configuration, and focused verification.
- Do not change Spring Boot or Spring Framework solely to address unrelated
  assessment findings. Establish compatibility against authoritative framework
  requirements before deciding whether a framework change is necessary.
- Do not modify frontend or Python runtime/container configuration.
- No source implementation changes are part of this planning phase.

## Open Questions & Questionnaire

No open questions. The request explicitly includes compatibility work required
for the existing Spring Boot 3.5.6 / Spring Framework stack; retain current
versions where compatible and constrain any necessary framework adjustment to
that requirement.
