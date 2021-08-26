package com.sportsbook.model.dto;

import lombok.Data;

@Data
public class NewEvent {

  private String matchTitle;
  private String homeTeamName;
  private String awayTeamName;
}
