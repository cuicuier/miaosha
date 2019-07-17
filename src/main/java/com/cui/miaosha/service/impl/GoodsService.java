package com.cui.miaosha.service.impl;

import com.cui.miaosha.dao.GoodsDao;
import com.cui.miaosha.domain.MiaoshaGoods;
import com.cui.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    private GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVO() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsByGoodsId(Integer goodsId) {
        return goodsDao.getGoodsByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {

        int ret = goodsDao.reduceStock(goodsVo);
        return ret > 0;
    }
}
