package ru.practicum.ewm.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEventIdAndEventInitiatorId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findByIdIn(List<Long> requestIds);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);
}
