package group.yzhs.alarm.controllerAdvice;

import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 14:26
 */
@Slf4j
@Order
@RestControllerAdvice
public class RestHttpExceptionHandler {


    //参数异常
    @ExceptionHandler(value = ParameterException.class )
    public RestHttpResponseEntity<Object> parameterExceptionHandler(HttpServletRequest httpServlet, ParameterException e){
        return RestHttpResponseEntity.error(e.getMessage(), e.getCode());
    }

    /**校验类错误*/
    //参数检验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class )
    public RestHttpResponseEntity<Object> methodArgumentNotValidExceptionHandler(HttpServletRequest httpServlet, MethodArgumentNotValidException e){
        StringBuilder errorMessage=new StringBuilder();
        BindingResult result = e.getBindingResult();
        if (result.hasErrors()) {
            List<ObjectError> errors = result.getAllErrors();
            errors.forEach(p ->{
                FieldError fieldError = (FieldError) p;
                log.error("Data check failure : object{"+fieldError.getObjectName()+"},field{"+fieldError.getField()+
                        "},errorMessage{"+fieldError.getDefaultMessage()+"}");
                errorMessage.append(fieldError.getDefaultMessage());

            });

        }

        return RestHttpResponseEntity.error(errorMessage.toString(), HttpStatus.BAD_REQUEST.value());
    }

    //参数检验异常
    @ExceptionHandler(value = BindException.class )
    public RestHttpResponseEntity<Object> bindExceptionHandler(HttpServletRequest httpServlet, BindException e){
        StringBuilder errorMessage=new StringBuilder();
        BindingResult result = e.getBindingResult();
        if (result.hasErrors()) {
            List<ObjectError> errors = result.getAllErrors();
            errors.forEach(p ->{
                FieldError fieldError = (FieldError) p;
                log.error("Data check failure : object{"+fieldError.getObjectName()+"},field{"+fieldError.getField()+
                        "},errorMessage{"+fieldError.getDefaultMessage()+"}");
                errorMessage.append(fieldError.getDefaultMessage());

            });

        }

        return  RestHttpResponseEntity.error(errorMessage.toString(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(value =Exception.class )
    public RestHttpResponseEntity<Object> exceptionHandler(HttpServletRequest httpServlet, Exception e){
        log.error(e.getMessage(),e);
        return RestHttpResponseEntity.error(String.format("系统异常:%s",e.getMessage()));
    }


}
