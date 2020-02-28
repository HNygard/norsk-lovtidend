package no.law;

import no.law.lawreference.LawReferenceFinder;

import java.util.List;

public interface LawReference {
    public String toString();
    public String toHtml();
    public boolean isMatchinLawRef(LawReferenceFinder lawRef);
    public List<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef);
}
