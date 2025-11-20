import { appState } from "./utils.js";
import "./poll-card.js";

/**
 * @typedef {Object} Poll
 * @property {string} id
 * @property {string} question
 * @property {string} status
 * @property {Date} startsAt
 * @property {Date} endsAt
 * @property {Array<{ id: string, text: string, votes: number }>} options
 */

/**
 * @typedef {Object} PollsData
 * @property {Poll[]} content
 * @property {number} page
 * @property {number} totalPages
 * @property {boolean} hasPrevious
 * @property {boolean} hasNext
 */

/**
 * @extends {HTMLElement}
 */
class PollsList extends HTMLElement {
  shadow;
  /** @type {PollsData | null} */
  polls;

  constructor() {
    super();

    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("polls-list-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);

    this.polls = null;
    this.currentPage = 0;
  }

  connectedCallback() {
    appState.subscribe("langChanged", () => this.updatePagination());
  }

  setData(data, page) {
    this.polls = data;
    this.currentPage = page;
    this.render();
  }

  showLoading() {
    const container = this.shadow.getElementById("polls-container");
    const loadingState = document
      .getElementById("loading-polls-template")
      .content.cloneNode(true);
    const loadingStateParagraph = loadingState.querySelector("p");
    loadingStateParagraph.textContent = appState.t("loadingText");
    container.innerHTML = "";
    container.appendChild(loadingState);
  }

  #onPreviousPageClick = () => {
    this.dispatchEvent(
      new CustomEvent("page-changed", {
        detail: { page: this.currentPage - 1 },
        bubbles: true,
        composed: true,
      })
    );
  };

  #onNextPageClick = () => {
    this.dispatchEvent(
      new CustomEvent("page-changed", {
        detail: { page: this.currentPage + 1 },
        bubbles: true,
        composed: true,
      })
    );
  };

  setupEventListeners() {
    this.shadow
      .getElementById("prevPage")
      ?.addEventListener("click", this.#onPreviousPageClick);

    this.shadow
      .getElementById("nextPage")
      ?.addEventListener("click", this.#onNextPageClick);
  }

  removeEventListeners() {
    this.shadow
      .getElementById("prevPage")
      ?.removeEventListener("click", this.#onPreviousPageClick);

    this.shadow
      .getElementById("nextPage")
      ?.removeEventListener("click", this.#onNextPageClick);
  }

  updatePagination() {
    if (!this.polls) return;

    const pageInfo = this.shadow.getElementById("pageInfo");

    if (pageInfo) {
      const lang = appState.lang;
      const pageText = lang === "en" ? "Page" : "Página";
      const ofText = lang === "en" ? "of" : "de";
      pageInfo.textContent = `${pageText} ${this.polls.page + 1} ${ofText} ${
        this.polls.totalPages
      }`;
    }
  }

  render() {
    const previousPageButton = this.shadow.getElementById("prevPage");
    const nextPageButton = this.shadow.getElementById("nextPage");

    previousPageButton.textContent = appState.t("prevText");
    nextPageButton.textContent = appState.t("nextText");

    if (this.polls) {
      this.renderPolls();
    }

    this.removeEventListeners();
    this.setupEventListeners();
  }

  renderPolls() {
    const container = this.shadow.getElementById("polls-container");
    const pagination = this.shadow.getElementById("pagination");

    if (!this.polls.content || this.polls.content.length === 0) {
      const emptyState = document
        .getElementById("empty-polls-template")
        .content.cloneNode(true);

      const emptyStateTitle = emptyState.querySelector(".empty-polls-title");
      const emptyStateDescription = emptyState.querySelector(
        ".empty-polls-description"
      );
      emptyStateTitle.textContent = appState.t("emptyStateTitle");
      emptyStateDescription.textContent = appState.t("emptyStateMessage");

      container.innerHTML = "";
      container.appendChild(emptyState);

      pagination.style.display = "none";

      return;
    }

    const pollsGrid = document.createElement("div");
    pollsGrid.className = "polls-grid";

    this.polls.content.forEach((poll) => {
      const card = document.createElement("poll-card");
      card.poll = poll;
      pollsGrid.appendChild(card);
    });

    container.innerHTML = "";
    container.appendChild(pollsGrid);

    // Update pagination
    pagination.style.display = "flex";
    const prevBtn = this.shadow.getElementById("prevPage");
    const nextBtn = this.shadow.getElementById("nextPage");

    prevBtn.disabled = !this.polls.hasPrevious;
    nextBtn.disabled = !this.polls.hasNext;

    this.updatePagination();
  }
}

customElements.define("polls-list", PollsList);
