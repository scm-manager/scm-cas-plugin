// @flow

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalCasConfiguration from "./GlobalCasConfiguration";

cfgBinder.bindGlobal(
  "/cas",
  "scm-cas-plugin.nav-link",
  "casConfig",
  GlobalCasConfiguration
);
