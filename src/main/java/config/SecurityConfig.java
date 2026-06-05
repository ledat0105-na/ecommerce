package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import repository.UserRepository;
import filter.UserStatusFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository repo) {
	     return username -> repo.findByUsername(username)
	             .map(u -> User.withUsername(u.getUsername()).password(u.getPassword())
	                     .roles(u.getRoleFromVaiTro().replace("ROLE_", ""))
	                     .disabled(!u.isEnabled())
	                     .build())
	             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		return new InMemoryTokenRepositoryImpl();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        http.authenticationProvider(authenticationProvider(userRepository))
                .authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/product/**", "/auth/**", "/h2-console/**", "/css/**", "/js/**", "/images/**", "/uploads/**")
				.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
                .formLogin(l -> l
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            // Check if user is disabled
                            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                String username = request.getParameter("username");
                                response.sendRedirect("/auth/account-locked?username=" + 
                                    (username != null ? java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8) : ""));
                            } else {
                                response.sendRedirect("/auth/login?error=" + exception.getClass().getSimpleName());
                            }
                        })
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getAuthorities().stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
				.logout(l -> l.logoutUrl("/auth/logout").logoutSuccessUrl("/").deleteCookies("JSESSIONID"))
				.rememberMe(rm -> rm
						.tokenRepository(persistentTokenRepository())
						.tokenValiditySeconds(86400)
						.userDetailsService(userDetailsService(userRepository)))
				.sessionManagement(sm -> sm
						.maximumSessions(1)
						.maxSessionsPreventsLogin(false)
						.expiredUrl("/auth/login?expired=true"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/auth/login", "/cart/add-ajax/**", "/cart/update/**"))
				.headers(h -> h.frameOptions(f -> f.disable()));
		return http.build();
	}

    @Bean
    public AuthenticationProvider authenticationProvider(UserRepository repo) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService(repo));
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public FilterRegistrationBean<UserStatusFilter> userStatusFilter(UserRepository userRepository) {
        FilterRegistrationBean<UserStatusFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserStatusFilter(userRepository));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
