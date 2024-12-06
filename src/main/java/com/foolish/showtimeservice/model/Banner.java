package com.foolish.showtimeservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Banners")
@Getter
@Setter
public class Banner {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @NotNull
  private String url;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cinema_id")
  private Cinema cinema;
}
