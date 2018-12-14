package com.cloudogu.scm.cas;

import sonia.scm.plugin.Extension;

@Extension
public class Configuration {

  private String casLoginUrl = "http://localhost:8082/cas/login";

  private String casUrl = "http://localhost:8082/cas";

  private String displayNameAttribute = "displayName";
  private String mailAttribute = "mail";

  public String getDisplayNameAttribute() {
    return displayNameAttribute;
  }

  public void setDisplayNameAttribute(String displayNameAttribute) {
    this.displayNameAttribute = displayNameAttribute;
  }

  public String getMailAttribute() {
    return mailAttribute;
  }

  public void setMailAttribute(String mailAttribute) {
    this.mailAttribute = mailAttribute;
  }

  public String getCasUrl() {
    return casUrl;
  }

  public void setCasUrl(String casUrl) {
    this.casUrl = casUrl;
  }

  public void setCasLoginUrl(String casLoginUrl) {
    this.casLoginUrl = casLoginUrl;
  }

  public String getCasLoginUrl() {
    return casLoginUrl;
  }
}
