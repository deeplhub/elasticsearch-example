package com.xh.es.model.dto;

import lombok.Data;

/**
 * @description: from size 分页
 *                  from+size形式，在深度分页的情况下，这种效率是非常低的，但是可以随机跳转页面；
 *                  es为了性能，会限制我们分页的深度，es目前支持最大的max_result_window = 10000，
 *                  也就是from+size的大小不能超过10000。
 * @author: black tea
 * @date: 2021/9/7 10:14
 */
@Data
public class RequestFromSizePage extends PageRequest{

    public static RequestFromSizePage of(int page,int limit){
        RequestFromSizePage requestFromSizePage = new RequestFromSizePage();
        requestFromSizePage.setPage(page);
        requestFromSizePage.setLimit(limit);
        return requestFromSizePage;
    }

}
