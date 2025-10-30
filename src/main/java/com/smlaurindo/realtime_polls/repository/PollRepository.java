package com.smlaurindo.realtime_polls.repository;

import com.smlaurindo.realtime_polls.domain.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP < p.startsAt
    """)
    @EntityGraph(attributePaths = {"options"})
    Page<Poll> findAllNotStarted(Pageable pageable);

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP >= p.startsAt AND CURRENT_TIMESTAMP < p.endsAt
    """)
    @EntityGraph(attributePaths = {"options"})
    Page<Poll> findAllInProgress(Pageable pageable);

    @Query("""
        SELECT p FROM Poll p
        WHERE CURRENT_TIMESTAMP >= p.endsAt
    """)
    @EntityGraph(attributePaths = {"options"})
    Page<Poll> findAllFinished(Pageable pageable);
}