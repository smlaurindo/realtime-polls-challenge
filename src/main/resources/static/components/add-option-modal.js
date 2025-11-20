import { appState } from "./utils.js";

class AddOptionModal extends HTMLElement {
  shadow;

  constructor() {
    super();
    this.shadow = this.attachShadow({ mode: "open" });
    this.pollId = null;

    const template = document
      .getElementById("add-option-modal-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);
  }

  connectedCallback() {
    this.setupEventListeners();
    this.render();

    appState.subscribe("langChanged", () => this.render());
  }

  open(pollId) {
    this.pollId = pollId;
    this.shadow.querySelector(".modal").classList.add("active");
    this.shadow.querySelector("#newOptionText").value = "";
    this.shadow.querySelector("#newOptionText").focus();
  }

  close() {
    this.shadow.querySelector(".modal").classList.remove("active");
    this.pollId = null;
  }

  setupEventListeners() {
    this.shadow.getElementById("closeModal")?.addEventListener("click", () => {
      this.close();
    });

    this.shadow.getElementById("cancelBtn")?.addEventListener("click", () => {
      this.close();
    });

    this.shadow
      .getElementById("addOptionForm")
      ?.addEventListener("submit", (e) => {
        e.preventDefault();
        this.handleSubmit(e);
      });

    // Close modal when clicking outside
    this.shadow.querySelector(".modal")?.addEventListener("click", (e) => {
      if (e.target.classList.contains("modal")) {
        this.close();
      }
    });
  }

  handleSubmit(e) {
    const form = e.target;
    const optionText = form.querySelector("#newOptionText").value.trim();

    if (!optionText) {
      return;
    }

    this.dispatchEvent(
      new CustomEvent("option-submit", {
        detail: {
          pollId: this.pollId,
          optionText,
        },
        bubbles: true,
        composed: true,
      })
    );

    this.close();
  }

  render() {
    const modalTitleH2 = this.shadow.querySelector(".modal-title");
    const newOptionLabel = this.shadow.querySelector(
      "label[for='newOptionText']"
    );
    const submitButton = this.shadow.querySelector(".btn-primary");
    const cancelButton = this.shadow.querySelector("#cancelBtn");
    modalTitleH2.textContent = appState.t("addOptionModalTitle");
    newOptionLabel.textContent = appState.t("newOptionLabel");
    submitButton.textContent = appState.t("submitOptionText");
    cancelButton.textContent = appState.t("cancelOptionText");
  }
}

customElements.define("add-option-modal", AddOptionModal);
