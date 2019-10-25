import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import GlobalCasConfigurationForm from "./GlobalCasConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class GlobalCasConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;
    return (
      <>
        <Title title={t("scm-cas-plugin.form.header")} />
        <Configuration link={link} render={props => <GlobalCasConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(GlobalCasConfiguration);
