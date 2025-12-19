package org.xioamila.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xioamila.entity.MusicMenu;
import org.xioamila.vo.MenuVo;

import java.util.List;

public interface MusicMenuService extends IService<MusicMenu> {

    /**
     * 菜单列表
     */
    Page<MenuVo> getPageList(Page page, MusicMenu musicMenu);

    /**
     * 删除菜单
     */
    boolean deleteMenu(List<String> ids);

}
