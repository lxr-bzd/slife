package com.slife.config.shiro;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author felixu
 * @date 2019.03.14
 */
@Slf4j
@Configuration
public class CacheConfig {

    @Bean()
    @ConditionalOnProperty(prefix = "slife.shiro", havingValue = "ehcacheManager", name = "cacheManager")
    public EhCacheManager ehCacheManager() {
        log.info("[CacheConfig#ehCacheManager] ---> init ehCacheManager");
        CacheManager cacheManager = CacheManager.getCacheManager("slife");
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(cacheManager);
        ehCacheManager.setCacheManagerConfigFile("classpath:cache/ehcache-shiro.xml");
        return ehCacheManager;
    }

    @Bean
    @ConditionalOnProperty(prefix = "slife.shiro", havingValue = "redisCacheManager", name = "cacheManager")
    public RedisCacheManager redisCacheManager(RedisTemplate<String, String> redisTemplate) {
        log.info("[CacheConfig#redisCacheManager] ---> init redisCacheManager");
        return new RedisCacheManager(redisTemplate);
    }
}
