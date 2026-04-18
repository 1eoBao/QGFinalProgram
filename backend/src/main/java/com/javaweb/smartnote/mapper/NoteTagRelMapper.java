package com.javaweb.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaweb.smartnote.entity.NoteTagRel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteTagRelMapper extends BaseMapper<NoteTagRel> {

    @Insert("INSERT IGNORE INTO note_tag_rel (note_id, tag_id) VALUES (#{noteId}, #{tagId})")
    int insertIgnore(NoteTagRel rel);

    @Select("SELECT note_id, tag_id FROM note_tag_rel WHERE note_id = #{noteId}")
    List<NoteTagRel> selectByNoteId(@Param("noteId") Long noteId);

    @Select("DELETE FROM note_tag_rel WHERE note_id = #{noteId}")
    int deleteByNoteId(@Param("noteId") Long noteId);
}
