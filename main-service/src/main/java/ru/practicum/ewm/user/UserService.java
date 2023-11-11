package ru.practicum.ewm.user;

import java.util.List;

public interface UserService {
    List<UserDto> findAll(List<Long> ids, int from, int size);

    UserDto createUser(UserDto userDto);

    void deleteUserById(Long userId);
}
