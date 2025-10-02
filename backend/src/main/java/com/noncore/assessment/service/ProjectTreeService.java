package com.noncore.assessment.service;

import java.nio.file.Path;
import java.util.List;

/**
 * 提供项目目录树读取能力，仅用于对外只读展示。
 * 约束：
 * - 仅允许读取工作区下的受限目录：backend、frontend、docs
 * - 支持最大深度限制与忽略目录白名单
 */
public interface ProjectTreeService {

    /**
     * 目录节点 DTO（轻量）
     */
    class Node {
        public String name;
        public boolean directory;
        public List<Node> children;
    }

    /**
     * 计算工作区根目录（在本项目下通常为 backend 的父级）
     */
    Path resolveWorkspaceRoot();

    /**
     * 读取目录树（限制在 workspace 下的受控子目录）。
     * @param maxDepth 最大深度（>=1），含根目录层
     * @return 根集合（backend、frontend、docs 中存在的项）
     */
    List<Node> readProjectTree(int maxDepth);
}


