package org.xioamila.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;
import org.xioamila.vo.Result;

import javax.servlet.http.HttpServletRequest;

public interface MusicService extends IService<Music> {

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
}
