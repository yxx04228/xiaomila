package org.xioamila.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xioamila.entity.Music;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FileParseUtil {

    private static final Tika tika = new Tika();
    private static final DecimalFormat df = new DecimalFormat("#.##");  // 定义 df 变量
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 解析音乐文件信息
     */
    public static Map<String, Object> parseMusicFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        result.put("fileExtension", fileExtension);

        // 获取文件大小（字节）
        long fileSizeBytes = file.getSize();
        // 获取格式化后的文件大小
        String formattedFileSize = formatFileSize(fileSizeBytes);
        result.put("fileSize", formattedFileSize);

        // 解析文件时长
        String duration = parseDuration(file);
        result.put("duration", duration);

        // 根据文件类型设置MIME类型
        String mimeType = getMimeType(fileExtension);
        result.put("mimeType", mimeType);

        return result;
    }

    /**
     * 解析音频文件时长（支持FLAC、MP3、WAV等格式）
     * @param file 上传的音频文件（MultipartFile）
     * @return 格式化的时长字符串（分:秒，如"05:30"），解析失败返回"未知时长"
     */
    private static String parseDuration(MultipartFile file) {
        // 若文件为空，直接返回未知
        if (file.isEmpty()) {
            return "未知时长";
        }

        // 获取原始文件的扩展名（关键：用于临时文件命名）
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 包含"."，如".flac"
        }

        File tempFile = null;
        try {
            // 创建临时文件时，使用原始文件的扩展名（避免.tmp导致的格式识别失败）
            tempFile = File.createTempFile("audio_temp_", fileExtension);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // 读取音频文件信息
            AudioFile audioFile = AudioFileIO.read(tempFile);
            // 获取时长（单位：秒）
            int durationSeconds = audioFile.getAudioHeader().getTrackLength();

            // 格式化时长为 分:秒（补零处理，如1分5秒 -> 01:05）
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);

        } catch (CannotReadException e) {
            // 打印详细错误信息（包含不支持的原因）
            System.err.println("无法读取音频文件：" + e.getMessage());
            e.printStackTrace(); // 输出完整堆栈，查看具体不支持的原因
            return "不支持的音频格式";
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            // 其他异常（如IO错误、解析失败）
            e.printStackTrace();
            return "未知时长";
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                // 标记临时文件为删除（JVM退出时删除，避免占用磁盘）
                tempFile.deleteOnExit();
            }
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }

        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private static String getMimeType(String fileExtension) {
        switch (fileExtension) {
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "m4a":
                return "audio/mp4";
            case "flac":
                return "audio/flac";
            case "aac":
                return "audio/aac";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 验证文件是否为音频文件
     */
    public static boolean isAudioFile(MultipartFile file) {
        try {
            String fileType = tika.detect(file.getInputStream());
            return fileType != null && fileType.startsWith("audio/");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 生成安全的文件名（格式：歌手-标题.文件类型）
     * @param title 歌曲标题
     * @param singer 歌手名称
     * @param fileType 文件类型（如：mp3, flac等）
     * @return 安全的文件名
     */
    public static String generateSafeFileName(String title, String singer, String fileType) {
        // 处理空值
        if (title == null) title = "未知标题";
        if (singer == null) singer = "未知歌手";
        if (fileType == null) fileType = "mp3";

        // 清理文件名中的非法字符
        String cleanTitle = INVALID_FILENAME_CHARS.matcher(title).replaceAll("").trim();
        String cleanSinger = INVALID_FILENAME_CHARS.matcher(singer).replaceAll("").trim();

        // 如果清理后为空，使用默认值
        if (cleanTitle.isEmpty()) cleanTitle = "未知标题";
        if (cleanSinger.isEmpty()) cleanSinger = "未知歌手";

        // 构建文件名：歌手-标题.文件类型
        String fileName = String.format("%s-%s.%s", cleanSinger, cleanTitle, fileType.toLowerCase());

        // 限制文件名长度（避免过长文件名）
        if (fileName.length() > 255) {
            fileName = fileName.substring(0, 255);
        }

        return fileName;
    }

    /**
     * 创建文件下载响应
     * @param file 文件对象
     * @param fileName 下载时显示的文件名
     * @return ResponseEntity<Resource> 下载响应
     */
    public static ResponseEntity<Resource> createDownloadResponse(File file, String fileName) {
        try {
            // 验证文件是否存在且可读
            if (!file.exists() || !file.canRead()) {
                log.warn("文件不存在或不可读: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 创建文件资源
            FileSystemResource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()) + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            // 自动检测MIME类型
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            log.info("文件下载: {} -> {} (MIME: {})", file.getAbsolutePath(), fileName, mimeType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);

        } catch (Exception e) {
            log.error("创建下载响应失败: {}", file.getAbsolutePath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建音乐播放响应
     * @param file 音频文件对象
     * @param music 音乐信息（用于获取元数据）
     * @param request HTTP请求（用于处理Range请求）
     * @return ResponseEntity<Resource> 播放响应
     */
    public static ResponseEntity<Resource> createPlayResponse(File file, Music music, HttpServletRequest request) {
        try {
            // 验证文件是否存在且可读
            if (!file.exists() || !file.canRead()) {
                log.warn("文件不存在或不可读: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 创建文件资源
            FileSystemResource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();

            // 内嵌播放，而不是下载
            String fileName = generateSafeFileName(music.getTitle(), music.getSinger(), music.getFileType());
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()) + "\"");

            // 支持断点续传
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            // 缓存控制 - 允许缓存以提高性能
            headers.add(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
            headers.add(HttpHeaders.EXPIRES, String.valueOf(System.currentTimeMillis() + 3600000));

            // 获取音频文件的正确MIME类型
            String mimeType = getMimeType(music.getFileType());

            // 处理Range请求（支持断点续传）
            long fileLength = file.length();
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);

            if (StringUtils.hasText(rangeHeader)) {
                return handleRangeRequest(file, resource, headers, mimeType, fileLength, rangeHeader);
            }

            log.info("音乐播放: {} -> {} (MIME: {}, Size: {})",
                    file.getAbsolutePath(), fileName, mimeType, formatFileSize(fileLength));

            // 完整文件响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileLength)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);

        } catch (Exception e) {
            log.error("创建播放响应失败: {}", file.getAbsolutePath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 处理Range请求（断点续传）
     */
    private static ResponseEntity<Resource> handleRangeRequest(File file, FileSystemResource resource,
                                                               HttpHeaders headers, String mimeType,
                                                               long fileLength, String rangeHeader) {
        try {
            // 解析Range头
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.size() != 1) {
                log.warn("不支持的多范围请求: {}", rangeHeader);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }

            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(fileLength);
            long end = range.getRangeEnd(fileLength);
            long rangeLength = end - start + 1;

            // 验证范围有效性
            if (start >= fileLength || end >= fileLength || start > end) {
                log.warn("无效的范围请求: {}-{}/{}", start, end, fileLength);
                headers.add(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .headers(headers)
                        .build();
            }

            // 设置范围响应头
            headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
            headers.setContentLength(rangeLength);

            log.info("处理范围请求: {}-{}/{} ({}%)",
                    start, end, fileLength, (rangeLength * 100 / fileLength));

            // 创建范围资源（需要自定义Resource实现来支持范围读取）
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(createRangeResource(file, start, rangeLength));

        } catch (IllegalArgumentException e) {
            log.warn("无效的Range头: {}", rangeHeader, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 创建支持范围读取的资源
     */
    private static Resource createRangeResource(File file, long start, long length) {
        return new AbstractResource() {
            @Override
            public String getDescription() {
                return "Range resource for file: " + file.getName();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                randomAccessFile.seek(start);

                return new InputStream() {
                    private long bytesRead = 0;

                    @Override
                    public int read() throws IOException {
                        if (bytesRead >= length) {
                            return -1;
                        }
                        int result = randomAccessFile.read();
                        if (result != -1) {
                            bytesRead++;
                        }
                        return result;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        if (bytesRead >= length) {
                            return -1;
                        }
                        int maxRead = (int) Math.min(len, length - bytesRead);
                        int read = randomAccessFile.read(b, off, maxRead);
                        if (read != -1) {
                            bytesRead += read;
                        }
                        return read;
                    }

                    @Override
                    public void close() throws IOException {
                        randomAccessFile.close();
                    }
                };
            }

            @Override
            public long contentLength() {
                return length;
            }

            @Override
            public String getFilename() {
                return file.getName();
            }
        };
    }

    /**
     * 验证文件是否为音频文件（重载方法，支持File类型）
     */
    public static boolean isAudioFile(File file) {
        try {
            String fileType = tika.detect(file);
            return fileType != null && fileType.startsWith("audio/");
        } catch (IOException e) {
            log.error("检测文件类型失败: {}", file.getAbsolutePath(), e);
            return false;
        }
    }
}