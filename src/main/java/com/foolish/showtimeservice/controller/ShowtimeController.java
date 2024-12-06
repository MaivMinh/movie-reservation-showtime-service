package com.foolish.showtimeservice.controller;

import com.foolish.showtimeservice.DTOs.CinemaDTO;
import com.foolish.showtimeservice.DTOs.RoomDTO;
import com.foolish.showtimeservice.DTOs.SeatDTO;
import com.foolish.showtimeservice.DTOs.ShowtimeDTO;
import com.foolish.showtimeservice.constants.ROLE;
import com.foolish.showtimeservice.grpcClients.CheckMovieActiveStateServiceGrpcClient;
import com.foolish.showtimeservice.mapper.CinemaMapper;
import com.foolish.showtimeservice.mapper.RoomMapper;
import com.foolish.showtimeservice.mapper.ShowtimeMapper;
import com.foolish.showtimeservice.model.*;
import com.foolish.showtimeservice.response.ResponseData;
import com.foolish.showtimeservice.response.ResponseError;
import com.foolish.showtimeservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.ActiveStateResponse;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
  private final ProvinceService provinceService;
  private final CinemaService cinemaService;
  private final CinemaMapper cinemaMapper;
  private final RoomService roomService;
  private final RoomMapper roomMapper;
  private final ShowtimeMapper showtimeMapper;
  private final SeatService seatService;
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
    IdentifyResponse iResponse = identifyService.doIdentify(request);
    if (iResponse == null || !iResponse.getActive() || !iResponse.getRoles().equals(ROLE.ADMIN))
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
    IdentifyResponse iResponse = identifyService.doIdentify(request);
    if (iResponse == null || !iResponse.getActive() || !iResponse.getRoles().equals(ROLE.ADMIN))
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
  public ResponseEntity<ResponseData> deleteShowtime(@PathVariable Integer id) {
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


  // Phương thức tạo mới Cinema.
  @PostMapping(value = "/cinemas")
  public ResponseEntity<ResponseData> createCinema(@RequestBody @NotNull CinemaDTO dto) {
    /*Thống nhất là ADMIN chỉ tạo các thông tin Cinema phía dưới và không có đính kèm thêm ảnh. Vì đơn giản là tại thời điểm tạo Cinema thì không có id để cho các Banner có thể refer tới.*/

    /*
     * Cinema:
     * {
     *   name: String,
     *   address: String,
     *   province: Integer
     * }
     * */

    Province province = null;
    try {
      province = provinceService.findByIdOrElseThrow(dto.getProvince().getId());
    } catch (NullPointerException e) {
      throw new RuntimeException("Can't find province");
    }
    Cinema cinema = new Cinema();
    cinema.setName(dto.getName());
    cinema.setAddress(dto.getAddress());
    cinema.setProvince(province);
    cinema.setBanners(new ArrayList<>());
    Cinema saved = cinemaService.save(cinema);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can't save cinema"));
    }
    dto = cinemaMapper.toDTO(saved);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", dto));
  }


  // Hàm tìm kiếm theo tiêu chí cho Role ADMIN. Chỉ thiết kế chức năng tìm kiếm và trả về theo dạng danh sách cho Role này.
  @PostMapping(value = "/cinemas/search")
  public ResponseEntity<ResponseData> searchCinemasByCriteria(@RequestBody @NotNull Map<String, String> criteria, @RequestParam(name = "sort", required = false) String sort, @RequestParam(name = "pageSize", required = false) Integer pageSize, @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {

    int pageNum = (pageNumber != null ? pageNumber : 1) - 1;
    int size = pageSize != null ? pageSize : 10;
    Pageable pageable = null;

    if (sort != null) {
      // sort=id:desc,name:asc
      List<String> list = Arrays.stream(sort.split(",")).toList();
      List<Sort.Order> orders = new ArrayList<>();
      for (String element : list) {
        // Nếu fromString bị lỗi nó sẽ throw ra IllegalException và GlobalExceptionHandling sẽ catch nó trong RuntimeException.
        orders.add(new Sort.Order(Sort.Direction.fromString(element.split(":")[1].toUpperCase()), element.split(":")[0]));
      }

      pageable = PageRequest.of(pageNum, size, Sort.by(orders));
    } else pageable = PageRequest.of(pageNum, size);

    Page<CinemaDTO> page = cinemaService.findCinemasByCriteria(criteria, pageable);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", page));
  }

  // Phương thức lấy thông tin của một Cinema. Lưu ý, nếu như Role ADMIN thì sẽ trả về CinemaDTO có chứa ProvinceDTO.
  @GetMapping(value = "/cinemas/{id}")
  public ResponseEntity<ResponseData> getCinema(@PathVariable Integer id) {
    Cinema cinema = cinemaService.getCinemaByIdOrElseThrow(id);
    CinemaDTO data = cinemaMapper.toDTO(cinema);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", data));
  }

  // Phương thức update Cinema
  @Transactional
  @PatchMapping(value = "/cinemas/{id}")
  public ResponseEntity<ResponseData> updateCinema(@PathVariable Integer id, @RequestBody @NotNull CinemaDTO dto) {
    Cinema cinema = cinemaService.getCinemaByIdOrElseThrow(id);
    if (StringUtils.hasText(dto.getName())) {
      cinema.setName(dto.getName());
    }
    if (StringUtils.hasText(dto.getAddress())) cinema.setAddress(dto.getAddress());
    if (dto.getProvince() != null) cinema.setProvince(provinceService.findByIdOrElseThrow(dto.getProvince().getId()));

    Cinema saved = cinemaService.save(cinema);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can't save cinema"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }

  @Transactional
  @PostMapping(value = "/rooms")
  public ResponseEntity<ResponseData> createRoom(@RequestBody @NotNull RoomDTO dto) {
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
  @PatchMapping(value = "/rooms/{id}")
  public ResponseEntity<ResponseData> updateRoom(@PathVariable Integer id, @RequestBody RoomDTO dto) {
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


  @PostMapping(value = "/seats")
  public ResponseEntity<ResponseData> createSeat(@RequestBody SeatDTO dto) {
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
  public ResponseEntity<ResponseData> updateSeat(@PathVariable Integer id, @RequestBody SeatDTO dto) {
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
      return ResponseEntity.ok(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't save seat"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }


  @Transactional
  @DeleteMapping(value = "/seats/{id}")
  public ResponseEntity<ResponseData> deleteSeat(@PathVariable Integer id) {
    Seat seat = seatService.getSeatByIdOrElseThrow(id);
    seatService.delete(seat);
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }
}
