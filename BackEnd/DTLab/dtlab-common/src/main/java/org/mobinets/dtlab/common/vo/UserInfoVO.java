package org.mobinets.dtlab.common.vo;

import lombok.Data;
import org.mobinets.dtlab.common.dao.UserDAO;
import org.mobinets.dtlab.common.enums.UserRole;

import java.io.Serializable;

@Data
public class UserInfoVO implements Serializable {

    private int id;

    private String username;

    private UserRole role;

    private String headerUrl;

    public UserInfoVO(UserDAO userDAO) {
        this.id = userDAO.getId();
        this.username = userDAO.getUsername();
        this.role = userDAO.getRole();
        this.headerUrl = userDAO.getHeaderUrl();
    }

}
