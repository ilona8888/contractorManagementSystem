package com.ilona.contractormanagementsystem.entity;

import com.ilona.contractormanagementsystem.enam.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.CONTRACTOR;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private ClientOrg clientOrg;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Contractor contractor;


    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
