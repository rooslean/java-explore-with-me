package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;


@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({ObjectNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleObjectNotFound(final RuntimeException e) {
        log.info(e.getMessage(), e);
        return new ApiError("Объект не найден", e.getMessage(), HttpStatus.NOT_FOUND.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationError(final MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return new ApiError("Ошибка валидации", e.getMessage(), HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingReqParamError(final MissingServletRequestParameterException e) {
        log.warn(e.getMessage());
        return new ApiError("Отсутствует обязательный параметр", e.getMessage(), HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatchError(final MethodArgumentTypeMismatchException e) {
        log.warn(e.getMessage());
        return new ApiError("Unknown " + e.getName() + ": " + e.getValue(), e.getMessage(),
                HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictError(final ConflictException e) {
        log.warn(e.getMessage());
        return new ApiError("Ошибка при создании", e.getMessage(), HttpStatus.CONFLICT.toString(), LocalDateTime.now());
    }

    @ExceptionHandler({DataAccessException.class, JDBCException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictError(final RuntimeException e) {
        log.warn(e.getMessage());
        return new ApiError("Ошибка при выполнении SQL запроса", e.getMessage(), HttpStatus.CONFLICT.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestError(final BadRequestException e) {
        log.warn(e.getMessage());
        return new ApiError("Некорректный запрос", e.getMessage(), HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleBadRequestError(final Throwable e) {
        log.warn(e.getMessage());
        log.warn(Arrays.toString(e.getStackTrace()));
        return new ApiError("Ошибка сервера", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), LocalDateTime.now());
    }
}
