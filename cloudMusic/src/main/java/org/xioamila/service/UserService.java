package org.xioamila.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xioamila.dto.LoginDto;
import org.xioamila.dto.RegisterDto;
import org.xioamila.entity.User;
import org.xioamila.vo.LoginVo;
import org.xioamila.vo.Result;

public interface UserService extends IService<User> {

    Result<LoginVo> login(LoginDto loginDto);

    Result<Object> register(RegisterDto registerDto);

    boolean updateUser(User user);

}
