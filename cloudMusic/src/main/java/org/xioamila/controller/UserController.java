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
import org.xioamila.dto.LoginDto;
import org.xioamila.dto.RegisterDto;
import org.xioamila.entity.User;
import org.xioamila.vo.LoginVo;
import org.xioamila.vo.Result;
import org.xioamila.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVo> login(@Valid @RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Object> register(@Valid @RequestBody RegisterDto registerDto) {
        return userService.register(registerDto);
    }

    @Operation(summary = "用户列表查询")
    @GetMapping("/getPageList")
    @Parameters({
            @Parameter(name = "nickname", description = "用户昵称", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Result<IPage<User>> getPageList(@RequestParam(required = false) String nickname,
                                    @Parameter(description = "当前页数") @RequestParam(defaultValue = "1") Integer nCurrent,
                                    @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer nSize) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(nickname)) {
            queryWrapper.like("nickname", nickname);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<User> pageList = userService.page(new Page<>(nCurrent, nSize), queryWrapper);
        return Result.success("查询成功", pageList);
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<Boolean> updateUser(@RequestBody User user) {
        return Result.data(userService.updateUser(user));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    public Result<Boolean> deleteUser(@Parameter(description = "用户ID", required = true) @RequestParam("id") String id) {
        return Result.data(userService.removeById(id));
    }
}