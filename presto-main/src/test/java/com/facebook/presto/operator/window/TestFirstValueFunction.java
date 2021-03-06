/*
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
package com.facebook.presto.operator.window;

import org.testng.annotations.Test;

import static com.facebook.presto.SessionTestUtils.TEST_SESSION;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.testing.MaterializedResult.resultBuilder;

public class TestFirstValueFunction
        extends AbstractTestWindowFunction
{
    @Test
    public void testFirstValueUnbounded()
    {
        assertWindowQuery("first_value(orderdate) OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, VARCHAR)
                        .row(3L, "F", "1993-10-14")
                        .row(5L, "F", "1993-10-14")
                        .row(6L, "F", "1993-10-14")
                        .row(33L, "F", "1993-10-14")
                        .row(1L, "O", "1996-01-02")
                        .row(2L, "O", "1996-01-02")
                        .row(4L, "O", "1996-01-02")
                        .row(7L, "O", "1996-01-02")
                        .row(32L, "O", "1996-01-02")
                        .row(34L, "O", "1996-01-02")
                        .build());
        assertWindowQueryWithNulls("first_value(orderdate) OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, VARCHAR)
                        .row(3L, "F", "1993-10-14")
                        .row(5L, "F", "1993-10-14")
                        .row(null, "F", "1993-10-14")
                        .row(null, "F", "1993-10-14")
                        .row(34L, "O", "1998-07-21")
                        .row(null, "O", "1998-07-21")
                        .row(1L, null, null)
                        .row(7L, null, null)
                        .row(null, null, null)
                        .row(null, null, null)
                        .build());

        assertWindowQuery("first_value(orderkey) OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, BIGINT)
                        .row(3L, "F", 3L)
                        .row(5L, "F", 3L)
                        .row(6L, "F", 3L)
                        .row(33L, "F", 3L)
                        .row(1L, "O", 1L)
                        .row(2L, "O", 1L)
                        .row(4L, "O", 1L)
                        .row(7L, "O", 1L)
                        .row(32L, "O", 1L)
                        .row(34L, "O", 1L)
                        .build());
        assertWindowQueryWithNulls("first_value(orderkey) OVER (PARTITION BY orderstatus ORDER BY orderkey)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, BIGINT)
                        .row(3L, "F", 3L)
                        .row(5L, "F", 3L)
                        .row(null, "F", 3L)
                        .row(null, "F", 3L)
                        .row(34L, "O", 34L)
                        .row(null, "O", 34L)
                        .row(1L, null, 1L)
                        .row(7L, null, 1L)
                        .row(null, null, 1L)
                        .row(null, null, 1L)
                        .build());

        // Timestamp
        assertWindowQuery("date_format(first_value(cast(orderdate as TIMESTAMP)) OVER (PARTITION BY orderstatus ORDER BY orderkey), '%Y-%m-%d')",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, VARCHAR)
                        .row(3L, "F", "1993-10-14")
                        .row(5L, "F", "1993-10-14")
                        .row(6L, "F", "1993-10-14")
                        .row(33L, "F", "1993-10-14")
                        .row(1L, "O", "1996-01-02")
                        .row(2L, "O", "1996-01-02")
                        .row(4L, "O", "1996-01-02")
                        .row(7L, "O", "1996-01-02")
                        .row(32L, "O", "1996-01-02")
                        .row(34L, "O", "1996-01-02")
                        .build());
    }

    @Test
    public void testFirstValueBounded()
    {
        assertWindowQuery("first_value(orderkey) OVER (PARTITION BY orderstatus ORDER BY orderkey " +
                        "ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, BIGINT)
                        .row(3L, "F", 3L)
                        .row(5L, "F", 3L)
                        .row(6L, "F", 3L)
                        .row(33L, "F", 5L)
                        .row(1L, "O", 1L)
                        .row(2L, "O", 1L)
                        .row(4L, "O", 1L)
                        .row(7L, "O", 2L)
                        .row(32L, "O", 4L)
                        .row(34L, "O", 7L)
                        .build());
        assertWindowQueryWithNulls("first_value(orderkey) OVER (PARTITION BY orderstatus ORDER BY orderkey " +
                        "ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING)",
                resultBuilder(TEST_SESSION, BIGINT, VARCHAR, BIGINT)
                        .row(3L, "F", 3L)
                        .row(5L, "F", 3L)
                        .row(null, "F", 3L)
                        .row(null, "F", 5L)
                        .row(34L, "O", 34L)
                        .row(null, "O", 34L)
                        .row(1L, null, 1L)
                        .row(7L, null, 1L)
                        .row(null, null, 1L)
                        .row(null, null, 7L)
                        .build());
    }
}
