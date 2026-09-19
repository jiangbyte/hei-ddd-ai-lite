package io.github.jiangbyte.hei.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 启动成功后打印常用访问地址（环回 / 本机 / 局域网等网卡 IP）。
 */
@Component
@ConditionalOnWebApplication
public class StartupInfoPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoPrinter.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 1. 解析端口、context-path、本机全部可用地址
        Environment env = event.getApplicationContext().getEnvironment();
        String port = resolvePort(event, env);
        String contextPath = normalizeContextPath(env.getProperty("server.servlet.context-path", ""));
        String apiDocsPath = env.getProperty("springdoc.api-docs.path", "/v3/api-docs");
        String appName = env.getProperty("spring.application.name", "hei-ddd-ai-lite");
        List<HostAddress> hosts = resolveAllHostAddresses();

        // 2. 组装启动横幅：每个 IP 都给出服务 / 文档 / OpenAPI
        StringBuilder banner = new StringBuilder();
        banner.append("\n----------------------------------------------------------\n");
        banner.append(" ").append(appName).append(" 启动成功\n");
        for (HostAddress host : hosts) {
            String base = "http://" + formatHost(host.ip()) + ":" + port + contextPath;
            banner.append(" [").append(host.label()).append("] ").append(host.ip()).append('\n');
            banner.append("   服务地址: ").append(base).append('\n');
            banner.append("   接口文档: ").append(base).append("/doc.html\n");
            banner.append("   OpenAPI:  ").append(base).append(apiDocsPath).append('\n');
        }
        banner.append("----------------------------------------------------------");
        log.info(banner.toString());
    }

    private static String resolvePort(ApplicationReadyEvent event, Environment env) {
        if (event.getApplicationContext() instanceof WebServerApplicationContext webCtx) {
            WebServer server = webCtx.getWebServer();
            if (server != null && server.getPort() > 0) {
                return String.valueOf(server.getPort());
            }
        }
        return env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
    }

    /**
     * 枚举网卡地址：环回、链路本地、站点本地（局域网）、其他本机地址。
     */
    private static List<HostAddress> resolveAllHostAddresses() {
        Set<String> seen = new LinkedHashSet<>();
        List<HostAddress> loopback = new ArrayList<>();
        List<HostAddress> linkLocal = new ArrayList<>();
        List<HostAddress> siteLocal = new ArrayList<>();
        List<HostAddress> others = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            if (networks == null) {
                return defaultLoopbackOnly();
            }
            for (NetworkInterface nif : Collections.list(networks)) {
                // 1. 跳过未启用网卡
                if (!nif.isUp()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    // 2. 跳过无效 / 重复地址；IPv6 仅保留环回与站点本地，避免刷屏
                    if (addr.isAnyLocalAddress() || addr.isMulticastAddress()) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if (addr instanceof Inet6Address) {
                        int zone = ip.indexOf('%');
                        if (zone > 0) {
                            ip = ip.substring(0, zone);
                        }
                        if (addr.isLoopbackAddress()) {
                            ip = "::1";
                        }
                        if (!(addr.isLoopbackAddress() || addr.isSiteLocalAddress())) {
                            continue;
                        }
                    } else if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    if (!seen.add(ip)) {
                        continue;
                    }
                    // 3. 按地址类型归类
                    if (addr.isLoopbackAddress()) {
                        loopback.add(new HostAddress(ip, "环回"));
                    } else if (addr.isLinkLocalAddress()) {
                        linkLocal.add(new HostAddress(ip, "链路本地"));
                    } else if (addr.isSiteLocalAddress()) {
                        siteLocal.add(new HostAddress(ip, "局域网"));
                    } else {
                        others.add(new HostAddress(ip, "本机"));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("枚举网卡地址失败: {}", e.getMessage());
            return defaultLoopbackOnly();
        }

        List<HostAddress> result = new ArrayList<>();
        result.addAll(loopback);
        result.addAll(siteLocal);
        result.addAll(others);
        result.addAll(linkLocal);
        if (result.isEmpty()) {
            return defaultLoopbackOnly();
        }
        // 4. 保证至少有 127.0.0.1
        boolean hasIpv4Loopback = result.stream().anyMatch(h -> "127.0.0.1".equals(h.ip()));
        if (!hasIpv4Loopback) {
            result.addFirst(new HostAddress("127.0.0.1", "环回"));
        }
        return result;
    }

    private static List<HostAddress> defaultLoopbackOnly() {
        return List.of(new HostAddress("127.0.0.1", "环回"));
    }

    private static String formatHost(String ip) {
        // IPv6 写入 URL 时需要方括号
        return ip.contains(":") ? "[" + ip + "]" : ip;
    }

    private static String normalizeContextPath(String contextPath) {
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
            return "";
        }
        String path = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private record HostAddress(String ip, String label) {
    }
}
