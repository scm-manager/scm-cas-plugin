package com.cloudogu.scm.cas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Configuration {

  private String casUrl;
  private String displayNameAttribute = "displayName";
  private String mailAttribute = "mail";
  private String groupAttribute = "groups";
  private boolean enabled = false;

}
