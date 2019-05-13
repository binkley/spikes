package x.loggy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityConfiguration
        extends WebSecurityConfigurerAdapter {
    private final SecurityProblemSupport problemSupport;

    @Override
    protected void configure(final HttpSecurity http)
            throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport);
        http
                .csrf().disable()
                .authorizeRequests().anyRequest().permitAll();
    }
}
