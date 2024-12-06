package com.foolish.showtimeservice.service;

import com.foolish.showtimeservice.DTOs.ProvinceDTO;
import com.foolish.showtimeservice.exceptions.ResourceNotFoundException;
import com.foolish.showtimeservice.mapper.ProvinceMapper;
import com.foolish.showtimeservice.model.Province;
import com.foolish.showtimeservice.repository.ProvinceRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProvinceService {
  private final ProvinceRepo provinceRepo;
  private final ProvinceMapper provinceMapper;

  public Province findByIdOrElseThrow(Integer id) {
    Optional<Province> result = provinceRepo.findById(id);
    return result.orElseThrow(() -> new ResourceNotFoundException("Province id not found", Map.of("province_id", String.valueOf(id))));
  }

  public List<ProvinceDTO> findAll() {
    List<Province> provinces = provinceRepo.findAll();
    return provinces.stream().map(provinceMapper::toDTO).toList();
  }
}
