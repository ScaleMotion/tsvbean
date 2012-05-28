package com.scalemotion.tsvbean;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

public class BeanSerializerTest {
    private static final Logger LOG = Logger.getLogger(BeanSerializer.class);

    public static class Bean1 {
        @Field(name = "eventDate", type = DateTimeType.class, args = "yyyy-MM-dd HH:mm:ss")
        private Date date = new Date();
        @Field(name = "s2")
        private int i = 666;
        @Field(name = "nullString")
        private String nullString = "null";
        @Field
        private String nullString2 = null;
        @EmbeddedDataField
        private Bean2 em1 = new Bean2();
        @Field
        private Boolean b2;
        @EmbeddedDataField(fieldPrefix = "second_")
        private Bean2 em2 = new Bean2();
        @Field
        private boolean b4;
        @EmbeddedDataField(fieldPrefix = "nullEmbedded_")
        private Bean2 em3 = null;
    }

    public static class Bean2 {
        @Field
        private Double d1 = 0.5;
        @Field
        private Float f3 = 0.6f;
        @EmbeddedDataField
        private Bean3 bean3 = new Bean3();
    }

    public static class Bean3 {
        @Field
        private Double d11 = 0.55;
        @Field
        private Float d12 = 0.66f;
    }

    public static class EnumContainer {
        @Field(args = "id")
        private EnumWithName withName = EnumWithName.ONE;
        @Field
        private EnumNoName noName = EnumNoName.TWO;
    }

    public static enum EnumWithName {
        ONE(1), TWO(2);
        private int id;

        EnumWithName(int id) {
            this.id = id;
        }
    }

    public static enum EnumNoName {
        ONE, TWO
    }

    @Test
    public void testEnums() {
        BeanSerializer<EnumContainer> mapper = new BeanSerializer<EnumContainer>(EnumContainer.class);
        String row = mapper.toString(new EnumContainer());
        Assert.assertEquals("1\tTWO", row);
        EnumContainer parsed = mapper.parse(row);
        Assert.assertEquals(EnumWithName.ONE, parsed.withName);
        Assert.assertEquals(EnumNoName.TWO, parsed.noName);
    }

    @Test
    public void testComplicatedCase() {
        BeanSerializer<Bean1> mapper = new BeanSerializer<Bean1>(Bean1.class);
        String[] header = mapper.getHeader();
        LOG.info("Header: " + Arrays.toString(header));
        Bean1 original = new Bean1();
        String row = mapper.toString(original);
        LOG.info("Field: " + row);
        Assert.assertEquals(Iterables.size(Splitter.on("\t").split(row)), header.length);
        Bean1 parsed = mapper.parse(row);
        String row2 = mapper.toString(parsed);
        LOG.info("Field: " + row2);
        Assert.assertEquals(row, row2);
    }

    @Test
    public void testHeader() {
        BeanSerializer<Bean1> mapper = new BeanSerializer<Bean1>(Bean1.class);
        mapper.setHeader(new String[]{"second_d1", "eventDate"});
        Bean1 parsed = mapper.parse("0.1\t2011-12-12 14:08:01");
        Assert.assertEquals(parsed.em2.d1, 0.1, 0.00001);
        Assert.assertNotNull(parsed.date);
    }

}




