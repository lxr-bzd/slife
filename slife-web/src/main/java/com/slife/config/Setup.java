package com.slife.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

/**
 * beanFactoryPostprocessor的作用是在beanFactory初始化之后提供一个修改的机会。
 * 在Spring实例化bean的前后执行一些附加操作
 *
 * @author jamen
 * @date 2016.12.15
 */
@Slf4j
//@Configuration
public class Setup implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        log.info("beanFactoryPostprocessor的作用是在beanFactory初始化之后提供一个修改的机会。......这时候bean 还没实例化（instance)");
    }
}
