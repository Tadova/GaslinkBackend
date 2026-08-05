//package com.gaslink.api.shared.exception;
//import com.gaslink.api.shared.response.ApiResponse;
//import org.springframework.http.*;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
//    }
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<ApiResponse<Void>> handleUnauth(UnauthorizedException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
//    }
//    @ExceptionHandler(ForbiddenException.class)
//    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
//    }
//    @ExceptionHandler(BusinessRuleException.class)
//    public ResponseEntity<ApiResponse<Void>> handleBiz(BusinessRuleException ex) {
//        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
//    }
//    @ExceptionHandler(PaymentVerificationException.class)
//    public ResponseEntity<ApiResponse<Void>> handlePayment(PaymentVerificationException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
//    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Map<String,String>>> handleValidation(MethodArgumentNotValidException ex) {
//        Map<String,String> errors = new LinkedHashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(e -> errors.put(((FieldError)e).getField(), e.getDefaultMessage()));
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//            .body(ApiResponse.<Map<String,String>>builder().success(false).message("Validation failed").data(errors).build());
//    }
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal error: " + ex.getMessage()));
//    }
//}