package com.michelecampanello.springshop.support;

import com.michelecampanello.springshop.domains.users.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.UUID;

public class WithMockAuthUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser annotation) {
        User user = new User();
        user.setId(UUID.fromString(annotation.id()));
        user.setEmail(annotation.email());
        user.setRole(User.Role.valueOf(annotation.role()));

        UsernamePasswordAuthenticationToken auth =
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }
}
