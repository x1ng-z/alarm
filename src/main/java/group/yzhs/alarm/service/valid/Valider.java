package group.yzhs.alarm.service.valid;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import group.yzhs.alarm.exception.ParameterException;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/21 12:40
 */
public class Valider {
    private final static String SEPARATOR ="=";

    public static  <T> void  valid(Boolean templeCheck,String context,Success<T> success,T object,String errormessage){
        if(templeCheck){
            if(StringUtils.isNotBlank(context)&&context.trim().matches("^(.+)"+ SEPARATOR +"(.+)$")){
                String[] ruleCodeArrays=context.split(SEPARATOR);
                if(ruleCodeArrays.length==2){
                        if(success.exceute(ruleCodeArrays,object,errormessage)){
                            return;
                    }
                }
            }
        }else{
            if(success.exceute(new String[]{context},object,errormessage)){
                return;
            }
        }

        throw new ParameterException(errormessage+"不匹配");
    }


}
