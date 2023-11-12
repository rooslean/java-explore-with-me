package ru.practicum.ewm.comment;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment mapToComment(NewCommentDto newCommentDto, User author, Event event) {
        return Comment.builder()
                .author(author)
                .event(event)
                .text(newCommentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    public static void mapToUpdateComment(Comment comment, NewCommentDto newCommentDto) {
        if (newCommentDto.getText() != null && !newCommentDto.getText().isEmpty()) {
            comment.setText(newCommentDto.getText());
        }
    }
}
