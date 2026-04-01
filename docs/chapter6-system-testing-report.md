# Chapter 6 System Testing Evidence Report

## 1. Scope and method

This report is a source-first evidence summary for Chapter 6. Every statement below is backed by one or more of the following:

- live local API calls executed on 2026-03-13 and 2026-03-17
- live local MySQL queries executed on 2026-03-13 and 2026-03-17
- raw API/WebSocket evidence files in `docs/evidence/ch6/20260317-round3`
- fresh build / packaging checks executed on 2026-03-13
- existing runtime logs preserved in the repository
- current implementation and API contracts in the repository

This report does not claim any result that was not directly verified.

## 2. Current live environment and data snapshot

### 2.1 Runtime status verified on 2026-03-13 and 2026-03-17

| Item | Verification method | Result | Notes |
| --- | --- | --- | --- |
| Frontend type check | `npm --prefix frontend run type-check` | Pass | `vue-tsc --noEmit` passed |
| Frontend production build | `npm --prefix frontend run build` | Pass | Vite production build completed |
| Backend package build | `mvn -f backend/pom.xml -q -DskipTests package` | Pass | JAR packaging completed |
| MySQL | local process + SQL query | Running | existing local MySQL instance was reused |
| Backend | `java -jar backend/target/student-assessment-system-1.0.5.jar` | Running | `dev` profile started successfully on `127.0.0.1:8080` |
| Frontend | `npm --prefix frontend run dev:solo -- --host 127.0.0.1 --port 5173` | Running | Vite dev server started successfully on `127.0.0.1:5173` |

### 2.2 Database snapshot used for representative verification

The live test round was not performed on empty tables. A representative local dataset was present during verification.

| Table | Row count |
| --- | ---: |
| `users` | 4 |
| `courses` | 9 |
| `assignments` | 10 |
| `submissions` | 6 |
| `grades` | 7 |
| `behavior_events` | 225 |
| `behavior_insights` | 17 |
| `ai_grading_history` | 13 |
| `ai_voice_sessions` | 24 |
| `ai_voice_turns` | 48 |
| `notifications` | 56 |

## 3. Evidence by Chapter 6 structure

### 3.1 Cross-platform compatibility

Status in this round: not thesis-ready yet.

What was verified:

- frontend type checking and production build both passed in the current local macOS development environment
- the frontend dev server started successfully and served the application on `127.0.0.1:5173`

What is still missing:

- browser-by-browser compatibility matrix
- Windows + Chrome evidence
- macOS + Chrome evidence
- macOS + Safari evidence
- mobile browser evidence if mobile access is claimed
- screenshots for login, `/student/analysis`, `/teacher/analytics`, behavior insight, and speaking practice pages

Thesis-safe wording at this stage:

- implementation-backed readiness in the current local environment
- not cross-platform compatibility testing in the full thesis sense

### 3.2 Functional testing

#### 3.2.1 System management

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `SM-01` | `POST /api/auth/login` | teacher login returned `code=200` and valid tokens | Pass |
| `SM-02` | `POST /api/auth/login` | admin login returned `code=200` and valid tokens | Pass |
| `SM-03` | `POST /api/auth/login` | student login returned `code=200` and valid tokens | Pass |
| `SM-04` | `GET /api/users/profile` without token | returned `HTTP 401` with standardized body | Pass |
| `SM-05` | `GET /api/teachers/analytics/course/1` with student token | returned `HTTP 403` with standardized body | Pass |
| `SM-06` | `POST /api/courses` -> `POST /api/courses/{id}/publish` -> `POST /api/courses/{id}/unpublish` -> `DELETE /api/courses/{id}` | full teacher-side course lifecycle executed on a temporary test course | Pass |
| `SM-07` | `POST /api/assignments` -> `POST /api/assignments/{id}/publish` -> `POST /api/assignments/{id}/unpublish` -> `DELETE /api/assignments/{id}` | full teacher-side assignment lifecycle executed on a temporary test assignment | Pass |
| `SM-08` | `POST /api/auth/logout` | endpoint returned `code=200` | Pass |
| `SM-09` | protected API access with same token after logout | same token still accessed `GET /api/users/profile` with `HTTP 200` | Limitation confirmed |
| `SM-10` | `GET /api/admin/users?page=1&size=5` with admin token | returned `HTTP 200`, paged user list returned successfully | Pass |
| `SM-11` | `GET /api/admin/users?page=1&size=5` with teacher token | returned `HTTP 403` with standardized body | Pass |

