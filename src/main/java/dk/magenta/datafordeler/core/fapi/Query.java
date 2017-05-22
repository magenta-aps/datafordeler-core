package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;

import javax.ws.rs.core.MultivaluedMap;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-04-17.
 */
public abstract class Query {

    protected int page = 1;
    protected int pageSize = 10;
    protected OffsetDateTime registrationFrom = null;
    protected OffsetDateTime registrationTo = null;
    protected OffsetDateTime effectFrom = null;
    protected OffsetDateTime effectTo = null;

    public Query() {
    }

    public Query(int page, int pageSize) {
        this.setPage(page);
        this.setPageSize(pageSize);
    }

    public Query(int page, int pageSize, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this(page, pageSize);
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    public Query(String page, String pageSize) {
        this(intFromString(page, 0), intFromString(pageSize, 10), null, null);
    }

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

    public abstract void setFromParameters(ListHashMap<String, String> parameters);

    protected static Integer intFromString(String s) {
        return Query.intFromString(s, null);
    }
    protected static Integer intFromString(String s, Integer def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected static Boolean booleanFromString(String s) {
        return Query.booleanFromString(s, null);
    }
    protected static Boolean booleanFromString(String s, Boolean def) {
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

    protected static OffsetDateTime parseDateTime(String dateTime) throws DateTimeParseException {
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

}
