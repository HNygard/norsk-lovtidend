package no.law;

import no.law.lawreference.LawReferenceFinder;

import java.util.Collection;

public interface LawReference {
    public String toString();
    public String toHtml();
    public boolean isMatchinLawRef(LawReferenceFinder lawRef);
    public Collection<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef);
}
