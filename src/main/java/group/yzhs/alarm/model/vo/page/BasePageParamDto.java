package group.yzhs.alarm.model.vo.page;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 19:48
 */
@Data
public class BasePageParamDto {

    @Min(value = 1,message = "当前页面最小为1")
    private Integer current;

    @Min(value = 1,message = "每页条数最小值为1")
    private Integer pageSize;
}
