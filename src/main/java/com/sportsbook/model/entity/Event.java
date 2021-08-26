package com.sportsbook.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Event {

  @Id
  @GeneratedValue
  private UUID eventId;

  @Column private String matchTitle;

  @Column private String homeTeamName;

  @Column private String awayTeamName;

  @Column private int homeTeamScore;

  @Column private int awayTeamScore;

  @Column private LocalDateTime scoreLastUpdatedTimestamp;

  @PrePersist
  private void assignEventId() {
    eventId = UUID.randomUUID();
  }
}
