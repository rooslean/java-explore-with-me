package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

/*    @ExceptionHandler({ObjectNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleObjectNotValid(final RuntimeException e) {
        log.info(e.getMessage(), e);
        return new ErrorResponse("Невалидные данные", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatchError(final MethodArgumentTypeMismatchException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Unknown " + e.getName() + ": " + e.getValue(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestError(final BadRequestException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Некорректный запрос", e.getMessage());
    }

    @ExceptionHandler({NoRightsForUpdateException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNoRightsForUpdate(final RuntimeException e) {
        log.info(e.getMessage(), e);
        return new ErrorResponse("Нет доступа", e.getMessage());
    }*/
}
