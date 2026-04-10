package com.yuexiang.shop.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopTypeVO;
import com.yuexiang.shop.service.ShopTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/shop-type")
@RequiredArgsConstructor
public class ShopTypeController {
    private final ShopTypeService typeService;

    @GetMapping("/list")
    public CommonResult<List<ShopTypeVO>> queryTypeList() {
        List<ShopTypeVO> list = typeService.queryOrderBySort();
        return CommonResult.success(list);
    }
}