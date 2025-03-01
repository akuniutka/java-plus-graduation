package ru.practicum.ewm.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.ewm.user.util.UserTestUtil.*;

class UserMapperTest {
    private final UserMapper userMapper = new UserMapper();

    @Test
    void testNewUserRequestToUser() {
        NewUserRequest request = new NewUserRequest(EMAIL_1, USER_NAME_1);

        User result = userMapper.mapToUser(request);

        assertNotNull(result);
        assertEquals(USER_NAME_1, result.getName());
        assertEquals(EMAIL_1, result.getEmail());
    }

    @Test
    void testNullNewUserRequestToUser() {
        assertNull(userMapper.mapToUser((NewUserRequest) null));
    }

    @Test
    void testIdToUser() {
        User result = userMapper.mapToUser(USER_ID_1);

        assertNotNull(result);
        assertEquals(USER_ID_1, result.getId());
    }

    @Test
    void testNullIdToUser() {
        assertNull(userMapper.mapToUser((Long) null));
    }

    @Test
    void testUserToUserDto() {
        User user = new User();
        user.setId(USER_ID_1);
        user.setName(USER_NAME_1);
        user.setEmail(EMAIL_1);

        UserDto result = userMapper.mapToDto(user);

        assertNotNull(result);
        assertEquals(USER_ID_1, result.id());
        assertEquals(USER_NAME_1, result.name());
        assertEquals(EMAIL_1, result.email());
    }

    @Test
    void testNullUserToUserDto() {
        assertNull(userMapper.mapToDto((User) null));
    }

    @Test
    void testListOfUsersToListOfUserDtos() {
        User user1 = new User();
        user1.setId(USER_ID_1);
        user1.setName(USER_NAME_1);
        user1.setEmail(EMAIL_1);

        User user2 = new User();
        user2.setId(USER_ID_2);
        user2.setName(USER_NAME_2);
        user2.setEmail(EMAIL_2);

        List<UserDto> result = userMapper.mapToDto(List.of(user1, user2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(USER_ID_1, result.get(0).id());
        assertEquals(USER_NAME_1, result.get(0).name());
        assertEquals(EMAIL_1, result.get(0).email());
        assertEquals(USER_ID_2, result.get(1).id());
        assertEquals(USER_NAME_2, result.get(1).name());
        assertEquals(EMAIL_2, result.get(1).email());
    }

    @Test
    void testNullListToListOfUsersDtos() {
        assertNull(userMapper.mapToDto((List<User>) null));
    }

    @Test
    void testUserToUserShortDto() {
        User user = new User();
        user.setId(USER_ID_1);
        user.setName(USER_NAME_1);

        UserShortDto result = userMapper.mapToShortDto(user);

        assertNotNull(result);
        assertEquals(USER_ID_1, result.id());
        assertEquals(USER_NAME_1, result.name());
    }

    @Test
    void testNullUserToUserShortDto() {
        assertNull(userMapper.mapToShortDto((User) null));
    }
}
