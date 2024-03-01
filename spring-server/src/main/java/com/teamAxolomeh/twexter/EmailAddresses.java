package com.teamAxolomeh.twexter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailAddresses {
  @JsonProperty("value")
  private String value;

  public EmailAddresses() {
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}