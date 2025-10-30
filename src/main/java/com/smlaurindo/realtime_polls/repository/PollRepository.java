package com.smlaurindo.realtime_polls.repository;

import com.smlaurindo.realtime_polls.domain.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP < p.startsAt
    """)
    Page<Poll> findAllNotStarted(Pageable pageable);

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP >= p.startsAt AND CURRENT_TIMESTAMP < p.endsAt
    """)
    Page<Poll> findAllInProgress(Pageable pageable);

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP >= p.endsAt
    """)
    Page<Poll> findAllFinished(Pageable pageable);

    @Query("""
        SELECT p FROM Poll p
        LEFT JOIN FETCH p.options
        WHERE p.id = :pollId
    """)
    Optional<Poll> findByIdWithOptions(String pollId);
}