package com.ztax.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.ztax.gateway.constants.AuthConstants;
import com.ztax.gateway.result.ResultCode;
import com.ztax.gateway.redis.utils.RedisUtils;
import com.ztax.gateway.utils.WebUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器
 * 1.解析token，并将需要的信息存入header，方便后续服务使用
 * 2.校验token是否过期及是否在黑名单中
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisUtils redisUtils;


    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();


        // 无token放行，已在Security认证其中校验过
        String token = exchange.getRequest().getHeaders().getFirst(AuthConstants.JWT_TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            return chain.filter(exchange);
        }

        // 解析JWT获取jti，以jti为key判断redis的黑名单列表是否存在，存在拦截响应token失效
        token = token.replace(AuthConstants.JWT_TOKEN_PREFIX, Strings.EMPTY);
        JWSObject jwsObject = JWSObject.parse(token);
        String payload = jwsObject.getPayload().toString();
        JSONObject jsonObject = JSONUtil.parseObj(payload);
        String jti = jsonObject.getStr(AuthConstants.JWT_JTI_KEY);
        //校验token是否在黑名单中
        Boolean isBlack = redisUtils.hasKey(AuthConstants.TOKEN_BLACKLIST_PREFIX + jti);
        if (isBlack) {
            return WebUtils.writeFailedToResponse(response, ResultCode.TOKEN_INVALID_OR_EXPIRED);
        }

        // 存在token且不是黑名单，request写入JWT的载体信息
        request = exchange.getRequest().mutate()
                .header(AuthConstants.JWT_PAYLOAD_KEY, payload)
                .build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
