/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.extensions.sql.impl.interpreter.operator;

import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.extensions.sql.impl.interpreter.BeamSqlExpressionEnvironments;
import org.apache.beam.sdk.extensions.sql.impl.interpreter.BeamSqlFnExecutorTestBase;
import org.apache.calcite.sql.type.SqlTypeName;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link BeamSqlCastExpression}. */
public class BeamSqlCastExpressionTest extends BeamSqlFnExecutorTestBase {

  private List<BeamSqlExpression> operands;

  @Before
  public void setup() {
    operands = new ArrayList<>();
  }

  @Test
  public void testForOperands() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.INTEGER, 1));
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "aaa"));
    Assert.assertFalse(new BeamSqlCastExpression(operands, SqlTypeName.BIGINT).accept());
  }

  @Test
  public void testForIntegerToBigintTypeCasting() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.INTEGER, 5));
    Assert.assertEquals(
        5L,
        new BeamSqlCastExpression(operands, SqlTypeName.BIGINT)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getLong());
  }

  @Test
  public void testForDoubleToBigIntCasting() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.DOUBLE, 5.45));
    Assert.assertEquals(
        5L,
        new BeamSqlCastExpression(operands, SqlTypeName.BIGINT)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getLong());
  }

  @Test
  public void testForIntegerToDateCast() {
    // test for yyyyMMdd format
    operands.add(BeamSqlPrimitive.of(SqlTypeName.INTEGER, 20170521));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 21).withTimeAtStartOfDay(),
        new BeamSqlCastExpression(operands, SqlTypeName.DATE)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test
  public void testyyyyMMddDateFormat() {
    //test for yyyy-MM-dd format
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "2017-05-21"));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 21).withTimeAtStartOfDay(),
        new BeamSqlCastExpression(operands, SqlTypeName.DATE)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test
  public void testyyMMddDateFormat() {
    // test for yy.MM.dd format
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "17.05.21"));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 21).withTimeAtStartOfDay(),
        new BeamSqlCastExpression(operands, SqlTypeName.DATE)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test
  public void testForTimestampCastExpression() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "17-05-21 23:59:59.989"));
    Assert.assertEquals(
        SqlTypeName.TIMESTAMP,
        new BeamSqlCastExpression(operands, SqlTypeName.TIMESTAMP)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getOutputType());
  }

  @Test
  public void testDateTimeFormatWithMillis() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "2017-05-21 23:59:59.989"));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 22).withTime(0, 0, 0, 0),
        new BeamSqlCastExpression(operands, SqlTypeName.TIMESTAMP)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test
  public void testDateTimeFormatWithTimezone() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "2017-05-21 23:59:59.89079 PST"));
    ReadableInstant expected =
        new DateTime()
            .withZone(DateTimeZone.forID("US/Pacific"))
            .withDate(2017, 05, 22)
            .withTime(0, 0, 0, 0);
    Assert.assertEquals(
        expected,
        new BeamSqlCastExpression(operands, SqlTypeName.TIMESTAMP)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test
  public void testDateTimeFormat() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.VARCHAR, "2017-05-21 23:59:59"));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 21).withTime(23, 59, 59, 0),
        new BeamSqlCastExpression(operands, SqlTypeName.TIMESTAMP)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }

  @Test(expected = RuntimeException.class)
  public void testForCastTypeNotSupported() {
    operands.add(BeamSqlPrimitive.of(SqlTypeName.TIME, DateTime.now()));
    Assert.assertEquals(
        new DateTime().withDate(2017, 05, 22).withTime(0, 0, 0, 0),
        new BeamSqlCastExpression(operands, SqlTypeName.TIMESTAMP)
            .evaluate(row, null, BeamSqlExpressionEnvironments.empty())
            .getValue());
  }
}
