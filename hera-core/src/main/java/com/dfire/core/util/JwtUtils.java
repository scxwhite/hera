package com.dfire.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaosuda
 * @date 2018/7/13
 */
public class JwtUtils {

    private final static String secret = "WWW.TWO_D_FIRE.COM/INFRASTRUCTURE";

    private static Algorithm algorithm = null;

    static {
        try {
            algorithm = Algorithm.HMAC256(secret);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String createToken(String username) {
        Map<String, Object> header = new HashMap<>(2);
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        Date expireDate = calendar.getTime();
        return JWT.create().withHeader(header)
                .withClaim("iss", "hera")
                .withClaim("aud", "2dfire")
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    public static boolean verifyToken(String token) {
        return getClaims(token) != null;
    }


    private static Map<String, Claim> getClaims(String token) {
        DecodedJWT jwt;
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            jwt = verifier.verify(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return jwt.getClaims();
    }

    public static String getObjectFromToken(String token, String name) {
        Map<String, Claim> claimMap = getClaims(token);
        if (claimMap != null && claimMap.get(name) != null) {
            return claimMap.get(name).asString();
        }
        return null;
    }
}
