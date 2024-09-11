/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Links} from "@scm-manager/ui-types";
import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import {binder} from "@scm-manager/ui-extensions";
import GlobalCasConfiguration from "./GlobalCasConfiguration";
import CasLoginLink from "./CasLoginLink";

cfgBinder.bindGlobal("/cas", "scm-cas-plugin.nav-link", "casConfig", GlobalCasConfiguration);
binder.bind("primary-navigation.login", CasLoginLink, props => "casLogin" in (props?.links as Links));
