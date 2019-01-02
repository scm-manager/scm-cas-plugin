// @flow
import React from "react";
import type {Links} from "@scm-manager/ui-types";

type Props = {
  links: Links,
  label: string
};

class LogoutLink extends React.Component<Props> {

  render() {
    const { label, links } = this.props;

    return (
      <li>
        <a href={ links.casLogout.href }>
          { label }
        </a>
      </li>
    );
  }

}

export default LogoutLink;
