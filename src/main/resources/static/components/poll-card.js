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

class PollCard extends HTMLElement {
  shadow;
  /** @type {Poll | undefined} */
  #poll;

  constructor() {
    super();

    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("poll-card-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);
  }

  /** @param {Poll} data */
  set poll(data) {
    this.#poll = data;
  }

  /** @returns {Poll | undefined} */
  get poll() {
    return this.#poll;
  }

  connectedCallback() {
    this.render();
    appState.subscribe("langChanged", () => this.render());
  }

  disconnectedCallback() {
    this.shadow
      .querySelector(".poll-card")
      .removeEventListener("click", this.handleClick);
  }
  
  handleClick = () => {
    this.dispatchEvent(
      new CustomEvent("poll-selected", {
        detail: { pollId: this.#poll.id },
        bubbles: true,
        composed: true,
      })
    );
  }

  /** @param {string} status */
  getStatusClass(status) {
    return status.toLowerCase().replace("_", "-");
  }

  /** @param {string} status */
  getStatusLabel(status) {
    const statusKey =
      "status" +
      status
        .split("_")
        .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
        .join("");
    return appState.t(statusKey);
  }

  render() {
    if (!this.#poll) return;

    const poll = this.#poll;

    const pollStatus = this.shadow.querySelector(".poll-status");
    const pollQuestion = this.shadow.querySelector(".poll-question");
    const pollMeta = this.shadow.querySelector(".poll-meta");
    const pollOptionsPreview = this.shadow.querySelector(
      ".poll-options-preview"
    );

    pollStatus.textContent = this.getStatusLabel(poll.status);
    pollStatus.classList.add(`status-${this.getStatusClass(poll.status)}`);

    pollQuestion.textContent = poll.question;

    pollMeta.textContent = `${formatDate(poll.startsAt)} - ${formatDate(
      poll.endsAt
    )}`;
    pollOptionsPreview.textContent = `${poll.options.length} ${appState.t(
      "options"
    )}`;

    this.shadow
      .querySelector(".poll-card")
      .addEventListener("click", this.handleClick);
  }
}

customElements.define("poll-card", PollCard);
