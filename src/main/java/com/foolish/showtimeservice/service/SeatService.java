package com.foolish.showtimeservice.service;

import com.foolish.showtimeservice.DTOs.SeatDTO;
import com.foolish.showtimeservice.exceptions.ResourceNotFoundException;
import com.foolish.showtimeservice.mapper.SeatMapper;
import com.foolish.showtimeservice.model.Room;
import com.foolish.showtimeservice.model.Seat;
import com.foolish.showtimeservice.repository.SeatRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SeatService {

  private final SeatRepo seatRepo;
  private final RoomService roomService;
  private final SeatMapper seatMapper;

  public Seat save(Seat seat) {
    return seatRepo.save(seat);
  }

  public Seat getSeatByIdOrElseThrow(Integer id) {
    return seatRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id, Map.of("id", String.valueOf(id))));
  }

  public void delete(Seat seat) {
    seatRepo.delete(seat);
  }

  public List<SeatDTO> findAllByRoomId(Integer roomId) {
    Room room = roomService.getRoomByIdOrElseThrow(roomId);
    List<Seat> seats = seatRepo.findAllByRoom(room);
    return seats.stream().map(seatMapper::toDTO).toList();
  }
}
