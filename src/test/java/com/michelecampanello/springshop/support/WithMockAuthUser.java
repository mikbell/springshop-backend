package com.michelecampanello.springshop.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation that sets up a SecurityContext with a real
 * {@link com.michelecampanello.springshop.domains.users.model.User} as principal.
 * Works with {@code @WebMvcTest(addFilters = false)} because
 * {@code @WithSecurityContext} populates {@code TestSecurityContextHolder} before
 * the test method runs, independently of the filter chain.
 *
 * <p>The {@code id} is a fixed UUID string so tests can reference it when
 * stubbing service calls.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAuthUserSecurityContextFactory.class)
public @interface WithMockAuthUser {
    String id() default "11111111-1111-1111-1111-111111111111";
    String email() default "mario@x.it";
    String role() default "CUSTOMER";
}
