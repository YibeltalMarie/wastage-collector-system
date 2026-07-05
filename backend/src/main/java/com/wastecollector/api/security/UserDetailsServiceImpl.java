package com.wastecollector.api.security;

import com.wastecollector.api.model.entity.User;
import com.wastecollector.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements Spring Security's UserDetailsService interface.
 *
 * Spring Security calls loadUserByUsername() when it needs to:
 *   1. Verify credentials during login
 *   2. Load the user from the database based on the JWT subject claim
 *
 * "Username" in Spring Security's world is our phone number.
 * Spring Security is generic — it uses "username" to mean "the identifier".
 *
 * @RequiredArgsConstructor (Lombok):
 *   Generates a constructor for all final fields.
 *   This enables constructor injection — the recommended way to inject
 *   dependencies in Spring (preferred over @Autowired on fields).
 *
 *   Generated constructor:
 *   public UserDetailsServiceImpl(UserRepository userRepository) {
 *       this.userRepository = userRepository;
 *   }
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their phone number.
     *
     * Spring Security calls this during:
     *   1. Login: to get the stored password hash for comparison
     *   2. JWT filter: to build the Authentication object from the token
     *
     * @Transactional: wraps this method in a database transaction.
     * Required here because we access the user's data within a session context.
     *
     * Returns UserDetails — Spring Security's representation of a user:
     *   - username (phone number)
     *   - password (the BCrypt hash)
     *   - authorities (roles as GrantedAuthority objects)
     *   - account status flags (enabled, locked, expired)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber)
            throws UsernameNotFoundException {

        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with phone number: " + phoneNumber
            ));

        /**
         * SimpleGrantedAuthority converts our Role enum to Spring Security format.
         *
         * Spring Security expects roles prefixed with "ROLE_".
         * So Role.CITIZEN becomes "ROLE_CITIZEN".
         *
         * This is what @PreAuthorize("hasRole('CITIZEN')") checks against.
         * hasRole('CITIZEN') internally checks for authority "ROLE_CITIZEN".
         */
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        /**
         * org.springframework.security.core.userdetails.User (not our User entity)
         * is a built-in Spring Security implementation of UserDetails.
         *
         * Parameters:
         *   username    → phone number (our login identifier)
         *   password    → the BCrypt hash from the database
         *   enabled     → is the account active?
         *   accountNonExpired    → always true for us
         *   credentialsNonExpired → always true for us
         *   accountNonLocked    → always true for us (we use isActive instead)
         *   authorities → the list of roles
         */
        return new org.springframework.security.core.userdetails.User(
            user.getPhoneNumber(),
            user.getPasswordHash(),
            user.isActive(),          // enabled
            true,                     // accountNonExpired
            true,                     // credentialsNonExpired
            true,                     // accountNonLocked
            authorities
        );
    }
}
