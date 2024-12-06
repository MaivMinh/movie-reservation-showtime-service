package com.foolish.showtimeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "provinces")
public class Province {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String name;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "province", targetEntity = Cinema.class)
  private List<Cinema> cinemas;
}
