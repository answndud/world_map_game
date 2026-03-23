# Blog Update Rules

Use this file only when deciding whether to create or update a `blog/` post.

## Write a blog update when

- A completed feature slice changed API, domain model, request flow, game loop, ranking flow, or test surface
- The work has real files, real classes, and real tests to explain
- A beginner can learn something concrete from this slice
- The user explicitly asks for a blog-style write-up

## Do not write a blog update when

- The work is only planning or brainstorming
- The change is a tiny wording fix, very small CSS tweak, or other low-signal patch
- The implementation is not finished enough to explain clearly
- The post would repeat `README.md` without code evidence

## Blog post minimum evidence

Do not draft a WorldMap implementation post unless you can point to:

- Real file paths
- Real class names
- At least one real method or request flow
- At least one test or explicit missing-test note

## Blog scope rule

Prefer one clear post per milestone:

- Bootstrap
- Country data
- Game session / round model
- Location game
- Population game
- Ranking
- Recommendation engine
- LLM explanation

Do not combine too many unrelated milestones into one post.

## Operating rule for this repository

- For `/Users/alex/project/worldmap`, meaningful feature work should default to `code + tests + docs + blog` in the same turn.
- If you skip a blog update, leave a short reason in the work log or final summary.
