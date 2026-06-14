package web_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import web_project.exceptions.GetWaterLossEx;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(GetWaterLossEx.class)
//    public ResponseEntity<Map<String, String>> handleAbsentObj(GetWaterLossEx e){
//        Map<String, String> error = new HashMap<>();
//        error.put("error", "Object not found");
//        error.put("message", e.getMessage());
//        log.warn(e.getMessage());
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//    }
}
