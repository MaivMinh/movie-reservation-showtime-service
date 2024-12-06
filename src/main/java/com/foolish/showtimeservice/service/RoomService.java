package com.foolish.showtimeservice.service;

import com.foolish.showtimeservice.DTOs.RoomDTO;
import com.foolish.showtimeservice.exceptions.ResourceNotFoundException;
import com.foolish.showtimeservice.mapper.RoomMapper;
import com.foolish.showtimeservice.model.Room;
import com.foolish.showtimeservice.repository.RoomRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoomService {
  private final RoomRepo roomRepo;
  private final RoomMapper roomMapper;

  public Room getRoomById(Integer id) {
    return roomRepo.findById(id).orElse(null);
  }

  public RoomDTO getRoomDTOByIdOrElseThrow(Integer id) {
    Room room = roomRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room id not found", Map.of("room_id", String.valueOf(id))));
    return roomMapper.toDTO(room);
  }

  public Room save(Room room) {
    return roomRepo.save(room);
  }

  public Room getRoomByIdOrElseThrow(Integer id) {
    Optional<Room> room = roomRepo.findById(id);
    return room.orElseThrow(() -> new ResourceNotFoundException("Room id not found", Map.of("room_id", String.valueOf(id))));
  }
}
