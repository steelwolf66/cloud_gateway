package com.ztax.gateway.config;

import cn.hutool.core.util.ArrayUtil;
import com.ztax.gateway.constants.AuthConstants;
import com.ztax.gateway.result.ResultCode;
import com.ztax.gateway.security.AuthorizationManager;
import com.ztax.gateway.utils.WebUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

/**
 * 资源服务器配置
 * OAuth2中有两种角色
 * 一个是认证服务
 * 一个是资源服务
 * gateway服务作为整个微服务架构中的入口，作为资源服务被保护起来
 */
@AllArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    private AuthorizationManager authorizationManager;
    private WhiteListConfig whiteListConfig;

    /**
     * security web filter chain
     *
     * @param http
     * @return
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        //jwt 覆盖器
        http.oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());

        http.oauth2ResourceServer().authenticationEntryPoint(authenticationEntryPoint());
        //配置免密(白名单无需校验)等
        http.authorizeExchange()
                .pathMatchers(ArrayUtil.toArray(whiteListConfig.getUrls(), String.class)).permitAll()
                .anyExchange().access(authorizationManager)
                .and()
                .exceptionHandling()
                // 处理未授权
                .accessDeniedHandler(accessDeniedHandler())
                //处理未认证
                .authenticationEntryPoint(authenticationEntryPoint())
                .and().csrf().disable();

        return http.build();
    }

    /**
     * 未授权处理器
     *
     * @return
     */
    @Bean
    ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse()))
                    .flatMap(response -> WebUtils.writeFailedToResponse(response, ResultCode.ACCESS_UNAUTHORIZED));

            return mono;
        };
    }

    /**
     * token认证失败时的自定义异常
     */
    @Bean
    ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, e) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse()))
                    .flatMap(response -> WebUtils.writeFailedToResponse(response, ResultCode.TOKEN_INVALID_OR_EXPIRED));
            return mono;
        };
    }

    /**
     * @return
     * @link https://blog.csdn.net/qq_24230139/article/details/105091273
     * ServerHttpSecurity没有将jwt中authorities的负载部分当做Authentication
     * 需要把jwt的Claim中的authorities加入
     * 方案：重新定义R 权限管理器，默认转换器JwtGrantedAuthoritiesConverter
     */
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(AuthConstants.AUTHORITY_PREFIX);
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AuthConstants.JWT_AUTHORITIES_KEY);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
