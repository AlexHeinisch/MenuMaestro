package dev.heinisch.menumaestro.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link MarkdownSanitizerService}.
 * Tests validation of markdown content to ensure links and images are properly detected and rejected.
 */
class MarkdownSanitizerServiceTest {

    private MarkdownSanitizerService service;

    @BeforeEach
    void setUp() {
        service = new MarkdownSanitizerService();
    }

    @Test
    void validateMarkdown_withLinks_throwsException() {
        String markdownWithLink = "Check out [this link](https://example.com) for more info.";

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithLink))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Links are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withImages_throwsException() {
        String markdownWithImage = "Here is an image: ![alt text](https://example.com/image.png)";

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Images are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withValidMarkdown_succeeds() {
        String validMarkdown = """
                # Heading 1
                ## Heading 2
                ### Heading 3

                This is **bold** and this is *italic*.

                - Bullet point 1
                - Bullet point 2

                1. Numbered item 1
                2. Numbered item 2

                > This is a blockquote

                `inline code` and code blocks:

                ```
                public void example() {
                    System.out.println("Hello");
                }
                ```

                ---

                Emojis are allowed: 😀 🎉 ⭐
                """;

        assertThatCode(() -> service.validateMarkdown(validMarkdown))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withNull_succeeds() {
        assertThatCode(() -> service.validateMarkdown(null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withEmptyString_succeeds() {
        assertThatCode(() -> service.validateMarkdown(""))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withBlankString_succeeds() {
        assertThatCode(() -> service.validateMarkdown("   \n\t  "))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withNestedStructures_detectsLinks() {
        String markdownWithNestedLink = """
                # Main Heading

                Some content here.

                > This is a quote with a [nested link](https://example.com) inside

                More content.
                """;

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithNestedLink))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Links are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withNestedStructures_detectsImages() {
        String markdownWithNestedImage = """
                # Main Heading

                - First item
                - Second item with image: ![image](https://example.com/img.png)
                - Third item
                """;

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithNestedImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Images are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withLinkInCodeBlock_doesNotThrow() {
        // CommonMark parser correctly does not parse links inside code blocks
        String markdownWithLinkInCode = """
                Some text before

                ```
                [link](https://example.com)
                ```
                """;

        // Links in code blocks are treated as literal text, not as links
        assertThatCode(() -> service.validateMarkdown(markdownWithLinkInCode))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withLinkInInlineCode_doesNotThrow() {
        String markdownWithLinkInInlineCode = "Here is some `[link](https://example.com)` code";

        // Links in inline code are treated as literal text, not as links
        assertThatCode(() -> service.validateMarkdown(markdownWithLinkInInlineCode))
                .doesNotThrowAnyException();
    }

    @Test
    void validateMarkdown_withMultipleLinks_throwsExceptionOnFirst() {
        String markdownWithMultipleLinks = """
                [First link](https://example1.com)
                [Second link](https://example2.com)
                """;

        // Should throw on the first link encountered
        assertThatThrownBy(() -> service.validateMarkdown(markdownWithMultipleLinks))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Links are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withBothLinkAndImage_throwsException() {
        String markdownWithBoth = """
                [link](https://example.com)
                ![image](https://example.com/img.png)
                """;

        // Should throw on the first forbidden element encountered
        assertThatThrownBy(() -> service.validateMarkdown(markdownWithBoth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withReferenceStyleLink_throwsException() {
        String markdownWithReferenceLink = """
                [link text][ref]

                [ref]: https://example.com
                """;

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithReferenceLink))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Links are not allowed in markdown content");
    }

    @Test
    void validateMarkdown_withAutomaticLink_throwsException() {
        String markdownWithAutoLink = "Visit <https://example.com> for more info";

        assertThatThrownBy(() -> service.validateMarkdown(markdownWithAutoLink))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Links are not allowed in markdown content");
    }
}
