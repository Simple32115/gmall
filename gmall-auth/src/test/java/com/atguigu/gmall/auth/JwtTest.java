package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "G:\\尚硅谷\\谷粒商城\\project\\rsa\\rsa.pub";

    private static final String priKeyPath = "G:\\尚硅谷\\谷粒商城\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234asdfzw");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "112");
        map.put("username", "liuyana");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 1);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExMiIsInVzZXJuYW1lIjoibGl1eWFuYSIsImV4cCI6MTU5MDgzNjU1OH0.hhf1jiaNJqf9TF8b8alxY0dnxdgmkThKb7bBiG2XAg0w4Az4-fh53RTRqPS7uxtTMQjMyTmFLct_twW9q-DkUmEJLyhwXrubda574lm6jg6XGxrPdQ8RR5kU8Vak7OEDlMGyQUi-6TZKwq0QUi51TXGo6AIUmEJr_58EBrV_kLy8onXD1VP3a1l-HkMCDO9caDw35zDOLzYIHcl-D4rAOHX5MEwrZt7_4MWc-C9JrRZ5dVfCV98EuqssncuF00BooHCRLvNQqh12-KwptvhNdQDxr5pdj6MEpN9k5fzaFG8nC70rpXPvot9puKsubzd-RDNpdrv3zWXvbKoXPgqXAw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
