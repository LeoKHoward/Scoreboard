package com.sportsbook.endpoint;

import com.sportsbook.model.entity.Event;
import com.sportsbook.repository.EventRepository;
import com.sportsbook.model.dto.NewEvent;
import com.sportsbook.model.dto.UpdateScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/event")
public class EventEndpoint {

  private final EventRepository eventRepository;

  @Autowired
  public EventEndpoint(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @GetMapping
  public List<Event> getAllEvents() {
    return eventRepository.findAll();
  }

  @GetMapping(path = "/{eventId}")
  public Event getEventById(@PathVariable("eventId") UUID eventId) {
    return eventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  public ResponseEntity createNewEvent(@RequestBody NewEvent newEvent) {
    Event event = new Event();
    event.setMatchTitle(newEvent.getMatchTitle());
    event.setHomeTeamName(newEvent.getHomeTeamName());
    event.setAwayTeamName(newEvent.getAwayTeamName());
    eventRepository.saveAndFlush(event);

    return new ResponseEntity(HttpStatus.CREATED);
  }

  // Has to wait for an update to finish before sending another (not concurrent)
  @PutMapping(path = "/{eventId}")
  public synchronized void updateScore(
      @PathVariable("eventId") UUID eventId, @RequestBody UpdateScore updateScore) {
    Event event =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (event.getScoreLastUpdatedTimestamp() != null
        && event.getScoreLastUpdatedTimestamp().isAfter(updateScore.getScoreValidAtTimestamp())) {
      return;
    }

    event.setHomeTeamScore(updateScore.getHomeTeamScore());
    event.setAwayTeamScore(updateScore.getAwayTeamScore());
    event.setScoreLastUpdatedTimestamp(updateScore.getScoreValidAtTimestamp());
    eventRepository.saveAndFlush(event);
  }
}