Representative live data:

- temporary course created successfully as `Codex Test Course 20260313172456` with returned `id=8`
- temporary assignment created successfully as `Codex Test Assignment 20260313172456` with returned `id=9`
- additional 2026-03-17 verification confirmed teacher/student/admin login all returned `code=200` in the current runtime dataset
- additional 2026-03-17 verification confirmed admin endpoint access is role-restricted (`admin=200`, `teacher=403`)
- logout endpoint returned success, but the same JWT still accessed a protected profile endpoint afterward

Implication for thesis writing:

- login, authorization enforcement, and teacher-side course / assignment management were directly verified
- logout should be described conservatively because centralized token invalidation was not observed

#### 3.2.2 Assessment and grading

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `AG-01` | `POST /api/assignments/{assignmentId}/submit` | live student submission returned `code=200`, `status=submitted`, `submissionCount=1`, `isLate=false` | Pass |
| `AG-02` | `GET /api/assignments/{assignmentId}/submission` | student-side query returned the same submission content and status | Pass |
| `AG-03` | `POST /api/ai/grade/essay` (2026-03-13) | returned a real upstream error: `API_KEY_INVALID` | Exception path verified |
| `AG-03B` | `POST /api/ai/grade/essay` (2026-03-17) | returned fresh success with `historyId=12` and structured `data.overall.*` | Pass |
| `AG-03C` | `POST /api/ai/grade/essay` (2026-03-17 retest) | returned fresh success with `historyId=13`, `overall.final_score=2.8`, and `meta.ensemble.confidence=1.0` | Pass |
| `AG-04` | `GET /api/ai/grade/history` | teacher grading history returned `total=13` on 2026-03-17 | Pass |
| `AG-05` | `GET /api/ai/grade/history/8` | successful historical grading record returned structured `overall` and `meta.ensemble` fields | Pass |
| `AG-05B` | `GET /api/ai/grade/history/13` | latest retest record returned `overall.final_score` and full `meta.ensemble` pair info | Pass |
| `AG-06` | `GET /api/ai/grade/history?page=1&size=5` | repeated records for the same file on 2026-02-09 confirmed review / regeneration / re-evaluation history | Pass |
| `AG-07` | `GET /api/ability/student/reports` + `GET /api/ability/student/report/latest-by-context?assignmentId=7` | student-side AI report history and latest-by-context record returned successfully | Pass |

Representative submission sample captured during this round:

- temporary course `Codex Submission Course 20260313174547` created and published
- student enrolled successfully
- temporary assignment `Codex Submission Assignment 20260313174547` created and published
- student submitted JSON content successfully
- submission response returned:
  - `id=7`
  - `status="submitted"`
  - `submissionCount=1`
  - `assignmentId=10`
  - `studentId=3`
  - `isLate=false`
  - `submittedAt="2026-03-13T17:45:48.704026"`

Representative successful grading history sample captured from record `id=8`:

```json
{
  "fileName": "优势.docx",
  "model": "google/gemini-2.5-pro",
  "finalScore": 4.5,
  "createdAt": "2026-02-09T16:17:17",
  "overall": {
    "final_score": 4.5,
    "dimension_averages": {
      "ability": 5.0,
      "strategy": 4.2,
      "moral_reasoning": 4.3,
      "attitude": 4.3
    },
    "holistic_feedback": "Averages — Moral: 4.3, Attitude: 4.3, Ability: 5.0, Strategy: 4.2. Final: 4.5/5. ..."
  },
  "meta": {
    "ensemble": {
      "samplesRequested": 2,
      "method": "mean2",
      "triggeredThird": false,
      "diffThreshold": 0.8,
      "confidence": 0.94,
      "chosenPair": [1, 2],
      "pairDiff": 0.3,
      "runs": [
        { "index": 1, "finalScore": 4.6 },
        { "index": 2, "finalScore": 4.3 }
      ]
    }
  }
}
```

Representative re-evaluation evidence from grading history:

| History ID | File name | Final score | Created at |
| --- | --- | ---: | --- |
| 8 | `优势.docx` | 4.5 | 2026-02-09 16:17:17 |
| 7 | `优势.docx` | 4.4 | 2026-02-09 15:37:09 |
| 6 | `优势.docx` | 4.4 | 2026-02-09 15:31:50 |

Representative fresh success sample from 2026-03-17:

