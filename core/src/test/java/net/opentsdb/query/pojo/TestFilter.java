// This file is part of OpenTSDB.
// Copyright (C) 2015-2017  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.query.pojo;

import net.opentsdb.query.filter.TagVFilter;
import net.opentsdb.utils.JSON;

import org.junit.Test;

import com.google.common.collect.Lists;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TestFilter {

  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenIdIsNull() throws Exception {
    String json = "{\"id\":null}";
    Filter filter = JSON.parseToObject(json, Filter.class);
    filter.validate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void validationBadId() throws Exception {
    String json = "{\"id\":\"bad.Id\",\"tags\":[]}";
    Filter filter = JSON.parseToObject(json, Filter.class);
    filter.validate();
  }
  
  @Test
  public void deserialize() throws Exception {
    String json = "{\"id\":\"f1\",\"tags\":[{\"tagk\":\"host\","
        + "\"filter\":\"*\",\"type\":\"iwildcard\",\"groupBy\":false}],"
        + "\"explicitTags\":\"true\"}";

    TagVFilter tag = new TagVFilter.Builder().setFilter("*").setGroupBy(
        false)
        .setTagk("host").setType("iwildcard").build();

    Filter expectedFilter = Filter.newBuilder().setId("f1")
        .setTags(Arrays.asList(tag)).setExplicitTags(true).build();

    Filter filter = JSON.parseToObject(json, Filter.class);
    filter.validate();
    assertEquals(expectedFilter, filter);
  }

  @Test
  public void serialize() throws Exception {
    TagVFilter tag = new TagVFilter.Builder().setFilter("*").setGroupBy(false)
        .setTagk("host").setType("iwildcard").build();

    Filter filter = Filter.newBuilder().setId("f1")
        .setTags(Arrays.asList(tag)).setExplicitTags(true).build();

    String actual = JSON.serializeToString(filter);
    assertTrue(actual.contains("\"id\":\"f1\""));
    assertTrue(actual.contains("\"tags\":["));
    assertTrue(actual.contains("\"tagk\":\"host\""));
    assertTrue(actual.contains("\"explicitTags\":true"));
  }

  @Test
  public void unknownShouldBeIgnored() throws Exception {
    String json = "{\"id\":\"1\",\"unknown\":\"yo\"}";
    JSON.parseToObject(json, Filter.class);
    // pass if no unexpected exception
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidTags() throws Exception {
    String json = "{\"id\":\"1\",\"tags\":[{\"tagk\":\"\","
        + "\"filter\":\"*\",\"type\":\"iwildcard\",\"group_by\":false}],"
        + "\"aggregation\":{\"tags\":[\"appid\"],\"aggregator\":\"sum\"}}";

    Filter filter = JSON.parseToObject(json, Filter.class);
    filter.validate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidAggregation() throws Exception {
    String json = "{\"id\":\"1\",\"tags\":[{\"tagk\":\"\","
        + "\"filter\":\"*\",\"type\":\"iwildcard\",\"group_by\":false}],"
        + "\"aggregator\":\"what\"}";
    Filter filter = JSON.parseToObject(json, Filter.class);
    filter.validate();
  }

  public void hashCodeEqualsCompareTo() throws Exception {
    final Filter f1 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    
    Filter f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertEquals(f1.hashCode(), f2.hashCode());
    assertEquals(f1, f2);
    assertEquals(0, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f2")  // <-- diff
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(-1, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(true)  // <-- diff
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(1, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web02")  // <-- diff
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(-1, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(true)  // <-- diff
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(1, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()  // <-- order changes, should be good!
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build(),
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(false)
              .build())
            )
        .build();
    assertEquals(f1.hashCode(), f2.hashCode());
    assertEquals(f1, f2);
    assertEquals(0, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            new TagVFilter.Builder()
              .setFilter("web01")
              .setTagk("host")
              .setType("literal_or")
              .setGroupBy(true)  // <-- diff
              .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(1, f1.compareTo(f2));
    
    f2 = new Filter.Builder()
        .setId("f1")
        .setExplicitTags(false)
        .setTags(Lists.newArrayList(
            //new TagVFilter.Builder()  // <-- diff
            //  .setFilter("web01")
            //  .setTagk("host")
            //  .setType("literal_or")
            //  .setGroupBy(false)
            //  .build(),
            new TagVFilter.Builder()
              .setFilter("phx*")
              .setTagk("dc")
              .setType("wildcard")
              .setGroupBy(true)
              .build()))
        .build();
    assertNotEquals(f1.hashCode(), f2.hashCode());
    assertNotEquals(f1, f2);
    assertEquals(-1, f1.compareTo(f2));
  }
  
}
