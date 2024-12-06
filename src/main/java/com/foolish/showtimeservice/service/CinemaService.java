package com.foolish.showtimeservice.service;

import com.foolish.showtimeservice.DTOs.CinemaDTO;
import com.foolish.showtimeservice.exceptions.ResourceNotFoundException;
import com.foolish.showtimeservice.mapper.CinemaMapper;
import com.foolish.showtimeservice.model.Cinema;
import com.foolish.showtimeservice.repository.CinemaRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.foolish.showtimeservice.specifications.CinemaSpecs.*;

@Service
@AllArgsConstructor
public class CinemaService {

  private final CinemaRepo cinemaRepo;
  private final CinemaMapper cinemaMapper;

  public Cinema getCinemaByIdOrElseThrow(Integer id) {
    Optional<Cinema> cinema = cinemaRepo.findById(id);
    return cinema.orElseThrow(() -> new ResourceNotFoundException("Cinema not found", Map.of("cinema_id", String.valueOf(id))));
  }

  public Cinema save(@Valid Cinema cinema) {
    return cinemaRepo.save(cinema);
  }

  public Page<CinemaDTO> findCinemasByCriteria(@NotNull Map<String, String> criteria, Pageable pageable) {
    Specification<Cinema> specification = Specification.where(null);
    if (StringUtils.hasText(criteria.get("name"))) {
      specification = specification.and(containsName(criteria.get("name")));
    }
    if (StringUtils.hasText(criteria.get("address"))) {
      specification = specification.and(containsAddress(criteria.get("address")));
    }
    if (StringUtils.hasText(criteria.get("province_id"))) {
      specification = specification.and(hasProvinceId(Integer.parseInt(criteria.get("province_id"))));
    }
    Page<Cinema> result = cinemaRepo.findAll(specification, pageable);
    List<CinemaDTO> list = result.getContent().stream().map(cinemaMapper::toDTO).toList();

    return new PageImpl<>(list, pageable, result.getTotalElements());
  }

  public CinemaDTO getCinemaDTOById(Integer id) {
    Optional<Cinema> result = cinemaRepo.findById(id);
    return cinemaMapper.toDTO(result.orElse(null));
  }

  public CinemaDTO getCinemaDTOByIdOrElseThrow(Integer id) {
    Optional<Cinema> result = cinemaRepo.findById(id);
    return cinemaMapper.toDTO(result.orElseThrow(() -> new ResourceNotFoundException("Cinema not found",Map.of("cinema_id", String.valueOf(id)))));
  }

  public List<CinemaDTO> getCinemasByProvinceId(Integer id) {
    List<Cinema> provinces = cinemaRepo.findCinemasByProvinceId(id);
    return provinces.stream().map(cinemaMapper::toDTO).toList();
  }
}
