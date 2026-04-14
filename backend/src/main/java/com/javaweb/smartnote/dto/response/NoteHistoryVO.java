package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteHistoryVO {
    private Long noteId;
    private String title;
    private LocalDateTime viewTime;
}
