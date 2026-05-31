---
name: docs
description:
    Use when editing Vico guide documentation, Markdown guide prose,
    API-reference-linked prose, or parallel Compose and Android Views
    documentation pages. Applies to wording changes in guide/ and documentation
    copy for Vico's Compose Multiplatform and Android Views stacks.
---

# Docs

## Guide Prose Rules

When editing Vico guide prose:

1. Link first mentions on each page. The first mention of a code element should
   link to the API reference.
2. Avoid nounifying code names. Do not use code element names as generic count
   nouns, such as "a `Foo`," "the `Bar`," or plural forms like "`Baz`s." Attach
   articles to generic nouns instead, such as "a `Foo` instance."
3. Distinguish classes from instances. If prose refers to an object, say
   "instance" or use a generic noun such as "chart," "model," "layer," "marker,"
   or "transaction." Introduce the code element name separately when needed.
4. Prefer generic nouns for concepts. Use ordinary language for the concept
   under discussion, and reserve code names for precise API references.
5. Keep terminology parallel. In comparisons and lists, phrase equivalent ideas
   symmetrically instead of mixing generic terms on one side and code names on
   the other.
6. Apply wording changes across stacks. If equivalent Compose and Android Views
   guide pages exist, keep the wording aligned unless a platform difference
   requires different prose.

## Editing Workflow

1. Identify whether the target page has an equivalent page under both
   `guide/**/compose/` and `guide/**/views/`.
2. Make matching prose changes in both stack-specific pages when the behavior is
   shared.
3. Check first mentions of code elements after edits, because moved or rewritten
   text can change which mention is first on the page.
4. Review the edited paragraphs for code-name nounification and for
   class-versus-instance ambiguity before finishing.
