package com.changgou.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全认证配置类：相当于SpringSecurity的配置文件
 */
@Configuration
@EnableWebSecurity
@Order(-1)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * @description: //TODO 放行安全不用拦截的URL：登录相关和静态资源
     * @param: [web]
     * @return: void
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/oauth/login",
                "/oauth/logout", "/oauth/toLogin", "/login.html",
                "/css/**", "/data/**", "/fonts/**", "/img/**", "/js/**");
    }


    /**
     * @description: //TODO 创建授权管理认证对象
     * @param: []
     * @return: org.springframework.security.authentication.AuthenticationManager
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        AuthenticationManager manager = super.authenticationManagerBean();
        return manager;
    }


    /**
     * @description: //TODO 采用BCryptPasswordEncoder对密码进行编码
     * @param: []
     * @return: org.springframework.security.crypto.password.PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /*
     *
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic()            // 启用Http基本身份验证
                .and()
                .formLogin()            // 启用表单身份验证
                .and()
                .authorizeRequests()    // 限制基于Request请求访问
                .anyRequest()
                .authenticated();       // 其他请求都需要经过验证

        // 开启表单登录
        http.formLogin().loginPage("/oauth/toLogin")    // 设置访问登录页面路径
                .loginProcessingUrl("/oauth/login");    // 设置执行登录操作路径
    }
}
