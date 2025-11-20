import { appState } from "./utils.js";
import { PollAPI } from "./api.js";
import "./poll-header.js";
import "./poll-filters.js";
import "./polls-list.js";
import "./poll-detail.js";
import "./poll-modal.js";
import "./add-option-modal.js";
import "./toast-notification.js";
import "./app-footer.js";

class PollApp extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.currentPage = 0;
    this.currentFilters = { status: "", sort: "startsAt,desc" };
    this.currentView = "list"; // 'list' or 'detail'
  }

  connectedCallback() {
    this.render();
    this.setupEventListeners();
    this.loadPolls();

    appState.setTheme(appState.theme);
    appState.setLang(appState.lang);
  }

  setupEventListeners() {
    // Header events
    this.addEventListener("create-poll", () => {
      this.shadowRoot.querySelector("poll-modal").open("create");
    });

    // Filter events
    this.addEventListener("filters-changed", (e) => {
      this.currentFilters = e.detail;
      this.currentPage = 0;
      this.loadPolls();
    });

    // Pagination events
    this.addEventListener("page-changed", (e) => {
      this.currentPage = e.detail.page;
      this.loadPolls();
    });

    // Poll card events
    this.addEventListener("poll-selected", (e) => {
      this.loadPollDetail(e.detail.pollId);
    });

    // Poll detail events
    this.addEventListener("back-to-list", () => {
      this.showListView();
    });

    this.addEventListener("edit-poll", (e) => {
      this.editPoll(e.detail.pollId);
    });

    this.addEventListener("delete-poll", async (e) => {
      await this.deletePoll(e.detail.pollId);
    });

    this.addEventListener("add-option", (e) => {
      this.shadowRoot.querySelector("add-option-modal").open(e.detail.pollId);
    });

    this.addEventListener("vote", async (e) => {
      await this.vote(e.detail.pollId, e.detail.optionId);
    });

    this.addEventListener("delete-option", async (e) => {
      await this.deleteOption(e.detail.pollId, e.detail.optionId);
    });

    // Modal events
    this.addEventListener("poll-submit", async (e) => {
      const data = {
        ...e.detail.data,
        startsAt: e.detail.data.startsAt
          ? new Date(e.detail.data.startsAt).toISOString()
          : undefined,
        endsAt: e.detail.data.endsAt
          ? new Date(e.detail.data.endsAt).toISOString()
          : undefined,
      };

      if (e.detail.mode === "create") {
        await this.createPoll(data);
      } else {
        await this.updatePoll(e.detail.pollId, data);
      }
    });

    this.addEventListener("option-submit", async (e) => {
      await this.addOption(e.detail.pollId, e.detail.optionText);
    });
  }

  async loadPolls() {
    try {
      const pollsList = this.shadowRoot.querySelector("polls-list");
      pollsList.showLoading();

      const data = await PollAPI.fetchPolls(
        this.currentPage,
        this.currentFilters
      );
      pollsList.setData(data, this.currentPage);
    } catch (error) {
      console.error("Error loading polls:", error);
      this.showToast("Error loading polls", "error");
    }
  }

  async loadPollDetail(pollId) {
    try {
      const poll = await PollAPI.fetchPollDetail(pollId);
      this.showDetailView(poll);
    } catch (error) {
      console.error("Error loading poll detail:", error);
      this.showToast("Error loading poll details", "error");
    }
  }

  async createPoll(pollData) {
    try {
      await PollAPI.createPoll(pollData);
      this.showToast(appState.t("pollCreatedSuccess"), "success");
      this.currentPage = 0;
      await this.loadPolls();
    } catch (error) {
      console.error("Error creating poll:", error);
      this.showToast("Error creating poll", "error");
    }
  }

  async updatePoll(pollId, pollData) {
    try {
      await PollAPI.updatePoll(pollId, pollData);
      this.showToast(appState.t("pollUpdatedSuccess"), "success");
      await this.loadPollDetail(pollId);
    } catch (error) {
      console.error("Error updating poll:", error);
      this.showToast("Error updating poll", "error");
    }
  }

  async deletePoll(pollId) {
    try {
      await PollAPI.deletePoll(pollId);
      this.showToast(appState.t("pollDeletedSuccess"), "success");
      this.showListView();
      await this.loadPolls();
    } catch (error) {
      console.error("Error deleting poll:", error);
      this.showToast("Error deleting poll", "error");
    }
  }

  async addOption(pollId, optionText) {
    try {
      await PollAPI.addOption(pollId, optionText);
      this.showToast(appState.t("optionAddedSuccess"), "success");
      await this.loadPollDetail(pollId);
    } catch (error) {
      console.error("Error adding option:", error);
      this.showToast("Error adding option", "error");
    }
  }

  async deleteOption(pollId, optionId) {
    try {
      await PollAPI.deleteOption(pollId, optionId);
      this.showToast(appState.t("optionDeletedSuccess"), "success");
      await this.loadPollDetail(pollId);
    } catch (error) {
      console.error("Error deleting option:", error);
      this.showToast("Error deleting option", "error");
    }
  }

  async vote(pollId, optionId) {
    try {
      await PollAPI.vote(pollId, optionId);
      this.showToast(appState.t("voteSuccess"), "success");
    } catch (error) {
      console.error("Error voting:", error);
      this.showToast("Error voting", "error");
    }
  }

  async editPoll(pollId) {
    try {
      const poll = await PollAPI.fetchPollDetail(pollId);
      this.shadowRoot.querySelector("poll-modal").open("edit", poll);
    } catch (error) {
      console.error("Error loading poll for edit:", error);
      this.showToast("Error loading poll", "error");
    }
  }

  showListView() {
    this.currentView = "list";
    this.shadowRoot.getElementById("pollsView").style.display = "block";
    this.shadowRoot.getElementById("pollDetailView").style.display = "none";
  }

  showDetailView(poll) {
    this.currentView = "detail";
    this.shadowRoot.getElementById("pollsView").style.display = "none";
    this.shadowRoot.getElementById("pollDetailView").style.display = "block";
    this.shadowRoot.querySelector("poll-detail").poll = poll;
  }

  showToast(message, type = "success") {
    this.shadowRoot.querySelector("toast-notification").show(message, type);
  }

  render() {
    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
        }

        .container {
          max-width: 1200px;
          margin: 0 auto;
          padding: 0 1rem;
        }

        main {
          padding: 2rem 0;
          min-height: calc(100vh - 200px);
        }

        #pollsView {
          display: block;
        }

        #pollDetailView {
          display: none;
        }
      </style>
      <toast-notification></toast-notification>
      <poll-header></poll-header>
      <main>
        <div class="container">
          <div id="pollsView">
            <poll-filters></poll-filters>
            <polls-list></polls-list>
          </div>
          <div id="pollDetailView">
            <poll-detail></poll-detail>
          </div>
        </div>
      </main>
      <app-footer></app-footer>
      <poll-modal></poll-modal>
      <add-option-modal></add-option-modal>
    `;
  }
}

customElements.define("poll-app", PollApp);
