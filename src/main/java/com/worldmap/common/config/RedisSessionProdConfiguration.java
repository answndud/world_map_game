package com.worldmap.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration
@Profile("prod")
@EnableRedisIndexedHttpSession(
	maxInactiveIntervalInSeconds = 60 * 60 * 24 * 14,
	redisNamespace = "worldmap:session"
)
public class RedisSessionProdConfiguration {
}
