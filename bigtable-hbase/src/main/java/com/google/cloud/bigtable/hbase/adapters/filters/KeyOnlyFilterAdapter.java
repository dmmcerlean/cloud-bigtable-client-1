/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.hbase.adapters.filters;

import com.google.bigtable.v1.RowFilter;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * An adapter for KeyOnlyFilter to a Bigtable strip_value_transform.
 */
public class KeyOnlyFilterAdapter implements TypedFilterAdapter<KeyOnlyFilter> {
  protected static final Cell TEST_CELL = new KeyValue(
      Bytes.toBytes('r'), // Row
      Bytes.toBytes('f'), // Family
      Bytes.toBytes('q'), // qualifier
      1L,
      Bytes.toBytes('v'));

  @Override
  public RowFilter adapt(FilterAdapterContext context, KeyOnlyFilter filter)
      throws IOException {
    return RowFilter.newBuilder()
        .setStripValueTransformer(true)
        .build();
  }

  @Override
  public FilterSupportStatus isFilterSupported(
      FilterAdapterContext context, KeyOnlyFilter filter) {
    // We don't support replacing the value of a stripped cell with
    // the its length (8-byte-big-endian). The KeyOnlyFilter supports this
    // via a constructor parameter that is not exposed via a getLengthAsValue().
    // In order to find out if this constructor parameter was set,
    // we perform a test transformation. If the test transformation
    // has a cell value length that is not 0 bytes, we know the
    // unsupported constructor param was passed:
    if (filter.transformCell(TEST_CELL).getValueLength() != 0) {
      return FilterSupportStatus.newNotSupported(
          "KeyOnlyFilters with lenAsVal = true are not supported");
    }
    return FilterSupportStatus.SUPPORTED;
  }
}
