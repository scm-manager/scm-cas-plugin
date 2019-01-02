// @flow

import { binder } from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalCasConfiguration from "./GlobalCasConfiguration";
import LogoutLink from "./LogoutLink";

cfgBinder.bindGlobal(
  "/cas",
  "scm-cas-plugin.nav-link",
  "casConfig",
  GlobalCasConfiguration
);

binder.bind("primary-navigation.logout", LogoutLink, (props: Object) => {
  return !!(props.links && props.links.casLogout && props.links.casLogout.href);
});
