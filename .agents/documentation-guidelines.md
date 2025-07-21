# Documentation & comments

## Commenting guidelines
- Avoid inline comments in production code unless necessary.
- Inline comments are helpful in tests.
- When using TODO comments, follow the format on the [dedicated page][todo-comments].
- File and directory names should be formatted as code.

## KDoc style
- Write concise descriptions for all public and internal APIs.
- Start parameter descriptions with capital letters.
- End parameter descriptions with periods.
- Use inline code with backticks for code references (`example`).
- Format code blocks with fences and language identifiers:
  ```kotlin
  // Example code
  fun example() {
      // Implementation
  }
  ```

### External links in `@see` tag

- The `@see` tag is for [referencing API elements](https://kotlinlang.org/docs/kotlin-doc.html#see-identifier).
- External links are [not officially supported](https://github.com/Kotlin/dokka/issues/518).
- External links CAN be added when migrating from Javadoc.
- Format is:
  ```kotlin
  /**
   * Documentation text.
   *
   * @see <a href="https://my.site.com/my-page.html">Link title</a>
   */
  ```

### KDoc tasks
- Remove `<p>` tags in the line with text: `"<p>This"` -> `"This"`.
- Replace `<p>` with empty line if the tag is the only text in the line.

## Using periods
- Use periods at the end of complete sentences.
- Use periods for full or multi-clause bullets.
- Use NO periods for short bullets.
- Use NO periods for fragments.
- Use NO periods in titles and headers.
- Use NO periods in parameter descriptions in Javadoc.
- DO USE periods in parameter and property descriptions in KDoc.
- Be consistent within the list!

[todo-comments]: https://github.com/SpineEventEngine/documentation/wiki/TODO-comments
