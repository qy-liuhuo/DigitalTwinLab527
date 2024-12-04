package org.mobinets.dtlab.user;

import org.mobinets.dtlab.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.mobinets.dtlab.common.dao.Ticket;
import org.mobinets.dtlab.common.dao.UserDAO;
import org.mobinets.dtlab.common.dto.LoginDTO;
import org.mobinets.dtlab.common.dto.RegisterDTO;
import org.mobinets.dtlab.common.enums.UserRole;
import org.mobinets.dtlab.common.enums.UserStatus;
import org.mobinets.dtlab.common.exception.BusinessException;
import org.mobinets.dtlab.common.exception.IllegalParamException;
import org.mobinets.dtlab.common.response.Response;
import org.mobinets.dtlab.common.utils.EncryptUtils;
import org.mobinets.dtlab.common.utils.Constants;
import org.mobinets.dtlab.common.vo.LoginVO;
import org.mobinets.dtlab.common.vo.UserInfoVO;
import org.mobinets.dtlab.user.mapper.TicketMapper;
import org.mobinets.dtlab.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.Random;


@DubboService
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public Response<LoginVO> login(LoginDTO loginDTO) {
        int expiredSeconds = loginDTO.getRememberMe() ? Constants.REMEMBER_EXPIRED_SECONDS : Constants.DEFAULT_EXPIRED_SECONDS;
        UserDAO user = userMapper.selectByUsername(loginDTO.getUsername());
        if(user == null){
            throw new BusinessException("The username does not exist.");
        }
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new BusinessException("The account has been frozen.");
        }
        if(!EncryptUtils.md5(loginDTO.getPassword() + user.getSalt()).equals(user.getPassword())){
            throw new BusinessException("The password is incorrect.");
        }
        Ticket ticket = new Ticket();
        ticket.setUserId(user.getId());
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        ticket.setStatus(0);
        ticket.setTicket(EncryptUtils.generateUUID());
        ticketMapper.insert(ticket);
        LoginVO loginVO = new LoginVO();
        loginVO.setTicket(ticket.getTicket());
        loginVO.setUserInfo(new UserInfoVO(user));
        return Response.success(loginVO);
    }

    @Override
    public Response<Boolean> register(RegisterDTO registerDTO) {
        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())){
            throw new IllegalParamException("The two passwords entered do not match.");
        }
        if(userMapper.selectByUsername(registerDTO.getUsername()) != null){
            throw new IllegalParamException("The username has been registered.");
        }
        if(userMapper.selectByEmail(registerDTO.getEmail()) != null){
            throw new IllegalParamException("The email has been registered.");
        }
        UserDAO newUser = new UserDAO();
        if(registerDTO.getEmail().split("@")[1].equals("mobinets.org")){
            newUser.setRole(UserRole.USER);
        }else{
            newUser.setRole(UserRole.VISITOR);
        }
        newUser.setSalt(EncryptUtils.generateUUID().substring(0, 5));
        newUser.setPassword(EncryptUtils.md5(newUser.getPassword() + newUser.getSalt()));
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setHeaderUrl(String.format("https://api.multiavatar.com/%d", new Random().nextInt(100)));
        newUser.setCreateTime(new Date());
        userMapper.insert(newUser);
        return Response.success();
    }


}
