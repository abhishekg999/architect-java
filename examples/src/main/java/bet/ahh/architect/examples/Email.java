package bet.ahh.architect.examples;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import bet.ahh.architect.Architect;

public record Email(
        String to,
        String subject,
        @Nullable String priority,
        @Nullable String replyTo,
        Optional<String> body,
        Optional<String> cc) {
    
    public Email(String to, String subject) {
        this(to, subject, null, null, Optional.empty(), Optional.empty());
    }

    @Architect
    public Email {
        if (priority == null) {
            priority = "NORMAL";
        }
    }
}
