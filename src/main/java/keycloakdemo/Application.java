package keycloakdemo;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@EnableWebSecurity
class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Override
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.sessionManagement()
            .sessionCreationPolicy(STATELESS)
            .and()

            .authorizeRequests()
            .antMatchers("/api/**").hasAuthority("user")
            .anyRequest().permitAll();
    }
}

@RestController
@RequestMapping("/api")
class ApiController {

    @GetMapping
    public Map<String, Object> root(Authentication authentication) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", getName(authentication));
        m.put("authorities", getAuthorities(authentication));
        return m;
    }

    private String getName(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }

    private List<String> getAuthorities(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(toList());
        } else {
            return null;
        }
    }
}
