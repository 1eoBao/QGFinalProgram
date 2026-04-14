package com.javaweb.smartnote.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class NoteCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private List<Long> tagIds;
}
