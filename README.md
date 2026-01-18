### Content 

Technical Specifications for SafetyNet Alerts
Technical Stack 2
Functional and Technical Specifications 2
URLs 3
Endpoints 4


### Technical Stack

● Spring Boot
● Maven preferred (Gradle can also be used)
● Code versioning in a Git repository on GitHub
● Java library for parsing JSON (e.g., Jackson or Gson)
● Unit testing with JUnit
● Code coverage measured using JaCoCo
● Logging (execution traces) with Log4j or Tinylog

### Functional and Technical Specifications

● The SafetyNet Alerts server starts successfully.
● All endpoint URLs function correctly.
● All endpoints log their requests and responses:
○ Successful responses are recorded at the Info level.
○ Errors or exceptions are recorded at the Error level.
○ Informational steps or calculations are recorded at the Debug level.
● Maven functions correctly, runs tests, and generates a code coverage report.
● All endpoints are covered by tests.
● The compilation generates a Surefire test report summarizing the JUnit test
results.
● The build includes a JaCoCo coverage report and reaches at least 80% code
coverage.
● SafetyNet Alerts is organized using MVC architecture.
● The codebase follows SOLID principles:
○ Single responsibility
○ Open/closed
○ Liskov substitutability
○ Interface segregation
○ Dependency inversion

### URLs

http://localhost:8080/firestation?stationNumber=<station_number>
This URL must return a list of people covered by the corresponding fire station. So, if
the station number = 1, it must return the residents covered by station number 1. The
list must include the following specific information: first name, last name, address,
phone number. Additionally, it must provide a count of the number of adults and the
number of children (any individual aged 18 years or younger) in the served area.
http://localhost:8080/childAlert?address=<address>
This URL must return a list of children (any individual aged 18 years or younger) living
at this address. The list must include the first name and last name of each child, their
age, and a list of other household members. If no children are found, this URL may
return an empty string.
http://localhost:8080/phoneAlert?firestation=<firestation_number>
This URL must return a list of phone numbers of residents served by the fire station. It
will be used to send emergency text messages to specific households.
http://localhost:8080/fire?address=<address>
This URL must return the list of residents living at the given address as well as the fire
station number serving the address. The list must include the name, phone number,
age, and medical history (medications, dosages, and allergies) of each person.
http://localhost:8080/flood/stations?stations=<a list of
station_numbers>
This URL must return a list of all households served by the fire station. This list must
group people by address. It must also include the name, phone number, and age of
the residents and display their medical history (medications, dosages, and allergies)
alongside each name.
http://localhost:8080/personInfolastName=<lastName>
This URL must return the name, address, age, email address, and medical history
(medications, dosages, and allergies) of each resident. If multiple people have the
same last name, they must all appear.
http://localhost:8080/communityEmail?city=<city>
This URL must return the email addresses of all residents in the city.

### Endpoints

The following endpoints will be required:
http://localhost:8080/person
This endpoint will allow the following actions via Post/Put/Delete with HTTP:
● Add a new person.
● Update an existing person (for now, assume that the first name and last name
do not change, but other fields can be modified).
● Delete a person (use a combination of first name and last name as a unique
identifier).
http://localhost:8080/firestation
This endpoint will allow the following actions via Post/Put/Delete with HTTP:
● Add a fire station/address mapping.
● Update the fire station number assigned to an address.
● Delete the mapping of a fire station or an address.
http://localhost:8080/medicalRecord
This endpoint will allow the following actions via Post/Put/Delete with HTTP:
● Add a medical record.
● Update an existing medical record (as previously mentioned, assume that the
first name and last name do not change).
● Delete a medical record (use a combination of first name and last name as a
unique identifier).
