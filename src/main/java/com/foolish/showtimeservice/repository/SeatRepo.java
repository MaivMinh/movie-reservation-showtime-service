package com.foolish.showtimeservice.repository;

import com.foolish.showtimeservice.model.Room;
import com.foolish.showtimeservice.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepo extends JpaRepository<Seat, Integer> {
  public List<Seat> findAllByRoom(Room room);
}
