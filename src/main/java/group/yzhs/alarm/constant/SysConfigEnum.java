package group.yzhs.alarm.constant;

import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/29 15:27
 */
@Getter
public enum SysConfigEnum {
    SYS_CONFIG_ioturl("ioturl","IOTUrl"),
    SYS_CONFIG_innerurl("innerurl","内部Url"),
    SYS_CONFIG_dataresurce("dataresurce","数据来源"),
    SYS_CONFIG_continueAlarmSec("continueAlarmSec","持续报警时间"),
    SYS_CONFIG_pushIntervalSec("pushIntervalSec","推送间隔时间"),
    SYS_CONFIG_department("department","推送群名"),
    SYS_CONFIG_rate("rate","语音播报速度"),
    SYS_CONFIG_url("url","微信推送Url"),
    SYS_CONFIG_companyCode("companyCode","公司编码"),
    SYS_CONFIG_companyName("companyName","公司名称")
    ;

    private String code;
    private String name;

    SysConfigEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
