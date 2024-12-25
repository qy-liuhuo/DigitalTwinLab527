package org.mobinets.dtlab.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mobinets.dtlab.common.dao.UserDAO;

public interface UserMapper extends BaseMapper<UserDAO> {

    @Select("select * from user where username = #{username}")
    public UserDAO selectByUsername(@Param("username") String username);

    @Select("select * from user where email = #{email}")
    public UserDAO selectByEmail(@Param("email") String email);

}
