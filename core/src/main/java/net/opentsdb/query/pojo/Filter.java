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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import net.opentsdb.core.Const;
import net.opentsdb.query.filter.TagVFilter;

import java.util.Collections;
import java.util.List;

/**
 * Pojo builder class used for serdes of a filter component of a query
 * @since 2.3
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = Filter.Builder.class)
public class Filter extends Validatable implements Comparable<Filter> {
  /** The id of the filter set to use in a metric query */
  private String id;
  
  /** The list of filters in the filter set */
  private List<TagVFilter> tags;
  
  /** Whether or not to only fetch series with exactly the same tag keys as 
   * in the filter list. */
  private boolean explicit_tags;
  
  /**
   * Default ctor
   * @param builder The builder to pull values from
   */
  private Filter(Builder builder) {
    this.id = builder.id;
    this.tags = builder.tags;
    this.explicit_tags = builder.explicitTags;
  }

  /** @return the id of the filter set to use in a metric query */
  public String getId() {
    return id;
  }

  /** @return the list of filters in the filter set */
  public List<TagVFilter> getTags() {
    return tags;
  }

  /** @return Whether or not to only fetch series with exactly the same tag keys as 
   * in the filter list. */
  public boolean getExplicitTags() {
    return explicit_tags;
  }
  
  /** @return A new builder for the filter */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Validates the filter set
   * @throws IllegalArgumentException if one or more parameters were invalid
   */
  public void validate() {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("Missing or empty id");
    }
    Query.validateId(id);
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final Filter filter = (Filter) o;

    return Objects.equal(id, filter.id)
        && Objects.equal(tags, filter.tags)
        && Objects.equal(explicit_tags, filter.explicit_tags);
  }

  @Override
  public int hashCode() {
    return buildHashCode().asInt();
  }
  
  /** @return A HashCode object for deterministic, non-secure hashing */
  public HashCode buildHashCode() {
    final HashCode hc = Const.HASH_FUNCTION().newHasher()
        .putString(Strings.nullToEmpty(id), Const.UTF8_CHARSET)
        .putBoolean(explicit_tags)
        .hash();
    final List<HashCode> hashes = Lists.newArrayListWithCapacity(tags.size() + 1);
    hashes.add(hc);
    for (final TagVFilter filter : tags) {
      hashes.add(filter.buildHashCode());
    }
    return Hashing.combineOrdered(hashes);
  }

  @Override
  public int compareTo(final Filter o) {
    return ComparisonChain.start()
        .compare(id, o.id, Ordering.natural().nullsFirst())
        .compareTrueFirst(explicit_tags, o.explicit_tags)
        .compare(tags, o.tags, 
            Ordering.<TagVFilter>natural().lexicographical().nullsFirst())
        .result();
  }

  /**
   * A builder for the downsampler component of a query
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
  public static final class Builder {
    @JsonProperty
    private String id;
    @JsonProperty
    private List<TagVFilter> tags;
    @JsonProperty
    private boolean explicitTags;
    
    public Builder setId(String id) {
      Query.validateId(id);
      this.id = id;
      return this;
    }

    public Builder setTags(List<TagVFilter> tags) {
      this.tags = tags;
      if (tags != null) {
        Collections.sort(this.tags);
      }
      return this;
    }

    public Builder setExplicitTags(boolean explicit_tags) {
      this.explicitTags = explicit_tags;
      return this;
    }
    
    public Filter build() {
      return new Filter(this);
    }
  }
}
