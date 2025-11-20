/**
 * @extends {HTMLElement}
 */
class AppFooter extends HTMLElement {
  shadow;

  constructor() {
    super();

    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("app-footer-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);
  }
}

customElements.define("app-footer", AppFooter);
