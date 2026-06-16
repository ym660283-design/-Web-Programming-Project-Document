import { mkdir, readdir, readFile, writeFile, copyFile } from 'node:fs/promises';
import { join } from 'node:path';
import { marked } from 'marked';

const siteDir = '_site';
const docsDir = join(siteDir, 'docs');
const imagesDir = join(docsDir, 'images');

await mkdir(imagesDir, { recursive: true });

const readme = await readFile('README.md', 'utf8');
const html = marked.parse(readme);

await writeFile(
  join(siteDir, 'index.html'),
  `<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Travel Itinerary Management 발표자료</title>
  <style>
    body {
      max-width: 960px;
      margin: 0 auto;
      padding: 40px 24px 72px;
      color: #1f2937;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Malgun Gothic", sans-serif;
      line-height: 1.65;
    }
    a {
      color: #2563eb;
    }
    h1, h2, h3 {
      line-height: 1.25;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 16px 0;
    }
    th, td {
      border: 1px solid #d1d5db;
      padding: 8px 10px;
      vertical-align: top;
    }
    th {
      background: #f3f4f6;
    }
    code {
      background: #f3f4f6;
      border-radius: 4px;
      padding: 2px 4px;
    }
  </style>
</head>
<body>
${html}
</body>
</html>
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
  await copyFile(join('docs/images', file), join(imagesDir, file));
}
