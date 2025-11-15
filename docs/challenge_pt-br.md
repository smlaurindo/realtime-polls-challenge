# 🗳️ Desafio Técnico – Sistema de Enquetes em Realtime

> [!IMPORTANT]  
> Este desafio foi retirado de um post público no LinkedIn e está aqui apenas como referência.  
> Não participei de nenhum processo seletivo relacionado a ele.  
> Fonte: [LinkedIn](https://www.linkedin.com/feed/update/urn:li:activity:7387095489164492800/)

## 🎯 Objetivo

Nesse desafio, você deve construir um sistema de enquetes em realtime,
permitindo que os usuários criem enquetes com perguntas de múltipla escolha.

## 📋 Requisitos

- Deve ser possível **criar uma enquete**
- Deve ser possível **editar uma enquete**
- Deve ser possível **excluir uma enquete**
- Deve ser possível **listar todas as enquetes**
- Deve ser possível **listar** enquetes por **status**
- Deve ser possível **adicionar opções ilimitadas** na enquete
- Deve ser **atualizado** o **número de votos** sem precisar atualizar a página **(realtime)**
- Deve conter **teste de todos os controllers**

## ⚙️ Regras de Negócio

- A enquete deve ter uma **pergunta**
- A enquete deve ter uma **data de início**
- A enquete deve ter uma **data de término**
- A enquete pode ter os status **não iniciado/iniciado/em andamento/finalizado**
- A enquete deve ter no **mínimo 3 opções**
- A enquete **não pode ser editada depois de iniciar**.

## 🗄️ Modelo de Banco de Dados

### polls

| Campo      | Tipo      |
| ---------- | --------- |
| id         | UUID      |
| question   | VARCHAR   |
| status     | VARCHAR   |
| start_date | TIMESTAMP |
| end_date   | TIMESTAMP |

### options

| Campo   | Tipo    |
| ------- | ------- |
| id      | UUID    |
| poll_id | UUID    |
| text    | VARCHAR |
| votes   | NUMBER  |

## 🧰 Stack Obrigatória

- Java
- Spring Boot
- PostgreSQL
- Docker
- WebSocket
- Bean Validation
- OpenAPI/Swagger
