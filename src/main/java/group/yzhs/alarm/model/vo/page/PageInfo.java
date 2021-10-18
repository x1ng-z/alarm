package group.yzhs.alarm.model.vo.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 20:00
 */  @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageInfo<T> {
        /**
         * 当前页
         */
        private long current;
        /**
         * 每页的数量
         */
        private long pageSize;
        /**
         * 当前页的数量
         */
        private long currentSize;
        /**
         * 总记录数
         */
        private long total;
        /**
         * 总页数
         */
        private long pages;
        /**
         * 数据
         */
        private List<T> list;

}
