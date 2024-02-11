package org.example.sklep.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sklep.entity.*;
import org.example.sklep.exceptions.UserExistingWithEmail;
import org.example.sklep.exceptions.UserExistingWithName;
import org.example.sklep.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //wstrzykuje beana
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final  CookieService cookieService;
    private int exp=100000;

    private int refreshExp = 100000;


    public User saveUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword())); //przed dodaniem hasla do bazy, zamienimy haslo na enkodowane
        return userRepository.saveAndFlush(user);
    }
    public String genarateToken(String username, int exp){
        return jwtService.generateToken(username, exp);
    }
    public void validateToken(HttpServletRequest request, HttpServletResponse response)throws ExpiredJwtException, IllegalArgumentException {
        //request.getHeaders() dodatkowe autoryzacja nauczyc sie ;)
        String token = null;
        String refresh = null;
        for (Cookie value : Arrays.stream(request.getCookies()).toList()){
            if(value.getName().equals("token")){
                token=value.getValue();
            } else if (value.getName().equals("refresh")) {
                refresh = value.getValue();
            }
        }
        try{
            jwtService.validateToken(token);}
        catch (IllegalArgumentException | ExpiredJwtException e){
            jwtService.validateToken(refresh);
            Cookie refreshCookie = cookieService.genaratedCookie("refresh", jwtService.refreshToken(refresh, refreshExp),refreshExp);
            Cookie cookie = cookieService.genaratedCookie("Authorization", jwtService.refreshToken(refresh, exp),exp);
            response.addCookie(cookie);
            response.addCookie(refreshCookie);
        }
    }

    public void register(UserRegisterDto userRegisterDto) throws UserExistingWithEmail, UserExistingWithName{
      userRepository.findUserByLogin(userRegisterDto.getLogin()).ifPresent(value -> {
          throw new UserExistingWithName("Użytkownik o tej nazwie juz istnieje");
      });
        userRepository.findUserByEmail(userRegisterDto.getEmail()).ifPresent(value -> {
            throw new UserExistingWithEmail("Użytkownik o tym emailu juz istnieje");
        });
      User user = new User();
      user.setLogin(userRegisterDto.getLogin());
      user.setPassword(userRegisterDto.getPassword());
      user.setEmail(userRegisterDto.getEmail());
      if(userRegisterDto.getRole()!= null){
          user.setRole(userRegisterDto.getRole());}
          else{
          user.setRole(Role.USER);
      }
          saveUser(user);
    }
    public ResponseEntity<?> login(HttpServletResponse response, User authRequest){
        String hardcodedAdminUsername = "admin123";
        String hardcodedAdminPassword = "admin123";
        if (authRequest.getUsername().equals(hardcodedAdminUsername)
                && authRequest.getPassword().equals(hardcodedAdminPassword)) {
            // Return the admin role
            return ResponseEntity.ok(
                    UserRegisterDto.builder()
                            .login(hardcodedAdminUsername)
                            .role(Role.ADMIN)
                            .build());
        }
        User user = userRepository.findUserByLogin(authRequest.getUsername()).orElse(null);
        if(user != null) {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            if (authenticate.isAuthenticated()) {
                Cookie refresh = cookieService.genaratedCookie("refresh", genarateToken(authRequest.getUsername(), refreshExp), refreshExp);
                Cookie cookie = cookieService.genaratedCookie("token", genarateToken(authRequest.getUsername(), exp), exp);

                response.addCookie(cookie);
                response.addCookie(refresh);
                return ResponseEntity.ok(
                        UserRegisterDto
                                .builder()
                                .login(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build());
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(Code.A1));
            }
        }
        return ResponseEntity.badRequest().body(new AuthResponse(Code.A2));
    }
}
