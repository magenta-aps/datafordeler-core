package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.LookupDefinition;

import java.lang.reflect.Field;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Query object specifying a search, with basic filter parameters
 * Subclasses should specify further searchable parameters, annotated with @QueryField.
 * SOAP clients should pass a serialized instance of a Query class to the SOAP interface
 * Created by lars on 19-04-17.
 */
public abstract class Query<E extends Entity> {

    @QueryField(queryName = "page", type = QueryField.FieldType.INT)
    protected int page = 1;

    @QueryField(queryName = "pageSize", type = QueryField.FieldType.INT)
    protected int pageSize = 10;

    @QueryField(queryName = "registrationFrom", type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationFrom = null;

    @QueryField(queryName = "registrationTo", type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationTo = null;

    @QueryField(queryName = "effectFrom", type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectFrom = null;

    @QueryField(queryName = "effectTo", type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectTo = null;

    public Query() {
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit)
     * @param page
     * @param pageSize
     */
    public Query(int page, int pageSize) {
        this.setPage(page);
        this.setPageSize(pageSize);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * @param page
     * @param pageSize
     */
    public Query(int page, int pageSize, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this(page, pageSize);
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit). This is the String parameter version; the parameters will be parsed as integers.
     * @param page
     * @param pageSize
     */
    public Query(String page, String pageSize) {
        this(intFromString(page, 0), intFromString(pageSize, 10), null, null);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * This is the String parameter version; the parameters will be parsed as integers and OffsetDateTimes
     * @param page
     * @param pageSize
     */
    public Query(String page, String pageSize, String registrationFrom, String registrationTo) {
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
        this.registrationFrom = registrationFrom;
    }

    public void setRegistrationFrom(String registrationFrom) throws DateTimeParseException {
        this.registrationFrom = parseDateTime(registrationFrom);
    }

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
    }

    public void setRegistrationTo(String registrationTo) throws DateTimeParseException {
        this.registrationTo = parseDateTime(registrationTo);
    }

    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = effectFrom;
    }

    public void setEffectFrom(String effectFrom) {
        this.effectFrom = parseDateTime(effectFrom);
    }

    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = effectTo;
    }

    public void setEffectTo(String effectTo) {
        this.effectTo = parseDateTime(effectTo);
    }

    public abstract Map<String, Object> getSearchParameters();

    public LookupDefinition getLookupDefinition() {
        return new LookupDefinition(this.getSearchParameters(), this);
    }

    public abstract void setFromParameters(ParameterMap parameters);

    /**
     * Convenience method for parsing a String as an integer, without throwing a parseexception
     * @param s String holding integer to be parsed
     * @return Parse result, or null if unparseable
     */
    public static Integer intFromString(String s) {
        return Query.intFromString(s, null);
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
        return Query.booleanFromString(s, null);
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
            throw new DateTimeParseException("Unable to parse date string, tried "+ zonedDateTimeFormatters.length+" parsers of "+DateTimeFormatter.class.getCanonicalName(), dateTime, 0);
        }
        return null;
    }

    /**
     * Subclasses should return the EntityClass that the Query class pertains to
     * @return
     */
    public abstract Class<E> getEntityClass();


    /**
     * Subclasses should return the base Data class that the Query class pertains to
     * @return
     */
    public abstract Class getDataClass();

    /**
     * Returns a Field for database query building
     * Subclass fields are exposed in this manner, so the database query manager can inspect them and cast search fields based on their annotations
     * @param fieldName A valid field (class member) name on the Query subclass
     * @return
     * @throws NoSuchFieldException
     */
    public Field getField(String fieldName) throws NoSuchFieldException {
        for (Class cls = this.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                return cls.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    protected String getSubtable(String key) {
        return null;
    }

    protected Set<String> getSubtables() {
        return null;
    }

    public String getJoins(String root) {
        StringJoiner s = new StringJoiner(" join ");
        Set<String> subtables = this.getSubtables();
        if (subtables != null) {
            for (String table : this.getSubtables()) {
                s.add(root + "." + table + " as " + root + "_" + table);
            }
        }
        return s.toString();
    }

}
