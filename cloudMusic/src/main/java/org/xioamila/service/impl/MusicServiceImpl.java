package org.xioamila.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;
import org.xioamila.mapper.MusicMapper;
import org.xioamila.service.MusicService;
import org.xioamila.common.utils.FileParseUtil;
import org.xioamila.vo.Result;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {

    @Value("${music.file.path}")
    private String musicFilePath;

    @Transactional
    @Override
    public Result<String> uploadMusic(MultipartFile file, String title, String singer, String album) {
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return Result.error("上传失败：文件不能为空");
            }

            // 验证文件类型
            if (!FileParseUtil.isAudioFile(file)) {
                return Result.error("上传失败：文件不是有效的音频文件");
            }

            // 解析文件信息
            Map<String, Object> fileInfo = FileParseUtil.parseMusicFile(file);
            String fileExtension = (String) fileInfo.get("fileExtension");
            String formattedFileSize = (String) fileInfo.get("fileSize");
            String duration = (String) fileInfo.get("duration");

            // 获取项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteMusicPath = new File(projectRoot, musicFilePath).getAbsolutePath();

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;

            // 保存文件
            File saveFile = new File(absoluteMusicPath + File.separator + fileName);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            file.transferTo(saveFile);

            // 保存音乐信息到数据库
            Music music = new Music();
            music.setTitle(title);
            music.setSinger(singer);
            music.setAlbum(album);
            music.setDuration(duration);
            music.setFileType(fileExtension);
            music.setFilePath(saveFile.getAbsolutePath());
            music.setFileSize(formattedFileSize);
            music.setPlayCount(0);

            boolean saveResult = this.save(music);

            if (saveResult) {
                return Result.success("音乐文件上传成功");
            } else {
                // 如果数据库保存失败，删除已上传的文件
                if (saveFile.exists()) {
                    saveFile.delete();
                }
                return Result.error("上传失败：数据库保存失败");
            }

        } catch (IOException e) {
            log.error("上传音乐文件失败", e);
            return Result.error("上传失败：文件保存异常");
        } catch (Exception e) {
            log.error("上传音乐文件发生未知错误", e);
            return Result.error("上传失败：系统异常");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadMusic(String id) {
        try {
            // 1. 查询音乐信息
            Music music = this.getById(id);
            if (music == null) {
                log.warn("音乐文件不存在, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 2. 验证文件路径
            String filePath = music.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("音乐文件路径为空, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 3. 创建文件对象并验证
            File file = new File(filePath);
            if (!FileParseUtil.isAudioFile(file)) {
                log.warn("音乐文件无效, path: {}, id: {}", filePath, id);
                return ResponseEntity.notFound().build();
            }

            // 4. 生成安全的文件名
            String fileName = FileParseUtil.generateSafeFileName(
                    music.getTitle(),
                    music.getSinger(),
                    music.getFileType()
            );

            log.info("准备下载文件: {} -> {}", filePath, fileName);

            // 5. 创建下载响应
            return FileParseUtil.createDownloadResponse(file, fileName);

        } catch (Exception e) {
            log.error("下载音乐文件失败, id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
