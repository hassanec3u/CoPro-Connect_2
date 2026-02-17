package com.copro.connect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
    
    @Id
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Indexed(unique = true)
    private String username;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @JsonIgnore
    private String password;
    
    private String name;
    
    @Email(message = "L'adresse email n'est pas valide")
    private String email;
    
    private String role;
    
    private boolean mfaEnabled = true;
    
    @CreatedDate
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @LastModifiedDate
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    // Implémentation de UserDetails pour Spring Security
    
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")));
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
