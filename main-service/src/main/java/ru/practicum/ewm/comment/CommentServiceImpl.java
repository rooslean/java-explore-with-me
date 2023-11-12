package ru.practicum.ewm.comment;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long eventId, Long userId, NewCommentDto newCommentDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(ObjectNotFoundException::new);
        if (!EventState.PUBLISHED.equals(event.getEventState())) {
            throw new BadRequestException("Оставлять комментарии можно только к опубликованным событиям");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(ObjectNotFoundException::new);
        Comment comment = CommentMapper.mapToComment(newCommentDto, user, event);
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, NewCommentDto newCommentDto) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException();
        }
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(ObjectNotFoundException::new);
        CommentMapper.mapToUpdateComment(comment, newCommentDto);
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size, CommentSort commentSort) {
        Sort sort = Sort.unsorted();
        if (CommentSort.CREATED_ASC.equals(commentSort)) {
            sort = Sort.by("created").ascending();
        } else if (CommentSort.CREATED_DESC.equals(commentSort)) {
            sort = Sort.by("created").descending();
        }
        PageRequest page = PageRequest.of(from / size, size, sort);

        return commentRepository.findByEventId(eventId, page)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        commentRepository.deleteByIdAndAuthorId(commentId, userId);
    }
}