```json
{
  "historyId": 12,
  "overall.final_score": 2.2,
  "overall.dimension_averages": {
    "ability": 2.3,
    "strategy": 3.0,
    "moral_reasoning": 1.0,
    "attitude": 2.3
  }
}
```

Representative fresh score-stabilization sample from 2026-03-17 (`historyId=10`):

```json
{
  "overall.final_score": 2.2,
  "meta.ensemble": {
    "samplesRequested": 2,
    "method": "mean2",
    "triggeredThird": false,
    "diffThreshold": 0.8,
    "confidence": 0.96,
    "chosenPair": [1, 2],
    "pairDiff": 0.2,
    "runs": [
      { "index": 1, "finalScore": 2.1 },
      { "index": 2, "finalScore": 2.3 }
    ]
  }
}
```

Representative latest grading-detail sample from 2026-03-17 (`historyId=13`):

```json
{
  "overall.final_score": 2.8,
  "meta.ensemble": {
    "samplesRequested": 2,
    "method": "mean2",
    "triggeredThird": false,
    "diffThreshold": 0.8,
    "confidence": 1.0,
    "chosenPair": [1, 2],
    "pairDiff": 0.0
  }
}
```

Representative current exception sample:

```json
{
  "code": 1000,
  "message": "Gemini HTTP 400: ... API_KEY_INVALID ..."
}
```

Student-side report evidence verified from live APIs:

- report history returned `total=4`
- latest report returned `id=6`, `title="测试啊 · AI批改报告"`, `reportType="ai"`
- latest-by-context for `assignmentId=7` also returned report `id=6`
- report detail `id=6` returned structured fields including:
  - `overallScore=90.00`
  - `dimensionScores`
  - `trendsAnalysis`
  - `improvementAreas`
  - `recommendations`
  - `gradeDescription="优秀"`

Implication for thesis writing:

- the assessment path from student submission to teacher-side AI grading history and student-side AI report retrieval is supported by live evidence
- fresh AI-supported preliminary evaluation generation is now verified (2026-03-17)
- history records continue to support verified re-evaluation samples

#### 3.2.3 Visualization and goal tracking

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `VG-01` | `GET /api/ability/student/dashboard` | returned current ability snapshot and recent assessments | Pass |
| `VG-02` | `GET /api/ability/student/trends` | returned monthly trend array | Pass |
| `VG-03` | `GET /api/ability/student/radar?courseId=1` | returned radar dimensions, student scores, class averages, and composite delta | Pass |
| `VG-04` | `GET /api/ability/student/goals` | returned existing active goals | Pass |
| `VG-05` | `POST /api/ability/student/goals` -> `PUT /api/ability/student/goals/{goalId}` -> `DELETE /api/ability/student/goals/{goalId}` | full goal create / update / delete path executed on a temporary test goal | Pass |
| `VG-06` | `GET /api/teachers/analytics/course/1` | returned teacher-side analytics metrics and time-series data | Pass |
| `VG-07` | `GET /api/ability/student/radar` without required `courseId` | returned dev diagnostic error instead of normal chart data | Limitation confirmed |

Representative student-side analysis data:

- dashboard returned four current ability records for student `id=3`
- trends returned one monthly point for `2026-03` with `averageScore=4.45`
- radar for `courseId=1` returned:
  - dimensions: `道德认知`, `学习态度`, `学习能力`, `学习方法`, `学习成绩`
  - student scores: `83.0, 88.0, 96.5, 79.5, 77.5`
  - class average scores: `66.6, 93.1, 92.0, 80.9, 78.0`
  - `studentComposite=84.9`
  - `classComposite=82.1`
  - `compositeDelta=2.8`
- existing goals list returned two active goals for dimension `学习态度`

Representative goal-tracking CRUD sample:

- create returned goal `id=4`, `title="Codex Goal Test"`, `targetScore=4.90`, `targetDate="2026-04-30"`, `status="active"`
- update returned `title="Codex Goal Test Updated"`, `targetScore=4.95`, `targetDate="2026-05-05"`, `priority="high"`
- delete returned `code=200`

Representative teacher-side analytics sample for `courseId=1`:

- `totalStudents=2`
- `activeStudents=2`
- `totalAssignments=6`
- `completionRate=58.33`
- `averageScore=78.0`
- time-series sample returned date-value pairs beginning with `2024-12-22 -> 80`

Important implementation boundary that still applies:

- teacher analytics contains partially simplified or placeholder-like computation paths in the current implementation
- therefore, analytics visualization can be claimed as functional, but every numeric series should not be overstated as finalized empirical measurement logic

