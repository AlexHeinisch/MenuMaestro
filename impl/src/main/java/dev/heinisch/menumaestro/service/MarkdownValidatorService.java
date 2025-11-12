package dev.heinisch.menumaestro.service;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.springframework.stereotype.Service;

/**
 * Service for sanitizing markdown content.
 * Ensures markdown doesn't contain disallowed elements like links and images.
 */
@Service
public class MarkdownValidatorService {

    private final Parser parser;

    public MarkdownValidatorService() {
        this.parser = Parser.builder().build();
    }

    /**
     * Validates that the markdown content doesn't contain links or images.
     *
     * This service follows a validation approach rather than sanitization:
     * markdown with forbidden elements is rejected entirely rather than having
     * those elements stripped out. This provides clearer feedback to users.
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
