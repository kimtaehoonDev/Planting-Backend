package com.example.demo.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider 단위테스트")
class JwtTokenProviderTest {
    private static final String TEMP_SECRET = "5plDwnuqJyjDbaTJai5nS9tLCA7QwYwnCn2MhW6K8e/ohLrV7QVzR9IUKyrIk" +
        "0f35GOOwIT4dQqfGKzLtV3NzV7qlr+7V6M6u5A4iJTh+YxTrLrQ8WgCmH8W7gKfCnS+R";
    private static final long ACCESS_TOKEN_EXPIRES = 213111;
    private static final long REFRESH_TOKEN_EXPIRES = 123123123;

    @Mock
    JwtProperties properties;

    JwtTokenProvider jwtTokenProvider;
    JwtParser jwtParser;

    @BeforeEach
    void beforeEach() {
        Mockito.when(properties.getSecret()).thenReturn(TEMP_SECRET);
        Mockito.when(properties.getAccessTokenExpires()).thenReturn(ACCESS_TOKEN_EXPIRES);
        Mockito.when(properties.getRefreshTokenExpires()).thenReturn(REFRESH_TOKEN_EXPIRES);

        jwtTokenProvider = new JwtTokenProvider(properties);

        // parser init
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }


    @Test
    @DisplayName("입력받은 ID로 토큰을 생성한다")
    void makeTokenInfo() {
        // given
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();

        // when
        TokenInfo tokenInfo = jwtTokenProvider.generateTokenInfo(id, now);

        // then
        String accessToken = tokenInfo.getAccessToken();
        String refreshToken = tokenInfo.getRefreshToken();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    @DisplayName("Access Token이 설정에 맞게 만료일이 맞춰졌는지 확인한다")
    void validateAccessToken() {
        // given
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        Date expected = makeExpiresDate(now, properties.getAccessTokenExpires());

        // when
        TokenInfo tokenInfo = jwtTokenProvider.generateTokenInfo(id, now);

        // then
        Claims accessTokenClaims = Jwts.parserBuilder()
            .setSigningKey(properties.getSecret())
            .build()
            .parseClaimsJws(tokenInfo.getAccessToken())
            .getBody();

        assertThat(accessTokenClaims.getSubject()).isEqualTo(String.valueOf(id));
        assertThat(accessTokenClaims.getExpiration()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Refresh Token이 설정에 맞게 만료일이 맞춰졌는지 확인한다")
    void validateRefreshToken() {
        // given
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        Date expected = makeExpiresDate(now, properties.getRefreshTokenExpires());

        // when
        TokenInfo tokenInfo = jwtTokenProvider.generateTokenInfo(id, now);

        // then
        Claims accessTokenClaims = Jwts.parserBuilder()
            .setSigningKey(properties.getSecret())
            .build()
            .parseClaimsJws(tokenInfo.getRefreshToken())
            .getBody();

        assertThat(accessTokenClaims.getSubject()).isEqualTo(String.valueOf(id));
        assertThat(accessTokenClaims.getExpiration()).isEqualTo(expected);
    }

    private Date makeExpiresDate(LocalDateTime now, long properties) {
        return new Date((now.plusSeconds(properties)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() / 1000) * 1000);
    }

}