package org.xioamila.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xioamila.entity.Dto.LoginDto;
import org.xioamila.entity.User;
import org.xioamila.entity.Vo.LoginVo;
import org.xioamila.mapper.UserMapper;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private UserMapper userMapper;

    @Override
    public LoginVo login(LoginDto loginDto) {
        LoginVo result = new LoginVo();

        // 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDto.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            result.setSuccess(false);
            result.setMessage("用户不存在");
            return result;
        }

        // 验证密码
        if (!user.getPassword().equals(loginDto.getPassword())) {
            result.setSuccess(false);
            result.setMessage("密码错误");
            return result;
        }

        // 登录成功
        result.setSuccess(true);
        result.setMessage("登录成功");
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());

        return result;
    }

    @Transactional
    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        Long count = this.count(queryWrapper);

        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        this.save(user);

        return true;
    }

}
