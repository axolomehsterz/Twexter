package com.teamAxolomeh.twexter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubResponse {

  @JsonProperty("login")
  private String login;

  public GitHubResponse() {
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }
}