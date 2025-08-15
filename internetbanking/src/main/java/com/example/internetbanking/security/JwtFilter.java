package com.example.internetbanking.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.internetbanking.service.impl.JWTService;
import com.example.internetbanking.service.impl.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtFilter extends OncePerRequestFilter {
	@Autowired
	private JWTService jwtService;
	@Autowired
	private ApplicationContext context;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		 if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
		        filterChain.doFilter(request, response);
		        return;  // skip JWT validation on OPTIONS
		    }
		String authHeader=request.getHeader("Authorization");
		String token=null;
		String userId=null;
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			token=authHeader.substring(7);
			userId=jwtService.extractUserId(token);
		}
		if(userId!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
			UserDetails userDetails=context.getBean(MyUserDetailsService.class).loadUserByUsername(userId);
			if(jwtService.validateToken(token,userDetails)) {
				UsernamePasswordAuthenticationToken authToken=
						new UsernamePasswordAuthenticationToken(userId,null, userDetails.getAuthorities());	
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
				}
		}
		filterChain.doFilter(request, response);
	}
	

}

