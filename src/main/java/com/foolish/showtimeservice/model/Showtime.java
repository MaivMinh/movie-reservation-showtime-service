package com.foolish.showtimeservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Entity
@Getter
@Setter
@Table(name = "showtimes")
public class Showtime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @Column(name = "movie_id")
  private Integer movieId;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "room_id", referencedColumnName = "id")
  private Room room;

  @NotNull
  private Date date;

  @NotNull
  @Column(name = "start_time")
  private Time startTime;

  @NotNull
  @Column(name = "end_time")
  private Time endTime;
}
