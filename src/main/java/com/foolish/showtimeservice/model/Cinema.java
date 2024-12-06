package com.foolish.showtimeservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "cinemas")
public class Cinema {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;


  private String name;
  private String address;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "province_id", referencedColumnName = "id")
  private Province province;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Banner.class, orphanRemoval = true)
  @JoinColumn(name = "cinema_id", referencedColumnName = "id")
  private List<Banner> banners;

  @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "cinema")
  private List<Room> room;
}
