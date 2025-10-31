package org.xioamila.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xioamila.entity.Dto.LoginDto;
import org.xioamila.entity.User;
import org.xioamila.entity.Vo.LoginVo;
import org.xioamila.service.impl.UserService;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public LoginVo login(@RequestBody LoginDto loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public boolean register(@RequestBody User user) {
        return userService.register(user);
    }

    @Operation(summary = "用户列表查询")
    @GetMapping("/getPageList")
    @Parameters({
            @Parameter(name = "nickname", description = "用户昵称", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public IPage<User> getPageList(@RequestParam(required = false) String nickname,
                                    @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                    @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(nickname)) {
            queryWrapper.like("nickname", nickname);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<User> pageList = userService.page(new Page<>(nCurrent, nSize), queryWrapper);
        return pageList;
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public boolean updateUser(@RequestBody User user) {
        return userService.saveOrUpdate(user);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    public boolean deleteUser(@Parameter(description = "用户ID") @RequestParam("id") String id) {
        return userService.removeById(id);
    }


}