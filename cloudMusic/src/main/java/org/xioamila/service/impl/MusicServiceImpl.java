package org.xioamila.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greatmap.modules.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;
import org.xioamila.mapper.MusicMapper;
import org.xioamila.service.MusicService;
import org.xioamila.common.utils.FileParseUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {

    @Value("${music.file.path}")
    private String musicFilePath;

    @Value("${cover.file.path}")
    private String coverFilePath;

    private final MusicMapper musicMapper;

    @Override
    public Page<Music> getPageList(Page<Music> page, Music music) {
        return musicMapper.getPageList(page, music);
    }

    @Transactional
    @Override
    public String uploadMusic(MultipartFile file) {
        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                throw new ServiceException("上传失败：文件不能为空");
            }

            // 验证文件类型
            if (!FileParseUtil.isAudioFile(file)) {
                throw new ServiceException("上传失败：文件不是有效的音频文件");
            }

            // 解析文件信息
            Map<String, Object> fileInfo = FileParseUtil.parseMusicFile(file);
            String fileExtension = (String) fileInfo.get("fileExtension");
            String formattedFileSize = (String) fileInfo.get("fileSize");
            String duration = (String) fileInfo.get("duration");
            String title = (String) fileInfo.get("title");
            String singer = (String) fileInfo.get("singer");
            String album = (String) fileInfo.get("album"); // 专辑名称
            byte[] coverImage = (byte[]) fileInfo.get("coverImage"); // 封面图片数据

            String fileName = singer + " - " + title;

            // 验证是否已存在该歌曲（title + singer）
            LambdaQueryWrapper<Music> queryWrapper = Wrappers.lambdaQuery(Music.class)
                    .eq(Music::getTitle, title)
                    .eq(Music::getSinger, singer);

            long count = musicMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new ServiceException("歌曲已存在：" + singer + " - " + title);
            }

            // 保存音乐文件
            // 获取项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteMusicPath = new File(projectRoot, musicFilePath).getAbsolutePath();
            // 生成唯一文件名
            String filePath = UUID.randomUUID().toString() + "." + fileExtension;
            // 保存文件
            File saveFile = new File(absoluteMusicPath + File.separator + filePath);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            file.transferTo(saveFile);

            // 保存封面图片
            String coverUrl = null;
            if (coverImage != null && coverImage.length > 0) {
                // 封面保存目录
                String absoluteCoverPath = new File(projectRoot, coverFilePath).getAbsolutePath();
                // 创建目录
                File coverDirectory = new File(absoluteCoverPath);
                if (!coverDirectory.exists()) {
                    coverDirectory.mkdirs();
                }
                // 生成唯一文件名
                String coverFileName = UUID.randomUUID().toString() + ".jpg";
                // 生成文件路径
                String absoluteCoverFilePath = absoluteCoverPath + File.separator + coverFileName;

                // 保存封面文件
                try (FileOutputStream fos = new FileOutputStream(absoluteCoverFilePath)){
                    fos.write(coverImage);
                }

                coverUrl = coverFileName;
            }

            // 保存音乐信息到数据库
            Music music = new Music();
            music.setTitle(title);
            music.setSinger(singer);
            music.setAlbum(album); // 设置专辑名称
            music.setDuration(duration);
            music.setFileType(fileExtension);
            music.setFilePath(filePath);
            music.setFileName(fileName);
            music.setFileSize(formattedFileSize);
            music.setCoverUrl(coverUrl); // 设置封面路径
            music.setPlayCount(0);
            music.setDownloadCount(0);

            boolean saveResult = this.save(music);

            if (saveResult) {
                return "音乐文件上传成功";
            } else {
                // 如果数据库保存失败，删除已上传的文件
                if (saveFile.exists()) {
                    saveFile.delete();
                }
                throw new ServiceException("上传失败：数据库保存失败");
            }

        } catch (ServiceException e) {
            throw e; // 业务异常直接抛出
        } catch (IOException e) {
            log.error("上传音乐文件失败", e);
            throw new ServiceException("上传失败：文件保存异常");
        } catch (Exception e) {
            log.error("上传音乐文件发生未知错误", e);
            throw new ServiceException("上传失败：系统异常");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadMusic(String id) {
        try {
            // 查询音乐信息
            Music music = this.getById(id);
            if (music == null) {
                log.warn("音乐文件不存在, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 验证文件路径
            String filePath = music.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("音乐文件路径为空, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 获取文件的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteMusicPath = new File(projectRoot, musicFilePath).getAbsolutePath();
            filePath = absoluteMusicPath + File.separator + filePath;

            // 创建文件对象并验证
            File file = new File(filePath);
            if (!FileParseUtil.isAudioFile(file)) {
                log.warn("音乐文件无效, path: {}, id: {}", filePath, id);
                return ResponseEntity.notFound().build();
            }

            // 生成安全的文件名
            String fileName = FileParseUtil.generateSafeFileName(
                    music.getTitle(),
                    music.getSinger(),
                    music.getFileType()
            );

            log.info("准备下载文件: {} -> {}", filePath, fileName);

            // 创建下载响应
            return FileParseUtil.createDownloadResponse(file, fileName);

        } catch (Exception e) {
            log.error("下载音乐文件失败, id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Resource> playMusic(String id, HttpServletRequest request) {
        try {
            // 查询音乐信息
            Music music = this.getById(id);
            if (music == null) {
                log.warn("音乐文件不存在, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 验证文件路径
            String filePath = music.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("音乐文件路径为空, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 获取文件的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteMusicPath = new File(projectRoot, musicFilePath).getAbsolutePath();
            filePath = absoluteMusicPath + File.separator + filePath;

            // 创建文件对象并验证
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                log.warn("音乐文件不存在或不是文件, path: {}, id: {}", filePath, id);
                return ResponseEntity.notFound().build();
            }

            log.info("准备播放音乐: {} -> {}", music.getTitle(), filePath);

            // 使用工具类创建播放响应
            return FileParseUtil.createPlayResponse(file, music, request);

        } catch (Exception e) {
            log.error("播放音乐文件失败, id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    @Transactional
    public boolean updateMusic(Music music) {
        // 验证是否已存在该歌曲（title + singer）
        LambdaQueryWrapper<Music> queryWrapper = Wrappers.lambdaQuery(Music.class)
                .eq(Music::getTitle, music.getTitle())
                .eq(Music::getSinger, music.getSinger())
                .ne(Music::getId, music.getId());

        long count = musicMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("歌曲已存在：" + music.getSinger() + " - " + music.getTitle());
        }

        return this.updateById(music);
    }

    @Override
    @Transactional
    public boolean deleteMusic(String id) {
        // 查询音乐信息，获取文件路径
        Music music = this.getById(id);
        if (music == null) {
            throw new ServiceException("音乐文件不存在");
        }

        try {
            // 删除音乐文件
            deleteMusicFile(music.getFilePath());
            // 删除音乐封面文件
            deleteCoverFile(music.getCoverUrl());

            // 删除数据库记录
            return this.removeById(id);

        } catch (ServiceException e) {
            log.error("删除音乐失败 - ID: {}, 错误: {}", id, e.getMessage());
            throw e; // 重新抛出，触发事务回滚
        }
    }

    /**
     * 删除音乐文件 - 失败时抛出异常，触发事务回滚
     */
    private void deleteMusicFile(String filePath) throws ServiceException {
        try {
            if (StringUtils.isBlank(filePath)) {
                return; // 没有文件路径，直接返回
            }

            // 获取项目的绝对基础路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteMusicPath = new File(projectRoot, musicFilePath).getAbsolutePath();
            Path basePath = Paths.get(absoluteMusicPath).toAbsolutePath().normalize();

            // 构建完整文件路径
            Path fileFullPath = basePath.resolve(filePath).normalize();

            // 路径安全检查 - 修正：比较两个绝对路径
            if (!fileFullPath.startsWith(basePath)) {
                log.error("非法文件路径尝试: basePath={}, filePath={}", basePath, fileFullPath);
                throw new ServiceException("非法文件路径: " + filePath);
            }

            // 检查文件是否存在且是普通文件
            if (Files.exists(fileFullPath) && !Files.isRegularFile(fileFullPath)) {
                throw new ServiceException("目标路径不是普通文件: " + filePath);
            }

            boolean deleted = Files.deleteIfExists(fileFullPath);
            if (!deleted) {
                log.warn("音乐文件不存在: {}", fileFullPath);
            }
            log.info("音乐文件删除成功: {}", fileFullPath);

        } catch (IOException e) {
            log.error("删除音乐文件失败: {}", filePath, e);
            throw new ServiceException("删除音乐文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除音乐封面文件 - 失败时抛出异常，触发事务回滚
     */
    private void deleteCoverFile(String filePath) throws ServiceException {
        try {
            if (StringUtils.isBlank(filePath)) {
                return; // 没有文件路径，直接返回
            }

            // 获取项目的绝对基础路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteCoverPath = new File(projectRoot, coverFilePath).getAbsolutePath();
            Path basePath = Paths.get(absoluteCoverPath).toAbsolutePath().normalize();

            // 构建完整文件路径
            Path fileFullPath = basePath.resolve(filePath).normalize();

            // 路径安全检查 - 修正：比较两个绝对路径
            if (!fileFullPath.startsWith(basePath)) {
                log.error("非法文件路径尝试: basePath={}, filePath={}", basePath, fileFullPath);
                throw new ServiceException("非法文件路径: " + filePath);
            }

            // 检查文件是否存在且是普通文件
            if (Files.exists(fileFullPath) && !Files.isRegularFile(fileFullPath)) {
                throw new ServiceException("目标路径不是普通文件: " + filePath);
            }

            boolean deleted = Files.deleteIfExists(fileFullPath);
            if (!deleted) {
                log.warn("音乐封面文件不存在: {}", fileFullPath);
            }
            log.info("音乐封面文件删除成功: {}", fileFullPath);

        } catch (IOException e) {
            log.error("删除音乐封面文件失败: {}", filePath, e);
            throw new ServiceException("删除音乐封面文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Resource> getCoverById(String id) {
        try {
            // 查询音乐信息
            Music music = this.getById(id);
            if (music == null) {
                log.warn("音乐文件不存在, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 验证文件路径
            String filePath = music.getCoverUrl();
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("音乐封面文件路径为空, id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 获取封面文件的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String absoluteCoverPath = new File(projectRoot, coverFilePath).getAbsolutePath();
            filePath = absoluteCoverPath + File.separator + filePath;

            // 创建文件对象并验证
            File coverFile = new File(filePath);

            if (!coverFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(coverFile);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // 根据实际格式调整

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("获取音乐封面文件失败, id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
