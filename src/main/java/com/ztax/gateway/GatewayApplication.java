package com.ztax.gateway;

import com.ztax.gateway.config.RsaKeyProperties;
import com.ztax.gateway.config.WhiteListConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.ztax.gateway"})
@EnableDiscoveryClient
@EnableConfigurationProperties({WhiteListConfig.class, RsaKeyProperties.class})  //将配置类放入Spring容器中
public class GatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
