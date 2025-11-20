import { appState } from "./utils.js";

class PollFilters extends HTMLElement {
  shadow;

  constructor() {
    super();
    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("poll-filters-template")
      .content.cloneNode(true);

    this.filters = { status: "", sort: "startsAt,desc" };

    this.shadow.appendChild(template);
  }

  connectedCallback() {
    this.render();
    appState.subscribe("langChanged", () => this.render());
  }

  #onStatusChange = (e) => {
    this.filters.status = e.target.value;
  }

  #onSortChange = (e) => {
    this.filters.sort = e.target.value;
  }

  #onApplyFilters = () => {
    this.dispatchEvent(
      new CustomEvent("filters-changed", {
        detail: this.filters,
        bubbles: true,
        composed: true,
      })
    );
  }

  setupEventListeners() {
    this.shadow
      .getElementById("statusFilter")
      .addEventListener("change", this.#onStatusChange);

    this.shadow
      .getElementById("sortFilter")
      .addEventListener("change", this.#onSortChange);

    this.shadow
      .getElementById("applyFilters")
      .addEventListener("click", this.#onApplyFilters);
  }

  removeEventListeners() {
    this.shadow
      .getElementById("statusFilter")
      .removeEventListener("change", this.#onStatusChange);
    this.shadow
      .getElementById("sortFilter")
      .removeEventListener("change", this.#onSortChange);
    this.shadow
      .getElementById("applyFilters")
      .removeEventListener("click", this.#onApplyFilters);
  }

  render() {
    const lang = appState.lang;

    const statusFilterLabel = this.shadow.querySelector(
      'label[for="statusFilter"]'
    );
    const allOption = this.shadow.querySelector(
      '#statusFilter option[value=""]'
    );
    const notStartedOption = this.shadow.querySelector(
      '#statusFilter option[value="NOT_STARTED"]'
    );
    const inProgressOption = this.shadow.querySelector(
      '#statusFilter option[value="IN_PROGRESS"]'
    );
    const finishedOption = this.shadow.querySelector(
      '#statusFilter option[value="FINISHED"]'
    );
    const sortFilterLabel = this.shadow.querySelector(
      'label[for="sortFilter"]'
    );
    const sortOption1 = this.shadow.querySelector(
      '#sortFilter option[value="startsAt,desc"]'
    );
    const sortOption2 = this.shadow.querySelector(
      '#sortFilter option[value="startsAt,asc"]'
    );
    const sortOption3 = this.shadow.querySelector(
      '#sortFilter option[value="question,asc"]'
    );
    const sortOption4 = this.shadow.querySelector(
      '#sortFilter option[value="question,desc"]'
    );
    const applyFiltersButton = this.shadow.querySelector("#applyFilters");

    statusFilterLabel.textContent = appState.t("statusFilterLabel");
    allOption.textContent = lang === "en" ? "All" : "Todos";
    notStartedOption.textContent = appState.t("statusNotStarted");
    inProgressOption.textContent = appState.t("statusInProgress");
    finishedOption.textContent = appState.t("statusFinished");
    sortFilterLabel.textContent = appState.t("sortFilterLabel");
    sortOption1.textContent =
      lang === "en" ? "Start Date (Newest)" : "Data de Início (Mais Recente)";
    sortOption2.textContent =
      lang === "en" ? "Start Date (Oldest)" : "Data de Início (Mais Antiga)";
    sortOption3.textContent =
      lang === "en" ? "Question (A-Z)" : "Pergunta (A-Z)";
    sortOption4.textContent =
      lang === "en" ? "Question (Z-A)" : "Pergunta (Z-A)";
    applyFiltersButton.textContent = appState.t("applyFiltersText");
    this.removeEventListeners();
    this.setupEventListeners();
  }
}

customElements.define("poll-filters", PollFilters);
