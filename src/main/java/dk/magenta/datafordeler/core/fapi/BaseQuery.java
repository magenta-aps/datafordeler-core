package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Query object specifying a search, with basic filter parameters
 * Subclasses should specify further searchable parameters, annotated with @QueryField.
 * SOAP clients should pass a serialized instance of a Query class to the SOAP interface
 */
public abstract class BaseQuery {

    public static final String[] PARAM_PAGE = new String[] {"side", "page"};
    public static final String[] PARAM_PAGESIZE = new String[] {"sidestoerrelse", "pageSize"};
    public static final String[] PARAM_REGISTRATION_FROM = new String[] {"registreringFra", "registrationFrom"};
    public static final String[] PARAM_REGISTRATION_TO = new String[] {"registreringTil", "registrationTo"};
    public static final String[] PARAM_EFFECT_FROM = new String[] {"virkningFra", "effectFrom"};
    public static final String[] PARAM_EFFECT_TO = new String[] {"virkningTil", "effectTo"};
    public static final String[] PARAM_RECORD_AFTER = new String[] { "opdateretEfter", "recordAfter" };

    public static final String[] PARAM_OUTPUT_WRAPPING = new String[] {"format", "fmt"};
    public static final Map<String, OutputWrapper.Mode> PARAM_OUTPUT_WRAPPING_VALUEMAP = new HashMap<>();
    static {
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("rvd", OutputWrapper.Mode.RVD);
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("rdv", OutputWrapper.Mode.RDV);
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("drv", OutputWrapper.Mode.DRV);
    }

    @QueryField(queryNames = {"side", "page"}, type = QueryField.FieldType.INT)
    protected int page = 1;

    @QueryField(queryNames = {"sidestoerrelse", "pageSize"}, type = QueryField.FieldType.INT)
    protected int pageSize = 10;

