package org.xioamila.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.xioamila.entity.Music;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface MusicMapper extends BaseMapper<Music>{

    Page<Music> getPageList(Page<Music> page, @Param("music") Music music);

}
