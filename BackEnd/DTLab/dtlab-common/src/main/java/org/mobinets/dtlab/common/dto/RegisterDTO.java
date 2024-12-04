package org.mobinets.dtlab.common.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {

    @NotBlank(message = "Username cannot be empty")
    String username;

    @NotBlank(message = "Password cannot be empty")
    @Length(min = 4, message = "Password length must be greater than 6")
    String password;

    @NotBlank(message = "Confirm password cannot be empty")
    @Length(min = 6, message = "Password length must be greater than 6")
    String confirmPassword;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is incorrect")
    String email;

}
