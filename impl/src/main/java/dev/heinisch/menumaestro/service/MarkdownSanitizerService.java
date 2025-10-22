package dev.heinisch.menumaestro.service;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.springframework.stereotype.Service;

/**
 * Service for sanitizing markdown content.
 * Ensures markdown doesn't contain disallowed elements like links and images.
 */
@Service
public class MarkdownSanitizerService {

    private final Parser parser;

    public MarkdownSanitizerService() {
        this.parser = Parser.builder().build();
    }

    /**
     * Validates that the markdown content doesn't contain links or images.
     *
     * @param markdown the markdown content to validate
     * @throws IllegalArgumentException if the markdown contains links or images
     */
    public void validateMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return;
        }

        Node document = parser.parse(markdown);
        validateNode(document);
    }

    /**
     * Sanitizes markdown by removing links and images while preserving other formatting.
     *
     * @param markdown the markdown content to sanitize
     * @return sanitized markdown content
     */
    public String sanitizeMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return markdown;
        }

        // For now, we'll use validation approach - reject markdown with links/images
        // This is simpler and gives clear feedback to users
        validateMarkdown(markdown);
        return markdown;
    }

    private void validateNode(Node node) {
        if (node instanceof Image) {
            throw new IllegalArgumentException("Images are not allowed in markdown content");
        }
        if (node instanceof Link) {
            throw new IllegalArgumentException("Links are not allowed in markdown content");
        }

        Node child = node.getFirstChild();
        while (child != null) {
            validateNode(child);
            child = child.getNext();
        }
    }
}
