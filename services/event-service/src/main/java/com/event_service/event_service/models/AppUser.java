package com.event_service.event_service.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@Setter
@Getter
@ToString
public class AppUser {

    private final Long id;
    private final List<String> roles;

}
