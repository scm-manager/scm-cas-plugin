// @flow
import React from "react";
import { Title } from "@scm-manager/ui-components";
import Configuration from "@scm-manager/ui-components/src/config/Configuration";
import GlobalCasConfigurationForm from "./GlobalCasConfigurationForm";
import { translate } from "react-i18next";

type Props = {
  link: string,
  t: string => string
};

class GlobalCasConfiguration extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-cas-plugin.form.header")} />
        <Configuration
          link={link}
          render={props => <GlobalCasConfigurationForm {...props} />}
          t={t}
        />
      </>
    );
  }
}

export default translate("plugins")(GlobalCasConfiguration);
