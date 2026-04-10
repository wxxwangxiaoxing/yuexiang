# Module Boundary Rules

## Core Rules

1. `biz` modules must not depend on other `biz` modules.
2. Cross-module collaboration must go through `*-api`.
3. Infrastructure capability belongs in `yuexiang-framework`.
4. Business status codes and type codes belong in `constant` or `constants`.
5. Object assembly belongs in `assembler`.
6. Cache, Redis, MQ, ES, and third-party integration details belong in `support`.

## Package Conventions

- Service implementations use `...service.impl`
- Assemblers use `...assembler`
- Supports use `...support`
- Constants use `...constant` or `...constants`

## Enforcement

These rules are enforced by tests in [`ModuleArchitectureRulesTest.java`](D:/yuexiang/yuexiang-server/src/test/java/com/yuexiang/server/architecture/ModuleArchitectureRulesTest.java).

If a new module or package pattern is introduced, update the architecture test first and then add the new code.