#### 3.2.4 Behavior insight

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `BI-01` | `GET /api/behavior/summary?studentId=3&courseId=1&range=30d` | returned real Stage-1 summary with evidence items and signals | Pass |
| `BI-02` | `GET /api/behavior/insights?studentId=3&courseId=1&range=30d` | returned real Stage-2 `behavior_insight.v2` record with explainScore, stageJudgements, riskAlerts, and actionRecommendations | Pass |
| `BI-03` | `POST /api/behavior/insights/generate?studentId=3&courseId=1&range=30d` | generated a fresh Stage-2 `behavior_insight.v2` record (`snapshotId=199`) in current runtime | Pass |

Representative Stage-1 summary sample:

- `schemaVersion="behavior_summary.v1"`
- `evidenceItems` count: `2`
- evidence item 1: `feedback_view`, description `本阶段内查看反馈 64 次`
- evidence item 2: `goal_focus`, description showing `goal_set=1` and `goal_update=2`
- signals included:
  - `goalSignals`
  - `goalCompletionRate`
  - `goalLinkedScoreDelta`
  - `goalNearestDueInDays`
  - `goalLatestActionAt`
  - `voiceTurnCount`
  - `voiceReplaySecondsTotal`
  - `timeSignals`
  - `antiSpam`

Representative Stage-2 behavior insight sample:

```json
{
  "schemaVersion": "behavior_insight.v2",
  "snapshotId": 190,
  "meta": {
    "generatedAt": "2026-03-10 13:12:19",
    "model": "google/gemini-2.5-pro",
    "promptVersion": "behavior_insight_prompt.v4",
    "status": "success"
  },
  "stageJudgementsCount": 4,
  "riskAlertsCount": 2,
  "actionRecommendationsCount": 1
}
```

Representative fresh generation sample on 2026-03-17:

```json
{
  "schemaVersion": "behavior_insight.v2",
  "snapshotId": 199,
  "meta": {
    "generatedAt": "2026-03-17 11:37:28",
    "model": "google/gemini-2.5-pro",
    "promptVersion": "behavior_insight_prompt.v4",
    "status": "success"
  },
  "riskAlertsCount": 1,
  "actionRecommendationsCount": 0
}
```

Representative non-scoring output fields verified from the live response:

- `explainScore.text` explained learning behavior and explicitly did not return a new score
- `stageJudgements` returned levels such as `PROFICIENT`, `DEVELOPING`, and `EMERGING`
- `riskAlerts` included:
  - `Potential for Inefficient Feedback Review` (`severity="warn"`)
  - `Limited Peer-to-Peer Engagement` (`severity="info"`)
- `actionRecommendations` included:
  - `Make Your Goals More Action-Oriented`
  - next actions to add a behavior-focused goal

Implication for thesis writing:

- the behavior insight workflow can be described as a verified two-stage behavior insight workflow
- it must remain explicitly non-scoring
- the response is grounded in evidence references and structured action-oriented outputs rather than numeric grading

#### 3.2.5 Speaking practice

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `SP-01` | `GET /api/ai/voice/sessions?page=1&size=5` | returned existing speaking-practice session history | Pass |
| `SP-02` | `GET /api/ai/voice/sessions/21/turns?page=1&size=20` | returned persisted historical turn data | Pass |
| `SP-03` | WebSocket `/api/ai/live/ws?token=...` | captured live `ready` event and `pong` reply | Pass |
| `SP-04` | WebSocket `start` event (2026-03-17) | reached `session_ready` successfully | Pass |
| `SP-05` | `POST /api/ai/voice/sessions` -> `POST /api/ai/voice/turns` -> `GET /api/ai/voice/sessions/{id}/turns` -> `POST /api/ai/voice/replay` -> `DELETE /api/ai/voice/sessions/{id}` | full persistence path executed on a temporary test session | Pass |
| `SP-06` | `POST /api/ai/voice/replay` with invalid payload | returned `code=422` and field-level validation message | Pass |
| `SP-07` | WebSocket one-round trigger (2026-03-17) | captured `ready -> session_ready -> started -> activity_started -> activity_ended -> generation_complete -> turn_complete` | Pass |
| `SP-08` | WebSocket one-round trigger with synthesized PCM input (2026-03-17 retest, 2 runs) | both runs reached `turn_complete`, but `transcript` / `assistant_text` / `assistant_transcript` were still not observed | Limitation confirmed |

