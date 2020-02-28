package no.law.app;

import no.law.lawreference.LawReferenceFinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
public class Ctrl_LawReferenceApi {
    @GetMapping(value = "/api/law-reference")
    public LawReferenceWithLawDto lawRef(@RequestParam String searchQuery) {
        LawReferenceFinder law = LawReferenceFinder.law(searchQuery, LocalDate.now(), null);
        return new LawReferenceWithLawDto(law);
    }

    public static class LawReferenceWithLawDto {
        private final String html;

        LawReferenceWithLawDto(LawReferenceFinder lawRef) {
            this.html = lawRef.getLaw().toHtml();
        }

        public String getHtml() {
            return html;
        }
    }
}
