package com.noncore.assessment.behavior;

import java.util.Arrays;
import java.util.Optional;

/**
 * 行为事件类型（事实记录，不代表评价）。
 *
 * <p>注意：行为证据不参与任何分数计算。本枚举仅用于事件分类与聚合口径统一。</p>
 */
public enum BehaviorEventType {

    /** AI 提问（首次问题） */
    AI_QUESTION("ai_question", false),
    /** AI 追问（基于上一轮进一步追问/澄清） */
    AI_FOLLOW_UP("ai_follow_up", false),

    /** 作业提交（首次/一次提交动作） */
    ASSIGNMENT_SUBMIT("assignment_submit", false),
    /** 作业修改/重交（对已提交作业进行修改后再次提交） */
    ASSIGNMENT_RESUBMIT("assignment_resubmit", false),

    /** 社区发问（发帖/提问） */
    COMMUNITY_ASK("community_ask", false),
    /** 社区回答（回复/回答） */
    COMMUNITY_ANSWER("community_answer", false),

    /** 查看反馈（如查看成绩评语、查看老师反馈、查看系统建议等） */
    FEEDBACK_VIEW("feedback_view", false),

    /**
     * 资源访问（只记录不评价）。
     *
     * <p>该类事件在阶段一可被统计，但必须进入 nonEvaluative 分组；
     * 阶段二也必须显式告知 AI：不得将其作为能力判断依据。</p>
     */
    RESOURCE_VIEW("resource_view", true);

    private final String code;
    private final boolean nonEvaluative;

    BehaviorEventType(String code, boolean nonEvaluative) {
        this.code = code;
        this.nonEvaluative = nonEvaluative;
    }

    /**
     * 获取事件类型 code（用于 JSON/DB 存储）。
     *
     * @return 事件类型 code
     */
    public String getCode() {
        return code;
    }

    /**
     * 是否属于“只记录不评价”的事件类型。
     *
     * @return true 表示只能统计/展示，不可作为正负证据参与能力判断
     */
    public boolean isNonEvaluative() {
        return nonEvaluative;
    }

    /**
     * 根据 code 解析事件类型。
     *
     * @param code 事件类型 code（如 ai_question）
     * @return 匹配的枚举值（不存在则 empty）
     */
    public static Optional<BehaviorEventType> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(t -> t.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }
}