Representative live WebSocket capture:

```json
{"role":"student","type":"ready","userId":3}
{"type":"pong","ts":12345}
```

Representative `start` / round events captured on 2026-03-17:

```json
{
  "types": [
    "ready",
    "session_ready",
    "started",
    "activity_started",
    "activity_ended",
    "generation_complete",
    "turn_complete"
  ]
}
```

Representative synthesized-audio WebSocket retest sample (`docs/evidence/ch6/20260317-round3/sp_ws_tts_events_v2.json`):

```json
{
  "reason": "turn_complete",
  "uniqueTypes": [
    "ready",
    "session_ready",
    "started",
    "activity_started",
    "activity_ended",
    "generation_complete",
    "turn_complete"
  ],
  "hasTranscript": false,
  "hasAssistantText": false,
  "hasAssistantTranscript": false
}
```

Representative live persistence sample from the temporary test session:

- session create returned `sessionId=24`
- turn save returned `turnId=48`
- turn list returned:
  - `userTranscript="This is a Chapter 6 speaking practice persistence test."`
  - `assistantText="Thanks. What learning goal will you focus on next?"`
- valid replay report returned `{ "recorded": true }`
- invalid replay returned:

```json
{
  "code": 422,
  "message": "参数验证失败",
  "data": {
    "deltaSeconds": "deltaSeconds 必须 >= 1"
  }
}
```

Representative historical speaking-practice content sample from persisted turn `id=46`, `sessionId=19`:

- the stored assistant text included a round follow-up question and a final summary block beginning with:
  - `Final Summary:`
  - `What you did well:`
  - `Key improvements:`

Important limitation in the current runtime:

- `session_ready` and `turn_complete` are now verified on fresh runs
- two additional synthesized-audio retests on 2026-03-17 still did not capture `transcript`, `assistant_text`, and `assistant_transcript` in the same fresh round
- therefore, this round verifies handshake and round completion, but still does not claim a full fresh event chain including all transcript-related event types

Thesis-safe wording:

- round-based, non-scoring speaking practice workflow verified at the persistence and transport level
- existing historical records confirm that stored assistant outputs include follow-up questioning and final-summary-style feedback
- do not claim a full fresh transcript event chain (`transcript` + `assistant_text` + `assistant_transcript`) for 2026-03-17 yet

#### 3.2.6 Supporting modules

Live verified samples:

| Test ID | Path | Evidence | Result |
| --- | --- | --- | --- |
| `SUP-01` | `GET /api/users/profile` without token | standardized `401` body returned | Pass |
| `SUP-02` | `GET /api/teachers/analytics/course/1` with student token | standardized `403` body returned | Pass |
| `SUP-03` | `POST /api/ai/voice/replay` with invalid payload | `HTTP 400` returned with business `code=422` and field-level validation body | Pass |
| `SUP-04` | `GET /api/notifications/my?page=1&size=5` | real notification list returned | Pass |

Representative error bodies captured during this round:

```json
{"code":401,"message":"未授权访问，请先登录","data":null}
```

```json
{"code":403,"message":"权限不足，无法访问该资源"}
```

```json
{
  "code": 422,
  "message": "参数验证失败",
  "data": {
    "deltaSeconds": "deltaSeconds 必须 >= 1",
    "audioRole": "audioRole 不能为空"
  }
}
```

Representative notification sample:

- teacher notification list returned item `id=56`
- title: `学生选课`
- content: `小张 加入了课程《Codex Submission Course 20260313174547》`
- related object: `course`, `relatedId=9`

Implementation-backed supporting evidence that remains relevant:

- frontend attaches `Authorization: Bearer <token>` automatically except for public auth endpoints
- frontend route guards enforce authentication and role matching
- backend uses a unified `{ code, message, data }` response wrapper

## 4. Reliability, security, and usability evaluation

### 4.1 Reliability

Directly supported by this round:

- backend and frontend build checks both passed
- standardized API wrapper was verified repeatedly across success and error paths
- grading history detail `id=8` confirmed structured output governance with stable fields such as `overall.final_score`, `overall.dimension_averages`, `overall.holistic_feedback`, and `meta.ensemble`
- latest grading history detail `id=13` also confirmed score stabilization and deterministic post-processing with `meta.ensemble` (`samplesRequested=2`, `method=mean2`, `confidence=1.0`)
- behavior insight returned a stable schema with `behavior_insight.v2`
- speaking practice persistence endpoints stored, listed, and validated turns and replay events successfully

