package org.xioamila.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xioamila.entity.MusicMenu;
import org.xioamila.vo.MenuVo;

public interface MusicMenuMapper extends BaseMapper<MusicMenu>{

    /**
     * 查询用户的音乐菜单列表
     * @param page
     * @param musicMenu
     * @return
     */
    Page<MenuVo> getPageList(Page page, MusicMenu musicMenu);
}
