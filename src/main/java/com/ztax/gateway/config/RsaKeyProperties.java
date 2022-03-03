package com.ztax.gateway.config;

import com.ztax.gateway.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * rsa密钥配置类
 * 公钥和私钥
 * 并加载到spring中
 */
@Data
@ConfigurationProperties("rsa.key")     //指定配置文件的key
@Component
public class RsaKeyProperties {
    //公钥路径
    private String pubKeyPath;
    //私钥路径
    private String priKeyPath;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void createKey() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }
}
