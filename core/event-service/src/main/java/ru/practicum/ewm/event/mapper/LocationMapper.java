package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.model.Location;

@Mapper
public interface LocationMapper {

    Location mapToLocation(LocationDto dto);

    LocationDto mapToDto(Location location);
}
