package bet.ahh.architect.examples;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import bet.ahh.architect.Architect;

public class User {
    private final String name;
    private final Optional<String> email;
    private final int age;
    private final String role;
    private final String nickname;
    private final Optional<String> phone;

    @Architect
    public User(
            String name,
            int age,
            @Nullable String role,
            @Nullable String nickname,
            Optional<String> email,
            Optional<String> phone) {
        this.name = name;
        this.age = age;
        this.role = role != null ? role : "USER";
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    public Optional<String> getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", role='" + role + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email=" + email +
                ", phone=" + phone +
                '}';
    }
}
