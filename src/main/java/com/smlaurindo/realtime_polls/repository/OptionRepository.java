package com.smlaurindo.realtime_polls.repository;

import com.smlaurindo.realtime_polls.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, String> {
    @Query("SELECT o FROM Option o WHERE o.poll.id IN :pollIds")
    List<Option> findByPollIds(@Param("pollIds") List<String> pollIds);
}
