package org.xioamila.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.modules.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xioamila.entity.Music;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.mapper.MusicMenuRelationMapper;
import org.xioamila.service.MusicMenuRelationService;
import org.xioamila.vo.MenuMusicVo;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MusicMenuRelationServiceImpl extends ServiceImpl<MusicMenuRelationMapper, MusicMenuRelation> implements MusicMenuRelationService {

    private MusicMenuRelationMapper musicMenuRelationMapper;

    @Override
    public Page<MenuMusicVo> listMusic(Page page, String menuId, Music music) {
        return musicMenuRelationMapper.listMusic(page, menuId, music);
    }

    @Override
    @Transactional
    public boolean addMusic(MusicMenuRelation musicMenuRelation) {
        long count = this.count(new LambdaQueryWrapper<MusicMenuRelation>()
                .eq(MusicMenuRelation::getMenuId, musicMenuRelation.getMenuId())
                .eq(MusicMenuRelation::getMusicId, musicMenuRelation.getMusicId()));
        if (count > 0) {
            throw new ServiceException("已存在该歌曲");
        }

        long musicNum = this.count(new LambdaQueryWrapper<MusicMenuRelation>()
                .eq(MusicMenuRelation::getMenuId, musicMenuRelation.getMenuId()));
        if (musicNum > 0) {
            // 插入添加的音乐
            musicMenuRelation.setPosition(String.valueOf(musicNum));
            this.save(musicMenuRelation);

            String start = musicMenuRelation.getPosition();
            String end = "0";
            // 更新关系表中的排序
            musicMenuRelationMapper.updatePosition(musicMenuRelation.getMenuId(), start, end);
        } else {
            // 插入添加的音乐
            musicMenuRelation.setPosition("0");
            this.save(musicMenuRelation);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean deleteMusic(String menuId, List<String> ids) {
        // 先删除
        this.removeBatchByIds(ids);

        musicMenuRelationMapper.updateBatchPosition(menuId);

        return true;
    }

    @Override
    @Transactional
    public boolean moveMusic(String menuId, String start, String end) {
        musicMenuRelationMapper.updatePosition(menuId, start, end);

        return true;
    }
}
