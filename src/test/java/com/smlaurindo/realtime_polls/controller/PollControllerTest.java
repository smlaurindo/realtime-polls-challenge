package com.smlaurindo.realtime_polls.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smlaurindo.realtime_polls.domain.Poll;
import com.smlaurindo.realtime_polls.domain.Option;
import com.smlaurindo.realtime_polls.repository.OptionRepository;
import com.smlaurindo.realtime_polls.repository.PollRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("Poll Controller End to End Tests")
class PollControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        optionRepository.deleteAll();
        pollRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /polls - Create Poll Tests")
    class CreatePollTests {

        private final String apiPath = "/polls";
        private OffsetDateTime futureStartsAt;
        private OffsetDateTime futureEndsAt;

        @BeforeEach
        void setUpCreateTests() {
            futureStartsAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            futureEndsAt = futureStartsAt.plusDays(1);
        }

        @Test
        @DisplayName("Should create poll with valid data")
        void shouldCreatePollWithValidData() {
            var question = "What is your favorite programming language?";
            var options = List.of("Java", "Python", "JavaScript");

            Map<String, Object> requestBody = Map.of(
                    "question", question,
                    "startsAt", futureStartsAt.toString(),
                    "endsAt", futureEndsAt.toString(),
                    "options", options
            );

            var expectedStatusCode = HttpStatus.CREATED;
            var expectedQuestionStatus = "NOT_STARTED";
            var expectedNumberOfOptions = 3;
            var expectedInitialVotes = 0;

            webTestClient.post().uri(apiPath)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.question").isEqualTo(question)
                    .jsonPath("$.status").isEqualTo(expectedQuestionStatus)
                    .jsonPath("$.startsAt").isEqualTo(futureStartsAt.toString())
                    .jsonPath("$.endsAt").isEqualTo(futureEndsAt.toString())
                    .jsonPath("$.options").isArray()
                    .jsonPath("$.options.length()").isEqualTo(expectedNumberOfOptions)
                    .jsonPath("$.options[0].id").isNotEmpty()
                    .jsonPath("$.options[0].text").isEqualTo(options.getFirst())
                    .jsonPath("$.options[0].votes").isEqualTo(expectedInitialVotes)
                    .jsonPath("$.options[1].id").isNotEmpty()
                    .jsonPath("$.options[1].text").isEqualTo(options.get(1))
                    .jsonPath("$.options[1].votes").isEqualTo(expectedInitialVotes)
                    .jsonPath("$.options[2].id").isNotEmpty()
                    .jsonPath("$.options[2].text").isEqualTo(options.getLast())
                    .jsonPath("$.options[2].votes").isEqualTo(expectedInitialVotes);
        }

        @Test
        @DisplayName("Should fail to create poll with blank question")
        void shouldFailToCreatePollWithBlankQuestion() {
            Map<String, Object> requestBody = Map.of(
                    "question", "",
                    "startsAt", futureStartsAt.toString(),
                    "endsAt", futureEndsAt.toString(),
                    "options", List.of("Option 1", "Option 2", "Option 3")
            );

            var expectedStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
            var expectedErrorTitle = "Validation Failed";
            var expectedErrorDetails = "Request validation failed";
            var expectedQuestionError = "The question cannot be blank";

            // Act & Assert
            webTestClient.post().uri(apiPath)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(apiPath)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.errors.question").isEqualTo(expectedQuestionError)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to create poll with less than 3 options")
        void shouldFailToCreatePollWithLessThan3Options() {
            var question = "Valid question?";
            var options = List.of("Option 1", "Option 2");

            Map<String, Object> requestBody = Map.of(
                    "question", question,
                    "startsAt", futureStartsAt.toString(),
                    "endsAt", futureEndsAt.toString(),
                    "options", options
            );

            var expectedStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
            var expectedErrorTitle = "Validation Failed";
            var expectedErrorDetails = "Request validation failed";
            var expectedPollOptionsError = "The poll must have at least 3 options";

            webTestClient.post().uri(apiPath)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(apiPath)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.errors.options").isEqualTo(expectedPollOptionsError)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to create poll with past start date")
        void shouldFailToCreatePollWithPastStartDate() {
            OffsetDateTime pastStartsAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
            OffsetDateTime futureEndsAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

            Map<String, Object> requestBody = Map.of(
                    "question", "Valid question?",
                    "startsAt", pastStartsAt.toString(),
                    "endsAt", futureEndsAt.toString(),
                    "options", List.of("Option 1", "Option 2", "Option 3")
            );

            var expectedStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
            var expectedErrorTitle = "Validation Failed";
            var expectedErrorDetails = "Request validation failed";
            var expectedStartsAtError = "The poll cannot start in the past";

            webTestClient.post().uri(apiPath)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(apiPath)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.errors.startsAt").isEqualTo(expectedStartsAtError)
                    .jsonPath("$.timestamp").isNotEmpty();
        }
    }

    @Nested
    @DisplayName("GET /polls - List Polls Tests")
    class ListPollsTests {

        private final String apiPath = "/polls";

        @Test
        @DisplayName("Should list all polls with pagination")
        void shouldListAllPollsWithPagination() {
            Instant now = Instant.now();

            var poll1StartsAt = now.plusSeconds(3600).truncatedTo(ChronoUnit.MILLIS);
            var poll1EndsAt = now.plusSeconds(7200).truncatedTo(ChronoUnit.MILLIS);
            var poll2StartsAt = now.plusSeconds(7200).truncatedTo(ChronoUnit.MILLIS);
            var poll2EndsAt = now.plusSeconds(10800).truncatedTo(ChronoUnit.MILLIS);

            var poll1 = createTestPoll("Poll 1", poll1StartsAt, poll1EndsAt);
            var poll2 = createTestPoll("Poll 2", poll2StartsAt, poll2EndsAt);

            var page = 0;
            var pageSize = 10;

            var expectedPage = 0;
            var expectedPageSize = 10;
            var expectedPageLength = 2;
            var expectedTotalPages = 1;
            var expectedTotalElements = 2;
            var expectedHasNext = false;
            var expectedHasPrevious = false;

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiPath)
                            .queryParam("page", page)
                            .queryParam("size", pageSize)
                            .queryParam("sort", "question,asc")
                            .build()
                    ).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content.length()").isEqualTo(expectedTotalElements)
                    .jsonPath("$.content[0].id").isEqualTo(poll1.getId())
                    .jsonPath("$.content[0].question").isEqualTo(poll1.getQuestion())
                    .jsonPath("$.content[0].status").isEqualTo(poll1.getStatus().toString())
                    .jsonPath("$.content[0].startsAt").isEqualTo(poll1.getStartsAt())
                    .jsonPath("$.content[0].endsAt").isEqualTo(poll1.getEndsAt())
                    .jsonPath("$.content[0].options.length()").isEqualTo(poll1.getOptions().size())
                    .jsonPath("$.content[1].id").isEqualTo(poll2.getId())
                    .jsonPath("$.page").isEqualTo(expectedPage)
                    .jsonPath("$.pageSize").isEqualTo(expectedPageSize)
                    .jsonPath("$.pageLength").isEqualTo(expectedPageLength)
                    .jsonPath("$.totalPages").isEqualTo(expectedTotalPages)
                    .jsonPath("$.totalElements").isEqualTo(expectedTotalElements)
                    .jsonPath("$.hasNext").isEqualTo(expectedHasNext)
                    .jsonPath("$.hasPrevious").isEqualTo(expectedHasPrevious);
        }

        @Test
        @DisplayName("Should filter polls by status NOT_STARTED")
        void shouldFilterPollsByStatusNotStarted() {
            Instant now = Instant.now();
            createTestPoll("Future Poll", now.plusSeconds(3600), now.plusSeconds(7200));
            createTestPoll("Current Poll", now.minusSeconds(3600), now.plusSeconds(3600));

            var expectedPollStatus = "NOT_STARTED";
            var expectedNumberOfPolls = 1;

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiPath)
                            .queryParam("status", expectedPollStatus)
                            .build()
                    ).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content.length()").isEqualTo(expectedNumberOfPolls)
                    .jsonPath("$.content[0].status").isEqualTo(expectedPollStatus);
        }

        @Test
        @DisplayName("Should filter polls by status IN_PROGRESS")
        void shouldFilterPollsByStatusInProgress() {
            Instant now = Instant.now();
            createTestPoll("Current Poll", now.minusSeconds(3600), now.plusSeconds(3600));
            createTestPoll("Future Poll", now.plusSeconds(3600), now.plusSeconds(7200));

            var expectedPollStatus = "IN_PROGRESS";
            var expectedNumberOfPolls = 1;

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiPath)
                            .queryParam("status", expectedPollStatus)
                            .build()
                    ).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content.length()").isEqualTo(expectedNumberOfPolls)
                    .jsonPath("$.content[0].status").isEqualTo(expectedPollStatus);
        }

        @Test
        @DisplayName("Should filter polls by status FINISHED")
        void shouldFilterPollsByStatusFinished() {
            Instant now = Instant.now();
            createTestPoll("Past Poll", now.minusSeconds(7200), now.minusSeconds(3600));
            createTestPoll("Current Poll", now.minusSeconds(3600), now.plusSeconds(3600));

            var expectedPollStatus = "FINISHED";
            var expectedNumberOfPolls = 1;

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiPath)
                            .queryParam("status", expectedPollStatus)
                            .build()
                    ).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content.length()").isEqualTo(expectedNumberOfPolls)
                    .jsonPath("$.content[0].status").isEqualTo(expectedPollStatus);
        }
    }

    @Nested
    @DisplayName("PUT /polls/{pollId} - Edit Poll Tests")
    class EditPollTests {

        private final String apiPath = "/polls/%s";

        private Poll notStartedPoll;
        private Poll inProgressPoll;

        @BeforeEach
        void setUpEditTests() {
            Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            notStartedPoll = createTestPoll("Original Question", now.plusSeconds(3600), now.plusSeconds(7200));
            inProgressPoll = createTestPoll("In Progress Poll", now.minusSeconds(3600), now.plusSeconds(3600));
        }

        @Test
        @DisplayName("Should edit poll that has not started")
        void shouldEditPollThatHasNotStarted() {
            var updatedQuestion = "Updated Question";

            Map<String, String> requestBody = Map.of(
                    "question", updatedQuestion
            );

            var uri = apiPath.formatted(notStartedPoll.getId());

            var expectedPollStatus = "NOT_STARTED";

            webTestClient.put()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(notStartedPoll.getId())
                    .jsonPath("$.question").isEqualTo(updatedQuestion)
                    .jsonPath("$.startsAt").isEqualTo(notStartedPoll.getStartsAt())
                    .jsonPath("$.endsAt").isEqualTo(notStartedPoll.getEndsAt())
                    .jsonPath("$.status").isEqualTo(expectedPollStatus);

        }

        @Test
        @DisplayName("Should fail to edit poll that has already started")
        void shouldFailToEditPollThatHasAlreadyStarted() {
            Map<String, Object> requestBody = Map.of(
                    "question", "Updated Question"
            );

            var uri = apiPath.formatted(inProgressPoll.getId());

            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();
            var expectedErrorTitle = "Poll Already Started";

            webTestClient.put()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to edit non-existent poll")
        void shouldFailToEditNonExistentPoll() {
            Map<String, Object> requestBody = Map.of(
                    "question", "Updated Question"
            );

            var uri = apiPath.formatted(randomUUID());

            var expectedStatusCode = HttpStatus.NOT_FOUND.value();
            var expectedErrorTitle = "Resource Not Found";

            webTestClient.put()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should update poll dates")
        void shouldUpdatePollDates() {
            Instant newStartsAt = Instant.now().plusSeconds(7200).truncatedTo(ChronoUnit.MILLIS);
            Instant newEndsAt = newStartsAt.plusSeconds(7200).truncatedTo(ChronoUnit.MILLIS);

            Map<String, String> requestBody = Map.of(
                    "startsAt", newStartsAt.toString(),
                    "endsAt", newEndsAt.toString()
            );

            var uri = apiPath.formatted(notStartedPoll.getId());

            webTestClient.put()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(notStartedPoll.getId())
                    .jsonPath("$.startsAt").isEqualTo(newStartsAt.toString())
                    .jsonPath("$.endsAt").isEqualTo(newEndsAt.toString());
        }
    }

    @Nested
    @DisplayName("POST /polls/{pollId}/options - Add Poll Option Tests")
    class AddPollOptionTests {

        private final String apiPath = "/polls/%s/options";

        private Poll notStartedPoll;
        private Poll inProgressPoll;

        @BeforeEach
        void setUpAddOptionTests() {
            Instant now = Instant.now();
            notStartedPoll = createTestPoll("Test Poll", now.plusSeconds(3600), now.plusSeconds(7200));
            inProgressPoll = createTestPoll("In Progress Poll", now.minusSeconds(3600), now.plusSeconds(3600));
        }

        @Test
        @DisplayName("Should add option to poll that has not started")
        void shouldAddOptionToPollThatHasNotStarted() {
            var optionText = "New Option";

            Map<String, Object> requestBody = Map.of(
                    "text", optionText
            );

            var uri = apiPath.formatted(notStartedPoll.getId());
            var expectedVotes = 0;

            webTestClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.text").isEqualTo(optionText)
                    .jsonPath("$.votes").isEqualTo(expectedVotes);
        }

        @Test
        @DisplayName("Should fail to add option to poll that has started")
        void shouldFailToAddOptionToPollThatHasStarted() {
            Map<String, Object> requestBody = Map.of(
                    "text", "New Option"
            );

            var uri = apiPath.formatted(inProgressPoll.getId());
            var expectedErrorTitle = "Poll Already Started";
            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();

            webTestClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to add blank option")
        void shouldFailToAddBlankOption() {
            Map<String, Object> requestBody = Map.of(
                    "text", ""
            );

            var uri = apiPath.formatted(notStartedPoll.getId());
            var expectedStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
            var expectedErrorTitle = "Validation Failed";
            var expectedErrorDetails = "Request validation failed";
            var expectedOptionTextError = "The option cannot be blank";

            webTestClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.errors.text").isEqualTo(expectedOptionTextError)
                    .jsonPath("$.timestamp").isNotEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH /polls/{pollId}/options/{optionId}/vote - Vote Poll Option Tests")
    class VotePollOptionTests {

        private final String apiPath = "/polls/%s/options/%s/vote";
        private final String wsPath = "ws://localhost:%d/ws/polls/%s";

        private Poll activePoll;
        private Poll notStartedPoll;
        private Poll finishedPoll;

        @BeforeEach
        void setUpVoteTests() {
            Instant now = Instant.now();
            activePoll = createTestPoll("Active Poll", now.minusSeconds(3600), now.plusSeconds(3600));
            notStartedPoll = createTestPoll("Future Poll", now.plusSeconds(3600), now.plusSeconds(7200));
            finishedPoll = createTestPoll("Finished Poll", now.minusSeconds(7200), now.minusSeconds(3600));
        }

        @Test
        @DisplayName("Should vote on option in active poll")
        void shouldVoteOnOptionInActivePoll() {
            Option option = activePoll.getOptions().getFirst();

            var uri = apiPath.formatted(activePoll.getId(), option.getId());

            webTestClient.patch().uri(uri)
                    .exchange()
                    .expectStatus().isOk();

            Option updatedOption = optionRepository.findById(option.getId()).orElseThrow();
            assertEquals(1, updatedOption.getVotes());
        }

        @Test
        @DisplayName("Should vote on option in active poll and notify WebSocket clients")
        void shouldNotifyWebSocketClientsOnVote() throws Exception {
            Option option = activePoll.getOptions().getFirst();

            WebSocketClient client = new StandardWebSocketClient();

            BlockingQueue<String> messages = new LinkedBlockingQueue<>();

            var wsUri = wsPath.formatted(port, activePoll.getId());

            client.execute(
                    new WebSocketHandler() {

                        @Override
                        public void afterConnectionEstablished(WebSocketSession session) throws Exception {}

                        @Override
                        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                            messages.add(message.getPayload().toString());
                        }

                        @Override
                        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {}

                        @Override
                        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {}

                        @Override
                        public boolean supportsPartialMessages() {
                            return false;
                        }
                    },
                    wsUri
            ).get();

            var uri = apiPath.formatted(activePoll.getId(), option.getId());

            webTestClient.patch().uri(uri)
                    .exchange()
                    .expectStatus()
                    .isOk();

            var expectedType = "VOTE_UPDATED";
            var expectedOptionId = option.getId();
            var expectedOptionText = option.getText();
            var expectedVoteCount = 1;

            String message = messages.take();
            JsonNode jsonNode = objectMapper.readTree(message);

            var type = jsonNode.get("type").asText();
            var optionId = jsonNode.path("payload").get("id").asText();
            var optionText = jsonNode.path("payload").get("text").asText();
            var optionVoteCount = jsonNode.path("payload").get("votes").asInt();

            assertEquals(expectedType, type);
            assertEquals(expectedOptionId, optionId);
            assertEquals(expectedOptionText, optionText);
            assertEquals(expectedVoteCount, optionVoteCount);
        }

        @Test
        @DisplayName("Should fail to vote on poll that has not started")
        void shouldFailToVoteOnPollThatHasNotStarted() {
            Option option = notStartedPoll.getOptions().getFirst();

            var uri = apiPath.formatted(notStartedPoll.getId(), option.getId());

            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();
            var expectedErrorTitle = "Poll Not In Progress";
            var expectedErrorDetails = "Votes can only be cast on polls that are in progress.";

            webTestClient.patch().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to vote on finished poll")
        void shouldFailToVoteOnFinishedPoll() {
            Option option = finishedPoll.getOptions().getFirst();

            var uri = apiPath.formatted(finishedPoll.getId(), option.getId());

            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();
            var expectedErrorTitle = "Poll Not In Progress";
            var expectedErrorDetails = "Votes can only be cast on polls that are in progress.";

            webTestClient.patch().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to vote on non-existent option")
        void shouldFailToVoteOnNonExistentOption() {
            var uri = apiPath.formatted(activePoll.getId(), randomUUID());

            var expectedStatusCode = HttpStatus.NOT_FOUND.value();
            var expectedErrorTitle = "Resource Not Found";

            webTestClient.patch()
                    .uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should increment votes correctly with multiple votes")
        void shouldIncrementVotesCorrectlyWithMultipleVotes() {
            Option option = activePoll.getOptions().getFirst();

            var uri = apiPath.formatted(activePoll.getId(), option.getId());

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = IntStream.range(0, 50)
                        .mapToObj(_ -> executor.submit(() -> {
                            webTestClient.patch()
                                    .uri(uri)
                                    .exchange()
                                    .expectStatus().isOk();
                        }))
                        .toList();

                for (var future : futures) {
                    future.get();
                }
            } catch (Exception e) {
                fail("Exception during voting: " + e.getMessage());
            }

            Option updatedOption = optionRepository.findById(option.getId()).orElseThrow();
            assertEquals(50, updatedOption.getVotes());
        }
    }

    @Nested
    @DisplayName("DELETE /polls/{pollId}/options/{optionId} - Delete Poll Option Tests")
    class DeletePollOptionTests {

        private final String apiPath = "/polls/%s/options/%s";

        private Poll notStartedPollWith3Options;
        private Poll notStartedPollWith4Options;
        private Poll inProgressPoll;

        @BeforeEach
        void setUpDeleteOptionTests() {
            Instant now = Instant.now();
            notStartedPollWith3Options = createTestPoll("Test Poll", now.plusSeconds(3600), now.plusSeconds(7200));
            notStartedPollWith4Options = createTestPollWithOptions("Test Poll 2", now.plusSeconds(3600), now.plusSeconds(7200), 4);
            inProgressPoll = createTestPollWithOptions("Active Poll", now.minusSeconds(3600), now.plusSeconds(3600), 4);
        }

        @Test
        @DisplayName("Should delete option from poll that has not started")
        void shouldDeleteOptionFromPollThatHasNotStarted() {
            Option option = notStartedPollWith4Options.getOptions().getFirst();

            var uri = apiPath.formatted(notStartedPollWith4Options.getId(), option.getId());

            webTestClient.delete().uri(uri)
                    .exchange()
                    .expectStatus().isNoContent();

            assertFalse(optionRepository.existsById(option.getId()));
        }

        @Test
        @DisplayName("Should fail to delete option if poll has only 3 options")
        void shouldFailToDeleteOptionIfPollHasOnly3Options() {
            Option option = notStartedPollWith3Options.getOptions().getFirst();

            var uri = apiPath.formatted(notStartedPollWith3Options.getId(), option.getId());
            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();
            var expectedErrorTitle = "Minimum Poll Options Violation";
            var expectedErrorDetails = "A poll must have at least three options.";

            webTestClient.delete().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to delete option from poll that has started")
        void shouldFailToDeleteOptionFromPollThatHasStarted() {
            Option option = inProgressPoll.getOptions().getFirst();

            var uri = apiPath.formatted(inProgressPoll.getId(), option.getId());

            var expectedStatusCode = HttpStatus.BAD_REQUEST.value();
            var expectedErrorTitle = "Poll Already Started";
            var expectedErrorDetails = "Options cannot be deleted after the poll has started.";

            webTestClient.delete().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isEqualTo(expectedErrorDetails)
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to delete non-existent option")
        void shouldFailToDeleteNonExistentOption() {
            var uri = apiPath.formatted(notStartedPollWith4Options.getId(), randomUUID());

            var expectedStatusCode = HttpStatus.NOT_FOUND.value();
            var expectedErrorTitle = "Resource Not Found";


            webTestClient.delete().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }
    }

    @Nested
    @DisplayName("DELETE /polls/{pollId} - Delete Poll Tests")
    class DeletePollTests {

        private final String apiPath = "/polls/%s";

        @Test
        @DisplayName("Should delete poll")
        void shouldDeletePoll() {
            Instant now = Instant.now();
            Poll poll = createTestPoll("Test Poll", now.plusSeconds(3600), now.plusSeconds(7200));

            var uri = apiPath.formatted(poll.getId());

            webTestClient.delete()
                    .uri(uri)
                    .exchange()
                    .expectStatus().isNoContent();

            assertFalse(pollRepository.existsById(poll.getId()));
        }

        @Test
        @DisplayName("Should fail to delete non-existent poll")
        void shouldFailToDeleteNonExistentPoll() {
            var uri = apiPath.formatted(randomUUID());

            var expectedStatusCode = HttpStatus.NOT_FOUND.value();
            var expectedErrorTitle = "Resource Not Found";

            webTestClient.delete().uri(uri)
                    .exchange()
                    .expectStatus().isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.apiPath").isEqualTo(uri)
                    .jsonPath("$.statusCode").isEqualTo(expectedStatusCode)
                    .jsonPath("$.title").isEqualTo(expectedErrorTitle)
                    .jsonPath("$.details").isNotEmpty()
                    .jsonPath("$.timestamp").isNotEmpty();
        }

        @Test
        @DisplayName("Should delete poll and cascade delete options")
        void shouldDeletePollAndCascadeDeleteOptions() {
            Instant now = Instant.now();
            Poll poll = createTestPoll("Test Poll", now.plusSeconds(3600), now.plusSeconds(7200));
            List<String> optionIds = poll.getOptions().stream().map(Option::getId).toList();

            var uri = apiPath.formatted(poll.getId());

            webTestClient.delete()
                    .uri(uri)
                    .exchange()
                    .expectStatus().isNoContent();

            assertFalse(pollRepository.existsById(poll.getId()));

            for (String optionId : optionIds) {
                assertFalse(optionRepository.existsById(optionId));
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests - Full Poll Lifecycle")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full poll lifecycle")
        void shouldCompleteFullPollLifecycle() {
            Instant startsAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toInstant().truncatedTo(ChronoUnit.MILLIS);
            Instant endsAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(25).toInstant().truncatedTo(ChronoUnit.MILLIS);

            Map<String, Object> createRequest = Map.of(
                    "question", "What is your favorite framework?",
                    "startsAt", startsAt.toString(),
                    "endsAt", endsAt.toString(),
                    "options", List.of("Spring Boot", "Django", "Express")
            );

            final String[] pollIdHolder = new String[1];

            webTestClient.post()
                    .uri("/polls")
                    .bodyValue(createRequest)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CREATED)
                    .expectBody()
                    .jsonPath("$.id").value(id -> pollIdHolder[0] = id.toString());

            String pollId = pollIdHolder[0];
            assertNotNull(pollId);

            // 2. Add an option
            Map<String, Object> addOptionRequest = Map.of("text", "Laravel");

            final String[] newOptionIdHolder = new String[1];
            webTestClient.post()
                    .uri("/polls/" + pollId + "/options")
                    .bodyValue(addOptionRequest)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CREATED)
                    .expectBody()
                    .jsonPath("$.id").value(id -> newOptionIdHolder[0] = id.toString());

            String newOptionId = newOptionIdHolder[0];
            assertNotNull(newOptionId);

            // 3. Edit poll
            Map<String, Object> editRequest = Map.of(
                    "question", "What is your favorite web framework?"
            );

            webTestClient.put()
                    .uri("/polls/" + pollId)
                    .bodyValue(editRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.question").isEqualTo("What is your favorite web framework?");

            // 4. List polls
            webTestClient.get()
                    .uri("/polls")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content").isArray()
                    .jsonPath("$.content.length()").isEqualTo(1)
                    .jsonPath("$.content[0].options.length()").isEqualTo(4);

            // 5. Delete an option
            webTestClient.delete()
                    .uri("/polls/" + pollId + "/options/" + newOptionId)
                    .exchange()
                    .expectStatus().isNoContent();

            // 6. Verify option was deleted
            webTestClient.get()
                    .uri("/polls")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[0].options.length()").isEqualTo(3);

            // 7. Delete poll
            webTestClient.delete()
                    .uri("/polls/" + pollId)
                    .exchange()
                    .expectStatus().isNoContent();
        }
    }

    private Poll createTestPoll(String question, Instant startsAt, Instant endsAt) {
        return createTestPollWithOptions(question, startsAt, endsAt, 3);
    }

    private Poll createTestPollWithOptions(String question, Instant startsAt, Instant endsAt, int optionsCount) {
        Poll poll = Poll.builder()
                .question(question)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .build();

        pollRepository.save(poll);

        List<Option> options = new java.util.ArrayList<>();
        for (int i = 1; i <= optionsCount; i++) {
            Option option = Option.builder()
                    .text("Option " + i)
                    .poll(poll)
                    .votes(0)
                    .build();
            options.add(option);
        }

        optionRepository.saveAll(options);
        poll.setOptions(options);

        return poll;
    }
}

