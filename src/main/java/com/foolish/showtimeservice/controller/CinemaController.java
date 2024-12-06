package com.foolish.showtimeservice.controller;

import com.foolish.showtimeservice.DTOs.CinemaDTO;
import com.foolish.showtimeservice.mapper.CinemaMapper;
import com.foolish.showtimeservice.model.Cinema;
import com.foolish.showtimeservice.model.Province;
import com.foolish.showtimeservice.response.ResponseData;
import com.foolish.showtimeservice.response.ResponseError;
import com.foolish.showtimeservice.service.CinemaService;
import com.foolish.showtimeservice.service.IdentifyService;
import com.foolish.showtimeservice.service.ProvinceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
  private final CinemaService cinemaService;
  private final ProvinceService provinceService;
  private final CinemaMapper cinemaMapper;
  private final IdentifyService identifyService;

  // Phương thức tạo mới Cinema.
  @PostMapping(value = "")
  public ResponseEntity<ResponseData> createCinema(@RequestBody @NotNull CinemaDTO dto, HttpServletRequest request) {
    /*Thống nhất là ADMIN chỉ tạo các thông tin Cinema phía dưới và không có đính kèm thêm ảnh. Vì đơn giản là tại thời điểm tạo Cinema thì không có id để cho các Banner có thể refer tới.*/

    /*
     * Cinema:
     * {
     *   name: String,
     *   address: String,
     *   province: ProvinceDTO{id, name}
     * }
     * */

    // Kiểm tra có phải là ADMIN không.
    if (!identifyService.isAdmin(request))
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));

    Province province = provinceService.findByIdOrElseThrow(dto.getProvince().getId());
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
  @PostMapping(value = "/search")
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


  @GetMapping(value = "/{id}")
  public ResponseEntity<ResponseData> getCinema(@PathVariable Integer id) {
    Cinema cinema = cinemaService.getCinemaByIdOrElseThrow(id);
    CinemaDTO data = cinemaMapper.toDTO(cinema);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", data));
  }

  // Phương thức update Cinema
  @Transactional
  @PatchMapping(value = "/{id}")
  public ResponseEntity<ResponseData> updateCinema(@PathVariable Integer id, @RequestBody @NotNull CinemaDTO dto, HttpServletRequest request) {
    // Kiểm tra có phải là ADMIN không.
    if (!identifyService.isAdmin(request))
      return ResponseEntity.ok(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));

    Cinema cinema = cinemaService.getCinemaByIdOrElseThrow(id);
    if (StringUtils.hasText(dto.getName())) {
      cinema.setName(dto.getName());
    }
    if (StringUtils.hasText(dto.getAddress())) cinema.setAddress(dto.getAddress());
    if (dto.getProvince() != null) cinema.setProvince(provinceService.findByIdOrElseThrow(dto.getProvince().getId()));

    Cinema saved = cinemaService.save(cinema);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.ok(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Can't update cinema"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.NO_CONTENT.value(), "Success", null));
  }

}
