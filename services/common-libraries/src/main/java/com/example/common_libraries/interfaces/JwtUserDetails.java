package com.example.common_libraries.interfaces;

import java.util.Collection;


public interface JwtUserDetails {
    Long getId();
    String getFullName();
    String getEmail();
    Collection<String> getRoles();
}
