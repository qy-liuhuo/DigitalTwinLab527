package org.mobinets.dtlab.common.dao;

import lombok.Data;
import org.mobinets.dtlab.common.enums.UserRole;
import org.mobinets.dtlab.common.enums.UserStatus;

import java.util.Date;

@Data
public class UserDAO {

    int id;

    String username;

    String password;

    String salt;

    String email;

    UserRole role;

    UserStatus status;

    String headerUrl;

    Date createTime;
}
