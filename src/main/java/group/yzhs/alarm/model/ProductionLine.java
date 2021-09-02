package group.yzhs.alarm.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductionLine {
   //公司名称
   private String name;
   //标识
   private String pNo;
   //报警工艺（烧成、制成、生料、开停机）
   private List<Device> devices=null;


}
