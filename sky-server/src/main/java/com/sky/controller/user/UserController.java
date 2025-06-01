package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("user/user")
@Api(tags = "C端-用户相关接口")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("login")
    @ApiOperation("用户端登录功能")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户端登录功能: {}", userLoginDTO);
        UserLoginVO userLoginVO = userService.userLogin(userLoginDTO);
        return Result.success(userLoginVO);
    }
}
