package com.foolish.showtimeservice.controller;

import com.foolish.showtimeservice.DTOs.RoomDTO;
import com.foolish.showtimeservice.DTOs.SeatDTO;
import com.foolish.showtimeservice.mapper.RoomMapper;
import com.foolish.showtimeservice.model.Cinema;
import com.foolish.showtimeservice.model.Room;
import com.foolish.showtimeservice.model.Seat;
import com.foolish.showtimeservice.response.ResponseData;
import com.foolish.showtimeservice.response.ResponseError;
import com.foolish.showtimeservice.service.CinemaService;
import com.foolish.showtimeservice.service.IdentifyService;
import com.foolish.showtimeservice.service.RoomService;
import com.foolish.showtimeservice.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {
  private final IdentifyService identifyService;
  private final RoomService roomService;
  private final RoomMapper roomMapper;
  private final SeatService seatService;
  private final CinemaService cinemaService;


  @Transactional
  @PostMapping(value = "")
  public ResponseEntity<ResponseData> createRoom(@RequestBody @NotNull RoomDTO dto, HttpServletRequest request) {
    /*
    request-body: {
      name: String,
      location: String,
      cinema: {id: Integer}
    }

    response-data: {
      name: String,
      location: String,
      cinema: CinemaDTO
    }
    */

    // Kiểm tra xem user có quyền tạo room không.
    if (!identifyService.isAdmin(request)) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "You don't have permission to create room"));
    }

    Room room = new Room();
    room.setName(dto.getName());
    room.setLocation(dto.getLocation());
    Cinema cinema = cinemaService.getCinemaByIdOrElseThrow(dto.getCinema().getId());
    room.setCinema(cinema);
    Room saved = roomService.save(room);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can't save room"));
    }
    dto = roomMapper.toDTO(saved);

    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", dto));
  }


  // Phương thức update room.
  @Transactional
  @PatchMapping(value = "/{id}")
  public ResponseEntity<ResponseData> updateRoom(@PathVariable Integer id, @RequestBody RoomDTO dto, HttpServletRequest request) {
    if (!identifyService.isAdmin(request)) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "You don't have permission to update room"));
    }

    Room room = roomService.getRoomById(id);
    if (StringUtils.hasText(dto.getName())) {
      room.setName(dto.getName());
    }
    if (StringUtils.hasText(dto.getLocation())) room.setLocation(dto.getLocation());
    if (dto.getCinema() != null) {
      room.setCinema(cinemaService.getCinemaByIdOrElseThrow(dto.getCinema().getId()));
    }
    Room saved = roomService.save(room);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't save room"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }


  // Tạo ghế ngồi.
  @PostMapping(value = "/seats")
  public ResponseEntity<ResponseData> createSeat(@RequestBody SeatDTO dto, HttpServletRequest request) {
    if (!identifyService.isAdmin(request)) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "You don't have permission to create seat"));
    }

    Seat seat = new Seat();
    seat.setType(dto.getType());
    seat.setPrice(dto.getPrice());
    seat.setSeatRow(dto.getSeatRow());
    seat.setSeatNumber(dto.getSeatNumber());
    seat.setStatus(dto.getStatus());
    Room room = roomService.getRoomByIdOrElseThrow(dto.getRoomId());
    seat.setRoom(room);
    Seat saved = seatService.save(seat);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can't save seat"));
    }
    dto.setId(saved.getId());
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", dto));
  }

  @Transactional
  @PatchMapping(value = "/seats/{id}")
  public ResponseEntity<ResponseData> updateSeat(@PathVariable Integer id, @RequestBody SeatDTO dto, HttpServletRequest request) {
    if (!identifyService.isAdmin(request)) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "You don't have permission to update seat"));
    }
    Seat seat = seatService.getSeatByIdOrElseThrow(id);
    if (StringUtils.hasText(dto.getType())) {
      seat.setType(dto.getType());
    }
    if (dto.getPrice() != null) {
      seat.setPrice(dto.getPrice());
    }
    if (StringUtils.hasText(dto.getSeatRow())) {
      seat.setSeatRow(dto.getSeatRow());
    }
    if (dto.getSeatNumber() != null) {
      seat.setSeatNumber(dto.getSeatNumber());
    }
    if (dto.getStatus() != null) {
      seat.setStatus(dto.getStatus());
    }
    if (dto.getRoomId() != null) {
      seat.setRoom(roomService.getRoomByIdOrElseThrow(dto.getRoomId()));
    }
    Seat saved = seatService.save(seat);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't update seat"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }


  @Transactional
  @DeleteMapping(value = "/seats/{id}")
  public ResponseEntity<ResponseData> deleteSeat(@PathVariable Integer id, HttpServletRequest request) {
    if (!identifyService.isAdmin(request)) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "You don't have permission to delete seat"));
    }
    Seat seat = seatService.getSeatByIdOrElseThrow(id);
    seatService.delete(seat);
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }
}
