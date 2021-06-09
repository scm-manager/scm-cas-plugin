/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import {WithTranslation, withTranslation} from "react-i18next";
import {Links} from "@scm-manager/ui-types";
import {InputField, Textarea, Checkbox, Subtitle} from "@scm-manager/ui-components";

type GlobalConfiguration = {
  casUrl: string;
  displayNameAttribute: string;
  enabled: boolean;
  groupAttribute: string;
  mailAttribute: string;

  acceptAnyProxy: boolean;
  allowedProxyChains: string;
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
    const {t} = this.props;
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
          <Subtitle subtitle={t("scm-cas-plugin.form.attributeMapping")}/>
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
        <div>
          <Subtitle subtitle={t("scm-cas-plugin.form.proxyConfiguration")}/>
          <Checkbox
            name="acceptAnyProxy"
            label={t("scm-cas-plugin.form.acceptAnyProxy")}
            helpText={t("scm-cas-plugin.form.acceptAnyProxyHelp")}
            checked={this.state.acceptAnyProxy}
            disabled={!this.state.enabled}
            onChange={this.valueChangeHandler}
          />
          <Textarea
            name="allowedProxyChains"
            label={t("scm-cas-plugin.form.allowedProxyChains")}
            helpText={t("scm-cas-plugin.form.allowedProxyChainsHelp")}
            disabled={!this.state.enabled}
            value={this.state.allowedProxyChains}
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
