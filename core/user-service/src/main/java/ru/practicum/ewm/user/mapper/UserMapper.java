package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Mapper
public interface UserMapper {

    User mapToUser(NewUserRequest dto);

    UserDto mapToDto(User user);

    List<UserDto> mapToDto(List<User> users);

    UserShortDto mapToShortDto(User user);

    List<UserShortDto> mapToShortDto(List<User> users);
}
