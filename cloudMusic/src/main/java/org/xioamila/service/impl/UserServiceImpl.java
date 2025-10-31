package org.xioamila.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.modules.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xioamila.dto.LoginDto;
import org.xioamila.dto.RegisterDto;
import org.xioamila.entity.User;
import org.xioamila.vo.LoginVo;
import org.xioamila.vo.Result;
import org.xioamila.mapper.UserMapper;
import org.xioamila.service.UserService;
import org.xioamila.common.jwt.JwtUtil;
import org.xioamila.common.utils.PasswordUtil;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private PasswordUtil passwordUtil;

    private JwtUtil jwtUtil;

    @Override
    public Result<LoginVo> login(LoginDto loginDto) {
        try {
            // 根据用户名查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDto.getUsername());
            User user = this.getOne(queryWrapper);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证密码
            if (!passwordUtil.matches(loginDto.getPassword(), user.getPassword())) {
                return Result.error("密码错误");
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // 构建返回数据
            LoginVo loginVo = new LoginVo();
            loginVo.setId(user.getId().toString());
            loginVo.setUsername(user.getUsername());
            loginVo.setNickname(user.getNickname());
            loginVo.setToken(token);
            loginVo.setTokenType("Bearer");
            loginVo.setExpiresIn(jwtUtil.getExpirationInSeconds());

            return Result.success("登录成功", loginVo);

        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    @Transactional
    @Override
    public Result<Object> register(RegisterDto registerDto) {
        try {
            // 检查用户名是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, registerDto.getUsername());
            Long count = this.count(queryWrapper);

            if (count > 0) {
                return Result.error("用户名已存在");
            }

            // 创建用户实体
            User user = new User();
            user.setUsername(registerDto.getUsername());
            user.setNickname(registerDto.getNickname());

            // 加密密码
            String encodedPassword = passwordUtil.encodePassword(registerDto.getPassword());
            user.setPassword(encodedPassword);

            // 保存用户
            boolean saved = this.save(user);

            if (saved) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败，请稍后重试");
            }

        } catch (Exception e) {
            // 记录日志
            log.error("用户注册失败: {}", e.getMessage(), e);
            return Result.error("系统错误，注册失败");
        }
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        if (StringUtils.isEmpty(user.getId())) {
            throw new ServiceException("用户ID不能为空");
        }

        User existingUser = this.getById(user.getId());
        if (ObjectUtils.isEmpty(existingUser)) {
            throw new ServiceException("用户不存在");
        }

        // 处理密码逻辑
        if (StringUtils.isNotBlank(user.getPassword())) {
            // 如果传了密码，需要加密
            String encodedPassword = passwordUtil.encodePassword(user.getPassword());
            user.setPassword(encodedPassword);
        } else {
            // 如果没传密码，保持原密码
            user.setPassword(existingUser.getPassword());
        }

        // 检查用户名是否重复（排除自己）
        if (StringUtils.isNotBlank(user.getUsername()) &&
                !user.getUsername().equals(existingUser.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, user.getUsername())
                    .ne(User::getId, user.getId());
            Long count = this.count(queryWrapper);
            if (count > 0) {
                throw new ServiceException("用户名已存在");
            }
        }

        return this.updateById(user);
    }
}
