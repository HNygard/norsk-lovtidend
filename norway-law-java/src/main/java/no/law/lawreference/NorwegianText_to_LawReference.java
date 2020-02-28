package no.law.lawreference;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NorwegianText_to_LawReference {
    /**
     * The text should only contain one reference.
     */
    public static LawReferenceFinder textToLawReference(String text, LocalDate date) {
        LawReferenceFinder ref = new LawReferenceFinder();
        text = " " + text + " ";
        Pattern pattern = Pattern.compile(" ((§|paragraf)\\s?([0-9]*)) ");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            ref.addParagraph("§ " + matcher.group(3));
            text = text.replace(matcher.group(1), "");
        }

        pattern = Pattern.compile(" (([a-zæøåA-ZÆØÅ]*) ledd) ");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            ref.addSection(matcher.group(2));
            text = text.replace(matcher.group(1), "");
        }

        try {
            ref.law(text.trim(), date, null);
        }
        catch (LawReferenceFinder.LawNotFoundException_LawIdInvalid
                | LawReferenceFinder.LawNotFoundException_LawIdNotFound
                | LawReferenceFinder.LawNotFoundException_ControlNameDoesNotMatch e) {
            // No match.
        }

        return ref;
    }
}
