package io.security.springsecuritymaster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity // SpringSecurity 사용
@Configuration // 스프링 IoC 컨테이너 등록
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(auth -> auth.anyRequest().authenticated()) // 모든 요청에 대해 인증이 필요
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails ronaldo = User.withUsername("Cristiano")
                .password("{noop}1234")
                .roles("GOD").build();

        UserDetails messi = User.withUsername("Lionel")
                .password("{noop}1234")
                .roles("DOG").build();

        return new InMemoryUserDetailsManager(ronaldo, messi);
    }
}
