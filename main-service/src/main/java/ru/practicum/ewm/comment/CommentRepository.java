package ru.practicum.ewm.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comment.dto.CountCommentByEvent;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    void deleteByIdAndAuthorId(Long commentId, Long authorId);

    long countByEventId(Long eventId);

    @Query("SELECT NEW ru.practicum.ewm.comment.dto.CountCommentByEvent(c.event.id, COUNT(c.id)) " +
            "FROM Comment c " +
            "join c.event as event " +
            "WHERE c.event.id IN :ids " +
            "GROUP BY c.event.id")
    List<CountCommentByEvent> countCommentForEvents(@Param("ids") List<Long> events);
}
