# birds-service
The purpose of this service is to maintain a persistent data store of birds, their characteristics, and sightings.

The REST API service supports:
* listing all birds and sightings
* bird queries by name and color
* sighting queries by bird, location, and time interval

## Development
`birds-service` is a Java project built with:
* Spring-Boot
* PostgreSQL
* Maven
* Java11

Unit and component tests are executed with a JUnit 5 runner, and they can be run within an IDE (e.g. IntelliJ) or as part of the Maven lifecycle

### Prerequisites
* Java 11
* [Maven](https://maven.apache.org/)
* [Docker](https://www.docker.com/get-started/)

### Build
 `mvn clean package docker:build -Drevision=latest`

### Unit tests
Run: `mvn clean verify`
There is also support for IT tests.

### Run
`docker-compose up -d`

### Local tests
Import `birds-service.postman_collection.json` in Postman and run the following actions:
* `save bird`                       -> add a new bird and returns its details
* `save sighting`                   -> adds a new sighting for a specific bird
* `get all birds`                   -> gives all registered birds
* `get bird by name and color`      -> get a bird by name and color
* `get all sightings for bird`      -> get all sighting for bird
* `search sightnigs`                -> search sighting by bird, location, and time interval

### Docker
Connect to postgresql docker database using
`docker exec -it container_id psql -U birdsservice -d birdsservicedb --password`

## Infrastructure
* **Dependencies** The direct dependencies for this project are:

| component               | expected address            | repository     |
|-------------------------|-----------------------------|----------------|
| birds-service-database  | birds-service-database:5432 | postgres:14.10 |

## Monitoring
* **Health check**:
`http://localhost:8888/rest/actuator/health`
