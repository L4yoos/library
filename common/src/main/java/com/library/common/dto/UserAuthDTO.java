package com.library.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    // private List<String> roles;
}