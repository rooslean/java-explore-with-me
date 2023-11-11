package ru.practicum.ewm.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Optional<Request> findByRequesterId(Long userId);

    @Query("SELECT NEW ru.practicum.ewm.request.dto.RequestCountByEventId(req.event.id, COUNT(req.id)) " +
            "FROM Request req " +
            "join req.event as event " +
            "WHERE req.event.id IN :ids " +
            "AND req.status = :status " +
            "GROUP BY req.event.id")
    List<RequestCountByEventId> countByUser(@Param("ids") List<Long> ids, @Param("status") RequestStatus status);

}
