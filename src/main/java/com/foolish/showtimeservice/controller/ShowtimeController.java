package com.foolish.showtimeservice.controller;

import com.foolish.showtimeservice.DTOs.ShowtimeDTO;
import com.foolish.showtimeservice.grpcClients.CheckMovieActiveStateServiceGrpcClient;
import com.foolish.showtimeservice.mapper.ShowtimeMapper;
import com.foolish.showtimeservice.model.*;
import com.foolish.showtimeservice.response.ResponseData;
import com.foolish.showtimeservice.response.ResponseError;
import com.foolish.showtimeservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.ActiveStateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/showtimes")
public class ShowtimeController {
  private final ShowtimeService showtimeService;
  private final RoomService roomService;
  private final ShowtimeMapper showtimeMapper;
  private final IdentifyService identifyService;
  private final CheckMovieActiveStateServiceGrpcClient checkActiveServiceClient;


  // Tạo showtime.
  @Transactional
  @PostMapping(value = "")
  public ResponseEntity<ResponseData> createShowtime(@RequestBody @NotNull Showtime showtime, HttpServletRequest request) {
    /*
    request-body: {
      movieId: Integer,
      room: {id: Integer},
      date: Date,
      start_time: Time,
      end_time: Time,
    }

    response-data: {
      id: Integer,
      movieId: Integer,
      room: RoomDTO,
      date: java.sql.Date,
      startTime: Time,
      endTime: Time,
    }
    */

    // Kiểm tra có phải là ADMIN không.
    if (!identifyService.isAdmin(request))
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));


    // Kiểm tra xem trạng thái của movie còn active(chưa hết hạn) hay khoong.
    ActiveStateResponse aResponse = checkActiveServiceClient.doCheckActiveState(showtime.getMovieId());
    if (!aResponse.getActive()) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Movie is not available for creating showtime"));
    }


    Room room = roomService.getRoomByIdOrElseThrow(showtime.getRoom().getId());
    showtime.setRoom(room);
    Showtime saved = showtimeService.save(showtime);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't save this showtime"));
    }
    ShowtimeDTO dto = showtimeMapper.toDTO(showtime);

    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", dto));
  }

  @Transactional
  @PatchMapping(value = "/{id}")
  public ResponseEntity<ResponseData> updateShowtime(@PathVariable(name = "id") Integer id, @RequestBody ShowtimeDTO updated, HttpServletRequest request) {
    /*
    request-body: {
      movieId: Integer,
      room: {id: Integer},
      date: Date,
      startTime: Time,
      endTime: Time
    }
    */

    // Kiểm tra có phải là ADMIN không.
    if (!identifyService.isAdmin(request))
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));


    Showtime showtime = showtimeService.getShowtimeByIdOrElseThrow(id);
    if (updated.getMovieId() != null) {
      showtime.setMovieId(updated.getMovieId());
    }
    if (updated.getRoom() != null) {
      showtime.setRoom(roomService.getRoomByIdOrElseThrow(updated.getRoom().getId()));
    }
    if (updated.getDate() != null) {
      showtime.setDate(updated.getDate());
    }
    if (updated.getStartTime() != null) {
      showtime.setStartTime(updated.getStartTime());
    }
    if (updated.getEndTime() != null) {
      showtime.setEndTime(updated.getEndTime());
    }

    Showtime saved = showtimeService.save(showtime);
    if (saved == null || saved.getId() <= 0)
      return ResponseEntity.ok(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't update this showtime"));
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<ResponseData> deleteShowtime(@PathVariable Integer id, HttpServletRequest request) {

    // Kiểm tra có phải là ADMIN không.
    if (!identifyService.isAdmin(request))
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));

    Showtime showtime = showtimeService.getShowtimeByIdOrElseThrow(id);
    showtimeService.delete(showtime);
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }

  // Hàm thực hiên tìm kiếm Showtimes thông qua các tiêu chí - criteria. OK
  @PostMapping(value = "/search")
  public ResponseEntity<ResponseData> searchShowtimesByCriteria(@RequestBody Map<String, String> criteria, @RequestParam(defaultValue = "0") Integer pageNumber, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(name = "sort", required = false) String sort) {

    int size = pageSize < 1 ? 10 : pageSize;
    int number = pageNumber < 0 ? 0 : pageNumber - 1;
    Pageable pageable;

    if (sort != null) {
      // sort=id:desc,date:asc
      List<String> list = Arrays.stream(sort.split(",")).toList();
      List<Sort.Order> orders = new ArrayList<>();
      for (String element : list) {
        // Nếu fromString bị lỗi nó sẽ throw ra IllegalException và GlobalExceptionHandling sẽ catch nó trong RuntimeException.
        orders.add(new Sort.Order(Sort.Direction.fromString(element.split(":")[1].toUpperCase()), element.split(":")[0]));
      }

      pageable = PageRequest.of(number, size, Sort.by(orders));
    } else pageable = PageRequest.of(number, size);

    Page<ShowtimeDTO> result = showtimeService.getShowtimesByCriteria(criteria, pageable);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", result));
  }

  // Thực hiện lấy thông tin Showtime thông qua id.
  @GetMapping("/{id}")
  public ResponseEntity<ResponseData> getShowtimeById(Integer id, HttpServletRequest request) {
    ShowtimeDTO showtimeDTO = showtimeService.getShowtimeDTOByIdOrElseThrow(id);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", showtimeDTO));
  }

}
