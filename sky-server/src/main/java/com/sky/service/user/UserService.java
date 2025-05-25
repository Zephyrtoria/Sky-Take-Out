package com.sky.service.user;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

public interface UserService {
    UserLoginVO userLogin(UserLoginDTO userLoginDTO);
}
