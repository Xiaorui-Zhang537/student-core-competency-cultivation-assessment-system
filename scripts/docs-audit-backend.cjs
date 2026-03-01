/* eslint-disable no-console */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const CONTROLLERS_DIR = path.join(ROOT, 'backend', 'src', 'main', 'java', 'com', 'noncore', 'assessment', 'controller');
const DOCS_BACKEND_DIR = path.join(ROOT, 'docs', 'backend', 'api');

function listFiles(dir, pred) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir)
    .map((name) => path.join(dir, name))
    .filter((p) => fs.statSync(p).isFile())
    .filter((p) => (pred ? pred(p) : true))
    .sort();
}

function readText(file) {
  return fs.readFileSync(file, 'utf8');
}

function firstStringLiteral(javaArgs) {
  if (!javaArgs) return '';
  const m = javaArgs.match(/"([^"]*)"/);
  return m ? m[1] : '';
}

function normalizePath(p) {
  if (!p) return '';
  if (p === '/') return '';
  return p.startsWith('/') ? p : `/${p}`;
}

function joinPaths(a, b) {
  const aa = normalizePath(a);
  const bb = normalizePath(b);
  const out = `${aa}${bb}`;
  // Collapse duplicate slashes except the leading one.
  return out.replace(/\/{2,}/g, '/');
}

function extractClassRequestMapping(src) {
  const m = src.match(/@RequestMapping\s*\(\s*"([^"]*)"\s*\)/);
  return m ? m[1] : '';
}

function extractEndpoints(src) {
  const base = extractClassRequestMapping(src);
  const endpoints = [];
  const re = /@(Get|Post|Put|Delete)Mapping\s*(?:\(([^)]*)\))?/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    const httpMethod = m[1].toUpperCase();
    const args = m[2] || '';
    const sub = firstStringLiteral(args);
    const full = joinPaths(base, sub);
    endpoints.push({ method: httpMethod, path: full });
  }
  return endpoints;
}

function loadDocText(docId) {
  const p = path.join(DOCS_BACKEND_DIR, `${docId}.md`);
  if (!fs.existsSync(p)) return null;
  return readText(p);
}

function docContainsEndpoint(docText, fullApiPath) {
  if (!docText) return false;
  const norm = (s) => String(s || '').replace(/\{[^}]+\}/g, '{}');
  const docNorm = norm(docText);
  const apiNorm = norm(fullApiPath);
  if (docNorm.includes(apiNorm)) return true;
  // Also accept the "no context-path" form (/auth/login) because some pages use it.
  const noApi = fullApiPath.replace(/^\/api\b/, '');
  const noApiNorm = norm(noApi);
  if (noApi !== fullApiPath && docNorm.includes(noApiNorm)) return true;
  return false;
}

// Map controller to one-or-more docs pages. An endpoint is considered documented if it appears in ANY mapped page.
// Note: AiController endpoints are split across multiple docs.
const CONTROLLER_DOC_MAP = {
  AuthController: ['auth'],
  UserController: ['user', 'auth'], // auth.md may reference some /users endpoints, but user.md is canonical.
  CourseController: ['course'],
  ChapterController: ['chapter'],
  LessonController: ['lesson'],
  AssignmentController: ['assignment'],
  SubmissionController: ['submission'],
  GradeController: ['grade'],
  StudentController: ['student'],
  TeacherController: ['teacher'],
  TeacherStudentController: ['teacher'],
  AbilityController: ['ability'],
  AbilityAnalyticsController: ['teacher'],
  BehaviorEvidenceController: ['behavior'],
  BehaviorInsightController: ['behavior'],
  ChatController: ['chat', 'notification'],
  CommunityController: ['community'],
  NotificationController: ['notification'],
  NotificationStreamController: ['notification'],
  FileController: ['file'],
  HelpController: ['help'],
  ReportController: ['report'],
  ProjectController: ['project'],
  AiController: ['ai', 'ai-live', 'ai-grading'],
  AiVoicePracticeController: ['ai-voice-practice'],
};

function main() {
  const controllers = listFiles(CONTROLLERS_DIR, (p) => p.endsWith('Controller.java') && !p.endsWith('BaseController.java'));
  const missing = [];
  const unmapped = [];

  for (const file of controllers) {
    const controllerName = path.basename(file, '.java');
    const docIds = CONTROLLER_DOC_MAP[controllerName];
    if (!docIds) {
      unmapped.push(controllerName);
      continue;
    }
    const src = readText(file);
    const endpoints = extractEndpoints(src);
    const docs = docIds.map((id) => ({ id, text: loadDocText(id) }));
    for (const ep of endpoints) {
      const apiPath = joinPaths('/api', ep.path);
      const ok = docs.some((d) => docContainsEndpoint(d.text, apiPath));
      if (!ok) {
        missing.push({
          controller: controllerName,
          method: ep.method,
          apiPath,
          docs: docIds.slice(),
        });
      }
    }
  }

  if (unmapped.length) {
    console.log('\n[WARN] Unmapped controllers (not checked):');
    for (const name of unmapped) console.log(`- ${name}`);
  }

  if (!missing.length) {
    console.log('\n[OK] No missing endpoints detected (string match).');
    process.exit(0);
  }

  console.log(`\n[FAIL] Missing endpoints (not found in mapped docs): ${missing.length}\n`);
  // Group by controller for readability.
  const byController = new Map();
  for (const m of missing) {
    const arr = byController.get(m.controller) || [];
    arr.push(m);
    byController.set(m.controller, arr);
  }
  for (const [controller, arr] of [...byController.entries()].sort((a, b) => a[0].localeCompare(b[0]))) {
    console.log(`## ${controller}`);
    for (const item of arr) {
      console.log(`- [${item.method}] ${item.apiPath} (docs: ${item.docs.join(', ')})`);
    }
    console.log('');
  }

  process.exitCode = 1;
}

main();
