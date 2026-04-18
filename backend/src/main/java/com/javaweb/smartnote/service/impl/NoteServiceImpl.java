package com.javaweb.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javaweb.smartnote.common.BusinessException;
import com.javaweb.smartnote.common.Constants;
import com.javaweb.smartnote.common.ResultCodeEnum;
import com.javaweb.smartnote.dto.request.NoteCreateRequest;
import com.javaweb.smartnote.dto.request.NotePermissionRequest;
import com.javaweb.smartnote.dto.request.NoteQueryRequest;
import com.javaweb.smartnote.dto.request.NoteUpdateRequest;
import com.javaweb.smartnote.dto.response.NoteDetailResponse;
import com.javaweb.smartnote.dto.response.NoteHistoryVO;
import com.javaweb.smartnote.dto.response.NoteListResponse;
import com.javaweb.smartnote.entity.*;
import com.javaweb.smartnote.mapper.*;
import com.javaweb.smartnote.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final TagMapper tagMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final NotePermissionMapper notePermissionMapper;
    private final NoteHistoryMapper noteHistoryMapper;
    private final AiAnalysisMapper aiAnalysisMapper;

    // 创建笔记：插入笔记主体 + 标签关联
    @Override
    @Transactional
    public Long createNote(Long userId, NoteCreateRequest request) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent() != null ? request.getContent() : "");
        note.setPermissionType(Constants.PERM_PRIVATE);
        noteMapper.insert(note);

        saveTagRelations(note.getId(), request.getTagIds());
        log.info("笔记创建成功: noteId={}, userId={}", note.getId(), userId);
        return note.getId();
    }

    @Override
    public Page<NoteListResponse> listNotes(Long userId, NoteQueryRequest request) {
        Page<Note> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .like(request.getKeyword() != null, Note::getTitle, request.getKeyword())
                .eq(request.getPermissionType() != null, Note::getPermissionType, request.getPermissionType())
                .orderByDesc(Note::getLastEditTime);

        Page<Note> notePage = noteMapper.selectPage(page, wrapper);

        Page<NoteListResponse> result = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        result.setRecords(notePage.getRecords().stream().map(note -> {
            NoteListResponse resp = toListResponse(note);
            resp.setCanEdit(true);
            return resp;
        }).collect(Collectors.toList()));

        return result;
    }

    // 笔记详情：校验权限后返回，同时记录浏览历史
    @Override
    public NoteDetailResponse getNoteDetail(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }

        boolean canEdit = false;
        boolean canView = false;

        if (note.getUserId().equals(userId)) {
            canEdit = true;
            canView = true;
        } else {
            NotePermission perm = notePermissionMapper.selectOne(
                    new LambdaQueryWrapper<NotePermission>()
                            .eq(NotePermission::getNoteId, noteId)
                            .eq(NotePermission::getTargetUserId, userId));
            if (perm != null) {
                canView = true;
                canEdit = perm.getPermType() == Constants.PERM_TYPE_EDIT;
            }
            if (note.getPermissionType() == Constants.PERM_PUBLIC) {
                canView = true;
            }
            if (!canView) {
                throw new BusinessException(ResultCodeEnum.FORBIDDEN, "无权查看此笔记");
            }
        }

        recordViewHistory(userId, noteId);

        NoteDetailResponse resp = toDetailResponse(note, canEdit);
        resp.setIsOwner(note.getUserId().equals(userId));
        log.info("笔记详情查询成功: noteId={}, userId={}", noteId, userId);
        return resp;
    }

    // 编辑笔记：所有者或被授权编辑者可修改，同时更新标签关联
    @Override
    @Transactional
    public void updateNote(Long userId, Long noteId, NoteUpdateRequest request) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }

        // 非所有者需检查编辑权限
        if (!note.getUserId().equals(userId)) {
            NotePermission perm = notePermissionMapper.selectOne(
                    new LambdaQueryWrapper<NotePermission>()
                            .eq(NotePermission::getNoteId, noteId)
                            .eq(NotePermission::getTargetUserId, userId)
                            .eq(NotePermission::getPermType, Constants.PERM_TYPE_EDIT));
            if (perm == null) {
                throw new BusinessException(ResultCodeEnum.FORBIDDEN, "无权编辑此笔记");
            }
        }

        Note updateNote = new Note();
        updateNote.setId(noteId);
        if (request.getTitle() != null) {
            updateNote.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            updateNote.setContent(request.getContent());
        }
        noteMapper.updateById(updateNote);

        // 更新标签：先删后插
        if (request.getTagIds() != null) {
            noteTagRelMapper.deleteByNoteId(noteId);
            saveTagRelations(noteId, request.getTagIds());
        }

        log.info("笔记更新: noteId={}, userId={}", noteId, userId);
    }

    // 逻辑删除笔记：仅所有者可操作
    @Override
    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "只有笔记所有者才能删除");
        }
        noteMapper.deleteById(noteId);
        log.info("笔记删除(逻辑): noteId={}, userId={}", noteId, userId);
    }

    // 修改笔记权限：4种权限类型，部分好友可见/可编辑时需指定目标用户
    @Override
    @Transactional
    public void updatePermission(Long userId, Long noteId, NotePermissionRequest request) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "只有笔记所有者才能修改权限");
        }

        // 更新笔记权限类型
        Note updateNote = new Note();
        updateNote.setId(noteId);
        updateNote.setPermissionType(request.getPermissionType());
        noteMapper.updateById(updateNote);

        // 清除旧的权限记录
        notePermissionMapper.delete(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, noteId));

        // 部分好友可见/可编辑时，逐条插入权限记录
        if ((request.getPermissionType() == Constants.PERM_FRIEND_READ || request.getPermissionType() == Constants.PERM_FRIEND_EDIT)
                && request.getTargetUserIds() != null) {
            int permType = request.getPermissionType() == Constants.PERM_FRIEND_READ ? Constants.PERM_TYPE_READ : Constants.PERM_TYPE_EDIT;
            for (Long targetUserId : request.getTargetUserIds()) {
                NotePermission np = new NotePermission();
                np.setNoteId(noteId);
                np.setTargetUserId(targetUserId);
                np.setPermType(permType);
                notePermissionMapper.insert(np);
            }
        }

        log.info("笔记权限更新: noteId={}, permissionType={}", noteId, request.getPermissionType());
    }

    // 获取浏览历史：按查看时间倒序，最多20条
    @Override
    public List<NoteHistoryVO> getRecentHistory(Long userId) {
        List<NoteHistory> histories = noteHistoryMapper.selectList(
                new LambdaQueryWrapper<NoteHistory>()
                        .eq(NoteHistory::getUserId, userId)
                        .orderByDesc(NoteHistory::getViewTime)
                        .last("LIMIT 20"));

        return histories.stream().map(h -> {
            NoteHistoryVO vo = new NoteHistoryVO();
            vo.setNoteId(h.getNoteId());
            vo.setViewTime(h.getViewTime());
            Note note = noteMapper.selectById(h.getNoteId());
            if (note != null) {
                vo.setTitle(note.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // 公开笔记访问：无需登录，仅PERM_PUBLIC的笔记可被非所有者查看
    @Override
    public NoteDetailResponse getPublicNote(Long noteId, Long currentUserId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }

        // 所有者直接返回
        if (currentUserId != null && note.getUserId().equals(currentUserId)) {
            NoteDetailResponse resp = toDetailResponse(note, true);
            resp.setIsOwner(true);
            return resp;
        }

        // 公开笔记：不可编辑
        if (note.getPermissionType() == Constants.PERM_PUBLIC) {
            NoteDetailResponse resp = toDetailResponse(note, false);
            resp.setIsOwner(false);
            return resp;
        }

        // 非公开笔记：必须登录且有权限记录
        if (currentUserId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "请先登录");
        }

        NotePermission perm = notePermissionMapper.selectOne(
                new LambdaQueryWrapper<NotePermission>()
                        .eq(NotePermission::getNoteId, noteId)
                        .eq(NotePermission::getTargetUserId, currentUserId));
        if (perm != null) {
            boolean canEdit = perm.getPermType() == Constants.PERM_TYPE_EDIT;
            NoteDetailResponse resp = toDetailResponse(note, canEdit);
            resp.setIsOwner(false);
            return resp;
        }

        throw new BusinessException(ResultCodeEnum.FORBIDDEN, "无权查看此笔记");
    }

    // 共享给我的笔记：查询note_permission中target_user_id=当前用户的笔记
    @Override
    public Page<NoteListResponse> listSharedNotes(Long userId, NoteQueryRequest request) {
        List<NotePermission> perms = notePermissionMapper.selectList(
                new LambdaQueryWrapper<NotePermission>()
                        .eq(NotePermission::getTargetUserId, userId));
        if (perms.isEmpty()) {
            Page<NoteListResponse> emptyPage = new Page<>(request.getPageNum(), request.getPageSize(), 0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        List<Long> noteIds = perms.stream().map(NotePermission::getNoteId).collect(Collectors.toList());
        java.util.Map<Long, Integer> notePermMap = perms.stream().collect(
                java.util.stream.Collectors.toMap(NotePermission::getNoteId, NotePermission::getPermType, (a, b) -> b));

        Page<Note> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .in(Note::getId, noteIds)
                .like(request.getKeyword() != null, Note::getTitle, request.getKeyword())
                .orderByDesc(Note::getLastEditTime);

        Page<Note> notePage = noteMapper.selectPage(page, wrapper);

        Page<NoteListResponse> result = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        result.setRecords(notePage.getRecords().stream().map(note -> {
            NoteListResponse resp = toListResponse(note);
            Integer permType = notePermMap.get(note.getId());
            resp.setCanEdit(permType != null && permType == Constants.PERM_TYPE_EDIT);
            return resp;
        }).collect(Collectors.toList()));
        return result;
    }

    // 检查用户是否有笔记的权限记录
    private boolean checkPermission(Long noteId, Long userId) {
        NotePermission perm = notePermissionMapper.selectOne(
                new LambdaQueryWrapper<NotePermission>()
                        .eq(NotePermission::getNoteId, noteId)
                        .eq(NotePermission::getTargetUserId, userId));
        return perm != null;
    }

    // 记录浏览历史
    private void recordViewHistory(Long userId, Long noteId) {
        try {
            NoteHistory history = new NoteHistory();
            history.setUserId(userId);
            history.setNoteId(noteId);
            noteHistoryMapper.insert(history);
        } catch (Exception e) {
            log.debug("浏览历史记录已存在，跳过: userId={}, noteId={}", userId, noteId);
        }
    }

    // 保存笔记-标签关联关系
    private void saveTagRelations(Long noteId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            NoteTagRel rel = new NoteTagRel();
            rel.setNoteId(noteId);
            rel.setTagId(tagId);
            noteTagRelMapper.insertIgnore(rel);
        }
    }

    // Note → 列表响应DTO
    private NoteListResponse toListResponse(Note note) {
        NoteListResponse resp = new NoteListResponse();
        resp.setId(note.getId());
        resp.setTitle(note.getTitle());
        resp.setContentPreview(note.getContent() != null && note.getContent().length() > 100
                ? note.getContent().substring(0, 100) + "..."
                : note.getContent());
        resp.setPermissionType(note.getPermissionType());
        resp.setLastEditTime(note.getLastEditTime());
        resp.setCreateTime(note.getCreateTime());
        resp.setTags(getTagsForNote(note.getId()));
        return resp;
    }

    // Note → 详情响应DTO（含AI分析结果）
    private NoteDetailResponse toDetailResponse(Note note, boolean canEdit) {
        NoteDetailResponse resp = new NoteDetailResponse();
        resp.setId(note.getId());
        resp.setTitle(note.getTitle());
        resp.setContent(note.getContent());
        resp.setPermissionType(note.getPermissionType());
        resp.setLastEditTime(note.getLastEditTime());
        resp.setCreateTime(note.getCreateTime());
        resp.setCanEdit(canEdit);
        try {
            List<NotePermission> permList = notePermissionMapper.selectList(
                    new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, note.getId()));
            resp.setTargetUserIds(permList.stream().map(NotePermission::getTargetUserId).collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("查询笔记权限用户失败: noteId={}, error={}", note.getId(), e.getMessage());
        }
        try {
            resp.setTags(getTagsForNote(note.getId()).stream().map(t -> {
                NoteDetailResponse.TagVO tagVO = new NoteDetailResponse.TagVO();
                tagVO.setId(t.getId());
                tagVO.setName(t.getName());
                return tagVO;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("查询笔记标签失败: noteId={}, error={}", note.getId(), e.getMessage());
        }

        try {
            AiAnalysis analysis = aiAnalysisMapper.selectOne(
                    new LambdaQueryWrapper<AiAnalysis>().eq(AiAnalysis::getNoteId, note.getId()));
            if (analysis != null) {
                NoteDetailResponse.AiAnalysisVO aiVO = new NoteDetailResponse.AiAnalysisVO();
                aiVO.setId(analysis.getId());
                aiVO.setSummary(analysis.getSummary());
                aiVO.setKeyPoints(analysis.getKeyPoints());
                aiVO.setSuggestedTags(analysis.getSuggestedTags());
                aiVO.setAnalysisTime(analysis.getAnalysisTime());
                resp.setAiAnalysis(aiVO);
            }
        } catch (Exception e) {
            log.warn("查询AI分析结果失败: noteId={}, error={}", note.getId(), e.getMessage());
        }

        return resp;
    }

    // 查询笔记关联的标签列表
    private List<NoteListResponse.TagVO> getTagsForNote(Long noteId) {
        List<NoteTagRel> rels = noteTagRelMapper.selectByNoteId(noteId);
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = rels.stream().map(NoteTagRel::getTagId).collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream().map(t -> {
            NoteListResponse.TagVO vo = new NoteListResponse.TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            return vo;
        }).collect(Collectors.toList());
    }
}
