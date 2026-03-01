/* eslint-disable no-console */
/**
 * Docs QA for VitePress.
 *
 * Checks:
 * - sidebar/nav `link: '/route'` targets exist in docs/
 * - orphan markdown pages not referenced by sidebar/nav
 * - internal markdown links resolve to an existing page/file (best-effort)
 *
 * Design goals:
 * - fast, zero dependencies (Node built-ins only)
 * - low false positives: anchors are not strictly validated (route/file existence is)
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const DOCS_DIR = path.join(ROOT, 'docs');
const VP_CONFIG = path.join(DOCS_DIR, '.vitepress', 'config.ts');

const IGNORE_DIRS = new Set([
  'node_modules',
  '.vitepress',
]);

function readText(p) {
  return fs.readFileSync(p, 'utf8');
}

function existsFile(p) {
  try {
    return fs.statSync(p).isFile();
  } catch {
    return false;
  }
}

function walkMdFiles(dir) {
  const out = [];
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const e of entries) {
    const abs = path.join(dir, e.name);
    if (e.isDirectory()) {
      if (IGNORE_DIRS.has(e.name)) continue;
      out.push(...walkMdFiles(abs));
      continue;
    }
    if (e.isFile() && e.name.endsWith('.md')) out.push(abs);
  }
  return out.sort();
}

function normalizeRoute(route) {
  if (!route) return '/';
  let r = String(route).trim();
  if (!r.startsWith('/')) r = `/${r}`;
  // Strip query/hash and trailing slash (except root)
  r = r.split('#')[0].split('?')[0];
  if (r.length > 1) r = r.replace(/\/+$/, '');
  return r;
}

function routeToCandidates(route) {
  const r = normalizeRoute(route);
  if (r === '/') return [path.join(DOCS_DIR, 'index.md')];
  const rel = r.replace(/^\//, '');
  return [
    path.join(DOCS_DIR, `${rel}.md`),
    path.join(DOCS_DIR, rel, 'index.md'),
  ];
}

function resolveRouteToFile(route) {
  for (const c of routeToCandidates(route)) {
    if (existsFile(c)) return c;
  }
  return null;
}

function fileToPrimaryRoute(fileAbs) {
  const rel = path.relative(DOCS_DIR, fileAbs).replace(/\\/g, '/');
  if (rel === 'index.md') return '/';
  if (rel.endsWith('/index.md')) return `/${rel.slice(0, -'/index.md'.length)}`;
  if (rel.endsWith('.md')) return `/${rel.slice(0, -'.md'.length)}`;
  return null;
}

function extractRoutesFromVitepressConfig(tsText) {
  // Best-effort: match link: '...'/ "..."
  const routes = new Set();
  const re = /\blink\s*:\s*(['"])(.*?)\1/g;
  let m;
  while ((m = re.exec(tsText)) !== null) {
    const link = (m[2] || '').trim();
    if (!link) continue;
    if (/^https?:\/\//i.test(link)) continue;
    if (/^mailto:/i.test(link)) continue;
    routes.add(normalizeRoute(link));
  }
  return [...routes].sort();
}

function extractMdLinks(mdText) {
  // Keep it simple; we just want obvious authoring mistakes.
  // Captures both links and images; we filter later.
  const links = [];
  const re = /(!)?\[[^\]]*\]\(([^)\s]+)(?:\s+["'][^"']*["'])?\)/g;
  let m;
  while ((m = re.exec(mdText)) !== null) {
    links.push({ isImage: Boolean(m[1]), raw: m[0], href: m[2] });
  }
  return links;
}

function stripHashAndQuery(href) {
  const noHash = href.split('#')[0];
  return noHash.split('?')[0];
}

function isExternalHref(href) {
  return (
    /^https?:\/\//i.test(href) ||
    /^mailto:/i.test(href) ||
    /^tel:/i.test(href)
  );
}

function resolveMdHref(fromFileAbs, href) {
  const h = String(href || '').trim();
  if (!h) return { kind: 'skip', reason: 'empty' };
  if (isExternalHref(h)) return { kind: 'skip', reason: 'external' };
  if (h.startsWith('#')) return { kind: 'anchor', targetFile: fromFileAbs };

  // Absolute route within site, like /backend/api/auth or /foo/bar.md
  if (h.startsWith('/')) {
    const base = stripHashAndQuery(h);
    // If author used explicit .md, validate file directly; else treat as route.
    if (base.endsWith('.md')) {
      const fileAbs = path.join(DOCS_DIR, base.replace(/^\//, ''));
      return { kind: 'file', targetFile: fileAbs };
    }
    const fileAbs = resolveRouteToFile(base);
    return { kind: 'route', route: base, targetFile: fileAbs };
  }

  // Relative link: ./foo, ../bar, foo.md
  const base = stripHashAndQuery(h);
  const dir = path.dirname(fromFileAbs);

  if (base.endsWith('.md')) {
    return { kind: 'file', targetFile: path.resolve(dir, base) };
  }

  // Relative route without extension; follow VitePress behavior for typical authoring:
  // - ./foo -> ./foo.md or ./foo/index.md
  const rel = base.replace(/\/+$/, '');
  const cand1 = path.resolve(dir, `${rel}.md`);
  const cand2 = path.resolve(dir, rel, 'index.md');
  if (existsFile(cand1)) return { kind: 'file', targetFile: cand1 };
  if (existsFile(cand2)) return { kind: 'file', targetFile: cand2 };

  // Might be asset (png/pdf/etc) or a directory route not backed by markdown.
  return { kind: 'file', targetFile: path.resolve(dir, rel) };
}

function main() {
  if (!existsFile(VP_CONFIG)) {
    console.error(`[FAIL] Missing VitePress config: ${VP_CONFIG}`);
    process.exit(2);
  }

  const cfgText = readText(VP_CONFIG);
  const referencedRoutes = extractRoutesFromVitepressConfig(cfgText);
  const referencedFiles = new Set();
  const missingRouteTargets = [];

  for (const r of referencedRoutes) {
    const f = resolveRouteToFile(r);
    if (!f) {
      missingRouteTargets.push(r);
      continue;
    }
    referencedFiles.add(path.resolve(f));
  }

  const mdFiles = walkMdFiles(DOCS_DIR);
  const byRoute = new Map();
  for (const f of mdFiles) {
    const route = fileToPrimaryRoute(f);
    if (route) byRoute.set(normalizeRoute(route), f);
  }

  const orphanFiles = [];
  for (const f of mdFiles) {
    // index.md is always reachable; treat it as referenced even if config changes.
    if (path.resolve(f) === path.join(DOCS_DIR, 'index.md')) continue;
    if (!referencedFiles.has(path.resolve(f))) orphanFiles.push(f);
  }

  const brokenLinks = [];
  for (const f of mdFiles) {
    const text = readText(f);
    const links = extractMdLinks(text);
    for (const l of links) {
      // images often point to external CDN or local assets; validate existence only if local.
      const resolved = resolveMdHref(f, l.href);
      if (resolved.kind === 'skip' || resolved.kind === 'anchor') continue;

      if (!resolved.targetFile) {
        brokenLinks.push({ file: f, href: l.href, reason: 'unresolved' });
        continue;
      }

      // targetFile may be a directory or asset; we only treat it as broken if it doesn't exist at all.
      if (!fs.existsSync(resolved.targetFile)) {
        brokenLinks.push({ file: f, href: l.href, reason: `missing: ${resolved.targetFile}` });
      }
    }
  }

  const problems = {
    missingRouteTargets,
    orphanFiles,
    brokenLinks,
  };

  const hasProblems =
    problems.missingRouteTargets.length ||
    problems.brokenLinks.length;

  // Orphans are warnings by default: not every page must be in sidebar/nav.
  const hasWarnings = problems.orphanFiles.length;

  console.log('\n[Docs QA]');

  if (missingRouteTargets.length) {
    console.log(`\n[FAIL] VitePress config links without target page: ${missingRouteTargets.length}`);
    for (const r of missingRouteTargets) console.log(`- ${r}`);
  } else {
    console.log('\n[OK] All sidebar/nav links resolve to an existing page.');
  }

  if (brokenLinks.length) {
    console.log(`\n[FAIL] Broken internal markdown links: ${brokenLinks.length}`);
    // Group by file for readability.
    const byFile = new Map();
    for (const b of brokenLinks) {
      const arr = byFile.get(b.file) || [];
      arr.push(b);
      byFile.set(b.file, arr);
    }
    for (const [file, arr] of [...byFile.entries()].sort((a, b) => a[0].localeCompare(b[0]))) {
      const rel = path.relative(ROOT, file).replace(/\\/g, '/');
      console.log(`\n## ${rel}`);
      for (const it of arr) console.log(`- ${it.href} (${it.reason})`);
    }
  } else {
    console.log('\n[OK] No broken internal markdown links detected (existence check).');
  }

  if (orphanFiles.length) {
    console.log(`\n[WARN] Markdown pages not referenced by sidebar/nav links: ${orphanFiles.length}`);
    for (const f of orphanFiles) {
      const rel = path.relative(ROOT, f).replace(/\\/g, '/');
      const route = fileToPrimaryRoute(f);
      console.log(`- ${rel}${route ? ` (route: ${route})` : ''}`);
    }
  } else {
    console.log('\n[OK] No orphan markdown pages detected.');
  }

  if (hasProblems) process.exit(1);
  if (hasWarnings) process.exit(0);
  process.exit(0);
}

main();

