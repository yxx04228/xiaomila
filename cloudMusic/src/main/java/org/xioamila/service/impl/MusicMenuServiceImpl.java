package org.xioamila.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xioamila.common.config.JwtProperties;
import org.xioamila.common.utils.SecurityUtil;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenu;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.mapper.MusicMenuMapper;
import org.xioamila.mapper.MusicMenuRelationMapper;
import org.xioamila.service.MusicMenuService;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MusicMenuServiceImpl extends ServiceImpl<MusicMenuMapper, MusicMenu> implements MusicMenuService {

    private MusicMenuRelationMapper musicMenuRelationMapper;

    @Override
    public Page<MusicMenu> getPageList(Page<MusicMenu> page, MusicMenu musicMenu) {
        LambdaQueryWrapper<MusicMenu> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(musicMenu.getTitle())) {
            queryWrapper.like(MusicMenu::getTitle, musicMenu.getTitle());
        }
        queryWrapper.eq(MusicMenu::getCreateBy, SecurityUtil.getUserId()).orderByDesc(MusicMenu::getCreateTime);
        return this.page(page, queryWrapper);
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
