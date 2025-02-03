package com.example.login.model;

import com.example.login.aspect.view.Views;
import com.example.login.enums.Role;
import com.example.login.util.Sensitive;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Data
@Entity
@Table(name = "tb_users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Basic.class)
    private Long id;

    @JsonView(Views.Basic.class)
    private String username;

    @Sensitive
    private String password;

    @NotBlank(message = "Email não pode ser nulo ou vazio")
    @Pattern(regexp = "^[\\w.]+@\\w+\\.\\w+(\\.\\w+)?$",
            message = "O formato do campo 'email' é inválido.")
    @JsonView(Views.Regular.class)
    private String email;

    @Sensitive
    private String lastPassword;

    private LocalDateTime lastAlterPass;

    @JsonView(Views.Basic.class)
    private Boolean enabled;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Enumerated(EnumType.STRING)
    @JsonView(Views.Regular.class)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(() -> role.getAuthority());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEmail(String email) {
        this.email = email;
        this.username = email;
    }
}
