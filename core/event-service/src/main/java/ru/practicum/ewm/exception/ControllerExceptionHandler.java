package ru.practicum.ewm.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
class ControllerExceptionHandler {

    private final Clock clock;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingQueryParameter(final MissingServletRequestParameterException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = List.of(new FieldErrorData(exception.getParameterName(),
                "no value provided", null));
        return handleFieldErrorInternally(ParameterType.PARAMETER, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongTypeOfQueryParameter(final MethodArgumentTypeMismatchException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = List.of(new FieldErrorData(exception.getName(), "value of wrong type",
                exception.getValue()));
        return handleFieldErrorInternally(ParameterType.PARAMETER, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongValuesInQueryString(final HandlerMethodValidationException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = exception.getValueResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldErrorData(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage(),
                                result.getArgument())))
                .toList();
        return handleFieldErrorInternally(ParameterType.PARAMETER, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleParameterValidationException(final ParameterValidationException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = List.of(new FieldErrorData(exception.getParameter(), exception.getError(),
                exception.getValue()));
        return handleFieldErrorInternally(ParameterType.PARAMETER, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleFieldValidationException(final FieldValidationException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = List.of(new FieldErrorData(exception.getField(), exception.getError(),
                exception.getValue()));
        return handleFieldErrorInternally(ParameterType.FIELD, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongValuesInRequestBody(final MethodArgumentNotValidException exception) {
        log.warn(exception.getMessage());
        final List<FieldErrorData> errors = exception.getFieldErrors().stream()
                .map(error -> new FieldErrorData(error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .toList();
        return handleFieldErrorInternally(ParameterType.FIELD, errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException exception) {
        log.warn(exception.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("The required object was not found")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now(clock))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDatabaseConstraintViolation(final DataIntegrityViolationException exception) {
        log.warn(exception.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Integrity constraint has been violated")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now(clock))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleNotPossibleException(final NotPossibleException exception) {
        log.warn(exception.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("For the requested operation the conditions are not met")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now(clock))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception exception) {
        log.error(exception.getMessage(), exception);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Unexpected error")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now(clock))
                .build();
    }

    private ApiError handleFieldErrorInternally(final ParameterType parameterType, final List<FieldErrorData> errors) {
        final List<FieldErrorData> orderedErrors = errors.stream()
                .sorted(Comparator.comparing(FieldErrorData::field).thenComparing(FieldErrorData::error))
                .toList();
        final Set<String> fields = orderedErrors.stream()
                .map(FieldErrorData::field)
                .map("'%s'"::formatted)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Wrong request format")
                .message(makeMessage(parameterType, fields, errors))
                .errors(orderedErrors)
                .timestamp(LocalDateTime.now(clock))
                .build();
    }

    private String makeMessage(final ParameterType parameterType, final Set<String> fields,
            final List<FieldErrorData> errors) {
        final String type = parameterType == ParameterType.FIELD ? "field" : "parameter";
        if (fields.size() == 1 && errors.size() == 1) {
            return "There is an error in %s %s".formatted(type, fields.iterator().next());
        } else if (fields.size() == 1) {
            return "There are errors in %s %s".formatted(type, fields.iterator().next());
        } else {
            return "There are errors in %ss %s".formatted(type, String.join(", ", fields));
        }
    }

    private enum ParameterType {
        PARAMETER,
        FIELD
    }

    private record FieldErrorData(String field, String error, Object value) {

    }
}
