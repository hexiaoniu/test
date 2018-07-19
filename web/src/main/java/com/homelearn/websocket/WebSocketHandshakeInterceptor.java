package com.homelearn.websocket;

import com.homelearn.utils.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Map;

/**
 * Copyright(C) 2017 CEIEC All rights reserved.
 * Original Author: zhuzhiyuan@ceiec.com.cn, 2017/5/11
 * @author zhuzhiyuan@ceiec.com.cn
 */
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {


    private static volatile  RedisTemplate<Serializable, Serializable> redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
            ServerHttpResponse serverHttpResponse,
            WebSocketHandler webSocketHandler,
            Map<String, Object> map) throws Exception {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            if (httpRequest != null) {

                String token = httpRequest.getParameter("token");
                map.put(Constant.WEBSOCKET_USERNAME,
                        token != null ? token : httpRequest.getSession().getId());
                RequestContext context = new RequestContext(httpRequest);
                map.put(Constant.WEBSOCKET_CONTEXT, context);

            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest,
            ServerHttpResponse serverHttpResponse,
            WebSocketHandler webSocketHandler,
            Exception e) {

    }

    private static RedisTemplate<Serializable, Serializable> getRedisTemplate() {
        if (null == redisTemplate) {
            synchronized (WebSocketHandshakeInterceptor.class) {
                if (null == redisTemplate) {
//                    redisTemplate = SpringContextUtils.getBean("redisTemplate");
                }
            }
        }
        return redisTemplate;
    }
}
