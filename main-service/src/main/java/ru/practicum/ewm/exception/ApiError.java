package ru.practicum.ewm.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ApiError {
    String message;

    String reason;

    String status;

    @DateTimeFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    LocalDateTime timestamp;

}
