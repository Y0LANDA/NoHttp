/*
 * AUTHOR：Yan Zhenjie
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © ZhiMore. All Rights Reserved
 *
 */
package com.yanzhenjie.nohttp.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Yan Zhenjie on 2017/2/24.
 */
public class Util {

    public static String realUrl(String target) {
        try {
            URL url = new URL(target);

            String protocol = url.getProtocol();
            String host = url.getHost();
            String path = url.getPath();
            String query = url.getQuery();

            path = URLEncoder.encode(path, "utf-8")
                    .replace("%3A", ":")
                    .replace("%2B", "+")
                    .replace("%2C", ",")
                    .replace("%5E", "^")
                    .replace("%2F", "/")
                    .replace("%21", "!")
                    .replace("%24", "$")
                    .replace("%25", "%")
                    .replace("%26", "&")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%40", "@")
                    .replace("%60", "`");
            // .replace("", "#"); // not support.

            StringBuilder urlBuild = new StringBuilder(protocol)
                    .append("://")
                    .append(host)
                    .append(path);
            if (query != null)
                urlBuild.append("?").append(query);
            return urlBuild.toString();
        } catch (IOException e) {
            return target;
        }
    }

}
