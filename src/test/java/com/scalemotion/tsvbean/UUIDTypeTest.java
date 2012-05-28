package com.scalemotion.tsvbean;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UUIDTypeTest {
    @Test
    public void test() {
        String initial = "04e0daff-7c40-4e41-b50d-f14f313cb7d9";
        UUID uuid = new UUIDType().parse(initial);
        String serialized = new UUIDType().toString(uuid);
        Assert.assertEquals(initial, serialized);
    }
}
