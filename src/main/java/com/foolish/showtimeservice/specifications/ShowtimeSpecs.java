package com.foolish.showtimeservice.specifications;

import com.foolish.showtimeservice.model.Showtime;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;


public class ShowtimeSpecs {
  public static  Specification<Showtime> containsShowtimeDate(Date date) {
    return (root, query, cb) -> {
      final Path<Date> d = root.get("date");
      return cb.equal(d, date);
    };
  }

  public static Specification<Showtime> containsMovieId(int movieId) {
    return (root, query, cb) -> {
      final Path<Integer> movie = root.get("movieId");
      return cb.equal(movie, movieId);
    };
  }
}
