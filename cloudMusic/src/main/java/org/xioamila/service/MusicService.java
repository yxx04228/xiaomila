package org.xioamila.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;
import org.xioamila.vo.Result;

public interface MusicService extends IService<Music> {

    /**
     * 上传音乐文件
     */
    Result<String> uploadMusic(MultipartFile file, String title, String singer, String album);

    /**
     * 下载音乐文件
     */
    ResponseEntity<Resource> downloadMusic(String id);
}
