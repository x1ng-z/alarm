package group.yzhs.alarm.utils;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/17 22:02
 */
public class StrUtil {
    public static String subAfter(String name, String startWith){
        return name.substring(name.indexOf(startWith)+startWith.length());
    }
}
