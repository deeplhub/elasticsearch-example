package com.xh.es.model.dto;

import lombok.Data;
import org.elasticsearch.common.unit.TimeValue;

/**
 * @description: ScrollPage 对象
 *                 在es中我们分页要请求大数据集或者一次请求要获取大的数据集，scroll[skrəʊl]都是一种非常好的解决方案。
 *               scroll也是滚动搜索。会在第一次搜索的时候，保存一个当时的快照。之后只会基于该旧的视图快照提供数据搜索。在这个期间发生变动，是不会让用户看到的。
 *               官方的建议并不是用于实时的请求，因为每一个 scroll_id 不仅会占用大量的资源（特别是排序的请求），而且是生成的历史快照，对于数据的变更不会反映到快照上。
 *               这种方式往往用于非实时处理大量数据的情况，比如要进行数据迁移或者索引变更之类的。
 *
 * @author: black tea
 * @date: 2021/9/7 10:16
 */
@Data
public class RequestScrollPage extends PageRequest{

    /** 快照失效时间 **/
   private TimeValue scrollTimeValue;

   public static RequestScrollPage of(int limit,TimeValue scrollTimeValue){
       RequestScrollPage requestScrollPage = new RequestScrollPage();
       requestScrollPage.setLimit(limit);
       requestScrollPage.setScrollTimeValue(scrollTimeValue);
       return requestScrollPage;
   }


}
