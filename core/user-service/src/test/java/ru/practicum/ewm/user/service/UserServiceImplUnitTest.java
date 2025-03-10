package ru.practicum.ewm.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.practicum.ewm.user.util.UserTestUtil.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;
    private NewUserRequest newUserRequest;
    private List<User> users;
    private List<UserDto> userDtos;


    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(USER_ID_1);
        user1.setName(USER_NAME_1);
        user1.setEmail(EMAIL_1);

        user2 = new User();
        user2.setId(USER_ID_2);
        user2.setName(USER_NAME_2);
        user2.setEmail(EMAIL_2);

        userDto1 = UserDto.builder().id(USER_ID_1).name(USER_NAME_1).email(EMAIL_1).build();
        userDto2 = UserDto.builder().id(USER_ID_2).name(USER_NAME_2).email(EMAIL_2).build();
        newUserRequest = new NewUserRequest(EMAIL_1, USER_NAME_1);

        users = List.of(user1, user2);
        userDtos = List.of(userDto1, userDto2);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(userRepository, mapper);
    }

    @Test
    void delete() {
        when(userRepository.existsById(USER_ID_1)).thenReturn(true);

        userService.delete(USER_ID_1);

        verify(userRepository).deleteById(USER_ID_1);
    }

    @Test
    void testDeleteWhetNotExists() {
        when(userRepository.existsById(USER_ID_1)).thenReturn(false);

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService.delete(USER_ID_1));
        assertThat(e.getMessage(), is("User with id = " + USER_ID_1 + " not found"));

        verify(userRepository).existsById(USER_ID_1);
        verify(userRepository, never()).deleteById(anyLong());
    }
}