package com.foolish.showtimeservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaDTO {
  private Integer id;
  private String name;
  private String address;
  private ProvinceDTO province;
}
