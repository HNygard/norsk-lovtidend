package no.law;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds the main text of a law. Plain text.
 */
public class LawText {
    private final String lawName;
    List<Chapter> chapters;

    public LawText(String lawName) {
        this.lawName = lawName;
    }

    public String toString() {
        return lawName + "\n\n"
                + chapters.stream()
                .map(Chapter::toString)
                .collect(Collectors.joining("\n\n"));
    }

    public String toHtml() {
        return
                "<div class=\"law\">\n"
                + "<h1 class=\"law-name\">" + lawName + "</h1>\n\n"
                + chapters.stream()
                .map(Chapter::toHtml)
                .collect(Collectors.joining("\n\n"))
                + "\n</div>";
    }

    public static class Chapter {
        String name;
        List<Paragraph> paragraphs;

        public Chapter(String name) {
            this.name = name;
        }

        public String toString() {
            return name + "\n\n" + paragraphs.stream()
                    .map(Paragraph::toString)
                    .collect(Collectors.joining("\n\n"));
        }

        public String toHtml() {
            return
                    "<div class=\"law-chapter\">"
                            + "<h2 class=\"law-chapter-name\">" + name + "</h2>\n\n"
                            + "<div class=\"law-chapter-paragraphs\">\n"
                            + paragraphs.stream()
                            .map(Paragraph::toHtml)
                            .collect(Collectors.joining("\n\n"))
                            + "</div>"
                            + "</div>";
        }
    }

    public static class Paragraph {
        String name;
        List<Section> sections;

        public String toString() {
            return name + "\n\n" + sections.stream()
                    .map(Section::toString)
                    .collect(Collectors.joining("\n\n"));
        }

        public String toHtml() {
            return "<div class=\"law-chapter-paragraph\">" +
                    "<h3 class=\"law-chapter-paragraph-name\">" + name + "</h3>\n\n"
                    + "<div class=\"law-chapter-paragraph-sections\">\n"
                    + sections.stream()
                    .map(Section::toHtml)
                    .collect(Collectors.joining("\n\n"))
                    + "</div>"
                    + "</div>";
        }
    }

    public static class Section {
        String text;

        public Section(String text) {
            this.text = text;
        }

        public String toString() {
            return text;
        }

        public String toHtml() {
            return "<div class=\"law-chapter-paragraph-section\">" + text + "</div>";
        }
    }
}
