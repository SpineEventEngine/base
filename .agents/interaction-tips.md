# 💬 Interaction tips – key to effective collaboration!

- Human programmers may use inline comments to guide agents:
  ```kotlin
    // ChatGPT: Suggest a refactor for better readability.
    // Codex: Complete the missing branches in this `when` block.
    // ChatGPT: explain this logic.
    // Codex: complete this function.
   ```
- Agents should ensure pull request messages are concise and descriptive:
  ```text
  feat(chatgpt): suggested DSL refactoring for query handlers  
  fix(codex): completed missing case in sealed class hierarchy
  ```
- Encourage `// TODO:` or `// FIXME:` comments to be clarified by ChatGPT.

- When agents or humans add TODO comments, they **must** follow the format described on
  the [dedicated page][todo-comments].

[todo-comments]: https://github.com/SpineEventEngine/documentation/wiki/TODO-comments
