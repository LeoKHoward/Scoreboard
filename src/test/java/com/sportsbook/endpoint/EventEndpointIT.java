package com.sportsbook.endpoint;

import com.sportsbook.model.entity.Event;
import com.sportsbook.repository.EventRepository;
import com.sportsbook.model.dto.NewEvent;
import com.sportsbook.model.dto.UpdateScore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EventEndpointIT {

  public static final String MATCH_TITLE = "World Cup";
  public static final String HOME_TEAM = "England";
  public static final String AWAY_TEAM = "France";

  @Autowired private EventRepository eventRepository;

  @LocalServerPort private int port;

  @BeforeEach
  public void setUp() {
    eventRepository.deleteAllInBatch();
  }

  @Test
  void testGetAllEvents() {
    LocalDateTime testLocalDateTime = LocalDateTime.now();

    Event testEvent = new Event();
    testEvent.setMatchTitle(MATCH_TITLE);
    testEvent.setHomeTeamName(HOME_TEAM);
    testEvent.setAwayTeamName(AWAY_TEAM);
    testEvent.setHomeTeamScore(5);
    testEvent.setAwayTeamScore(1);
    testEvent.setScoreLastUpdatedTimestamp(testLocalDateTime);
    eventRepository.saveAndFlush(testEvent).getEventId();

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String url = String.format("http://localhost:%d/event", port);
    ResponseEntity<Event[]> allEventsResponse = testRestTemplate.getForEntity(url, Event[].class);

    assertThat(allEventsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(allEventsResponse.getBody().length).isEqualTo(1);

    Event actualEvent = allEventsResponse.getBody()[0];
    assertThat(actualEvent.getMatchTitle()).isEqualTo(MATCH_TITLE);
    assertThat(actualEvent.getHomeTeamName()).isEqualTo(HOME_TEAM);
    assertThat(actualEvent.getAwayTeamName()).isEqualTo(AWAY_TEAM);
    assertThat(actualEvent.getHomeTeamScore()).isEqualTo(5);
    assertThat(actualEvent.getAwayTeamScore()).isEqualTo(1);
    assertThat(actualEvent.getScoreLastUpdatedTimestamp()).isEqualTo(testLocalDateTime);
  }

  @Test
  void testGetEventById() {
    LocalDateTime testLocalDateTime = LocalDateTime.now();

    Event testEvent = new Event();
    testEvent.setMatchTitle(MATCH_TITLE);
    testEvent.setHomeTeamName(HOME_TEAM);
    testEvent.setAwayTeamName(AWAY_TEAM);
    testEvent.setHomeTeamScore(5);
    testEvent.setAwayTeamScore(1);
    testEvent.setScoreLastUpdatedTimestamp(testLocalDateTime);
    UUID testEventId = eventRepository.saveAndFlush(testEvent).getEventId();

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String url = String.format("http://localhost:%d/event/%s", port, testEventId);
    ResponseEntity<Event> eventByIdResponse = testRestTemplate.getForEntity(url, Event.class);

    assertThat(eventByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    Event actualEvent = eventByIdResponse.getBody();
    assertThat(actualEvent.getMatchTitle()).isEqualTo(MATCH_TITLE);
    assertThat(actualEvent.getHomeTeamName()).isEqualTo(HOME_TEAM);
    assertThat(actualEvent.getAwayTeamName()).isEqualTo(AWAY_TEAM);
    assertThat(actualEvent.getHomeTeamScore()).isEqualTo(5);
    assertThat(actualEvent.getAwayTeamScore()).isEqualTo(1);
    assertThat(actualEvent.getScoreLastUpdatedTimestamp()).isEqualTo(testLocalDateTime);
    assertThat(actualEvent.getEventId()).isEqualTo(testEventId);
  }

  @Test
  void testCreateNewEvent() {
    NewEvent testNewEvent = new NewEvent();
    testNewEvent.setMatchTitle(MATCH_TITLE);
    testNewEvent.setHomeTeamName(HOME_TEAM);
    testNewEvent.setAwayTeamName(AWAY_TEAM);

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String url = String.format("http://localhost:%d/event", port);
    ResponseEntity<Event> newEventResponse =
        testRestTemplate.postForEntity(url, testNewEvent, Event.class);

    assertThat(newEventResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    List<Event> actualEvents = eventRepository.findAll();
    Assertions.assertThat(actualEvents.size()).isEqualTo(1);

    Event actualEvent = actualEvents.get(0);
    assertThat(actualEvent.getMatchTitle()).isEqualTo(MATCH_TITLE);
    assertThat(actualEvent.getHomeTeamName()).isEqualTo(HOME_TEAM);
    assertThat(actualEvent.getAwayTeamName()).isEqualTo(AWAY_TEAM);
    assertThat(actualEvent.getHomeTeamScore()).isZero();
    assertThat(actualEvent.getAwayTeamScore()).isZero();
  }

  @Test
  void testUpdateScore() {
    Event testEvent = new Event();
    UUID testEventId = eventRepository.saveAndFlush(testEvent).getEventId();

    UpdateScore testUpdateScore = new UpdateScore();
    testUpdateScore.setHomeTeamScore(5);
    testUpdateScore.setAwayTeamScore(3);
    testUpdateScore.setScoreValidAtTimestamp(LocalDateTime.now());

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String url = String.format("http://localhost:%d/event/%s", port, testEventId);
    HttpEntity<UpdateScore> requestUpdate = new HttpEntity<>(testUpdateScore);
    ResponseEntity testResponse =
        testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, Void.class);

    assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    Event actualEvent = eventRepository.findById(testEventId).get();
    assertThat(actualEvent.getHomeTeamScore()).isEqualTo(5);
    assertThat(actualEvent.getAwayTeamScore()).isEqualTo(3);
  }

  @Test
  void testUpdateScoreButTimeNotMostRecentSoNotConsidered() {
    Event testEvent = new Event();
    testEvent.setHomeTeamScore(5);
    testEvent.setAwayTeamScore(3);
    testEvent.setScoreLastUpdatedTimestamp(LocalDateTime.now());
    UUID testEventId = eventRepository.saveAndFlush(testEvent).getEventId();

    UpdateScore testUpdateScoreBehindMostRecent = new UpdateScore();
    testUpdateScoreBehindMostRecent.setHomeTeamScore(4);
    testUpdateScoreBehindMostRecent.setAwayTeamScore(1);
    testUpdateScoreBehindMostRecent.setScoreValidAtTimestamp(LocalDateTime.now().minusMinutes(10));

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String url = String.format("http://localhost:%d/event/%s", port, testEventId);
    HttpEntity<UpdateScore> requestUpdate = new HttpEntity<>(testUpdateScoreBehindMostRecent);
    ResponseEntity testResponse =
        testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, Void.class);

    assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    Event actualEvent = eventRepository.findById(testEventId).get();
    assertThat(actualEvent.getHomeTeamScore()).isEqualTo(5);
    assertThat(actualEvent.getAwayTeamScore()).isEqualTo(3);
  }
}
