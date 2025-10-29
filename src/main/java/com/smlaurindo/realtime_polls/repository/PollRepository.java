package com.smlaurindo.realtime_polls.repository;

import com.smlaurindo.realtime_polls.domain.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {}