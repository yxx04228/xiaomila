package org.xioamila.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;

import javax.servlet.http.HttpServletRequest;

public interface MusicService extends IService<Music> {

    /**
     * 音乐列表
     */
    Page<Music> getPageList(Page<Music> page, Music music);

    /**
     * 上传音乐文件
     */
    String uploadMusic(MultipartFile file);

    /**
     * 下载音乐文件
     */
    ResponseEntity<Resource> downloadMusic(String id);

    /**
     * 播放音乐文件
     */
    ResponseEntity<Resource> playMusic(String id, HttpServletRequest request);

    /**
     * 更新音乐信息
     */
    boolean updateMusic(Music music);

    /**
     * 删除音乐
     */
    boolean deleteMusic(String id);
}
