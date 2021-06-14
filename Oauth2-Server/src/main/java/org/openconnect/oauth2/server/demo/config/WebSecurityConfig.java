package org.openconnect.oauth2.server.demo.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final DataSource dataSource;

  @Autowired
  public WebSecurityConfig(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(new BCryptPasswordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/h2-console/**").permitAll()
        .antMatchers("/login", "/oauth/authorize")
        .permitAll()
        .anyRequest().authenticated()
        .and().formLogin().loginPage("/login");
  }

  @Override
  public void configure(WebSecurity web) {
    String[] swaggerResources = new String[]{
        "/webjars/**",
        "/h2-console/**",
    };
    web.ignoring().antMatchers(swaggerResources);
  }
}
