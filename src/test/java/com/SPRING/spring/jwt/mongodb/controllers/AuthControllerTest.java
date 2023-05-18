package com.SPRING.spring.jwt.mongodb.controllers;

import static org.mockito.Mockito.when;

import com.SPRING.spring.jwt.mongodb.payload.request.LoginRequest;
import com.SPRING.spring.jwt.mongodb.payload.request.SignupRequest;
import com.SPRING.spring.jwt.mongodb.security.jwt.JwtUtils;
import com.SPRING.spring.jwt.mongodb.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {AuthController.class})
@ExtendWith(SpringExtension.class)
class AuthControllerTest {
    @Autowired
    private AuthController authController;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    /**
     * Method under test: {@link AuthController#authenticateUser(LoginRequest)}
     */
    @Test
    void testAuthenticateUser() throws Exception {
        when(jwtUtils.generateJwtToken(Mockito.<Authentication>any())).thenReturn("ABC123");
        when(authenticationManager.authenticate(Mockito.<Authentication>any())).thenReturn(new TestingAuthenticationToken(
                new UserDetailsImpl("42", "janedoe", "jane.doe@example.org", "iloveyou", new ArrayList<>()), "Credentials"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("iloveyou");
        loginRequest.setUsername("janedoe");
        String content = (new ObjectMapper()).writeValueAsString(loginRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"id\":\"42\",\"username\":\"janedoe\",\"email\":\"jane.doe@example.org\",\"roles\":[],\"accessToken\":\"ABC123\","
                                        + "\"tokenType\":\"Bearer\"}"));
    }

    /**
     * Method under test: {@link AuthController#authenticateUser(LoginRequest)}
     */
    @Test
    void testAuthenticateUser2() throws Exception {
        when(jwtUtils.generateJwtToken(Mockito.<Authentication>any())).thenReturn("ABC123");
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenReturn(new TestingAuthenticationToken("Principal", "Credentials"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("");
        loginRequest.setUsername("janedoe");
        String content = (new ObjectMapper()).writeValueAsString(loginRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    /**
     * Method under test: {@link AuthController#registerUser(SignupRequest)}
     */
    @Test
    void testRegisterUser() throws Exception {
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setRoles(new HashSet<>());
        user.setUsername("janedoe");
        when(userRepository.existsByEmail(Mockito.<String>any())).thenReturn(true);
        when(userRepository.existsByUsername(Mockito.<String>any())).thenReturn(true);
        when(userRepository.save(Mockito.<User>any())).thenReturn(user);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("jane.doe@example.org");
        signupRequest.setPassword("iloveyou");
        signupRequest.setRole(new HashSet<>());
        signupRequest.setUsername("janedoe");
        String content = (new ObjectMapper()).writeValueAsString(signupRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("{\"message\":\"Error: Username is already taken!\"}"));
    }

    /**
     * Method under test: {@link AuthController#registerUser(SignupRequest)}
     */
    @Test
    void testRegisterUser2() throws Exception {
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setRoles(new HashSet<>());
        user.setUsername("janedoe");
        when(userRepository.existsByEmail(Mockito.<String>any())).thenReturn(true);
        when(userRepository.existsByUsername(Mockito.<String>any())).thenReturn(false);
        when(userRepository.save(Mockito.<User>any())).thenReturn(user);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("jane.doe@example.org");
        signupRequest.setPassword("iloveyou");
        signupRequest.setRole(new HashSet<>());
        signupRequest.setUsername("janedoe");
        String content = (new ObjectMapper()).writeValueAsString(signupRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("{\"message\":\"Error: Email is already in use!\"}"));
    }
}

