package com.javaweb.smartnote.service;

import com.javaweb.smartnote.dto.response.NoteDetailResponse;

public interface AiService {

    NoteDetailResponse.AiAnalysisVO analyzeNote(Long userId, Long noteId);

    NoteDetailResponse.AiAnalysisVO getAnalysis(Long noteId);
}
