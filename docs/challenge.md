# 🗳️ Technical Challenge – Realtime Poll System

> [!IMPORTANT]  
> This challenge was taken from a public post on LinkedIn and is here for reference only.  
> I did not participate in any selection process related to it.  
> Source: [LinkedIn](https://www.linkedin.com/feed/update/urn:li:activity:7387095489164492800/)

## 🎯 Objective

In this challenge, you must build a realtime poll system,
allowing users to create polls with multiple-choice questions.

## 📋 Requirements

- It must be possible to **create a poll**
- It must be possible to **edit a poll**
- It must be possible to **delete a poll**
- It must be possible to **list all polls**
- It must be possible to **list** polls by **status**
- It must be possible to **add unlimited options** to the poll
- The **number of votes** must be **updated in realtime** without refreshing the page
- There must be **tests for all controllers**

## ⚙️ Business Rules

- The poll must have a **question**
- The poll must have a **start date**
- The poll must have an **end date**
- The poll can have the statuses **not started/started/in progress/finished**
- The poll must have **at least 3 options**
- The poll **cannot be edited after it starts**

## 🗄️ Database Model

### polls

| Field      | Type      |
| ---------- | --------- |
| id         | UUID      |
| question   | VARCHAR   |
| status     | VARCHAR   |
| start_date | TIMESTAMP |
| end_date   | TIMESTAMP |

### options

| Field   | Type    |
| ------- | ------- |
| id      | UUID    |
| poll_id | UUID    |
| text    | VARCHAR |
| votes   | NUMBER  |

## 🧰 Required Stack

- Java
- Spring Boot
- PostgreSQL
- Docker
- WebSocket
- Bean Validation
- OpenAPI/Swagger

