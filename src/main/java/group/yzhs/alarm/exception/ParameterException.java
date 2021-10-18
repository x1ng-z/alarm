package group.yzhs.alarm.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 14:36
 *
 * 参数错误异常类
 */
@Data
public class ParameterException extends RuntimeException{
    private String message;
    private Integer code;


    public ParameterException(){
        super();
    }

    public ParameterException(String message){
        super(message);
        this.message=message;
        this.code= HttpStatus.BAD_REQUEST.value();
    }

    public  ParameterException(String message, Throwable cause) {
        super(message, cause);
        this.message=message;
        this.code=HttpStatus.BAD_REQUEST.value();
    }


}
