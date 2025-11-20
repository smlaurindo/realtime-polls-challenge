export const translations = {
  en: {
    appTitle: "Realtime Polls",
    createPollText: "+ Create Poll",
    statusFilterLabel: "Status",
    sortFilterLabel: "Sort By",
    applyFiltersText: "Apply",
    loadingText: "Loading polls...",
    prevText: "← Previous",
    nextText: "Next →",
    pollModalTitle: "Create New Poll",
    editPollModalTitle: "Edit Poll",
    questionLabel: "Question",
    startsAtLabel: "Starts At",
    endsAtLabel: "Ends At",
    optionsLabel: "Options (minimum 3)",
    addOptionText: "+ Add Option",
    submitPollText: "Create Poll",
    updatePollText: "Update Poll",
    cancelText: "Cancel",
    addOptionModalTitle: "Add Option",
    newOptionLabel: "Option Text",
    submitOptionText: "Add",
    cancelOptionText: "Cancel",
    emptyStateTitle: "No polls found",
    emptyStateMessage: "Create your first poll to get started!",
    pollCreatedSuccess: "Poll created successfully!",
    pollUpdatedSuccess: "Poll updated successfully!",
    pollDeletedSuccess: "Poll deleted successfully!",
    optionAddedSuccess: "Option added successfully!",
    optionDeletedSuccess: "Option removed successfully!",
    voteSuccess: "Vote registered successfully!",
    backToPolls: "← Back to Polls",
    edit: "Edit",
    delete: "Delete",
    addOption: "+ Add Option",
    vote: "Vote",
    remove: "Remove",
    votes: "votes",
    totalVotes: "Total Votes",
    confirmDelete: "Are you sure you want to delete this poll?",
    confirmDeleteOption: "Are you sure you want to remove this option?",
    statusNotStarted: "Not Started",
    statusInProgress: "In Progress",
    statusFinished: "Finished",
    options: "options",
  },
  pt: {
    appTitle: "Enquetes em Tempo Real",
    createPollText: "+ Criar Enquete",
    statusFilterLabel: "Status",
    sortFilterLabel: "Ordenar Por",
    applyFiltersText: "Aplicar",
    loadingText: "Carregando enquetes...",
    prevText: "← Anterior",
    nextText: "Próximo →",
    pollModalTitle: "Criar Nova Enquete",
    editPollModalTitle: "Editar Enquete",
    questionLabel: "Pergunta",
    startsAtLabel: "Início",
    endsAtLabel: "Término",
    optionsLabel: "Opções (mínimo 3)",
    addOptionText: "+ Adicionar Opção",
    submitPollText: "Criar Enquete",
    updatePollText: "Atualizar Enquete",
    cancelText: "Cancelar",
    addOptionModalTitle: "Adicionar Opção",
    newOptionLabel: "Texto da Opção",
    submitOptionText: "Adicionar",
    cancelOptionText: "Cancelar",
    emptyStateTitle: "Nenhuma enquete encontrada",
    emptyStateMessage: "Crie sua primeira enquete para começar!",
    pollCreatedSuccess: "Enquete criada com sucesso!",
    pollUpdatedSuccess: "Enquete atualizada com sucesso!",
    pollDeletedSuccess: "Enquete excluída com sucesso!",
    optionAddedSuccess: "Opção adicionada com sucesso!",
    optionDeletedSuccess: "Opção removida com sucesso!",
    voteSuccess: "Voto registrado com sucesso!",
    backToPolls: "← Voltar para Enquetes",
    edit: "Editar",
    delete: "Excluir",
    addOption: "+ Adicionar Opção",
    vote: "Votar",
    remove: "Remover",
    votes: "votos",
    totalVotes: "Total de Votos",
    confirmDelete: "Tem certeza que deseja excluir esta enquete?",
    confirmDeleteOption: "Tem certeza que deseja remover esta opção?",
    statusNotStarted: "Não Iniciada",
    statusInProgress: "Em Andamento",
    statusFinished: "Finalizada",
    options: "opções",
  },
};

export function formatDate(dateString) {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export function formatDateTimeLocal(dateString) {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

export class AppState {
  constructor() {
    this.lang = localStorage.getItem("lang") || "en";
    this.theme = localStorage.getItem("theme") || "dark";
    this.listeners = new Map();
  }

  subscribe(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }

    this.listeners.get(event).push(callback);
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach((callback) => callback(data));
    }
  }

  setLang(lang) {
    this.lang = lang;
    localStorage.setItem("lang", lang);
    this.emit("langChanged", lang);
  }

  setTheme(theme) {
    this.theme = theme;
    localStorage.setItem("theme", theme);
    document.documentElement.setAttribute("data-theme", theme);
    this.emit("themeChanged", theme);
  }

  t(key) {
    return translations[this.lang][key] || key;
  }
}

export const appState = new AppState();
