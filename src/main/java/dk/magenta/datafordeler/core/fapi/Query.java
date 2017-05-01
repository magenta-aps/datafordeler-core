package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;

import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Created by lars on 19-04-17.
 */
public class Query {

    protected int page = 0;
    protected int pageSize = 10;
    protected OffsetDateTime registrationFrom = null;
    protected OffsetDateTime registrationTo = null;

    public Query() {
    }

    public Query(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
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
        this.page = page;
    }

    public void setPage(String page) {
        if (page != null) {
            this.page = Integer.parseInt(page);
        }
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

    protected static int intFromString(String s, int def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }


    private static DateTimeFormatter[] parseFormats = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.BASIC_ISO_DATE
    };

    private static OffsetDateTime parseDateTime(String dateTime) throws DateTimeParseException {
        if (dateTime != null) {
            for (DateTimeFormatter formatter : parseFormats) {
                try {
                    return OffsetDateTime.parse(dateTime, formatter);
                } catch (DateTimeParseException e) {
                    // try next parser
                }
            }
            throw new DateTimeParseException("Unable to parse date string, tried "+parseFormats.length+" parsers of "+DateTimeFormatter.class.getCanonicalName(), dateTime, 0);
        }
        return null;
    }

}
