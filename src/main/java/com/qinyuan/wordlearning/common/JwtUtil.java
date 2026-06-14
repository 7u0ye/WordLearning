package com.qinyuan.wordlearning.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    //生成token
    public  String generateToken(Long userId,String username){
        return Jwts.builder()
                //存用户id
                .setSubject(String.valueOf(userId))
                //存用户名
                .claim("username",username)
                //签发时间
                .setIssuedAt(new Date())
                //过期时间
                .setExpiration(new Date(System.currentTimeMillis()+jwtProperties.getExpire()))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();

    }

    //  解析 token
    public Claims parseJWT(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }


    //从 token 中获取用户 ID
    public Long getUserId(String token) {
        return Long.valueOf(parseJWT(token).getSubject());
    }
}
