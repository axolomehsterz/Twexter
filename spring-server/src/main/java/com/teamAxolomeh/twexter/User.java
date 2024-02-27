package com.teamAxolomeh.twexter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  private String username;
  private String password;
  private String updatedAt;

  private User() {
  }
  
  public Integer getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getUpdatedAt() {
    return this.updatedAt;
  }

}
