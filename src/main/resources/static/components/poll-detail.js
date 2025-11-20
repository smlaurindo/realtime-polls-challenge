import { appState, formatDate } from "./utils.js";

/**
 * @typedef {Object} Poll
 * @property {string} id
 * @property {string} question
 * @property {"NOT_STARTED" | "IN_PROGRESS" | "FINISHED"} status
 * @property {Date} startsAt
 * @property {Date} endsAt
 * @property {Array<{ id: string, text: string, votes: number }>} options
 */

class PollDetail extends HTMLElement {
  shadow;
  /** @type {Poll | null} */
  #poll = null;
  #websocket = null;
  #boundHandlers = {};

  constructor() {
    super();

    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("poll-detail-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);

    this.#boundHandlers = {
      back: () => this.handleBack(),
      edit: () => this.handleEdit(),
      delete: () => this.handleDelete(),
      addOption: () => this.handleAddOption(),
      optionClick: (e) => this.handleOptionClick(e),
    };
  }

  connectedCallback() {
    this.setupStaticEventListeners();
    this.render();
    appState.subscribe("langChanged", () => this.render());
  }

  disconnectedCallback() {
    if (this.#websocket) {
      this.#websocket.close();
      this.#websocket = null;
    }
    this.removeStaticEventListeners();
  }

  /** @param {Poll} poll */
  set poll(poll) {
    this.#poll = poll;
    this.render();
    this.connectWebSocket();
  }

  /** @returns {Poll | null} */
  get poll() {
    return this.#poll;
  }

