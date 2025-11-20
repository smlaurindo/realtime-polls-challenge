import { appState } from "./utils.js";

class PollHeader extends HTMLElement {
  shadow;

  constructor() {
    super();
    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("poll-header-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);
  }

  connectedCallback() {
    this.render();
    this.setupEventListeners();

    appState.subscribe("langChanged", () => this.render());
    appState.subscribe("themeChanged", () => this.updateThemeIcon());
  }

  setupEventListeners() {
    this.shadow
      .getElementById("createPollBtn")
      .addEventListener("click", () => {
        this.dispatchEvent(
          new CustomEvent("create-poll", { bubbles: true, composed: true })
        );
      });

    this.shadow
      .getElementById("themeToggle")
      .addEventListener("click", () => {
        const newTheme = appState.theme === "dark" ? "light" : "dark";
        appState.setTheme(newTheme);
      });

    this.shadow
      .getElementById("langToggle")
      .addEventListener("click", () => {
        const newLang = appState.lang === "en" ? "pt" : "en";
        appState.setLang(newLang);
      });
  }

  updateThemeIcon() {
    const btn = this.shadow.getElementById("themeToggle");
    
    if (btn) {
      btn.textContent = appState.theme === "dark" ? "☀️" : "🌙";
    }
  }

  render() {
    const appTitleH1 = this.shadow.querySelector(".logo");
    const createPollButton = this.shadow.getElementById("createPollBtn");
    const themeToggleButton = this.shadow.getElementById("themeToggle");
    const langToggleButton = this.shadow.getElementById("langToggle");

    appTitleH1.textContent = `${appState.t("appTitle")}`
    createPollButton.textContent = appState.t("createPollText")
    themeToggleButton.title = appState.t("toggleThemeTitle")
    langToggleButton.title = appState.t("changeLanguageTitle");
  }
}

customElements.define("poll-header", PollHeader);
