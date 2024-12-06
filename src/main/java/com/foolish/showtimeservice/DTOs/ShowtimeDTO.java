package com.foolish.showtimeservice.DTOs;

import com.foolish.showtimeservice.DTOs.RoomDTO;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
public class ShowtimeDTO {
  private Integer id;
  private Integer movieId;
  private RoomDTO room;
  private Date date;
  private Time startTime;
  private Time endTime;
}
