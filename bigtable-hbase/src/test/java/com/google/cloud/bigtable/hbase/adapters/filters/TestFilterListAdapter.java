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

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public class TestFilterListAdapter {

  // Adapting a filterlist is a cooperative between the filter list adapter
  // and the filter adapter.
  FilterAdapter filterAdapter = FilterAdapter.buildAdapter();
  Scan emptyScan = new Scan();
  FilterAdapterContext emptyScanContext = new FilterAdapterContext(emptyScan);

  FilterList makeFilterList(Operator filterOperator) {
    return new FilterList(
        filterOperator,
        new ValueFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("value"))),
        new ValueFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("value2"))));
  }

  @Test
  public void interleavedFiltersAreAdapted() throws IOException {
    FilterList filterList = makeFilterList(Operator.MUST_PASS_ONE);
    RowFilter rowFilter = filterAdapter.adaptFilter(emptyScanContext, filterList);
    Assert.assertEquals(
        "value",
        rowFilter.getInterleave().getFilters(0).getValueRegexFilter().toStringUtf8());
    Assert.assertEquals(
        "value2",
        rowFilter.getInterleave().getFilters(1).getValueRegexFilter().toStringUtf8());
  }

  @Test
  public void chainedFiltersAreAdapted() throws IOException {
    FilterList filterList = makeFilterList(Operator.MUST_PASS_ALL);
    RowFilter rowFilter = filterAdapter.adaptFilter(emptyScanContext, filterList);
    Assert.assertEquals(
        "value",
        rowFilter.getChain().getFilters(0).getValueRegexFilter().toStringUtf8());
    Assert.assertEquals(
        "value2",
        rowFilter.getChain().getFilters(1).getValueRegexFilter().toStringUtf8());
  }
}
