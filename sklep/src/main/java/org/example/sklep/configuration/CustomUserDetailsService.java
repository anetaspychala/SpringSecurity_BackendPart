package org.example.sklep.configuration;

import lombok.RequiredArgsConstructor;
import org.example.sklep.entity.User;
import org.example.sklep.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<User> user =userRepository.findUserByLogin(username);
        return user.map(CustomUserDetails::new).orElseThrow(()-> new UsernameNotFoundException("User not found with name: "+ username));
    }

}
