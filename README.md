# Scoreboard

This API allows football events to be created and persisted, scores to be updated, and the events viewed (via a client browser).

## Requirements

Java JDK 11, Maven and Docker required to build this project.

Postgres required to be able to run the API. 

Postgres is automatically started in a Docker container when you run the tests, but you will need to specify a Postgres instance to run against
in "production" mode. 

You can specify the postgres hostname, port, username and password in `src/main/resources/application.yml` or override them.

## Build & Test

To build and test, run `mvn clean install`

## Run Locally

If you need to override the default Postgres connection details, use the following environment variables:

- Postgres URL: `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>`
- Postgres username: `SPRING_DATASOURCE_USERNAME=<username>`
- Postgres password: `SPRING_DATASOURCE_PASSWORD=<password>`

Once built, you can start the API by running:
`java -jar target/sports-book-0.0.1-SNAPSHOT.jar`

Or, just start the application running in your favourite IDE using the `Application` class in the
project.

## API Contract

### Get All Events

`GET /event`

Example response body:

```json
[
  {
    "eventId": "5dd0ce7b-db45-44da-b1b2-64adeab06d2a",
    "matchTitle": "World Cup",
    "homeTeamName": "England",
    "awayTeamName": "France",
    "homeTeamScore": 5,
    "awayTeamScore": 1,
    "scoreLastUpdatedTimestamp": "2021-08-03T16:30:00.000"
  },
  {
    "id": "965093f4-298c-4106-88e4-130be49ac886",
    "matchTitle": "FA Cup Final",
    "homeTeamName": "Manchester United",
    "awayTeamName": "Chelsea",
    "homeTeamScore": 2,
    "awayTeamScore": 0,
    "scoreLastUpdatedTimestamp": "2020-03-01T12:15:01.123"
  }
]
```

### Get a single Event

`GET /event/<id>`

Example response body:

```json
{
  "eventId": "08c111cb-e86e-4e8b-bf40-88e7a28ec9f9",
  "matchTitle": "The UEFA Champions League Final",
  "homeTeamName": "Bristol City",
  "awayTeamName": "Bristol Rovers",
  "homeTeamScore": 10,
  "awayTeamScore": 2
}
```

### Create New Event

`POST /event`

Example required body to POST:

```json
{
  "matchTitle": "National League Playoff One",
  "homeTeamName": "Grimsby",
  "awayTeamName": "Boreham Wood"
}
```

Response is `201 CREATED` if successful.

Will automatically asign the new event a random ID.

### Update an Event Score

`PUT /event/<id>`

Example required body to PUT:

```json
{
  "homeTeamScore": 7,
  "awayTeamScore": 3,
  "scoreValidAtTimestamp": "2021-08-03T16:59:59.999"
}
```

Then when running a GET request on the event (or all events) you should see the scores have been updated.
