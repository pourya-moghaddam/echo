package io.github.pourya_moghaddam.echo.user;

import io.github.pourya_moghaddam.echo.community.Community;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @ManyToMany
    @JoinTable(
        name = "user_communities",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "community_id")
    )
    private java.util.Set<Community> joinedCommunities = new java.util.HashSet<>();

    public void joinCommunity(Community community) {
        this.joinedCommunities.add(community);
        community.getMembers().add(this);
    }

    public void leaveCommunity(Community community) {
        this.joinedCommunities.remove(community);
        community.getMembers().remove(this);
    }

}
