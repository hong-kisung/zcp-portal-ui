package com.skcc.cloudz.zcp.common.security.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcc.cloudz.zcp.api.iam.domain.vo.ZcpUserResVo;
import com.skcc.cloudz.zcp.api.iam.service.IamApiService;
import com.skcc.cloudz.zcp.common.security.vo.OpenIdConnectUserDetailsVo;

public class OpenIdConnectFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final Logger log = LoggerFactory.getLogger(OpenIdConnectFilter.class);
    
    @Autowired
    private IamApiService iamApiService;
    
    public OpenIdConnectFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(new NoopAuthenticationManager());
    }
    
    public OAuth2RestOperations restTemplate;
    
    public void setRestTemplate(OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static class NoopAuthenticationManager implements AuthenticationManager {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
        }
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        OAuth2AccessToken accessToken;
        
        try {
            accessToken = restTemplate.getAccessToken();
        } catch (final OAuth2Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("Could not obtain access token", e);
        }
        
        try {
            final String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
            final Jwt tokenDecoded = JwtHelper.decode(idToken);
            
            @SuppressWarnings("unchecked")
            final Map<String, String> authInfo = new ObjectMapper().readValue(tokenDecoded.getClaims(), Map.class);
            
            /* get user info */
            ZcpUserResVo zcpUserResVo = iamApiService.getUser(authInfo.get("sub"));
            
            /* set service account */
            iamApiService.serviceAccount(authInfo.get("sub"));
            
            final OpenIdConnectUserDetailsVo user = new OpenIdConnectUserDetailsVo(authInfo, accessToken, zcpUserResVo.getData());
            log.info("=======> user : {}", user.toString());
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } catch (final InvalidTokenException e) {
            throw new BadCredentialsException("Could not obtain user details from token", e);
        }
    }

}
