package com.cloudogu.scm.cas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "cas")
public class Configuration {

  private String casUrl;
  private String displayNameAttribute = "displayName";
  private String mailAttribute = "mail";
  private String groupAttribute = "groups";
  private boolean enabled;

}
