package com.cui.miaosha.domain;


public class MiaoshaGoods {

  private Integer id;
  private Integer goodsId;
  private double miaoshaPrice;
  private Integer stockCount;
  private java.util.Date startDate;
  private java.util.Date endDate;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public Integer getGoodsId() {
    return goodsId;
  }

  public void setGoodsId(Integer goodsId) {
    this.goodsId = goodsId;
  }


  public double getMiaoshaPrice() {
    return miaoshaPrice;
  }

  public void setMiaoshaPrice(double miaoshaPrice) {
    this.miaoshaPrice = miaoshaPrice;
  }


  public Integer getStockCount() {
    return stockCount;
  }

  public void setStockCount(Integer stockCount) {
    this.stockCount = stockCount;
  }


  public java.util.Date getStartDate() {
    return startDate;
  }

  public void setStartDate(java.util.Date startDate) {
    this.startDate = startDate;
  }


  public java.util.Date getEndDate() {
    return endDate;
  }

  public void setEndDate(java.util.Date endDate) {
    this.endDate = endDate;
  }

}
