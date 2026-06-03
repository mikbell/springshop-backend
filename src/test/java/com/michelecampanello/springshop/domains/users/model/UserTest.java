package com.michelecampanello.springshop.domains.users.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void getAuthoritiesReflectsCustomerRole() {
        User user = new User();
        user.setRole(User.Role.CUSTOMER);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void getAuthoritiesReflectsAdminRole() {
        User user = new User();
        user.setRole(User.Role.ADMIN);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
}
