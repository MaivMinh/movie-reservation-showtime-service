package com.foolish.showtimeservice.mapper;

import com.foolish.showtimeservice.DTOs.RoomDTO;
import com.foolish.showtimeservice.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CinemaMapper.class})
public interface RoomMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "location", target = "location")
  RoomDTO toDTO(Room room);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "location", target = "location")
  Room toEntity(RoomDTO roomDTO);
}
