package org.openconnect.oauth2.server.demo.config;

import java.util.Arrays;
import javax.sql.DataSource;
import org.openconnect.oauth2.server.demo.enhancer.CustomTokenEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class OAuth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {

  private final DataSource dataSource;

  private final AuthenticationManager authenticationManager;

  @Autowired
  public OAuth2ServerConfiguration(final DataSource dataSource, final AuthenticationManager authenticationManager) {
    this.dataSource = dataSource;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource);
  }

  /**
   * 这里干了两个事情，首先打开了验证Token的访问权限（以便之后我们演示）。 然后允许ClientSecret明文方式保存并且可以通过表单提交（而不仅仅是Basic
   * Auth方式提交），之后会演示到这个。
   */
  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    security.checkTokenAccess("permitAll()").allowFormAuthenticationForClients().passwordEncoder(NoOpPasswordEncoder.getInstance());
  }

  /**
   * 干了几个事情： 1、配置我们的Token存放方式不是内存、数据库或Redis方式，而是JWT方式。 JWT是Json Web Token缩写也就是使用JSON数据格式包装的Token，由.句号把整个JWT分隔为头、数据体、签名三部分。
   * JWT保存Token虽然易于使用但是不是那么安全，一般用于内部，并且需要走HTTPS+配置比较短的失效时间。 2、配置了JWT Token的非对称加密来进行签名
   * 3、配置了一个自定义的Token增强器，把更多信息放入Token中 4、配置了使用JDBC数据库方式来保存用户的授权批准记录
   */
  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtTokenEnhancer()));

    endpoints.approvalStore(approvalStore())
        .authorizationCodeServices(authorizationCodeServices())
        .tokenStore(tokenStore())
        .tokenEnhancer(tokenEnhancerChain)
        .authenticationManager(authenticationManager);
  }

  /**
   * 使用JDBC数据库方式来保存授权码
   */
  @Bean
  public AuthorizationCodeServices authorizationCodeServices() {
    return new JdbcAuthorizationCodeServices(dataSource);
  }

  /**
   * 使用JWT令牌存储
   */
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtTokenEnhancer());
  }

  /**
   * 使用JDBC数据库方式来保存用户的授权批准记录
   */
  @Bean
  public JdbcApprovalStore approvalStore() {
    return new JdbcApprovalStore(dataSource);
  }

  /**
   * 自定义的Token增强器，把更多信息放入Token中
   */
  @Bean
  public TokenEnhancer tokenEnhancer() {
    return new CustomTokenEnhancer();
  }

  /**
   * 配置JWT令牌使用非对称加密方式来验证
   */
  @Bean
  protected JwtAccessTokenConverter jwtTokenEnhancer() {
    KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "mySecretKey".toCharArray());
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt"));
    return converter;
  }
}
