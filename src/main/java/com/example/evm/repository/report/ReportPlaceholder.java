package com.example.evm.repository.report;

// Interface này đóng vai trò là một "domain type" (Entity) giả.
// Nó thỏa mãn yêu cầu generic của CrudRepository mà không cần là một Entity JPA thực sự,
// rất phù hợp cho các Repository chỉ chứa Native Query.
public interface ReportPlaceholder {}