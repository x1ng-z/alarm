package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 10:31
 */
@Getter
public enum ProductTypeEnum {
    PRODUCT_TYPE_SL("sl","生料",true),
    PRODUCT_TYPE_SC("sc","烧成",true),
    PRODUCT_TYPE_ZC("zc","制成",true),
    PRODUCT_TYPE_KTJ("ktj","开停机",false);

    @EnumValue
//    @JsonValue
    private String code;
    private String decs;
    //是否展示在web页的报警列表中
    private Boolean display;

    ProductTypeEnum(String code, String decs,Boolean display) {
        this.code = code;
        this.decs = decs;
        this.display=display;
    }


    public static final Map<String,ProductTypeEnum> PRODUCT_TYPE_ENUM_MAP;

    static {
        PRODUCT_TYPE_ENUM_MAP=new HashMap<>();
        Arrays.stream(ProductTypeEnum.values()).forEach(a->{
            PRODUCT_TYPE_ENUM_MAP.put(a.getCode(),a);
        });
    }
}
