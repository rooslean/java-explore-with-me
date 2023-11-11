package ru.practicum.ewm.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.dto.RequestCountByEventId;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEventIdAndEventInitiatorId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findByIdIn(List<Long> requestIds);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    @Query(value = "select req.event_id, count(req) as count " +
            "from requests req " +
            "where req.event_id in ?1 " +
            "and req.status = ?2", nativeQuery = true)
    List<RequestCountByEventId> countByUser(List<Long> ids, RequestStatus status);
}
