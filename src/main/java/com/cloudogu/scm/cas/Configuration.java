package com.cloudogu.scm.cas;

import sonia.scm.plugin.Extension;

@Extension
public class Configuration {

  private String casLoginUrl = "https://cas.hitchhiker.com:8443/cas/login";

  private String casUrl = "https://cas.hitchhiker.com:8443/cas";

  private String displayNameAttribute = "displayName";
  private String mailAttribute = "mail";
  private String groupAttribute = "groups";

  public String getGroupAttribute() {
    return groupAttribute;
  }

  public void setGroupAttribute(String groupAttribute) {
    this.groupAttribute = groupAttribute;
  }

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
