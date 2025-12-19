package org.xioamila.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.vo.MenuMusicVo;

public interface MusicMenuRelationMapper extends BaseMapper<MusicMenuRelation>{

    /**
     * 查询歌单中的音乐列表
     * @param page
     * @param menuId
     * @param music
     * @return
     */
    Page<MenuMusicVo> listMusic(Page page, @Param("menuId") String menuId, @Param("music") Music music);

    /**
     * 修改歌曲位置
     * @param menuId  歌曲所在菜单id
     * @param start   歌曲移动前的位置
     * @param end     歌曲移动后的位置
     * @return
     */
    @Update("<script>" +
            "UPDATE music_menu_relation " +
            "SET position = CASE " +
            "   WHEN position = #{start} THEN #{end} " +
            "   WHEN #{start} &gt; #{end} THEN position + 1 " +
            "   WHEN #{start} &lt; #{end} THEN position - 1 " +
            "   ELSE position " +
            "END " +
            "WHERE deleted = '0' AND menu_id = #{menuId} " +
            "AND position BETWEEN LEAST(#{start}, #{end}) AND GREATEST(#{start}, #{end}) " +
            "</script>")
    int updatePosition(@Param("menuId") String menuId, @Param("start") String start, @Param("end") String end);

    /**
     * 调整菜单列表中所有音乐的排序
     * @param menuId
     * @return
     */
    @Update("<script>" +
            "UPDATE music_menu_relation t1 " +
            "SET t1.position = ( " +
            "   SELECT count(1) " +
            "   FROM ( " +
            "       SELECT * FROM music_menu_relation " +
            "       WHERE deleted = '0' AND menu_id = #{menuId} " +
            "   ) t2 " +
            "   WHERE t1.position > t2.position " +
            ") " +
            "WHERE t1.deleted = '0' AND t1.menu_id = #{menuId}" +
            "</script>")
    int updateBatchPosition(@Param("menuId") String menuId);

}
