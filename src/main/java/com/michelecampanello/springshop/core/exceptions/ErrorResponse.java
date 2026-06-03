package com.michelecampanello.springshop.core.exceptions;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        LocalDateTime timestamp,
        String message,
        String details
) {}