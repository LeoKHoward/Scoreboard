package com.sportsbook.endpoint;

import com.sportsbook.model.entity.Event;
import com.sportsbook.repository.EventRepository;
import com.sportsbook.model.dto.NewEvent;
import com.sportsbook.model.dto.UpdateScore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventEndpointUnitTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  public static final String MATCH_TITLE = "World Cup";
  public static final String HOME_TEAM = "England";
  public static final String AWAY_TEAM = "France";

  static {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Mock private EventRepository eventRepository;

  @InjectMocks private EventEndpoint underTest;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
  }

  @Test
  void testGetAllEvents() throws Exception {
    // Given
    Event testEvent = new Event();
    testEvent.setMatchTitle(MATCH_TITLE);
    testEvent.setHomeTeamName(HOME_TEAM);
    testEvent.setAwayTeamName(AWAY_TEAM);
    testEvent.setHomeTeamScore(5);
    testEvent.setAwayTeamScore(1);

    // When
    when(eventRepository.findAll()).thenReturn(List.of(testEvent));

    // Then
    mockMvc
        .perform(get("/event"))
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("getAllEvents"))
        .andExpect(jsonPath("$[0].matchTitle", is(MATCH_TITLE)))
        .andExpect(jsonPath("$[0].homeTeamName", is(HOME_TEAM)))
        .andExpect(jsonPath("$[0].awayTeamName", is(AWAY_TEAM)))
        .andExpect(jsonPath("$[0].homeTeamScore", is(5)))
        .andExpect(jsonPath("$[0].awayTeamScore", is(1)));
  }

  @Test
  void testGetEventById() throws Exception {
    // Given
    UUID testEventId = UUID.randomUUID();

    Event testEvent = new Event();
    testEvent.setMatchTitle(MATCH_TITLE);
    testEvent.setHomeTeamName(HOME_TEAM);
    testEvent.setAwayTeamName(AWAY_TEAM);
    testEvent.setHomeTeamScore(3);
    testEvent.setAwayTeamScore(0);

    // When Then
    when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

    mockMvc
        .perform(get(String.format("/event/%s", testEventId)))
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("getEventById"))
        .andExpect(jsonPath("matchTitle", is(MATCH_TITLE)))
        .andExpect(jsonPath("homeTeamName", is(HOME_TEAM)))
        .andExpect(jsonPath("awayTeamName", is(AWAY_TEAM)))
        .andExpect(jsonPath("homeTeamScore", is(3)))
        .andExpect(jsonPath("awayTeamScore", is(0)));
  }

  @Test
  void testCreateNewEvent() throws Exception {

    // Given
    NewEvent newTestEvent = new NewEvent();
    newTestEvent.setMatchTitle(MATCH_TITLE);
    newTestEvent.setHomeTeamName(HOME_TEAM);
    newTestEvent.setAwayTeamName(AWAY_TEAM);

    // When
    mockMvc
        .perform(
            post("/event")
                .content(asJsonString(newTestEvent))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("createNewEvent"));

    // Then
    ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);

    verify(eventRepository).saveAndFlush(eventArgumentCaptor.capture());

    Event capturedEvent = eventArgumentCaptor.getValue();
    assertThat(capturedEvent.getMatchTitle()).isEqualTo(MATCH_TITLE);
    assertThat(capturedEvent.getHomeTeamName()).isEqualTo(HOME_TEAM);
    assertThat(capturedEvent.getAwayTeamName()).isEqualTo(AWAY_TEAM);
    Assertions.assertThat(capturedEvent.getHomeTeamScore()).isZero();
    Assertions.assertThat(capturedEvent.getAwayTeamScore()).isZero();
  }

  @Test
  void testUpdateScoreFirstTime() throws Exception {
    // Given
    LocalDateTime testLocalDateTime = LocalDateTime.now();
    UUID testEventId = UUID.randomUUID();

    UpdateScore testUpdateScoreFirstTime = new UpdateScore();
    testUpdateScoreFirstTime.setHomeTeamScore(1);
    testUpdateScoreFirstTime.setAwayTeamScore(0);
    testUpdateScoreFirstTime.setScoreValidAtTimestamp(testLocalDateTime);

    Event testEvent = new Event();
    when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

    // When
    mockMvc
        .perform(
            put(String.format("/event/%s", testEventId))
                .content(asJsonString(testUpdateScoreFirstTime))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("updateScore"));

    // Then
    ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);

    verify(eventRepository).saveAndFlush(eventArgumentCaptor.capture());

    Event capturedEvent = eventArgumentCaptor.getValue();
    assertThat(capturedEvent.getHomeTeamScore()).isEqualTo(1);
    assertThat(capturedEvent.getAwayTeamScore()).isEqualTo(0);
    assertThat(capturedEvent.getScoreLastUpdatedTimestamp()).isEqualTo(testLocalDateTime);
  }

  @Test
  void testUpdateScoreSecondTime() throws Exception {
    // Given
    LocalDateTime testLocalDateTime = LocalDateTime.now();
    UUID testEventId = UUID.randomUUID();

    UpdateScore testUpdateScoreSecondTime = new UpdateScore();
    testUpdateScoreSecondTime.setHomeTeamScore(5);
    testUpdateScoreSecondTime.setAwayTeamScore(1);
    testUpdateScoreSecondTime.setScoreValidAtTimestamp(testLocalDateTime);

    Event testEvent = new Event();
    testEvent.setHomeTeamScore(3);
    testEvent.setAwayTeamScore(0);
    testEvent.setScoreLastUpdatedTimestamp(LocalDateTime.now().minusMinutes(10));
    when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

    // When
    mockMvc
        .perform(
            put(String.format("/event/%s", testEventId))
                .content(asJsonString(testUpdateScoreSecondTime))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("updateScore"));

    // Then
    ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);

    verify(eventRepository).saveAndFlush(eventArgumentCaptor.capture());

    Event capturedEvent = eventArgumentCaptor.getValue();
    assertThat(capturedEvent.getHomeTeamScore()).isEqualTo(5);
    assertThat(capturedEvent.getAwayTeamScore()).isEqualTo(1);
    assertThat(capturedEvent.getScoreLastUpdatedTimestamp()).isEqualTo(testLocalDateTime);
  }

  @Test
  void testUpdateScoreSecondTimeButTimeNotMostRecentSoNotConsidered() throws Exception {
    // Given
    LocalDateTime testLocalDateTime = LocalDateTime.now();
    UUID testEventId = UUID.randomUUID();

    UpdateScore testUpdateScoreBehindMostRecent = new UpdateScore();
    testUpdateScoreBehindMostRecent.setHomeTeamScore(3);
    testUpdateScoreBehindMostRecent.setAwayTeamScore(1);
    testUpdateScoreBehindMostRecent.setScoreValidAtTimestamp(testLocalDateTime.minusMinutes(10));

    Event testEvent = new Event();
    testEvent.setScoreLastUpdatedTimestamp(testLocalDateTime);

    when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

    // When
    mockMvc
        .perform(
            put(String.format("/event/%s", testEventId))
                .content(asJsonString(testUpdateScoreBehindMostRecent))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("updateScore"));

    // Then
    verify(eventRepository, never()).saveAndFlush(any(Event.class));
  }

  @Test
  void testUpdateUnknownEvent() throws Exception {
    // Given
    UUID testEventId = UUID.randomUUID();

    when(eventRepository.findById(testEventId)).thenReturn(Optional.empty());

    UpdateScore testUpdateScore = new UpdateScore();

    // When
    mockMvc
        .perform(
            put(String.format("/event/%s", testEventId))
                .content(asJsonString(testUpdateScore))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(handler().handlerType(EventEndpoint.class))
        .andExpect(handler().methodName("updateScore"));

    // Then
    verify(eventRepository, never()).saveAndFlush(any(Event.class));
  }

  public static String asJsonString(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
}
