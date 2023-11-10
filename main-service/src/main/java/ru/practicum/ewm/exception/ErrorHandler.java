package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;


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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestError(final BadRequestException e) {
        log.warn(e.getMessage());
        return new ApiError("Некорректный запрос", e.getMessage(), HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

/*    @ExceptionHandler({ObjectNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleObjectNotValid(final RuntimeException e) {
        log.info(e.getMessage(), e);
        return new ErrorResponse("Невалидные данные", e.getMessage());
    }



    @ExceptionHandler({NoRightsForUpdateException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNoRightsForUpdate(final RuntimeException e) {
        log.info(e.getMessage(), e);
        return new ErrorResponse("Нет доступа", e.getMessage());
    }*/
}
