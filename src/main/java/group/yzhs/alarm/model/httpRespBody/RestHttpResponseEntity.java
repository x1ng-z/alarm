package group.yzhs.alarm.model.httpRespBody;

import group.yzhs.alarm.exception.ParameterException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 14:34
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RestHttpResponseEntity<T> implements Serializable {
    private String message;
    private Integer status;
    private T data;


    public static <T> RestHttpResponseEntity<T> error(ParameterException e){
        RestHttpResponseEntity<T> response=new RestHttpResponseEntity();
        response.message=e.getMessage();
        response.status=e.getCode();
        return response;
    }

    public static <T> RestHttpResponseEntity<T> error(String message){
        RestHttpResponseEntity<T> response=new RestHttpResponseEntity<>();
        response.message=message;
        response.status= HttpStatus.INTERNAL_SERVER_ERROR.value();
        return response;
    }

    public static <T> RestHttpResponseEntity<T> error(String message,Integer code){
        RestHttpResponseEntity<T> response=new RestHttpResponseEntity<T>();
        response.message=message;
        response.status= code;
        return response;
    }

    public static <T> RestHttpResponseEntity<T> success(T data, String message){
        RestHttpResponseEntity<T> response=new RestHttpResponseEntity<T>();
        response.message=message;
        response.data=data;
        response.status= HttpStatus.OK.value();
        return response;
    }

    public static <T> RestHttpResponseEntity<T> success(T data){
        RestHttpResponseEntity<T> response=new RestHttpResponseEntity<T>();
        response.data=data;
        response.message="operate success!";
        response.status= HttpStatus.OK.value();
        return response;
    }

    public static <T> RestHttpResponseEntity<T> success(){
        return success(null);
    }


}
