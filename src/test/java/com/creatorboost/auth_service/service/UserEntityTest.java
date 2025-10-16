package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.entiy.UserRole;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void testUserEntityBuilderAndGetters() {
        Instant now = Instant.now();

        UserEntity user = UserEntity.builder()
                .id(1L)
                .userId("UID123")
                .name("John Doe")
                .email("john@example.com")
                .password("securePass")
                .role(UserRole.CLIENT)
                .imageUrl("http://example.com/image.jpg")
                .verifyOtp("123456")
                .isAccountVerified(true)
                .verifyOtpExpiry(now.plusSeconds(300))
                .resetOtp("654321")
                .resetOtpExpiry(now.plusSeconds(600))
                .isSuspended(false)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUserId()).isEqualTo("UID123");
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("securePass");
        assertThat(user.getRole()).isEqualTo(UserRole.CLIENT);
        assertThat(user.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(user.getVerifyOtp()).isEqualTo("123456");
        assertThat(user.isAccountVerified()).isTrue();
        assertThat(user.getResetOtp()).isEqualTo("654321");
        assertThat(user.isSuspended()).isFalse();
    }

    @Test
    void testSettersAndEqualsHashcode() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setEmail("a@example.com");

        UserEntity user2 = new UserEntity();
        user2.setId(1L);
        user2.setEmail("a@example.com");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testDefaultValues() {
        UserEntity user = new UserEntity();
        assertThat(user.isSuspended()).isFalse();
        assertThat(user.isAccountVerified()).isFalse();
    }
}