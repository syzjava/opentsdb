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

import java.util.List;
import java.util.NoSuchElementException;

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

import net.opentsdb.core.Aggregators;
import net.opentsdb.core.Const;
import net.opentsdb.utils.DateTime;

/**
 * Pojo builder class used for serdes of the downsampler component of a query
 * 
 * @since 2.3
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = Downsampler.Builder.class)
public class Downsampler extends Validatable implements Comparable<Downsampler> {
  /** The relative interval with value and unit, e.g. 60s */
  private String interval;
  
  /** The aggregator to use for downsampling */
  private String aggregator;
  
  /** A fill policy for downsampling and working with missing values */
  private NumericFillPolicy fill_policy;
  
  /**
   * Default ctor
   * @param builder The builder to pull values from
   */
  protected Downsampler(final Builder builder) {
    interval = builder.interval;
    aggregator = builder.aggregator;
    fill_policy = builder.fillPolicy;
  }
  
  /** @return A new builder for the downsampler */
  public static Builder newBuilder() {
    return new Builder();
  }
  
  /** Validates the downsampler
   * @throws IllegalArgumentException if one or more parameters were invalid
   */
  public void validate() {
    if (interval == null || interval.isEmpty()) {
      throw new IllegalArgumentException("Missing or empty interval");
    }
    DateTime.parseDuration(interval);
    
    if (aggregator == null || aggregator.isEmpty()) {
      throw new IllegalArgumentException("Missing or empty aggregator");
    }
    try {
      Aggregators.get(aggregator.toLowerCase());
    } catch (final NoSuchElementException e) {
      throw new IllegalArgumentException("Invalid aggregator");
    }
    
    if (fill_policy != null) {
      fill_policy.validate();
    }
  }
  
  /** @return the interval for the downsampler */
  public String getInterval() {
    return interval;
  }
  
  /** @return the name of the aggregator to use */
  public String getAggregator() {
    return aggregator;
  }
  
  /** @return the fill policy to use */
  public NumericFillPolicy getFillPolicy() {
    return fill_policy;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final Downsampler downsampler = (Downsampler) o;

    return Objects.equal(interval, downsampler.interval)
        && Objects.equal(aggregator, downsampler.aggregator)
        && Objects.equal(fill_policy, downsampler.fill_policy);
  }
  
  @Override
  public int hashCode() {
    return buildHashCode().asInt();
  }

  /** @return A HashCode object for deterministic, non-secure hashing */
  public HashCode buildHashCode() {
    final HashCode hc = Const.HASH_FUNCTION().newHasher()
        .putString(Strings.nullToEmpty(interval), Const.ASCII_CHARSET)
        .putString(Strings.nullToEmpty(aggregator), Const.ASCII_CHARSET)
        .hash();
    final List<HashCode> hashes = Lists.newArrayListWithCapacity(2);
    hashes.add(hc);
    if (fill_policy != null) {
      hashes.add(fill_policy.buildHashCode());
    }
    return Hashing.combineOrdered(hashes);
  }
  
  @Override
  public int compareTo(final Downsampler o) {
    return ComparisonChain.start()
        .compare(interval, o.interval, Ordering.natural().nullsFirst())
        .compare(aggregator, o.aggregator, Ordering.natural().nullsFirst())
        .compare(fill_policy, o.fill_policy, Ordering.natural().nullsFirst())
        .result();
  }
  
  /**
   * A builder for the downsampler component of a query
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
  public static final class Builder {
    @JsonProperty
    private String interval;
    @JsonProperty
    private String aggregator;
    @JsonProperty
    private NumericFillPolicy fillPolicy;
    
    public Builder setInterval(String interval) {
      this.interval = interval;
      return this;
    }
    
    public Builder setAggregator(String aggregator) {
      this.aggregator = aggregator;
      return this;
    }
    
    public Builder setFillPolicy(NumericFillPolicy fill_policy) {
      this.fillPolicy = fill_policy;
      return this;
    }
    
    public Downsampler build() {
      return new Downsampler(this);
    }
  }
}
