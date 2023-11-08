package ru.practicum.ewm.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<UserDto> users;
        Page<User> userPage;
        if (ids != null && !ids.isEmpty()) {
            userPage = userRepository.findByIdIn(ids, page);
        } else {
            userPage = userRepository.findAll(page);
        }

        users = userPage
                .getContent()
                .stream()
                .map(UserMapper::mapUserToUserDto)
                .collect(Collectors.toList());

        return users;
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.mapUserDtoToUser(userDto);
        userDto = UserMapper.mapUserToUserDto(userRepository.save(user));
        log.info("Пользователь с идентификатором {} и почтой {} был создан", user.getId(), user.getEmail());
        return userDto;
    }

    @Transactional
    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}
