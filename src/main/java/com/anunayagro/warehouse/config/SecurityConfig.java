package com.anunayagro.warehouse.config;

import com.anunayagro.warehouse.repositories.SellerRepository;
import com.anunayagro.warehouse.repositories.BuyerRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {

    private final SellerRepository sellerRepository;
    private final BuyerRepository buyerRepository;
    private final StockistRepository stockistRepository;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(SellerRepository sellerRepository, BuyerRepository buyerRepository, StockistRepository stockistRepository) {
        this.sellerRepository = sellerRepository;
        this.buyerRepository = buyerRepository;
        this.stockistRepository = stockistRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/", "/admin/**").hasRole("ADMIN") // Only admin can access admin URLs and home
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // ADMIN login
            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password("admin123")
                        .roles("ADMIN")
                        .build();
            }

            // User logins (seller/buyer/stockist)
            var sellerOpt = sellerRepository.findByMobile(username);
            if (sellerOpt.isPresent()) {
                return User.withUsername(username)
                        .password("password")
                        .roles("SELLER")
                        .build();
            }
            var buyerOpt = buyerRepository.findByMobile(username);
            if (buyerOpt.isPresent()) {
                return User.withUsername(username)
                        .password("password")
                        .roles("BUYER")
                        .build();
            }
            var stockistOpt = stockistRepository.findByMobile(username);
            if (stockistOpt.isPresent()) {
                return User.withUsername(username)
                        .password("password")
                        .roles("STOCKIST")
                        .build();
            }
            throw new UsernameNotFoundException("Mobile number not registered: " + username);
        };
    }

    @Bean
    public static org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Only for demo/testing!
    }
}
