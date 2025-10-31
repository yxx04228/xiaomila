package org.xioamila.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xioamila.entity.Dto.LoginDto;
import org.xioamila.entity.User;
import org.xioamila.entity.Vo.LoginVo;

public interface UserService extends IService<User> {

    LoginVo login(LoginDto loginDto);

    boolean register(User user);

}
