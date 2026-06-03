package com.michelecampanello.springshop.domains.users.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.michelecampanello.springshop.domains.addresses.model.Address;
import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false, updatable = false, length = 100)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    private Boolean active = true;

    public enum Role {
        CUSTOMER,
        ADMIN
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Deriva l'authority dal ruolo dell'utente (es. ROLE_CUSTOMER, ROLE_ADMIN)
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return this.email; // Usiamo l'email come username per l'autenticazione
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // Ritorna true per i metodi di validazione dello stato account (o gestiscili con campi dedicati)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}