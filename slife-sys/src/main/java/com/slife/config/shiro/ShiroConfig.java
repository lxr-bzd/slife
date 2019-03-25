package com.slife.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro的配置类
 *
 * @author jamen
 * @author felixu
 * @date 2017.7.14
 */
@Slf4j
@Configuration
public class ShiroConfig {

    /**
     * 为了填Spring Boot升级到2.x之后，整合Shiro的一个大坑
     * 在2.x中Forward之后ShiroFilter重新被加载
     *
     * @return FilterRegistrationBean<DelegatingFilterProxy>
     */
    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> delegatingFilterProxy(){
        FilterRegistrationBean<DelegatingFilterProxy> filterRegistrationBean = new FilterRegistrationBean<>();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");
        //filterRegistrationBean.setAsyncSupported(true);
        EnumSet<DispatcherType> types = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);
        filterRegistrationBean.setDispatcherTypes(types);
        return filterRegistrationBean;
    }

    /**
     * 核心过滤器
     *
     * @param securityManager SessionsSecurityManager
     * @return ShiroFilter
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);

        // 配置登陆url
        bean.setLoginUrl("/login");
        // 配置登陆成功url
        bean.setSuccessUrl("/index");

        // 配置访问权限
        bean.setFilterChainDefinitionMap(fillFilterChainDefinitionMap());
        return bean;
    }

    /**
     * url拦截器配置填充
     *
     * @return FilterChainDefinitionMap
     */
   private Map<String, String> fillFilterChainDefinitionMap() {
        return new LinkedHashMap<String, String>() {{
            // 登出，可自定义，做些自定义操作
            put("/logout","logout");

            // 不需要权限
            put("/favicon.ico", "anon");
            put("/css/**","anon");
            put("/js/**","anon");
            put("/img/**","anon");
            put("/fonts/**","anon");
            put("/health/**","anon");
            put("/env/**","anon");
            put("/metrics/**","anon");
            put("/trace/**","anon");
            put("/dump/**","anon");
            put("/jolokia/**","anon");
            put("/info/**","anon");
            put("/logfile/**","anon");
            put("/refresh/**","anon");
            put("/flyway/**","anon");
            put("/liquibase/**","anon");
            put("/heapdump/**","anon");
            put("/loggers/**","anon");
            put("/auditevents/**","anon");
            put("/layouts/**","anon");
            put("/attach/**","anon");

            // 需要权限
            put("/*", "authc");
            put("/**", "authc");
            put("/*.*", "authc");
        }};
   }


    /**
     * 创建自定义的AuthorizingRealm
     * 关于缓存可以选择使用Ehcache或者Redis
     * {@link org.apache.shiro.cache.ehcache.EhCacheManager}
     * {@link RedisCacheManager}
     * {@link CacheConfig}
     *
     * @param cacheManager 这里注入接口，在yaml文件中配置具体使用Ehcache还是Redis.
     * @return AuthorizingRealm
     */
    @Bean
    public AuthorizingRealm authorizingRealm(CacheManager cacheManager) {
        AuthRealm realm = new AuthRealm();
        realm.setCacheManager(cacheManager);
        return realm;
    }


    /**
     * 在Spring Boot2.x中如果直接使用SecurityManager不会生效，而是使用SessionSecurityManager
     *
     * @param authRealm {@link AuthRealm}
     * @return {@link SessionsSecurityManager}
     */
    @Bean
    public SessionsSecurityManager securityManager(AuthorizingRealm authRealm, CookieRememberMeManager rememberMeManager) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 设置realm.
        manager.setRealm(authRealm);

        //注入缓存管理器;
        //注意:开发时请先关闭，如不关闭热启动会报错
//        manager.setCacheManager(cacheManager);//这个如果执行多次，也是同样的一个对象;
        //注入记住我管理器;
        manager.setRememberMeManager(rememberMeManager);
        log.info("[ShiroConfig#securityManager] ---> shiro已经加载");
        return manager;
    }

    /**
     * Cookie对象
     *
     * @return
     * */
    @Bean
    public SimpleCookie rememberMeCookie(){
        // 这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        // 单位秒
        simpleCookie.setMaxAge(20);
        log.info("[ShiroConfig#rememberMeCookie] ---> rememberMeCookie init success");
        return simpleCookie;
    }

    /**
     * Cookie管理对象
     *
     * @return {@link CookieRememberMeManager}
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie);
        log.info("[ShiroConfig#rememberMeManager] ---> rememberMeManager init success");
        return cookieRememberMeManager;
    }


    /**
     * 保证实现了Shiro内部lifecycle函数的bean执行
     *
     * @return LifecycleBeanPostProcessor
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    /**
     * AOP式方法级权限检查
     *
     * @return DefaultAdvisorAutoProxyCreator
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator=new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor=new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
