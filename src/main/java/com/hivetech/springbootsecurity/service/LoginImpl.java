package com.hivetech.springbootsecurity.service;

import com.hivetech.springbootsecurity.payload.request.LoginRequest;
import com.hivetech.springbootsecurity.payload.response.JwtResponse;

public interface LoginImpl {
    JwtResponse authentication(LoginRequest loginRequest);

}
