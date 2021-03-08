package com.bhusalb.runtrackingservice.models;

import com.bhusalb.runtrackingservice.Constants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@Document (collection = "Users")
public class User implements UserDetails, Serializable {

    @Id
    private ObjectId id;

    @NotBlank
    @Size (max = Constants.MAX_EMAIL_SIZE)
    @Email
    @Indexed (unique = true)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Indexed
    private String fullName;

    @NotEmpty
    private Set<Role> roles;

    @Indexed
    @CreatedDate
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime updated;

    @CreatedBy
    @Indexed (sparse = true)
    private ObjectId createdBy;

    @LastModifiedBy
    private ObjectId lastModifiedBy;

    public User (final String email, final String password) {
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>();
    }

    @Override
    public Collection<Role> getAuthorities () {
        return roles;
    }

    @Override
    public String getUsername () {
        return email;
    }

    @Override
    public boolean isAccountNonExpired () {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked () {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired () {
        return isEnabled();
    }

    @Override
    public boolean isEnabled () {
        return true;
    }
}
