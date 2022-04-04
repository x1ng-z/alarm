package group.yzhs.alarm.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 11:16
 */
@UtilityClass
public class NumTools {

    public  double round(double f) {
        BigDecimal bg = new BigDecimal(f);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
