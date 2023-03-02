package com.tian.service;

import com.tian.dao.IStorageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StorageServiceImpl implements IStorageService{

    @Autowired
    private IStorageDao iStorageDao;

    //添加库存
    public Map<String, Object> insertStorage(int sku_id, double in_quanty, double out_quanty){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(sku_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "传入的商品不能为空！");
            return resultMap;
        }
        if(in_quanty==0 && out_quanty==0){
            resultMap.put("result", false);
            resultMap.put("msg", "入库数量和出库数量不能同时为0！");
            return resultMap;
        }

        //2.取自storageDao
        //dao层已经得到了map
        resultMap = iStorageDao.insertStorage(sku_id, in_quanty, out_quanty);

        //3.返回信息
        return resultMap;
    }



}
