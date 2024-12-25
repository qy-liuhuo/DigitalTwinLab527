package org.mobinets.dtlab;

import org.mobinets.dtlab.common.dto.LoginDTO;
import org.mobinets.dtlab.common.dto.RegisterDTO;
import org.mobinets.dtlab.common.response.Response;
import org.mobinets.dtlab.common.vo.LoginVO;

public interface UserService {

    Response<LoginVO> login(LoginDTO loginDTO);

    Response<Boolean> register(RegisterDTO registerDTO);

}
