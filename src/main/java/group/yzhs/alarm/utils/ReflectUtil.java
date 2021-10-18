package group.yzhs.alarm.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/17 21:29
 */
@Slf4j
public class ReflectUtil {
    public static Method[] getMethods(Class<?> clazz,String startName){
        Method[] methods =clazz.getDeclaredMethods();
        List<Method> res=new ArrayList<>();
        for(int index=0;index<methods.length;index++){
            methods[index].setAccessible(true);
            if(methods[index].getName().indexOf(startName)==0){
                res.add(methods[index]);
            }
        }
        Method[] arrayRes=new Method[res.size()];
        res.toArray(arrayRes);
        return arrayRes;
    }

    public static Object invoke(Object o,Method method,Object... parameters) {
        Object res= null;
        try {
            res = method.invoke(o,parameters);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(),e);
        }
        return res;
    }
}
