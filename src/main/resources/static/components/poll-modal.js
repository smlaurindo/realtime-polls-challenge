import { appState, formatDateTimeLocal } from "./utils.js";

/**
 * @typedef {Object} Poll
 * @property {string} id
 * @property {string} question
 * @property {"NOT_STARTED" | "IN_PROGRESS" | "FINISHED"} status
 * @property {Date} startsAt
 * @property {Date} endsAt
 * @property {Array<{ id: string, text: string, votes: number }>} options
 */

class PollModal extends HTMLElement {
  shadow;
  /** @type {Poll | null} */
  #editingPoll;
  /** @type {"create" | "edit"} */
  #mode;

  constructor() {
    super();
    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("poll-modal-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);

    this.#editingPoll = null;
    this.#mode = "create"; // 'create' or 'edit'
  }

  connectedCallback() {
    this.setupEventListeners();
    this.render();
    appState.subscribe("langChanged", () => this.updateLabels());
  }

  open(mode = "create", poll = null) {
    this.#mode = mode;
    this.#editingPoll = poll;
    this.render();

    if (mode === "edit" && poll) {
      this.fillForm(poll);
    } else {
      this.resetForm();
    }

    this.shadow.querySelector(".modal").classList.add("active");
  }

  close() {
    this.shadow.querySelector(".modal").classList.remove("active");
    this.resetForm();
  }

  fillForm(poll) {
    const form = this.shadow.getElementById("pollForm");
    form.querySelector("#pollQuestion").value = poll.question;
    form.querySelector("#pollStartsAt").value = formatDateTimeLocal(
      poll.startsAt
    );
    form.querySelector("#pollEndsAt").value = formatDateTimeLocal(poll.endsAt);

    const dynamicOptions = this.shadow.querySelector("#dynamicOptions");
    dynamicOptions.innerHTML = "";

    this.shadow.querySelector("#optionsGroup").style.display = "none";
  }

  resetForm() {
    const form = this.shadow.querySelector("#pollForm");
    if (form) {
      form.reset();
    }

    const dynamicOptions = this.shadow.querySelector("#dynamicOptions");
    if (dynamicOptions) {
      dynamicOptions.innerHTML = "";
      for (let i = 0; i < 3; i++) {
        this.addOptionInput();
      }
    }

    const optionsGroup = this.shadow.querySelector("#optionsGroup");
    if (optionsGroup) {
      optionsGroup.style.display = "block";
    }
  }

  addOptionInput() {
    const dynamicOptions = this.shadow.querySelector("#dynamicOptions");
    const optionIndex = dynamicOptions.children.length;

    const optionGroup = document.createElement("div");
    optionGroup.className = "option-input-group";
    optionGroup.innerHTML = `
      <input type="text" placeholder="${appState.t("optionsLabel")}" required />
      ${
        optionIndex >= 3
          ? `
        <button type="button" class="btn btn-danger btn-small remove-option-btn">x</button>
      `
          : ""
      }
    `;

    dynamicOptions.appendChild(optionGroup);

    const removeButton = optionGroup.querySelector(".remove-option-btn");
    if (removeButton) {
      removeButton.addEventListener("click", () => {
        optionGroup.remove();
      });
    }
  }

  setupEventListeners() {
    this.shadow.getElementById("closeModal")?.addEventListener("click", () => {
      this.close();
    });

    this.shadow.getElementById("cancelBtn")?.addEventListener("click", () => {
      this.close();
    });

    this.shadow
      .getElementById("addOptionBtn")
      ?.addEventListener("click", () => {
        this.addOptionInput();
      });

    this.shadow.getElementById("pollForm")?.addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleSubmit(e);
    });

    this.shadow.querySelector(".modal")?.addEventListener("click", (e) => {
      if (e.target.classList.contains("modal")) {
        this.close();
      }
    });
  }

  handleSubmit(e) {
    const form = e.target;
    const question = form.querySelector("#pollQuestion").value;
    const startsAt = form.querySelector("#pollStartsAt").value;
    const endsAt = form.querySelector("#pollEndsAt").value;

    const pollData = {
      question,
      startsAt,
      endsAt,
    };

    if (this.#mode === "create") {
      const optionInputs = this.shadow.querySelectorAll(
        ".option-input-group input"
      );
      const options = Array.from(optionInputs)
        .map((input) => input.value.trim())
        .filter((value) => value !== "");

      if (options.length < 3) {
        alert("Please add at least 3 options");
        return;
      }

      pollData.options = options;
    }

    this.dispatchEvent(
      new CustomEvent("poll-submit", {
        detail: {
          mode: this.#mode,
          data: pollData,
          pollId: this.#editingPoll?.id,
        },
        bubbles: true,
        composed: true,
      })
    );

    this.close();
  }

  updateLabels() {
    const modalTitleH2 = this.shadow.querySelector(".modal-title");
    const submitButton = this.shadow.querySelector("#submitBtn");

    if (modalTitleH2) {
      modalTitleH2.textContent = appState.t(
        this.#mode === "create" ? "pollModalTitle" : "editPollModalTitle"
      );
    }

    if (submitButton) {
      submitButton.textContent = appState.t(
        this.#mode === "create" ? "submitPollText" : "updatePollText"
      );
    }
  }

  render() {
    const modalTitleH2 = this.shadow.querySelector(".modal-title");
    const pollQuestionLabel = this.shadow.querySelector(
      "label[for='pollQuestion']"
    );
    const pollStartsAtLabel = this.shadow.querySelector(
      "label[for='pollStartsAt']"
    );
    const pollEndsAtLabel = this.shadow.querySelector(
      "label[for='pollEndsAt']"
    );
    const optionsLabel = this.shadow.querySelector("#optionsGroup label");
    const addOptionButton = this.shadow.querySelector("#addOptionBtn");
    const submitButton = this.shadow.querySelector("#submitBtn");
    const cancelButton = this.shadow.querySelector("#cancelBtn");
    modalTitleH2.textContent = appState.t(
      this.#mode === "create" ? "pollModalTitle" : "editPollModalTitle"
    );
    pollQuestionLabel.textContent = appState.t("questionLabel");
    pollStartsAtLabel.textContent = appState.t("startsAtLabel");
    pollEndsAtLabel.textContent = appState.t("endsAtLabel");
    optionsLabel.textContent = appState.t("optionsLabel");
    addOptionButton.textContent = appState.t("addOptionText");
    submitButton.textContent = appState.t(
      this.#mode === "create" ? "submitPollText" : "updatePollText"
    );
    cancelButton.textContent = appState.t("cancelText");

    if (this.#mode === "create") {
      const dynamicOptions = this.shadow.getElementById("dynamicOptions");
      dynamicOptions.innerHTML = "";
      for (let i = 0; i < 3; i++) {
        this.addOptionInput();
      }
    }
  }
}

customElements.define("poll-modal", PollModal);
