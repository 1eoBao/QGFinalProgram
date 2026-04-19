package com.javaweb.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaweb.smartnote.common.BusinessException;
import com.javaweb.smartnote.common.ResultCodeEnum;
import com.javaweb.smartnote.dto.response.NoteDetailResponse;
import com.javaweb.smartnote.entity.AiAnalysis;
import com.javaweb.smartnote.entity.Note;
import com.javaweb.smartnote.mapper.AiAnalysisMapper;
import com.javaweb.smartnote.mapper.NoteMapper;
import com.javaweb.smartnote.service.AiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiAnalysisMapper aiAnalysisMapper;
    private final NoteMapper noteMapper;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${ai.timeout:60000}")
    private long aiTimeout;

    private static final String SYSTEM_PROMPT = """
            你是一个智能笔记助手。用户会给你一段笔记内容，请你按以下格式进行分析：
            
            【摘要】
            用一段话概括笔记的核心内容（不超过200字），不要以第三人称叙述
            
            【要点】
            列出3-5个关键要点，每个要点一行，用数字编号
            
            【建议标签】
            建议2-3个标签，用逗号分隔
            
            请严格按照上述格式输出。
            """;

    @Override
    public NoteDetailResponse.AiAnalysisVO analyzeNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "只有笔记所有者才能触发AI分析");
        }

        String userMessage = "请分析以下笔记内容：\n\n" + note.getContent();

        String aiResponse;
        try {
            log.info("开始AI分析: noteId={}", noteId);
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            aiResponse = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();
            
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "AI分析返回结果为空，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI分析调用失败: noteId={}", noteId, e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("timeout")) {
                throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "AI分析超时，请稍后再试");
            }
            if (errorMsg != null && errorMsg.contains("connection")) {
                throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "AI服务连接失败，请检查网络或稍后再试");
            }
            throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "AI分析服务暂时不可用，请稍后再试");
        }

        log.info("AI分析完成: noteId={}, responseLength={}", noteId, aiResponse.length());

        String summary = extractSection(aiResponse, "摘要");
        String keyPointsJson = toJsonArray(extractKeyPoints(aiResponse));
        String suggestedTagsJson = toJsonArray(extractSuggestedTags(aiResponse));

        AiAnalysis existing = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>().eq(AiAnalysis::getNoteId, noteId));

        AiAnalysis analysis;
        if (existing != null) {
            AiAnalysis update = new AiAnalysis();
            update.setId(existing.getId());
            update.setSummary(summary);
            update.setKeyPoints(keyPointsJson);
            update.setSuggestedTags(suggestedTagsJson);
            aiAnalysisMapper.updateById(update);
            analysis = update;
            analysis.setUserId(existing.getUserId());
            analysis.setAnalysisTime(existing.getAnalysisTime());
        } else {
            analysis = new AiAnalysis();
            analysis.setNoteId(noteId);
            analysis.setUserId(userId);
            analysis.setSummary(summary);
            analysis.setKeyPoints(keyPointsJson);
            analysis.setSuggestedTags(suggestedTagsJson);
            aiAnalysisMapper.insert(analysis);
        }

        return toVO(analysis);
    }

    @Override
    public NoteDetailResponse.AiAnalysisVO getAnalysis(Long noteId) {
        AiAnalysis analysis = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>().eq(AiAnalysis::getNoteId, noteId));
        if (analysis == null) {
            return null;
        }
        return toVO(analysis);
    }

    private String extractSection(String response, String sectionName) {
        if (response == null) {
            return "";
        }
        String startMarker = "【" + sectionName + "】";
        int start = response.indexOf(startMarker);
        if (start == -1) {
            return "";
        }
        start += startMarker.length();

        int end = response.length();
        for (String marker : new String[]{"【摘要】", "【要点】", "【建议标签】"}) {
            if (!marker.equals(startMarker)) {
                int idx = response.indexOf(marker, start);
                if (idx != -1 && idx < end) {
                    end = idx;
                }
            }
        }

        return response.substring(start, end).trim();
    }

    private List<String> extractKeyPoints(String response) {
        String raw = extractSection(response, "要点");
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split("\n"))
                .map(line -> line.replaceAll("^\\d+[.、)\\s]+", "").trim())
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    // 确保字符串是有效的JSON字符串（转义特殊字符）
                    return line.replace("\"", "\\\"")
                            .replace("\\", "\\\\")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t");
                })
                .collect(Collectors.toList());
    }

    private List<String> extractSuggestedTags(String response) {
        String raw = extractSection(response, "建议标签");
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split("[,，、\\s]+"))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> {
                    // 确保字符串是有效的JSON字符串（转义特殊字符）
                    return tag.replace("\"", "\\\"")
                            .replace("\\", "\\\\")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t");
                })
                .collect(Collectors.toList());
    }

    private String toJsonArray(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败", e);
            return "[]";
        }
    }

    private NoteDetailResponse.AiAnalysisVO toVO(AiAnalysis analysis) {
        NoteDetailResponse.AiAnalysisVO vo = new NoteDetailResponse.AiAnalysisVO();
        vo.setId(analysis.getId());
        vo.setSummary(analysis.getSummary());
        vo.setKeyPoints(analysis.getKeyPoints());
        vo.setSuggestedTags(analysis.getSuggestedTags());
        vo.setAnalysisTime(analysis.getAnalysisTime());
        return vo;
    }
}
