package org.mobinets.dtlab.common.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {

    @NotBlank(message = "Username cannot be empty")
    @Length(min = 4, message = "Username length must be greater than 6")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Length(min = 6, message = "Password length must be greater than 6")
    private String password;

    private Boolean rememberMe;

}
