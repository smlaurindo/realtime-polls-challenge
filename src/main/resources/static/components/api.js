export class PollAPI {
  static async fetchPolls(page = 0, filters = {}) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: "9",
      sort: filters.sort || "startsAt,desc",
    });

    if (filters.status) {
      params.append("status", filters.status);
    }

    const response = await fetch(`/polls?${params}`);
    if (!response.ok) throw new Error("Failed to fetch polls");
    return response.json();
  }

  static async fetchPollDetail(pollId) {
    const response = await fetch(`/polls/${pollId}`);
    if (!response.ok) throw new Error("Failed to fetch poll details");
    return response.json();
  }

  static async createPoll(pollData) {
    const response = await fetch("/polls", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(pollData),
    });

    if (!response.ok) throw new Error("Failed to create poll");
    return response.json();
  }

  static async updatePoll(pollId, pollData) {
    const response = await fetch(`/polls/${pollId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(pollData),
    });

    if (!response.ok) throw new Error("Failed to update poll");
    return response.json();
  }

  static async deletePoll(pollId) {
    const response = await fetch(`/polls/${pollId}`, {
      method: "DELETE",
    });

    if (!response.ok) throw new Error("Failed to delete poll");
  }

  static async addOption(pollId, optionText) {
    const response = await fetch(`/polls/${pollId}/options`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text: optionText }),
    });

    if (!response.ok) throw new Error("Failed to add option");
    return response.json();
  }

  static async deleteOption(pollId, optionId) {
    const response = await fetch(`/polls/${pollId}/options/${optionId}`, {
      method: "DELETE",
    });

    if (!response.ok) throw new Error("Failed to delete option");
  }

  static async vote(pollId, optionId) {
    const response = await fetch(
      `/polls/${pollId}/options/${optionId}/vote`,
      {
        method: "PATCH",
      }
    );

    if (!response.ok) throw new Error("Failed to vote");
    return;
  }

  static connectWebSocket(pollId, onMessage) {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = `${protocol}//${window.location.host}/ws/polls/${pollId}`;
    const websocket = new WebSocket(wsUrl);

    websocket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      onMessage(data);
    };

    websocket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    return websocket;
  }
}