    @QueryField(queryNames = {"registreringFra", "registrationFrom"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationFrom = null;

    @QueryField(queryNames = {"registreringTil", "registrationTo"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationTo = null;

    @QueryField(queryNames = {"virkningFra", "effectFrom"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectFrom = null;

    @QueryField(queryNames = {"virkningTil", "effectTo"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectTo = null;

    @QueryField(queryNames = { "opdateretEfter", "recordAfter" }, type = QueryField.FieldType.STRING)
    protected OffsetDateTime recordAfter = null;

    @QueryField(queryName = "UUID", type = QueryField.FieldType.STRING)
    protected List<UUID> uuid = new ArrayList<>();

    private List<String> kommunekodeRestriction = new ArrayList<>();

    private OutputWrapper.Mode mode = null;

    public BaseQuery() {
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit)
     * @param page
     * @param pageSize
     */
    public BaseQuery(int page, int pageSize) {
        this.setPage(page);
        this.setPageSize(pageSize);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * @param page
     * @param pageSize
     */
    public BaseQuery(int page, int pageSize, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this(page, pageSize);
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit). This is the String parameter version; the parameters will be parsed as integers.
     * @param page
     * @param pageSize
     */
    public BaseQuery(String page, String pageSize) {
        this(intFromString(page, 0), intFromString(pageSize, 10), null, null);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * This is the String parameter version; the parameters will be parsed as integers and OffsetDateTimes
     * @param page
     * @param pageSize
     */
    public BaseQuery(String page, String pageSize, String registrationFrom, String registrationTo) {
        this(intFromString(page, 0), intFromString(pageSize, 10), parseDateTime(registrationFrom), parseDateTime(registrationTo));
    }


    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be at least 1");
        }
        this.pageSize = pageSize;
    }

    public void setPageSize(String pageSize) {
        if (pageSize != null) {
            this.pageSize = Integer.parseInt(pageSize);
        }
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1");
        }
        this.page = page;
    }

    public void setPage(String page) {
        if (page != null) {
            this.page = Integer.parseInt(page);
        }
    }

    public int getOffset() {
        return (this.page - 1) * this.pageSize;
    }

    public int getCount() {
        return this.pageSize;
    }

    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.setRegistrationFrom(registrationFrom, null);
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom, OffsetDateTime fallback) {
        this.registrationFrom = registrationFrom;
        if (registrationFrom == null && fallback != null) {
            this.registrationFrom = fallback;
        }
    }

    public void setRegistrationFrom(String registrationFrom) {
        this.setRegistrationFrom(registrationFrom, null);
    }

    public void setRegistrationFrom(String registrationFrom, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationFrom(parseDateTime(registrationFrom), fallback);
    }

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.setRegistrationTo(registrationTo, null);
    }

    public void setRegistrationTo(OffsetDateTime registrationTo, OffsetDateTime fallback) {
        this.registrationTo = registrationTo;
        if (registrationTo == null && fallback != null) {
            this.registrationTo = fallback;
        }
    }

    public void setRegistrationTo(String registrationTo) {
        this.setRegistrationTo(registrationTo, null);
    }

    public void setRegistrationTo(String registrationTo, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationTo(parseDateTime(registrationTo), fallback);
    }

    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.setEffectFrom(effectFrom, null);
    }

    public void setEffectFrom(OffsetDateTime effectFrom, OffsetDateTime fallback) {
        this.effectFrom = effectFrom;
        if (effectFrom == null && fallback != null) {
            this.effectFrom = fallback;
        }
    }

    public void setEffectFrom(String effectFrom) {
        this.setEffectFrom(effectFrom, null);
    }

    public void setEffectFrom(String effectFrom, OffsetDateTime fallback) {
        this.setEffectFrom(parseDateTime(effectFrom), fallback);
    }

    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.setEffectTo(effectTo, null);
    }

    public void setEffectTo(OffsetDateTime effectTo, OffsetDateTime fallback) {
        this.effectTo = effectTo;
        if (effectTo == null && fallback != null) {
            this.effectTo = fallback;
        }
    }

    public void setEffectTo(String effectTo) {
        this.setEffectTo(effectTo, null);
    }

    public void setEffectTo(String effectTo, OffsetDateTime fallback) {
        this.setEffectTo(parseDateTime(effectTo), fallback);
    }

    public OffsetDateTime getRecordAfter() {
        return this.recordAfter;
    }

    public void setRecordAfter(OffsetDateTime recordAfter) {
        this.recordAfter = recordAfter;
    }

    public void setRecordAfter(String recordAfter) throws DateTimeParseException {
        this.recordAfter = parseDateTime(recordAfter);
        if (recordAfter != null) {
            this.increaseDataParamCount();
        }
    }

    public void setUuid(Collection<UUID> uuid) {
        this.uuid = new ArrayList<>(uuid);
    }

    public List<UUID> getUuid() {
        return this.uuid;
    }

    public void addUUID(String uuid) throws InvalidClientInputException {
        if (uuid != null){
            try {
                this.uuid.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                throw new InvalidClientInputException("Invalid uuid "+uuid, e);
            }
        }
    }


    public void addKommunekodeRestriction(String kommunekode) {
        this.kommunekodeRestriction.add(kommunekode);
    }
    public List<String> getKommunekodeRestriction() {
        return this.kommunekodeRestriction;
    }


    public abstract Map<String, Object> getSearchParameters();

    /**
     * Obtain a LookupDefinition object that describes the query in that form.
     * This means a definition where keys are set to the full lookup path for
     * the attribute in question, and values are set from the query.
     * @return
     */
    public abstract BaseLookupDefinition getLookupDefinition();

    public OutputWrapper.Mode getMode() {
        return this.mode;
    }

    public OutputWrapper.Mode getMode(OutputWrapper.Mode fallback) {
        return this.mode != null ? this.mode : fallback;
    }

    /**
     * Parse a ParameterMap from a http request and insert values in this Query object
     * @param parameterMap
     */
    public void fillFromParameters(ParameterMap parameterMap, boolean limitsOnly) throws InvalidClientInputException {
        this.setPage(parameterMap.getFirstOf(PARAM_PAGE));
        this.setPageSize(parameterMap.getFirstOf(PARAM_PAGESIZE));
        try {
            OffsetDateTime now = OffsetDateTime.now();
            this.setRegistrationFrom(parameterMap.getFirstOf(PARAM_REGISTRATION_FROM), now);
            this.setRegistrationTo(parameterMap.getFirstOf(PARAM_REGISTRATION_TO), now);
            this.setEffectFrom(parameterMap.getFirstOf(PARAM_EFFECT_FROM), now);
            this.setEffectTo(parameterMap.getFirstOf(PARAM_EFFECT_TO), now);
            this.setRecordAfter(parameterMap.getFirstOf(PARAM_RECORD_AFTER));
        } catch (DateTimeParseException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
        if (!limitsOnly) {
            this.setFromParameters(parameterMap);
            if (this.getDataParamCount() == 0) {
                throw new InvalidClientInputException("Missing query parameters");
            }
        }
        String modeString = parameterMap.getFirstOf(PARAM_OUTPUT_WRAPPING);
        if (modeString != null) {
            this.mode = PARAM_OUTPUT_WRAPPING_VALUEMAP.get(modeString);
        }
    }

    private int dataParamCount = 0;

    protected void increaseDataParamCount() {
        this.dataParamCount++;
    }

    protected int getDataParamCount() {
        return this.dataParamCount;
    }

    public abstract void setFromParameters(ParameterMap parameterMap) throws InvalidClientInputException;

    /**
     * Convenience method for parsing a String as an integer, without throwing a parseexception
     * @param s String holding integer to be parsed
     * @return Parse result, or null if unparseable
     */
    public static Integer intFromString(String s) {
        return BaseQuery.intFromString(s, null);
    }

    /**
     * Convenience method for parsing a String as an integer, without throwing a parseexception
     * @param s String holding integer to be parsed
     * @param def Fallback value if string is unparseable
     * @return Parse result, or def if unparseable
     */
    public static Integer intFromString(String s, Integer def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Convenience method for parsing a String as a boolean
     * @param s String holding boolean to be parsed ("1", "true", "yes", "0", "false", "no")
     * @return Parse result, or null if neither of the above are found
     */
    public static Boolean booleanFromString(String s) {
        return BaseQuery.booleanFromString(s, null);
    }

    /**
     * Convenience method for parsing a String as a boolean
     * @param s String holding boolean to be parsed ("1", "true", "yes", "0", "false", "no")
     * @param def Fallback value if string doesn't match
     * @return Parse result, or def if neither of the above are found
     */
    public static Boolean booleanFromString(String s, Boolean def) {
        if (s != null) {
            s = s.toLowerCase();
            if (s.equals("1") || s.equals("true") || s.equals("yes")) {
                return true;
            }
            if (s.equals("0") || s.equals("false") || s.equals("no")) {
                return false;
            }
        }
        return def;
    }


    private static DateTimeFormatter[] zonedDateTimeFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.RFC_1123_DATE_TIME,
    };

    private static DateTimeFormatter[] zonedDateFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE,
    };

    private static DateTimeFormatter[] unzonedDateTimeFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    };


    private static DateTimeFormatter[] unzonedDateFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.BASIC_ISO_DATE
    };

    private static int formatterCount = zonedDateTimeFormatters.length + zonedDateFormatters.length + unzonedDateTimeFormatters.length + unzonedDateFormatters.length;

    /**
     * Convenience method for parsing a String as an OffsetDateTime
     * A series of parsers will attempt to parse the input string, returning on the first success.
     * The Parsers, in order, are:
     *   DateTimeFormatter.ISO_OFFSET_DATE_TIME
     *   DateTimeFormatter.ISO_ZONED_DATE_TIME
     *   DateTimeFormatter.ISO_INSTANT
     *   DateTimeFormatter.RFC_1123_DATE_TIME
     *   DateTimeFormatter.ISO_OFFSET_DATE
     *   DateTimeFormatter.ISO_DATE_TIME
     *   DateTimeFormatter.ISO_LOCAL_DATE_TIME
     *   DateTimeFormatter.ISO_DATE
     *   DateTimeFormatter.BASIC_ISO_DATE
     * @param dateTime
     * @return Parsed OffsetDateTime, or null if input was null
     * @throws DateTimeParseException if no parser succeeded on a non-null input string
     */
    public static OffsetDateTime parseDateTime(String dateTime) throws DateTimeParseException {
        if (dateTime != null) {
            for (DateTimeFormatter formatter : zonedDateTimeFormatters) {
                try {
                    return OffsetDateTime.parse(dateTime, formatter);
                } catch (DateTimeParseException e) {
                }
            }
            for (DateTimeFormatter formatter : zonedDateFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDate.from(accessor), LocalTime.MIDNIGHT, ZoneOffset.from(accessor));
                } catch (DateTimeParseException e) {
                }
            }
            for (DateTimeFormatter formatter : unzonedDateTimeFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDateTime.from(accessor), ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                }
            }
            for (DateTimeFormatter formatter : unzonedDateFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDate.from(accessor), LocalTime.MIDNIGHT, ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                }
            }
            throw new DateTimeParseException("Unable to parse date string \""+dateTime+"\", tried "+ formatterCount + " parsers of "+DateTimeFormatter.class.getCanonicalName(), dateTime, 0);
        }
        return null;
    }


    /**
     * Put Query parameters into the Hibernate session. Subclasses should override this and call this method, then
     * put their own Query-subclass-specific parameters in as well
     * @param session Hibernate session in use
     */
    public void applyFilters(Session session) {
        if (this.getRegistrationFrom() != null) {
            this.applyFilter(session, Registration.FILTER_REGISTRATION_FROM, Registration.FILTERPARAM_REGISTRATION_FROM, this.getRegistrationFrom());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATION_AFTER, Monotemporal.FILTERPARAM_REGISTRATION_AFTER, this.getRegistrationFrom());
        }
        if (this.getRegistrationTo() != null) {
            this.applyFilter(session, Registration.FILTER_REGISTRATION_TO, Registration.FILTERPARAM_REGISTRATION_TO, this.getRegistrationTo());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATION_BEFORE, Monotemporal.FILTERPARAM_REGISTRATION_BEFORE, this.getRegistrationFrom());
        }
        if (this.getEffectFrom() != null) {
            this.applyFilter(session, Effect.FILTER_EFFECT_FROM, Effect.FILTERPARAM_EFFECT_FROM, this.getEffectFrom());
            this.applyFilter(session, Bitemporal.FILTER_EFFECT_AFTER, Bitemporal.FILTERPARAM_EFFECT_AFTER, this.getEffectFrom());
        }
        if (this.getEffectTo() != null) {
            this.applyFilter(session, Effect.FILTER_EFFECT_TO, Effect.FILTERPARAM_EFFECT_TO, this.getEffectTo());
            this.applyFilter(session, Bitemporal.FILTER_EFFECT_BEFORE, Bitemporal.FILTERPARAM_EFFECT_BEFORE, this.getEffectTo());
        }
        if (this.getRecordAfter() != null) {
            this.applyFilter(session, DataItem.FILTER_RECORD_AFTER, DataItem.FILTERPARAM_RECORD_AFTER, this.getRecordAfter());
            this.applyFilter(session, Nontemporal.FILTER_LASTUPDATED_AFTER, Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, this.getRecordAfter());
        }
    }

    private void applyFilter(Session session, String filterName, String parameterName, Object parameterValue) {
        if (session.getSessionFactory().getDefinedFilterNames().contains(filterName)) {
            session.enableFilter(filterName).setParameter(parameterName, parameterValue);
        }
    }

}
