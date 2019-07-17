package com.cui.miaosha.domain;


public class User {

  private Long id;
  private String nickname;
  private String password;
  private String salt;
  private String head;
  private java.util.Date registerDate;
  private java.util.Date lastLoginDate;
  private String loginCount;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }


  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }


  public java.util.Date getRegisterDate() {
    return registerDate;
  }

  public void setRegisterDate(java.util.Date registerDate) {
    this.registerDate = registerDate;
  }


  public java.util.Date getLastLoginDate() {
    return lastLoginDate;
  }

  public void setLastLoginDate(java.util.Date lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }


  public String getLoginCount() {
    return loginCount;
  }

  public void setLoginCount(String loginCount) {
    this.loginCount = loginCount;
  }

}
