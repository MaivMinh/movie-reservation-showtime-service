package com.foolish.showtimeservice.mapper;

import com.foolish.showtimeservice.DTOs.ShowtimeDTO;
import com.foolish.showtimeservice.model.Showtime;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface ShowtimeMapper {
  ShowtimeDTO toDTO(Showtime showtime);
  Showtime toEntity(ShowtimeDTO showtimeDTO);
}
