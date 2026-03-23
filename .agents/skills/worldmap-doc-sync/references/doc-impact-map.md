# Document Impact Map

Use this file to decide which WorldMap docs need updates after a task.

## Always Update

### `docs/WORKLOG.md`

Update this file after any meaningful change:

- Code implementation
- Test addition or deletion
- Architecture decision
- Workflow or documentation structure change

## Update Conditionally

### `docs/PORTFOLIO_PLAYBOOK.md`

Update when one of these changed:

- Current phase status
- Completion criteria
- Next recommended implementation step
- What the developer must understand for that phase

Do not update it for every small code edit.

### `README.md`

Update when one of these changed:

- Project concept or scope
- Public architecture explanation
- Core domain model
- API outline
- Implementation order or major milestone wording

Do not use it as a running diary.

### `docs/AI_AGENT_OPERATING_MODEL.md`

Update when one of these changed:

- How the project uses AI agents
- Which custom skills exist
- When to use subagents
- LLM integration rules for the service itself

### `blog/`

Update when one of these is true:

- The user explicitly asks for a blog-style explanation
- A completed feature slice has real code/test evidence and changes API, domain, request flow, ranking, or game loop
- A milestone is complete and can teach a beginner something concrete

Do not create implementation posts from plans alone.

## Decision Table

| Change type | Work Log | Playbook | README | Blog |
| --- | --- | --- | --- | --- |
| Small code fix | Yes | Usually no | No | No |
| New feature slice | Yes | Maybe | Maybe | Yes |
| Phase completion | Yes | Yes | Maybe | Maybe |
| Architecture change | Yes | Yes | Yes | Maybe |
| Skill / agent policy change | Yes | Maybe | No | No |
| Docs-only editorial cleanup | Maybe | No | Maybe | No |

## Tie-break Rule

If unsure:

1. Update `docs/WORKLOG.md`
2. Leave higher-level docs unchanged unless the project explanation actually changed
