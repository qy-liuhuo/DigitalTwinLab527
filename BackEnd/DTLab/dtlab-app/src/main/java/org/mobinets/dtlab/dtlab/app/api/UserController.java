package org.mobinets.dtlab.dtlab.app.api;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mobinets.dtlab.UserService;
import org.mobinets.dtlab.common.dto.LoginDTO;
import org.mobinets.dtlab.common.dto.RegisterDTO;
import org.mobinets.dtlab.common.exception.BusinessException;
import org.mobinets.dtlab.common.response.Response;
import org.mobinets.dtlab.common.utils.Constants;
import org.mobinets.dtlab.common.vo.LoginVO;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Controller
@RequestMapping("/user")
public class UserController {

    @DubboReference
    private UserService userService;

    private static final Logger Log = LogManager.getLogger(UserController.class);


    /**
     * 注册页面，POST4个参数
     * @param registerDTO registerDTO
     * @return Response
     */
    @RequestMapping(path="/register", method = RequestMethod.POST)
    public Response<Boolean> register(@Validated @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }
    /**
     * 激活账号
     * @param userId UserId
     * @param code 激活码
     * @return return
     */
    @RequestMapping(path="/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(@PathVariable("userId") int userId, @PathVariable("code") String code) {
        return null;
    }

    /**
     * 登录
     * @param loginDTO loginDTO
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public Response<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse httpResponse) {
        String origin = request.getHeader("Origin");
        httpResponse.setHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
        return userService.login(loginDTO);
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        return null;
    }

}
