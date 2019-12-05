import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { InputField, Checkbox, Subtitle } from "@scm-manager/ui-components";

type GlobalConfiguration = {
  casUrl: string;
  displayNameAttribute: string;
  enabled: boolean;
  groupAttribute: string;
  mailAttribute: string;
  _links: Links;
};
// navposition
type Props = WithTranslation & {
  initialConfiguration: GlobalConfiguration;
  onConfigurationChange: (p1: GlobalConfiguration, p2: boolean) => void;
};

type State = GlobalConfiguration & {
  configurationChanged?: boolean;
};

class GlobalCasConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  render() {
    const { t } = this.props;
    return (
      <>
        {this.renderConfigChangedNotification()}
        <Checkbox
          name="enabled"
          label={t("scm-cas-plugin.form.enabled")}
          helpText={t("scm-cas-plugin.form.enabledHelp")}
          checked={this.state.enabled}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name="casUrl"
          label={t("scm-cas-plugin.form.url")}
          helpText={t("scm-cas-plugin.form.urlHelp")}
          disabled={!this.state.enabled}
          value={this.state.casUrl}
          onChange={this.valueChangeHandler}
          type="url"
        />
        <div>
          <Subtitle subtitle={t("scm-cas-plugin.form.attributeMapping")} />
          <InputField
            name="displayNameAttribute"
            label={t("scm-cas-plugin.form.displayName")}
            helpText={t("scm-cas-plugin.form.displayNameHelp")}
            disabled={!this.state.enabled}
            value={this.state.displayNameAttribute}
            onChange={this.valueChangeHandler}
          />
          <InputField
            name="mailAttribute"
            label={t("scm-cas-plugin.form.mail")}
            helpText={t("scm-cas-plugin.form.mailHelp")}
            disabled={!this.state.enabled}
            value={this.state.mailAttribute}
            onChange={this.valueChangeHandler}
          />
          <InputField
            name="groupAttribute"
            label={t("scm-cas-plugin.form.groups")}
            helpText={t("scm-cas-plugin.form.groupsHelp")}
            disabled={!this.state.enabled}
            value={this.state.groupAttribute}
            onChange={this.valueChangeHandler}
          />
        </div>
      </>
    );
  }

  renderConfigChangedNotification = () => {
    if (this.state.configurationChanged) {
      return (
        <div className="notification is-info">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                ...this.state,
                configurationChanged: false
              })
            }
          />
          {this.props.t("scm-cas-plugin.configurationChangedSuccess")}
        </div>
      );
    }
    return null;
  };

  valueChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };
}

export default withTranslation("plugins")(GlobalCasConfigurationForm);