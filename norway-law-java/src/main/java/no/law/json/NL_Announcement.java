package no.law.json;

public class NL_Announcement {
    /**
     * Example:
     * https://lovdata.no/dokument/LTI/lov/2020-01-10-1
     */
    public String url;

    /**
     * Example:
     * Lov om endringer i politiloven mv. (bevæpnet patruljering mellom sårbare objekter mv.)
     */
    public String title;

    /**
     * Example:
     * LOV-2020-01-10-1
     */
    public String dato;

    /**
     * Example:
     * Justis- og beredskapsdepartementet
     */
    public String departement;

    /**
     * Example:
     * Kongen bestemmer
     */
    public String ikrafttredelse;

    /**
     * Example:
     * LOV-1961-06-09-1, LOV-1995-08-04-53
     */
    public String endrer;

    /**
     * Example:
     * 10.01.2020   kl. 14.30
     */
    public String kunngjort;

    /**
     * Example:
     * 2019-1229
     */
    public String journalnr;

    /**
     * Example:
     * Endringslov til politiloven mv.
     */
    public String korttittel;

    public NL_AnnouncementType type;
}
