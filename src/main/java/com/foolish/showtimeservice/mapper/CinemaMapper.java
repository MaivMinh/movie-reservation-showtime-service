package com.foolish.showtimeservice.mapper;

import com.foolish.showtimeservice.DTOs.CinemaDTO;
import com.foolish.showtimeservice.model.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {ProvinceMapper.class})
public interface CinemaMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "address", target = "address")
  /*
  * Ở @Mapping phía dưới, chúng ta mong muốn Mapping tử Province -> ProvinceDTO. Nhiệm vụ của chúng ta là chỉ cần Inject ProvinceMapperImpl dependency vào cho CinemaMapper. Sau đó, MapStruct sẽ tự động @Autowired ProvinceMapperImpl bên trong CinemaMapperImpl cho chúng ta và nó sẽ tạo ra operation cho việc mapping này.
  * */
  @Mapping(source = "province", target = "province")
  CinemaDTO toDTO(Cinema cinema);
}
