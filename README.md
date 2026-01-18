# SafetyNet Alerts

A Spring Boot application that provides emergency alert information (people, fire stations, medical records) through a set of REST endpoints.

## Table of contents

- [Project overview](#project-overview)
- [Technical stack](#technical-stack)
- [Functional & technical specifications](#functional--technical-specifications)
- [API: Query URLs](#api-query-urls)
- [API: Resource endpoints and actions](#api-resource-endpoints-and-actions)
- [Logging and testing](#logging-and-testing)
- [Architecture & design principles](#architecture--design-principles)

## Project overview

SafetyNet Alerts exposes endpoints to retrieve resident information, phone lists, children at an address, flood information grouped by station, and other emergency-related data. It also provides CRUD operations for persons, fire station mappings, and medical records.

## Technical stack

- Java + Spring Boot
- Maven (preferred; Gradle is also supported)
- JSON parsing: Jackson or Gson
- Unit testing: JUnit
- Code coverage: JaCoCo (target >= 80%)
- Test reports: Surefire plugin (JUnit results)
- Logging: Log4j or Tinylog
- Version control: GitHub

## Functional & technical specifications

- The SafetyNet Alerts server starts successfully.
- All endpoint URLs function correctly and return expected data.
- All endpoints log requests and responses:
  - Info level: successful responses
  - Error level: errors/exceptions
  - Debug level: informational steps or calculations
- Maven builds, runs tests, and generates a JaCoCo coverage report.
- All endpoints are covered by unit tests.
- The build produces a Surefire test report summarizing JUnit results.
- Code coverage target: at least 80% (measured by JaCoCo).
- The project follows MVC architecture.
- The codebase follows SOLID principles:
  - Single Responsibility
  - Open/Closed
  - Liskov Substitution
  - Interface Segregation
  - Dependency Inversion

## API — Query URLs (examples)

Use these GET-style query endpoints to retrieve emergency information.

- `/firestation?stationNumber=<station_number>`
  - Returns a list of people covered by the given fire station number.
  - Each person: `firstName`, `lastName`, `address`, `phone`.
  - Also returns counts: number of adults and number of children (children are age <= 18).

- `/childAlert?address=<address>`
  - Returns a list of children (age <= 18) at the given address.
  - Each child: `firstName`, `lastName`, `age`.
  - Include a list of other household members (adults and children).
  - If no children are found, may return an empty list.

- `/phoneAlert?firestation=<firestation_number>`
  - Returns a list of phone numbers for residents served by the given fire station.
  - Used to send emergency SMS notifications.

- `/fire?address=<address>`
  - Returns residents at the given address and the fire station number serving that address.
  - Each resident: `name`, `phone`, `age`, `medicalHistory` (medications with dosages, allergies).

- `/flood/stations?stations=<comma_separated_station_numbers>`
  - Returns households served by the listed stations, grouped by address.
  - For each person: `name`, `phone`, `age`, and medical history (medications, dosages, allergies).

- `/personInfo?lastName=<lastName>`
  - Returns name, address, age, email, and medical history for each resident with the given last name.
  - If multiple people share the last name, include them all.

- `/communityEmail?city=<city>`
  - Returns email addresses of all residents in the given city.

## API — Resource endpoints and actions

The following endpoints support CRUD operations via POST, PUT, DELETE (and GET where applicable).

- `/person`
  - POST: Add a new person.
  - PUT: Update an existing person (assume `firstName` + `lastName` are the unique identifiers that do not change).
  - DELETE: Delete a person (identify by `firstName` + `lastName`).

- `/firestation`
  - POST: Add a mapping from address to fire station number.
  - PUT: Update the fire station number assigned to an address.
  - DELETE: Remove a mapping (by address or by station mapping).

- `/medicalRecord`
  - POST: Add a medical record.
  - PUT: Update an existing medical record (assume `firstName` + `lastName` do not change).
  - DELETE: Delete a medical record (by `firstName` + `lastName`).

## Logging and testing

- Ensure endpoints log requests/responses at the appropriate levels:
  - Info for successful responses
  - Error for exceptions and failed requests
  - Debug for internal calculation and processing steps
- Unit tests should be implemented for all endpoints.
- Use Maven Surefire for test execution and reporting.
- Use JaCoCo to generate coverage reports and ensure coverage >= 80%.

## Architecture & design principles

- Follow MVC architecture (Controllers, Services, Repositories).
- Apply SOLID principles across the codebase.
- Keep classes small, well-tested, and single-responsibility.
- Prefer dependency injection for components and interfaces for testability.
