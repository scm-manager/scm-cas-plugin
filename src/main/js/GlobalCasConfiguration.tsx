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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { useRequiredIndexLink } from "@scm-manager/ui-api";
import { Configuration } from "@scm-manager/ui-components";
import { Title } from "@scm-manager/ui-core";
import GlobalCasConfigurationForm from "./GlobalCasConfigurationForm";

const GlobalCasConfiguration: FC = () => {
  const [t] = useTranslation("plugins");
  const link = useRequiredIndexLink("casConfig");

  return (
    <>
      <Title title={t("scm-cas-plugin.form.header")} />
      <Configuration link={link} render={(props) => <GlobalCasConfigurationForm {...props} />} />
    </>
  );
};

export default GlobalCasConfiguration;
