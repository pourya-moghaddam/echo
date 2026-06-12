package io.github.pourya_moghaddam.echo.user;

import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_validEmail_returnsUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setAvatarUrl("avatar1.png");
        user.setThemePreference(ThemePreference.LIGHT);

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_validUsername_returnsUser() {
        // Given
        User user = new User();
        user.setEmail("test2@example.com");
        user.setUsername("testuser2");
        user.setPassword("hashedpassword");
        
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser2");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test2@example.com");
    }
}
