package com.scalemotion.tsvbean;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

public class DateTimeType implements DataType<Date>, ParamsAware, BinaryType<Date> {
    private DateTimeFormatter dateFormat;
    private String pattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Date parse(String str) {
        if (str == null || str.equals("null")) {
            return null;
        }
        try {
            return dateFormat.parseDateTime(str).toDate();
        } catch (Exception e) {
            throw new IllegalStateException("Can't parse date '" + str + "' as " + pattern, e);
        }
    }

    @Override
    public String toString(Date object) {
        if (object == null) {
            return "null";
        } else {
            return dateFormat.print(new DateTime(object));
        }
    }

    @Override
    public String describe() {
        return "Date[" + pattern + "]";
    }

    @Override
    public void setParams(String[] args) {
        this.dateFormat = DateTimeFormat.forPattern(args[0]);
        this.pattern = args[0];
    }

    @Override
    public Date read(DataInput in) throws IOException {
        return new Date(in.readLong());
    }

    @Override
    public void write(Date obj, DataOutput out) throws IOException {
        out.writeLong(obj.getTime());
    }
}
