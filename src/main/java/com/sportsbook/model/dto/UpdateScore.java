package com.sportsbook.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateScore {

  private int homeTeamScore;

  private int awayTeamScore;

  private LocalDateTime scoreValidAtTimestamp;
}
