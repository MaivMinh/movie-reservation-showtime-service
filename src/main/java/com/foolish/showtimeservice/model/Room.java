package com.foolish.showtimeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String location;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cinema_id", referencedColumnName = "id")
  private Cinema cinema;

  @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
  private Set<Showtime> showtimes;

  @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE, targetEntity = Seat.class)
  private Set<Seat> seats;
}
