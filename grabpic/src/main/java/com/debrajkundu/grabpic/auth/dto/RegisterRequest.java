package com.debrajkundu.grabpic.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

    @Email
    @NotBlank
    String email,

    @NotBlank
    @Size(min = 5, max = 25)
    String password

){

}
