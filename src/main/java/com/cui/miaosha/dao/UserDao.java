package com.cui.miaosha.dao;

import com.cui.miaosha.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {

    @Select("select * from user where id=#{id}")
    User findUserByMobile(@Param("id") Long id);

    @Update("update user set password=#{password} where id=#{id}")
    void update(User toBeUpdate);
}
