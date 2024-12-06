package com.foolish.showtimeservice.specifications;

import com.foolish.showtimeservice.model.Cinema;
import com.foolish.showtimeservice.model.Province;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class CinemaSpecs {
  public static Specification<Cinema> containsName(String name) {
    return new Specification<Cinema>() {
      @Override
      public Predicate toPredicate(Root<Cinema> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.like(root.get("name"), "%" + name + "%");
      }
    };
  }

  public static Specification<Cinema> containsAddress(String address) {
    return new Specification<Cinema>() {
      @Override
      public Predicate toPredicate(Root<Cinema> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.like(root.get("address"), "%" + address + "%");
      }
    };
  }

  public static Specification<Cinema> hasProvinceId(Integer province_id) {
    return new Specification<Cinema>() {
      @Override
      public Predicate toPredicate(Root<Cinema> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<Province> province = root.get("province");
        return criteriaBuilder.equal(province.get("id"), province_id);
      }
    };
  }

}
