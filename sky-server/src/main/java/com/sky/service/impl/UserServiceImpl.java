package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;

import static com.sky.constant.JwtClaimsConstant.USER_ID;
import static com.sky.constant.MessageConstant.LOGIN_FAILED;
import static com.sky.constant.WeChatConstant.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private WeChatProperties weChatProperties;

    @Override
    public UserLoginVO userLogin(UserLoginDTO userLoginDTO) {
        // 1. 调用接口，获取微信用户的openId
        String openid = getOpenid(userLoginDTO.getCode());

        // 2. 如果openId为空，则表示登录失败
        if (openid == null) {
            throw new LoginFailedException(LOGIN_FAILED);
        }

        // 3. 否则，判断当前用户是否为新用户（对于当前项目）
        User user = userMapper.getByOpenId(openid);

        // 4. 如果是新用户，则自动完成注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        // 5. 封装结果返回
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
    }

    private String getOpenid(String code) {
        // 1.1 封装请求体
        HashMap<String, String> map = new HashMap<>();
        map.put(WECHAT_APP_ID, weChatProperties.getAppid());
        map.put(WECHAT_SECRET, weChatProperties.getSecret());
        map.put(WECHAT_JS_CODE, code);
        map.put(WECHAT_GRANT_TYPE, WECHAT_GRANT_TYPE_VALUE);

        // 1.2 发送请求
        String responseJson = HttpClientUtil.doGet(WECHAT_LOGIN_URL, map);
        // 1.3 解析响应值
        JSONObject jsonObject = JSON.parseObject(responseJson);
        return jsonObject.getString(WECHAT_OPEN_ID);
    }
}
