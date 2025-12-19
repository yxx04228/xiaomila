package org.xioamila.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xioamila.entity.MusicMenu;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.mapper.MusicMenuMapper;
import org.xioamila.mapper.MusicMenuRelationMapper;
import org.xioamila.service.MusicMenuService;
import org.xioamila.vo.MenuVo;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MusicMenuServiceImpl extends ServiceImpl<MusicMenuMapper, MusicMenu> implements MusicMenuService {

    private MusicMenuRelationMapper musicMenuRelationMapper;

    private MusicMenuMapper musicMenuMapper;

    @Override
    public Page<MenuVo> getPageList(Page page, MusicMenu musicMenu) {
        return musicMenuMapper.getPageList(page, musicMenu);
    }

    @Override
    @Transactional
    public boolean deleteMenu(List<String> ids) {
        if (CollectionUtil.isNotEmpty(ids)){
            // 删除菜单音乐关系
            musicMenuRelationMapper.delete(new LambdaQueryWrapper<MusicMenuRelation>().in(MusicMenuRelation::getMenuId, ids));
            // 删除菜单
            this.removeBatchByIds(ids);
        }

        return true;
    }

}
