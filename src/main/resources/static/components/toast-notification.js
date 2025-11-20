class ToastNotification extends HTMLElement {
  shadow;

  constructor() {
    super();
    this.shadow = this.attachShadow({ mode: "open" });

    const template = document
      .getElementById("toast-notification-template")
      .content.cloneNode(true);

    this.shadow.appendChild(template);
  }

  connectedCallback() {}

  show(message, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.textContent = message;

    const container = this.shadow.querySelector(".toast-container");
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.animation = "slideOut 0.3s ease";
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  }
}

customElements.define("toast-notification", ToastNotification);
