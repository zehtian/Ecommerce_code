package com.tian.service;

import java.util.Map;

public interface IStorageService {

    //添加库存
    Map<String, Object> insertStorage(int sku_id, double in_quanty, double out_quanty);



}
