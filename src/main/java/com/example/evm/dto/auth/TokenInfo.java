package com.example.evm.dto.auth;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo {
    private Date expirationDate;
    private long remainingTimeInMs;
    private long expiresIn;

}
