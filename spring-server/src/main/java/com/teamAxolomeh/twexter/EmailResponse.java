package com.teamAxolomeh.twexter;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailResponse {
  @JsonProperty("emailAddresses")
  private List<EmailAddresses> emailAddresses;

  public EmailResponse() {
  }

  public List<EmailAddresses> getEmailAddresses() {
    return this.emailAddresses;
  }

  public void setEmailAddresses(List<EmailAddresses> emailAddresses) {
    this.emailAddresses = emailAddresses;
  }
}