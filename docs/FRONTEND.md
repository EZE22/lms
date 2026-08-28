# Frontend

Working reference for the LMS frontend. It lives in `frontend/` and talks to the
Spring Boot backend at `http://localhost:8080` (see [API.md](API.md)).

## Stack

| Piece | What it's for |
|-------|---------------|
| **Vite** | Dev server and build tool (`@vitejs/plugin-react`). |
| **React 19 + TypeScript** | UI library and typing. |
| **Tailwind CSS v4** | Styling, wired in through the `@tailwindcss/vite` plugin (no `tailwind.config.js` — config is CSS-first in `src/index.css`). |
| **shadcn/ui** | Component generator. Configured in `components.json` with the **`base-nova`** style — Base UI primitives (`@base-ui/react`) plus the Nova preset. Icons from `lucide-react`. |
| **TanStack React Query** | Server-state / data fetching (`@tanstack/react-query`). |

## Running the dev server

```bash
cd frontend
npm install      # first time
npm run dev
```

Vite serves the app at **`http://localhost:5173`**. It expects the backend to
be running separately on port 8080.

Other scripts: `npm run build` (`tsc -b && vite build`), `npm run preview`,
`npm run lint`.

## Folder structure

```
frontend/src/
├── main.tsx            app entry — sets up React Query, renders <App />
├── App.tsx             top-level screen switch (currently just <CourseList />)
├── index.css           Tailwind v4 entry + theme tokens
├── api/                backend fetch functions, one file per resource
│   └── courses.ts      fetchCourses() -> GET /api/courses
├── types/              TypeScript interfaces mirroring backend entities
│   └── course.ts       interface Course { id, title, description, createdAt }
├── components/         screen-level components
│   ├── CourseList.tsx
│   └── ui/             shadcn-generated primitives — DO NOT hand-edit
│       └── button.tsx
└── lib/
    └── utils.ts        cn() helper (clsx + tailwind-merge)
```

Conventions:

- **`src/api/`** — one module per backend resource, exporting plain `async`
  functions that `fetch` and return typed data. No React in here.
- **`src/types/`** — interfaces that match the JSON the backend returns. Keep
  field names in sync with the entity classes (`entity/Course.java`, etc.).
- **`src/components/`** — screens and feature components you write by hand.
- **`src/components/ui/`** — output of `npx shadcn add ...`. Treat as
  generated; regenerate rather than editing in place.

## Path alias

`@/` maps to `src/`, so imports are absolute from the source root:

```ts
import { fetchCourses } from "@/api/courses"
import type { Course } from "@/types/course"
```

Defined in two places that must agree: `vite.config.ts` (`resolve.alias`) for
the bundler and `tsconfig.json` (`compilerOptions.paths`) for the type checker.

## Data fetching

1. `main.tsx` creates a single `QueryClient` and wraps `<App />` in
   `<QueryClientProvider>`.
2. Each screen component calls `useQuery` with a `queryKey` and a `queryFn`
   imported from `src/api/`.

Example — `components/CourseList.tsx`:

```tsx
const { data: courses, isLoading, error } = useQuery({
  queryKey: ["courses"],
  queryFn: fetchCourses,   // from @/api/courses
})
```

React Query handles caching, loading, and error state; the component just
renders off `data` / `isLoading` / `error`.

## CORS

The backend restricts cross-origin calls in
`backend/.../config/WebConfig.java`:

```java
registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
```

The dev server port (`5173`) and this `allowedOrigins` value must stay in
sync. If you ever change the Vite port, update `WebConfig.java` too or every
request from the browser will fail CORS preflight.

## Current state

- **`CourseList`** is the only screen. It fetches `GET /api/courses` via
  `fetchCourses` + `useQuery` and renders the results in a Tailwind/shadcn
  Card-style list. This path works end to end against the running backend.

## Not built yet

- Enrollment screen (`POST /api/enrollments`)
- Lesson completion UI (`POST /api/progress`)
- Dashboard view (`GET /api/dashboard/{userId}`)
- Any routing — `App.tsx` renders one component directly
- **No authentication.** There's no login, and the backend's `User` entity is
  a stand-in (no password, no roles). User ids are passed around directly.
