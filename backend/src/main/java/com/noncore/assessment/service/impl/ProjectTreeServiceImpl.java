package com.noncore.assessment.service.impl;

import com.noncore.assessment.service.ProjectTreeService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectTreeServiceImpl implements ProjectTreeService {

    private static final Set<String> ROOTS = new HashSet<>(Arrays.asList("backend", "frontend", "docs"));
    private static final Set<String> IGNORE_DIRS = new HashSet<>(Arrays.asList(
            ".git", ".idea", ".vscode", "node_modules", "target", "dist", "out"
    ));

    @Override
    public Path resolveWorkspaceRoot() {
        // 自底向上查找，更严格：优先返回同时包含 backend 与 frontend 的目录（通常是项目根）
        Path start = Paths.get("").toAbsolutePath().normalize();
        Path current = start;
        for (int i = 0; i < 10 && current != null; i++) {
            boolean hasBackend = Files.isDirectory(current.resolve("backend"));
            boolean hasFrontend = Files.isDirectory(current.resolve("frontend"));
            if (hasBackend && hasFrontend) {
                return current;
            }
            current = current.getParent();
        }
        // 次优先：包含 backend 或 frontend 与 docs 的目录
        current = start;
        for (int i = 0; i < 10 && current != null; i++) {
            boolean hasAnyRoot = Files.isDirectory(current.resolve("backend"))
                    || Files.isDirectory(current.resolve("frontend"))
                    || Files.isDirectory(current.resolve("docs"));
            if (hasAnyRoot) return current;
            current = current.getParent();
        }
        // 兜底：运行目录
        return start;
    }

    @Override
    public List<Node> readProjectTree(int maxDepth) {
        Path root = resolveWorkspaceRoot();
        List<Node> list = new ArrayList<>();
        for (String dir : ROOTS) {
            Path p = root.resolve(dir);
            if (Files.isDirectory(p)) {
                Node n = new Node();
                n.name = dir;
                n.directory = true;
                n.children = readChildren(p, 1, maxDepth);
                list.add(n);
            }
        }
        return list;
    }

    private List<Node> readChildren(Path path, int depth, int maxDepth) {
        List<Node> children = new ArrayList<>();
        if (maxDepth > 0 && depth > maxDepth) return children;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path child : stream) {
                String name = child.getFileName().toString();
                if (name.startsWith(".")) continue;
                if (Files.isDirectory(child) && IGNORE_DIRS.contains(name)) continue;

                Node node = new Node();
                node.name = name;
                node.directory = Files.isDirectory(child);
                if (node.directory) {
                    node.children = readChildren(child, depth + 1, maxDepth);
                    // 保证目录下即便为空也返回空数组，避免前端判断失效
                    if (node.children == null) node.children = new ArrayList<>();
                }
                children.add(node);
            }
        } catch (IOException ignored) {
        }
        return children;
    }
}


