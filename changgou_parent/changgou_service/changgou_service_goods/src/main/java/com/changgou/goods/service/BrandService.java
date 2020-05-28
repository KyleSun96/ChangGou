package com.changgou.goods.service;

import com.changgou.goods.pojo.Brand;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Program: ChangGou
 * @InterfaceName: BrandService
 * @Description:
 * @Author: KyleSun
 **/
public interface BrandService {


    /**
     * @description: //TODO 查询所有品牌
     * @param: []
     * @return: java.util.List<com.changgou.goods.pojo.Brand>
     */
    List<Brand> findAll();


    /**
     * @description: //TODO 根据ID查询
     * @param: [id]
     * @return: com.changgou.goods.pojo.Brand
     */
    Brand findById(Integer id);


    /**
     * @description: //TODO 新增品牌
     * @param: [brand]
     * @return: void
     */
    void add(Brand brand);

}
