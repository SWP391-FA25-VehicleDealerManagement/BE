
package com.example.evm.dto.admin;


public record UpdateUserRequest(
    String fullName,
  String phone,
    String email
) {}