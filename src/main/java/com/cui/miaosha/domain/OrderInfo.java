package com.cui.miaosha.domain;


public class OrderInfo {

  private Integer id;
  private Long userId;
  private Integer goodsId;
  private Integer deliveryAddrId;
  private String goodsName;
  private Integer goodsCount;
  private double goodsPrice;
  private Integer orderChannel;
  private Integer orderStatus;
  private java.util.Date createDate;
  private java.util.Date payDate;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }


  public Integer getGoodsId() {
    return goodsId;
  }

  public void setGoodsId(Integer goodsId) {
    this.goodsId = goodsId;
  }


  public Integer getDeliveryAddrId() {
    return deliveryAddrId;
  }

  public void setDeliveryAddrId(Integer deliveryAddrId) {
    this.deliveryAddrId = deliveryAddrId;
  }


  public String getGoodsName() {
    return goodsName;
  }

  public void setGoodsName(String goodsName) {
    this.goodsName = goodsName;
  }


  public Integer getGoodsCount() {
    return goodsCount;
  }

  public void setGoodsCount(Integer goodsCount) {
    this.goodsCount = goodsCount;
  }


  public double getGoodsPrice() {
    return goodsPrice;
  }

  public void setGoodsPrice(double goodsPrice) {
    this.goodsPrice = goodsPrice;
  }


  public Integer getOrderChannel() {
    return orderChannel;
  }

  public void setOrderChannel(Integer orderChannel) {
    this.orderChannel = orderChannel;
  }


  public Integer getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(Integer orderStatus) {
    this.orderStatus = orderStatus;
  }


  public java.util.Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(java.util.Date createDate) {
    this.createDate = createDate;
  }


  public java.util.Date getPayDate() {
    return payDate;
  }

  public void setPayDate(java.util.Date payDate) {
    this.payDate = payDate;
  }

}
