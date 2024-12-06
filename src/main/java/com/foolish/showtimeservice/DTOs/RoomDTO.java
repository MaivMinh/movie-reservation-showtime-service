package com.foolish.showtimeservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {
  private Integer id;
  private String name;
  private String location;
  private CinemaDTO cinema;
}
