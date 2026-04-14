package com.javaweb.smartnote.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class NoteUpdateRequest {
    private String title;
    private String content;
    private List<Long> tagIds;
}
