package ru.practicum.ewm.exception;

public class ConflictException extends RuntimeException {
    public ConflictException() {
        super("Конфликт при создании объекта");
    }
}
