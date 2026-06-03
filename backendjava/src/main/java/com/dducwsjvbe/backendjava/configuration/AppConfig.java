package com.dducwsjvbe.backendjava.configuration;

import com.dducwsjvbe.backendjava.filter.JwtAuthenticationFilter;
import com.dducwsjvbe.backendjava.service.auth.CustomUserDetailsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class AppConfig {

    private String[] WHITE_LIST={"/auth/**","/actuator/**","/backend-project/**"};
    private String[] Authorities_ADMIN_POST={"/files/upload-chunk","/files/file-upload","/files/update-product"};
    private String[] Authorities_ADMIN_UPDATE={"/files/product-inactive","/files/approve-product/{productId}"};
    private String[] Authorities_SYSTEM_FULL={"/user/create-admin"};

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    //Spring Security
    @Bean
    public SecurityFilterChain configure(@NonNull HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(Authorities_ADMIN_POST).hasAnyAuthority("SYSTEM_FULL","ADMIN_POST")
                                .requestMatchers(Authorities_ADMIN_UPDATE).hasAnyAuthority("SYSTEM_FULL","ADMIN_UPDATE")
                                .requestMatchers(Authorities_SYSTEM_FULL).hasAnyAuthority("SYSTEM_FULL")
                                .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    //PasswordEncoder
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
    //provider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(getPasswordEncoder());
        return provider;
    }
    //CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8500")
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Allowed HTTP methods
                        .allowedHeaders("*") // Allowed request headers
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
    //openApi
    @Bean
    public WebSecurityCustomizer ignoreResources() {
        return (webSecurity) -> webSecurity
                .ignoring()
                .requestMatchers("/actuator/**", "/v3/**", "/webjars/**", "/swagger-ui*/*swagger-initializer.js", "/swagger-ui*/**");
    }

}
/*
cách 1:implements WebMvcConfigurer
 @Override
    public void addCorsMappings(CorsRegistry registry) {
 registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOrigins("http://localhost:4200") FE Web/app nào được gọi
                .allowedMethods("POST,GET,PUT,DELETE,OPTIONS") phương thức nào đươc vào
                .allowedHeaders("*") header(vd:token) nào được vào
                ;
                }
cách 2:no impl
   @Bean
    public WebMvcConfigurer corsFilter() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
cách 3:no impl
   @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:5173"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        //chỉ định api nào đc vào
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean<>(new CorsFilter(source));
        //khởi rạo bean đầu tiên
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
    public class AppConfig extends OncePerRequestFilter {
    //cách 4:lọc request(chạy trc cả controller,vd:token)
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //chỉ nhận header:Access-Control-Allow-Origin từ http://localhost:4200
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        filterChain.doFilter(request, response);

    }
}
 */