  connectWebSocket() {
    if (this.#websocket) {
      this.#websocket.close();
    }

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = `${protocol}//${window.location.host}/ws/polls/${
      this.#poll.id
    }`;
    this.#websocket = new WebSocket(wsUrl);

    this.#websocket.onmessage = (event) => {
      const { payload: optionUpdated } = JSON.parse(event.data);
      this.#poll = {
        ...this.#poll,
        options: this.#poll.options.map((option) =>
          option.id === optionUpdated.id ? optionUpdated : option
        ),
      };
      this.renderOptions();
    };

    this.#websocket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };
  }

  disconnectWebSocket() {
    if (this.#websocket) {
      this.#websocket.close();
      this.#websocket = null;
    }
  }

  setupStaticEventListeners() {
    this.shadow
      .getElementById("backBtn")
      ?.addEventListener("click", this.#boundHandlers.back);
    this.shadow
      .getElementById("editBtn")
      ?.addEventListener("click", this.#boundHandlers.edit);
    this.shadow
      .getElementById("deleteBtn")
      ?.addEventListener("click", this.#boundHandlers.delete);
    this.shadow
      .getElementById("addOptionBtn")
      ?.addEventListener("click", this.#boundHandlers.addOption);

    this.shadow
      .getElementById("optionsList")
      ?.addEventListener("click", this.#boundHandlers.optionClick);
  }

  removeStaticEventListeners() {
    this.shadow
      .getElementById("backBtn")
      ?.removeEventListener("click", this.#boundHandlers.back);
    this.shadow
      .getElementById("editBtn")
      ?.removeEventListener("click", this.#boundHandlers.edit);
    this.shadow
      .getElementById("deleteBtn")
      ?.removeEventListener("click", this.#boundHandlers.delete);
    this.shadow
      .getElementById("addOptionBtn")
      ?.removeEventListener("click", this.#boundHandlers.addOption);
    this.shadow
      .getElementById("optionsList")
      ?.removeEventListener("click", this.#boundHandlers.optionClick);
  }

  handleBack() {
    this.dispatchEvent(
      new CustomEvent("back-to-list", {
        bubbles: true,
        composed: true,
      })
    );
    this.disconnectWebSocket();
    this.#poll = null;
  }

  handleEdit() {
    this.dispatchEvent(
      new CustomEvent("edit-poll", {
        detail: { pollId: this.#poll.id },
        bubbles: true,
        composed: true,
      })
    );
  }

  handleDelete() {
    if (confirm(appState.t("confirmDelete"))) {
      this.dispatchEvent(
        new CustomEvent("delete-poll", {
          detail: { pollId: this.#poll.id },
          bubbles: true,
          composed: true,
        })
      );
    }
  }

  handleAddOption() {
    this.dispatchEvent(
      new CustomEvent("add-option", {
        detail: { pollId: this.#poll.id },
        bubbles: true,
        composed: true,
      })
    );
  }

  handleOptionClick(e) {
    if (e.target.classList.contains("vote-btn")) {
      const optionId = e.target.dataset.optionId;
      this.dispatchEvent(
        new CustomEvent("vote", {
          detail: { pollId: this.#poll.id, optionId },
          bubbles: true,
          composed: true,
        })
      );
    } else if (e.target.classList.contains("remove-btn")) {
      if (confirm(appState.t("confirmDeleteOption"))) {
        const optionId = e.target.dataset.optionId;
        this.dispatchEvent(
          new CustomEvent("delete-option", {
            detail: { pollId: this.#poll.id, optionId },
            bubbles: true,
            composed: true,
          })
        );
      }
    }
  }

  getStatusClass(status) {
    return "status-" + status.toLowerCase().replace("_", "-");
  }

  getStatusLabel(status) {
    const statusKey =
      "status" +
      status
        .split("_")
        .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
        .join("");
    return appState.t(statusKey);
  }

  renderOptions() {
    const optionsListUl = this.shadow.getElementById("optionsList");

    if (!optionsListUl) return;

    const totalVotes = this.#poll.options.reduce(
      (sum, opt) => sum + opt.votes,
      0
    );
    const canEdit = this.#poll.status === "NOT_STARTED";
    const canVote = this.#poll.status === "IN_PROGRESS";
    const canDelete = this.#poll.options.length > 3;

    optionsListUl.innerHTML = "";

    this.#poll.options.forEach((option) => {
      const percentage = totalVotes > 0 ? (option.votes / totalVotes) * 100 : 0;

      const optionsListTemplate = document
        .getElementById("poll-detail-options-template")
        .content.cloneNode(true);

      const optionTextSpan = optionsListTemplate.querySelector(".option-text");
      const optionVotesSpan =
        optionsListTemplate.querySelector(".option-votes");
      const voteBarFillDiv =
        optionsListTemplate.querySelector(".vote-bar-fill");
      const voteBtn = optionsListTemplate.querySelector(".vote-btn");
      const removeBtn = optionsListTemplate.querySelector(".remove-btn");

      optionTextSpan.textContent = option.text;
      optionVotesSpan.textContent = `${option.votes} ${appState.t("votes")}`;
      voteBarFillDiv.style.width = `${percentage}%`;
      voteBtn.style.display = canVote ? "inline-block" : "none";
      voteBtn.dataset.optionId = option.id;
      voteBtn.textContent = appState.t("vote");
      removeBtn.style.display = canEdit && canDelete ? "inline-block" : "none";
      removeBtn.dataset.optionId = option.id;
      removeBtn.textContent = appState.t("remove");

      optionsListUl.appendChild(optionsListTemplate);
    });
  }

  render() {
    if (!this.#poll) return;

    const totalVotes = this.#poll.options.reduce(
      (sum, opt) => sum + opt.votes,
      0
    );

    const canEdit = this.#poll.status === "NOT_STARTED";

    const backButton = this.shadow.getElementById("backBtn");
    const pollStatusSpan = this.shadow.querySelector(".poll-status");
    const pollQuestionH2 = this.shadow.querySelector(".poll-question");
    const pollDateDiv = this.shadow.querySelector(".poll-date");
    const pollVotesDiv = this.shadow.querySelector(".poll-votes");
    const pollEditButton = this.shadow.getElementById("editBtn");
    const pollAddOptionButton = this.shadow.getElementById("addOptionBtn");
    const pollDeleteButton = this.shadow.getElementById("deleteBtn");

    backButton.textContent = appState.t("backToPolls");
    pollStatusSpan.classList.remove(
      "status-not-started",
      "status-in-progress",
      "status-finished"
    );
    pollStatusSpan.classList.add(this.getStatusClass(this.#poll.status));
    pollStatusSpan.textContent = this.getStatusLabel(this.#poll.status);
    pollQuestionH2.textContent = this.#poll.question;
    pollDateDiv.textContent = `${formatDate(
      this.#poll.startsAt
    )} - ${formatDate(this.#poll.endsAt)}`;
    pollVotesDiv.textContent = `${appState.t("totalVotes")}: ${totalVotes}`;
    pollEditButton.style.display = canEdit ? "inline-block" : "none";
    pollEditButton.textContent = appState.t("edit");
    pollAddOptionButton.style.display = canEdit ? "inline-block" : "none";
    pollAddOptionButton.textContent = appState.t("addOption");
    pollDeleteButton.textContent = appState.t("delete");

    this.renderOptions();
  }
}

customElements.define("poll-detail", PollDetail);
