package org.xioamila.common.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xioamila.common.config.JwtProperties;
import org.xioamila.common.context.UserContext;
import org.xioamila.entity.User;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityUtil {

    private static JwtProperties jwtProperties;
    private static SecretKey key;

    @Autowired
    private JwtProperties tempJwtProperties;

    // 常量定义
    public static final String USERID = "userId";
    public static final String USERNAME = "username";
    public static final String NICKNAME = "nickName";

    @PostConstruct
    public void init() {
        jwtProperties = this.tempJwtProperties;
        // 初始化密钥
        key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public SecurityUtil() {
    }

    /**
     * 创建JWT Token
     */
    public static TokenInfo createJWT(Map<String, String> claims) {
        if (key == null) {
            throw new IllegalStateException("JWT密钥未初始化，请检查配置");
        }

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + jwtProperties.getExpiration());

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setExpire(expiryDate.getTime());

        return tokenInfo;
    }

    /**
     * 根据User对象创建Token
     */
    public static TokenInfo createJWT(User user) {
        Map<String, String> claims = new HashMap<>();
        claims.put(USERID, user.getId().toString());
        claims.put(USERNAME, user.getUsername());
        claims.put(NICKNAME, user.getNickname());

        return createJWT(claims);
    }

    /**
     * 解析JWT Token
     */
    public static Map<String, Object> parseJWT(String token) {
        if (key == null) {
            throw new IllegalStateException("JWT密钥未初始化，请检查配置");
        }

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token创建User对象
     */
    public static User createUserFromToken(String token) {
        Map<String, Object> claims = parseJWT(token);

        User user = new User();
        user.setId(String.valueOf(claims.get(USERID).toString()));
        user.setUsername(claims.get(USERNAME).toString());
        user.setNickname(claims.get(NICKNAME).toString());

        return user;
    }

    /**
     * 获取当前用户ID（无需参数）
     */
    public static String getUserId() {
        User user = UserContext.getUser();
        return user != null ? user.getId().toString() : null;
    }

    /**
     * 获取当前用户名（无需参数）
     */
    public static String getUsername() {
        User user = UserContext.getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户昵称（无需参数）
     */
    public static String getNickname() {
        User user = UserContext.getUser();
        return user != null ? user.getNickname() : null;
    }

    /**
     * 获取当前用户对象（无需参数）
     */
    public static User getCurrentUser() {
        return UserContext.getUser();
    }

    /**
     * 验证Token是否有效
     */
    public static void validateToken(String token) {
        try {
            parseJWT(token);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token格式错误", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Token签名验证失败", e);
        } catch (Exception e) {
            throw new RuntimeException("Token验证失败", e);
        }
    }

    /**
     * 获取JWT配置
     */
    public static JwtProperties getJwtProperties() {
        return jwtProperties;
    }
}