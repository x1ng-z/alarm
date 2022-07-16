package group.yzhs.alarm.model.entity;

import group.yzhs.alarm.constant.SwitchLogic;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:11
 */
@Data
public class Switch extends BaseEntity {
    private String name;
    private SwitchLogic switchLogic;
}
