package com.foolish.showtimeservice.mapper;

import com.foolish.showtimeservice.DTOs.SeatDTO;
import com.foolish.showtimeservice.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface SeatMapper {

  @Mapping(source = "room.id", target = "roomId")
  SeatDTO toDTO(Seat seat);

  @Mapping(source = "roomId", target = "room.id")
  Seat toEntity(SeatDTO seatDTO);
}
