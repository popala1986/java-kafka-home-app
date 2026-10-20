package com.homeapp.planner.chore;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoreExclusionRepository extends JpaRepository<ChoreExclusion, UUID> {

    boolean existsByChoreIdAndUserId(UUID choreId, UUID userId);

    void deleteByChoreIdAndUserId(UUID choreId, UUID userId);

    List<ChoreExclusion> findAllByChoreId(UUID choreId);

    List<ChoreExclusion> findAllByChoreIdIn(Collection<UUID> choreIds);
}
