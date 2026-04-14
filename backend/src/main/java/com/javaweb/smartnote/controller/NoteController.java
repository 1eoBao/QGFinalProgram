package com.javaweb.smartnote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javaweb.smartnote.common.Result;
import com.javaweb.smartnote.common.UserContext;
import com.javaweb.smartnote.dto.request.NoteCreateRequest;
import com.javaweb.smartnote.dto.request.NotePermissionRequest;
import com.javaweb.smartnote.dto.request.NoteQueryRequest;
import com.javaweb.smartnote.dto.request.NoteUpdateRequest;
import com.javaweb.smartnote.dto.response.NoteDetailResponse;
import com.javaweb.smartnote.dto.response.NoteHistoryVO;
import com.javaweb.smartnote.dto.response.NoteListResponse;
import com.javaweb.smartnote.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "笔记模块")
@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // 创建笔记
    @Operation(summary = "创建笔记")
    @PostMapping
    public Result<Long> createNote(@Valid @RequestBody NoteCreateRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(noteService.createNote(userId, request));
    }

    // 笔记列表（分页，支持关键词和标签筛选）
    @Operation(summary = "笔记列表（分页）")
    @GetMapping("/list")
    public Result<Page<NoteListResponse>> listNotes(NoteQueryRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(noteService.listNotes(userId, request));
    }

    // 笔记详情（含AI分析结果）
    @Operation(summary = "笔记详情")
    @GetMapping("/{id}")
    public Result<NoteDetailResponse> getNoteDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(noteService.getNoteDetail(userId, id));
    }

    // 编辑笔记
    @Operation(summary = "编辑笔记")
    @PutMapping("/{id}")
    public Result<Void> updateNote(@PathVariable Long id, @RequestBody NoteUpdateRequest request) {
        Long userId = UserContext.getUserId();
        noteService.updateNote(userId, id, request);
        return Result.success(null);
    }

    // 删除笔记（逻辑删除）
    @Operation(summary = "删除笔记")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        noteService.deleteNote(userId, id);
        return Result.success(null);
    }

    // 修改笔记权限（4种：仅自己/部分好友只读/部分好友可编辑/所有人可见）
    @Operation(summary = "修改笔记权限")
    @PutMapping("/{id}/permission")
    public Result<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody NotePermissionRequest request) {
        Long userId = UserContext.getUserId();
        noteService.updatePermission(userId, id, request);
        return Result.success(null);
    }

    // 浏览历史（最近20条）
    @Operation(summary = "浏览历史")
    @GetMapping("/history")
    public Result<List<NoteHistoryVO>> getRecentHistory() {
        Long userId = UserContext.getUserId();
        return Result.success(noteService.getRecentHistory(userId));
    }

    // 公开笔记访问（无需登录也可访问）
    @Operation(summary = "公开笔记访问（无需登录也可访问）")
    @GetMapping("/public/{id}")
    public Result<NoteDetailResponse> getPublicNote(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        return Result.success(noteService.getPublicNote(id, currentUserId));
    }

    // 共享给我的笔记列表
    @Operation(summary = "共享给我的笔记列表")
    @GetMapping("/shared")
    public Result<Page<NoteListResponse>> listSharedNotes(NoteQueryRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(noteService.listSharedNotes(userId, request));
    }
}
