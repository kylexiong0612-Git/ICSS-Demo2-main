# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev        # Start dev server on http://0.0.0.0:3000 (hash routing, HMR)
npm run build      # Type-check (vue-tsc --noEmit) + Vite production build to dist/
npm run preview    # Preview production build
npm run lint       # Type-check only (vue-tsc --noEmit — no ESLint configured)
npm run clean      # Remove dist/
```

Run the code review agent against a directory:
```bash
npx tsx agents/code-review.ts src/components/agent
```

## Environment

Copy `.env.example` to `.env` and set `ZHIPU_API_KEY`. The dev server proxies `POST /api/ai` to Zhipu's API endpoint and injects the key server-side — it never ends up in the frontend bundle.

## Architecture

**Three-role SPA** demonstrating an insurance omnichannel service system. Routes (hash mode):

| Route | View | Purpose |
|-------|------|---------|
| `/#/customer` | CustomerView | Mobile phone-frame UI for end customers |
| `/#/agent/1` | AgentWorkstationView | L1 agent 3-panel workstation |
| `/#/agent/2` | AgentWorkstationView | L2 agent workstation (same component, different level prop) |
| `/#/admin` | AdminDashboardView | KPI cards + ECharts trend/category charts |

**State flow**: All service tasks live in `taskStore` (Pinia). The customer chat creates tasks via `preProcessTask()` (Zhipu JSON-mode call → structured `{summary, suggestion, tags}`). Agents read those tasks from the same store and mutate them (`grabTask`, `escalateTask`, complete). Chat history is persisted to localStorage via `chatStore`.

**AI calls** (`src/api/ai.ts`):
- `getBotResponse(message, history)` — chat completion with "宏小二" system prompt
- `preProcessTask(history)` — JSON-mode call returning task metadata for agent panel

**Component tree**:
- `App.vue` → header + `<router-view>`
- `CustomerView` → `HomeScreen` + `ChatInterface`
- `AgentWorkstationView` → `TaskList` | `ChatPanel` | `CustomerPanel`
- `AdminDashboardView` → `StatsGrid` + `TrendChart` + `CategoryChart` + `IssueList`

## Development Standards

Project-specific coding standards are maintained in the `dev-standards/` directory:

- [dev-standards/开发规范-PC端.md](dev-standards/前端开发规范-PC端.md) — Desktop/agent/admin UI conventions
- [dev-standards/开发规范-移动端.md](dev-standards/前端开发规范-移动端.md) — Mobile customer UI conventions

Read the relevant file before making changes to the corresponding UI layer.

## Code Conventions

- Vue 3 Composition API (`<script setup lang="ts">`) throughout
- Scoped Sass for component styles; CSS custom properties defined in `src/assets/styles/variable.scss`
- Path alias `@/` maps to `src/`
- Element Plus for desktop/agent/admin UI; Vant for the mobile customer UI
- `postcss-pxtorem` converts `px` → `rem` for mobile; `amfe-flexible` sets the root font size
- Auto-imports are configured for Vue/Vant/Element Plus APIs — do not manually import these
- All shared TypeScript interfaces are in `src/types/index.ts`
- Demo prototype: no real backend — all task data is Pinia in-memory (seeded with 2 tasks: TASK-001, TASK-002)

## Key Domain Concepts

- **ServiceTask** statuses: `Pending` → `Processing` → `Completed` (or `Escalated`)
- **Task levels**: 1 = L1 agent, 2 = L2 specialist; escalation moves level 1→2
- **Customer tiers**: `普通` / `银卡` / `金卡` / `白金` (stored in Customer type)
- **Bot persona**: "宏小二" — Zhipu GLM-4-Flash, insurance service assistant
