package com.aritra.ecs.demo.controller;

import com.aritra.ecs.demo.inputs.SigninInput;
import com.aritra.ecs.demo.inputs.SignupInput;
import com.aritra.ecs.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @RequestMapping(path = "/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void signUp(@RequestBody SignupInput input) {
        authService.registerUser(input.getUsername(), input.getPassword());
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String login(@RequestBody SigninInput input) {
        return authService.signin(input.getUsername(), input.getPassword());
    }
}
