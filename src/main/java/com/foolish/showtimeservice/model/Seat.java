package com.foolish.showtimeservice.model;

import com.foolish.showtimeservice.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Seats")
public class Seat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String type;
  private Double price;
  private String seatRow;
  private Integer seatNumber;

  @Enumerated(EnumType.STRING)
  private SeatStatus status;

  @ManyToOne
  @JoinColumn(name = "room_id", referencedColumnName = "id")
  private Room room;
}
