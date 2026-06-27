package com.training.dto;

import lombok.Data;

@Data
public class SendOtpRequest {

    private String name;

    private String email;

    private String password;
}
