package com.sinsoled.myblog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinsoled.myblog.security.handler.MyAccessDeniedHandler;
import com.sinsoled.myblog.security.handler.MyAuthEntryPoint;
import com.sinsoled.myblog.security.handler.MyAuthFailureHandler;
import com.sinsoled.myblog.security.handler.MyAuthSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsUtils;

@Configuration
public class MyWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyAuthSuccessHandler myAuthSuccessHandler;

    @Autowired
    private MyAuthFailureHandler myAuthFailureHandler;

    @Autowired
    private MyAuthEntryPoint myAuthEntryPoint;

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;


    /**
     * 使用BCrypt算法加密
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 将用户信息交给security
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String[] urls = {"baseUser/register"};

        // 关闭跨域攻击
        http.csrf().disable();

        // 开启跨域
//        http.cors();

        // cors预检请求放行PreFlightRequest
        http
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest)
                .permitAll()
                .antMatchers(urls)
                .permitAll()
                .anyRequest()
                .authenticated();

        http
                .logout()
                .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    httpServletResponse.setContentType("application/json;charset=UTF-8");
                    httpServletResponse.getWriter().write(objectMapper.writeValueAsString(authentication));
                })
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {

                });


        http
                .exceptionHandling()
                .authenticationEntryPoint(myAuthEntryPoint)
                .accessDeniedHandler(myAccessDeniedHandler);

    }
}
