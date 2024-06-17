package com.caiya.companion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Swagger 配置类
 *
 * @author caiya
 */

@Configuration
@EnableSwagger2WebMvc
@Profile({"dev", "text"})
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) // 用于生成API信息
                .select() // select()函数返回一个ApiSelectorBuilder实例,用来控制接口被swagger做成文档
                // 扫描指定包下的接口，最为常用
                .apis(RequestHandlerSelectors.basePackage("com.caiya.companion.controller"))
                //.withClassAnnotation(RestController.class) // 扫描带有指定注解的类下所有接口
                //.withMethodAnnotation(PostMapping.class) // 扫描带有指定注解的方法接口
                //.apis(RequestHandlerSelectors.any()) // 扫描所有

                // 选择所有的API,如果你想只为部分API生成文档，可以配置这里
                .paths(PathSelectors.any()
                        //.any() // 满足条件的路径，该断言总为true
                        //.none() // 不满足条件的路径，该断言总为false（可用于生成环境屏蔽 swagger）
                        //.ant("/user/**") // 满足字符串表达式路径
                        //.regex("") // 符合正则的路径
                )
                .build();
    }

    /**
     * 用于定义API主界面的信息，比如可以声明所有的API的总标题、描述、版本
     *
     */
    private ApiInfo apiInfo() {
        Contact contact = new Contact(
                "菜牙买菜", // 作者姓名
                "https://blog.csdn.net/weixin_52102740", // 作者 csdn 网址
                "xxxx@qq.com"); // 作者邮箱

        return new ApiInfoBuilder()
                .title("友伴-伙伴匹配系统API") //  可以用来自定义API的主标题
                .description("友伴-伙伴匹配系统SwaggerAPI管理") // 可以用来描述整体的API
                .termsOfServiceUrl("https://blog.csdn.net/weixin_52102740") // 用于定义服务的域名（跳转链接）
                .version("1.0") // 可以用来定义版本
                .contact(contact)
                .build();
    }
}
