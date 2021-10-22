package group.yzhs.alarm.service.valid;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/21 16:53
 */
public interface Success<T> {

   boolean exceute(String[] value, T object,String errormessage);
}
