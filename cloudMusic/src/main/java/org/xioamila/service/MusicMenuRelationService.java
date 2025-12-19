package org.xioamila.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.vo.MenuMusicVo;

import java.util.List;

public interface MusicMenuRelationService extends IService<MusicMenuRelation> {

    /**
     * 菜单音乐列表
     */
    Page<MenuMusicVo> listMusic(Page page, String menuId, Music music);

    /**
     * 添加音乐
     */
    boolean addMusic(MusicMenuRelation musicMenuRelation);

    /**
     * 删除音乐
     */
    boolean deleteMusic(String menuId, List<String> ids);

    /**
     * 调整音乐顺序
     */
    boolean moveMusic(String menuId, String start, String end);

}
