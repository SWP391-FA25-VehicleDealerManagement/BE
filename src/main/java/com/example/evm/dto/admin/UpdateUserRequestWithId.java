package com.example.evm.dto.admin;

import lombok.Data;

@Data
public class UpdateUserRequestWithId {
Long userId;
     String phone;
    String email;
    String fullName;
}

