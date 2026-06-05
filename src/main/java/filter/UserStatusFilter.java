package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import repository.UserRepository;
import entity.User;

import java.io.IOException;

public class UserStatusFilter extends OncePerRequestFilter {
    
    private final UserRepository userRepository;
    
    public UserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Only check for authenticated users (not anonymous)
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            
            try {
                User user = userRepository.findByUsername(authentication.getName()).orElse(null);
                
                // If user exists and is disabled, force logout
                if (user != null && !user.isEnabled()) {
                    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                    logoutHandler.logout(request, response, authentication);
                    
                    // Redirect to account locked page
                    response.sendRedirect(request.getContextPath() + "/auth/account-locked?username=" + 
                        java.net.URLEncoder.encode(user.getUsername(), java.nio.charset.StandardCharsets.UTF_8));
                    return;
                }
            } catch (Exception e) {
                // Log error but continue with request
                e.printStackTrace();
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

