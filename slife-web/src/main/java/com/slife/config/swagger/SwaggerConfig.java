
package com.slife.config.swagger;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 2017/4/19.
 * Describe: swagger 配置类
 *
 * @author jamen
 * @author felixu
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private List<Parameter> parameter() {
        List<Parameter> params = new ArrayList<>();
        params.add(new ParameterBuilder().name("Authorization")
                .description("Authorization Bearer token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build());
        return params;

    }


    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.slife"))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(parameter());


    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("slife")
                .description("基于Spring Boot2.x的脚手架项目")
                .termsOfServiceUrl("https://gitee.com/jamen/slife/contributors?ref=master")
                .contact(new Contact("jamen & felixu", "https://gitee.com/jamen/slife/tree/boot_2.x/", ""))
                .license("Apache License 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .version("2.0.0")
                .build();
    }
}

