package ru.practicum.ewm.comment;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long eventId, Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long commentId, Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getEventComments(Long eventId, int from, int size, CommentSort sort);

    void deleteComment(Long commentId);

    void deleteComment(Long commentId, Long userId);
}