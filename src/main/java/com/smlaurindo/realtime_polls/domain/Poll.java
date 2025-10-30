package com.smlaurindo.realtime_polls.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "polls")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "question")
    private String question;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @OneToMany(mappedBy = "poll", fetch = FetchType.LAZY)
    private List<Option> options;

    @Transient
    public PollStatus getStatus() {
        Instant now = Instant.now();
        if (now.isBefore(startsAt)) return PollStatus.NOT_STARTED;
        if (now.isAfter(endsAt) || now.equals(endsAt)) return PollStatus.FINISHED;
        return PollStatus.IN_PROGRESS;
    }
}
