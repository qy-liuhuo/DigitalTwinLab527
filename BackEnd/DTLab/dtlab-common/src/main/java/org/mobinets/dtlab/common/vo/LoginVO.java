package org.mobinets.dtlab.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginVO implements Serializable {

    String ticket;

    UserInfoVO userInfo;
}
