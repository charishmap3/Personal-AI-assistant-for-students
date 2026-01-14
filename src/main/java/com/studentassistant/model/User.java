package com.studentassistant.model;

import jakarta.persistence.*;

@Entity
@Table(name = "logindet")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Emailid")      // INT AUTO_INCREMENT PRIMARY KEY
    private Integer id;

    @Column(name = "Name", nullable = false, length = 45)
    private String name;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email_id", nullable = false, length = 255)
    private String email;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
