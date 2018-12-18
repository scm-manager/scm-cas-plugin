package com.cloudogu.scm.cas.config;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfigurationDto extends HalRepresentation {

  private String casUrl;
  private String displayNameAttribute;
  private String mailAttribute;
  private String groupAttribute;
  private boolean enabled;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
