package com.aritra.ecs.demo.service;

import com.aritra.ecs.demo.dao.UserDao;
import com.aritra.ecs.demo.entity.User;

import com.aritra.ecs.demo.exceptions.CustomException;
import com.aritra.ecs.demo.exceptions.ResourceAlreadyExists;
import com.aritra.ecs.demo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    @Qualifier("users")
    private UserDao userDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public void registerUser(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null) {
            throw new ResourceAlreadyExists("Username is already in use.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        userDao.registerUser(username,encodedPassword);
    }

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            User user = userDao.findByUsername(username);
            if (user != null) {
                return jwtTokenProvider.createToken(username, user.getRoles());
            }
            throw new CustomException("Bad credentials", HttpStatus.OK);
        } catch (BadCredentialsException ex) {
            throw new CustomException("Bad Credentials", HttpStatus.OK);
        }
    }
}
