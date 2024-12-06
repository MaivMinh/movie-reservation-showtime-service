package com.foolish.showtimeservice.repository;

import com.foolish.showtimeservice.model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CinemaRepo extends JpaRepository<Cinema, Integer>, JpaSpecificationExecutor<Cinema> {
  @Query(value = "select * from cinemas C where C.province_id = :province", nativeQuery = true)
  List<Cinema> findCinemasByProvinceId(@Param("province") Integer province);
}
