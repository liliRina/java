package car.leasing;


import car.leasing.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAbsentObj(ObjectNotFoundException e){
        Map<String, String> error = new HashMap<>();
        error.put("error", "Object not found");
        error.put("message", e.getMessage());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParam(InvalidParameterException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid parameter");
        error.put("message", e.getMessage());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } 
    @ExceptionHandler(DeletionNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleDeletionNA(DeletionNotAllowedException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Deletion not allowed");
        error.put("message", e.getMessage());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CarUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleCarUnavailable(CarUnavailableException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Сar is occupied");
        error.put("message", e.getMessage());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(PayClosedContractException.class)
    public ResponseEntity<Map<String, String>> handleClosedContract(PayClosedContractException e){
        Map<String, String> error = new HashMap<>();
        error.put("error", "Contract is closed");
        error.put("message", e.getMessage());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Database error");
        error.put("message", e.getMessage());
        log.error("Критическая ошибка: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Параметр " + e.getName() +" должен быть: " + e.getRequiredType());
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateKeyException e) {
        Map<String, String> error = new HashMap<>();

        String message = e.getCause().getMessage();
        Pattern pattern = Pattern.compile("Key \\((\\w+)\\)=\\((.+?)\\)");

        Matcher matcher = pattern.matcher(message);
        if (matcher.find()){
            String column = matcher.group(1);
            String value = matcher.group(2);
            error.put("error", "Объект с " + column + " = " + value + " уже существует");
        }
        else{
            error.put("error", "Объект с такими уникальными параметрами уже существует");
        }
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrityViolationException(DataIntegrityViolationException e){
        Map<String, String> error = new HashMap<>();

        String message = e.getCause().getMessage();
        Pattern pattern = Pattern.compile("Key \\((\\w+)\\)=\\((.+?)\\)");
        System.out.println("DataIntegrityViolationException" + message);
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    @ExceptionHandler(FileException.class)
    public ResponseEntity<Map<String, String>> handleFileException(FileException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка при работе с файлом с договорами");
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}