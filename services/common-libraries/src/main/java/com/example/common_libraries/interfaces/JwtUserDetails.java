package com.example.common_libraries.interfaces;


public interface JwtUserDetails {
    Long getId();
    String getFullName();
    String getEmail();
    String getRoles();
}
