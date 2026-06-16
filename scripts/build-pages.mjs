import { mkdir, readdir, readFile, writeFile, copyFile } from 'node:fs/promises';
import { join } from 'node:path';
import { marked } from 'marked';

const siteDir = '_site';
const buildDir = '_build';
const docsDir = join(siteDir, 'docs');
const rootImagesDir = join(siteDir, 'images');
const imagesDir = join(docsDir, 'images');
const buildImagesDir = join(buildDir, 'images');

const slideSources = [
  'docs/0-project-overview.md',
  'docs/1-requirement-analysis.md',
  'docs/2-project-structure.md',
  'docs/3-feature-Implementation.md',
  'docs/4-summary.md',
];

await mkdir(imagesDir, { recursive: true });
await mkdir(rootImagesDir, { recursive: true });
await mkdir(buildImagesDir, { recursive: true });

function stripFrontMatter(markdown) {
  return markdown.replace(/^---\r?\n[\s\S]*?\r?\n---\r?\n?/, '').trim();
}

function normalizeImagePaths(markdown) {
  return markdown.replace(/\]\((?:\.\.\/docs\/|\.\/)?images\//g, '](./images/');
}

const slideBodies = [];
for (const source of slideSources) {
  const markdown = await readFile(source, 'utf8');
  slideBodies.push(normalizeImagePaths(stripFrontMatter(markdown)));
}

await writeFile(
  join(buildDir, 'presentation.md'),
  `---
marp: true
paginate: true
size: 16:9
style: |
  section {
    font-size: 23px;
  }
  table {
    font-size: 16px;
  }
  th, td {
    padding: 5px 7px;
  }
  code, pre {
    font-size: 0.82em;
    overflow-wrap: anywhere;
  }
  .columns {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
  }
  .small {
    font-size: 20px;
  }
  .source {
    font-size: 18px;
  }
  .tree-columns {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 18px;
    align-items: start;
  }
  .tree-columns pre {
    font-size: 14px;
    line-height: 1.15;
    margin: 0;
  }
  .tree-columns h3 {
    margin-bottom: 8px;
  }
---

${slideBodies.join('\n\n---\n\n')}
`,
  'utf8',
);

const aiPrompts = marked.parse(await readFile('docs/5-AI-prompts.md', 'utf8'));
await writeFile(
  join(docsDir, '5-AI-prompts.html'),
  `<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>AI 활용 내역</title>
  <style>
    body {
      max-width: 960px;
      margin: 0 auto;
      padding: 40px 24px 72px;
      color: #1f2937;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Malgun Gothic", sans-serif;
      line-height: 1.65;
    }
    a { color: #2563eb; }
    h1, h2, h3 { line-height: 1.25; }
    code { background: #f3f4f6; border-radius: 4px; padding: 2px 4px; }
  </style>
</head>
<body>
${aiPrompts}
</body>
</html>
`,
  'utf8',
);

for (const file of await readdir('docs/images')) {
  await copyFile(join('docs/images', file), join(rootImagesDir, file));
  await copyFile(join('docs/images', file), join(imagesDir, file));
  await copyFile(join('docs/images', file), join(buildImagesDir, file));
}
