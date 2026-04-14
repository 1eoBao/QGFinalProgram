package com.javaweb.smartnote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javaweb.smartnote.dto.request.NoteCreateRequest;
import com.javaweb.smartnote.dto.request.NotePermissionRequest;
import com.javaweb.smartnote.dto.request.NoteQueryRequest;
import com.javaweb.smartnote.dto.request.NoteUpdateRequest;
import com.javaweb.smartnote.dto.response.NoteDetailResponse;
import com.javaweb.smartnote.dto.response.NoteHistoryVO;
import com.javaweb.smartnote.dto.response.NoteListResponse;

import java.util.List;

public interface NoteService {

    Long createNote(Long userId, NoteCreateRequest request);

    Page<NoteListResponse> listNotes(Long userId, NoteQueryRequest request);

    NoteDetailResponse getNoteDetail(Long userId, Long noteId);

    void updateNote(Long userId, Long noteId, NoteUpdateRequest request);

    void deleteNote(Long userId, Long noteId);

    void updatePermission(Long userId, Long noteId, NotePermissionRequest request);

    List<NoteHistoryVO> getRecentHistory(Long userId);

    NoteDetailResponse getPublicNote(Long noteId, Long currentUserId);

    Page<NoteListResponse> listSharedNotes(Long userId, NoteQueryRequest request);
}