Representative reliability strengths that are safe to write:

- unified response structure
- structured output governance
- score stabilization
- deterministic post-processing
- reusable structured contracts across frontend-facing modules

Representative reliability limitations observed in this round:

- `GET /api/ability/student/radar` without `courseId` returned a dev diagnostic `500` instead of a cleaner request-level failure response
- full fresh live oral transcript event chain was not stably captured in this CLI-driven round

### 4.2 Security

Directly supported by this round:

- student, teacher, and admin authentication all succeeded through the login endpoint
- unauthenticated access to protected endpoints returned `401`
- insufficient-permission access returned `403`
- protected teacher analytics endpoints were inaccessible to the student role
- protected admin user-management endpoint was inaccessible to the teacher role (`GET /api/admin/users` -> `403`)
- local WebSocket handshake required the access token and returned role-bound identity information (`role="student"`, `userId=3`)

Important limitation that must be written conservatively:

- after `POST /api/auth/logout` returned success, the same JWT still accessed `GET /api/users/profile` successfully
- therefore, centralized post-logout token invalidation was not verified in the current implementation
- Chapter 6 should not claim a strong server-side logout invalidation mechanism unless additional evidence is added

### 4.3 Usability

Directly supported by this round:

- major user tasks are decomposed into clear API-supported flows: login, course management, assignment management, submission, grading history, student analysis, goal tracking, behavior insight, notifications, and speaking-practice persistence
- student-side analysis data and teacher-side analytics data are both structurally consumable and readable
- student-side AI report detail returned readable fields such as `overallScore`, `gradeDescription`, `recommendations`, and `improvementAreas`
- behavior insight outputs are directly readable by category (`stageJudgements`, `riskAlerts`, `actionRecommendations`)
- speaking practice persistence records are easy to interpret at the turn level (`userTranscript`, `assistantText`, `createdAt`)

Current usability limits that should remain modest in the thesis:

- no fresh screenshots or recordings were collected in this round
- no formal questionnaire or SUS evaluation was conducted in this round
- therefore, usability claims should remain limited to interface clarity, task completion convenience, and readability of structured feedback

## 5. Thesis-safe findings and remaining gaps

### 5.1 Claims that are safe now

- The system operated successfully in representative local runtime environments on 2026-03-13 and 2026-03-17 with MySQL, backend, and frontend services running.
- Authentication and role-based access control were verified through successful login, `401` interception, and `403` interception.
- Admin-only route protection was additionally verified (`/api/admin/users` denied for teacher role).
- Teacher-side course and assignment management were verified through real create / publish / unpublish / delete operations.
- Student-side assignment submission was verified through a real submission success sample.
- A successful AI grading record with `overall.final_score`, `dimension_averages`, `holistic_feedback`, and `meta.ensemble` was verified from live history retrieval.
- Repeated grading records for the same file support a real review / regeneration / re-evaluation sample.
- Student analysis, teacher analytics, and goal tracking were all verified through real API responses.
- The behavior insight workflow was verified as a non-scoring two-stage workflow with real Stage-1 and Stage-2 outputs.
- The speaking practice module was verified as a non-scoring, round-based module at the session, turn, replay, and WebSocket-handshake levels.
- Unified response formatting and representative error handling were verified across multiple modules.

### 5.2 Claims that are still not safe

- comprehensive cross-platform compatibility testing
- fresh full transcript event chain (`transcript` + `assistant_text` + `assistant_transcript` + `turn_complete`) in a single repeatable round
- full-scale security audit or penetration testing
- large-scale concurrency or stress testing
- strong server-side logout invalidation
- fully production-final analytics computation for every metric

### 5.3 Remaining gaps before the final thesis draft

Still recommended:

- compatibility matrix with screenshots
- screenshots for login, `/student/analysis`, `/teacher/analytics`, behavior insight, and speaking practice
- one fresh successful `session_ready -> transcript -> assistant_text -> assistant_transcript -> turn_complete` WebSocket sequence with real microphone/browser capture (current CLI+synthetic-audio retests still miss transcript-related events)

Recommended thesis wording:

- representative environments
- representative functional paths
- implementation-backed verification
- basic reliability, security, and usability under expected classroom conditions

Wording to avoid without further evidence:

- comprehensive stress testing
- full-scale security audit
- complete cross-platform optimization
- large-scale concurrency verification
- fully validated server-side session revocation
