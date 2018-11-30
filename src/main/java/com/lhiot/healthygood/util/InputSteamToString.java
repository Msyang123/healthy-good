package com.lhiot.healthygood.util;

import com.leon.microx.util.IOUtils;
import com.leon.microx.util.StringUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class InputSteamToString {

    @NonNull
    public static String fromInputStream(InputStream inputStream) throws IOException {
            if (Objects.nonNull(inputStream)) {
                @Cleanup BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String parameterString = StringUtils.collectionToDelimitedString(IOUtils.readLines(in),"");
                log.info("request转换成字符串结果：{}", parameterString);
                return parameterString;
            }
            return null;
    }
}
