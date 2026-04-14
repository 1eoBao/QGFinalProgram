package com.javaweb.smartnote.controller;

import com.javaweb.smartnote.common.Result;
import com.javaweb.smartnote.common.UserContext;
import com.javaweb.smartnote.dto.response.NoteDetailResponse;
import com.javaweb.smartnote.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI智能分析模块")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI分析笔记")
    @PostMapping("/analyze/{noteId}")
    public Result<NoteDetailResponse.AiAnalysisVO> analyzeNote(@PathVariable Long noteId) {
        Long userId = UserContext.getUserId();
        NoteDetailResponse.AiAnalysisVO result = aiService.analyzeNote(userId, noteId);
        return Result.success(result);
    }

    @Operation(summary = "获取AI分析结果")
    @GetMapping("/analysis/{noteId}")
    public Result<NoteDetailResponse.AiAnalysisVO> getAnalysis(@PathVariable Long noteId) {
        return Result.success(aiService.getAnalysis(noteId));
    }
}
