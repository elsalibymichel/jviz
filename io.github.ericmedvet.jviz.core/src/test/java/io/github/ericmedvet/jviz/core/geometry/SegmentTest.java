/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jviz.core.geometry;

import static org.assertj.core.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

class SegmentTest {

  @Test
  void center() {
    assertThat(new Segment(new Point(0, 0), new Point(2, 1)).center())
        .as("center of (0;0)->(2;1) is (1;0.5)")
        .isEqualTo(new Point(1, 0.5));
  }

  @Test
  void direction() {
    assertThat(new Segment(new Point(0, 0), new Point(2, 2)).direction())
        .as("direction of (0;0)->(2;2) is ~pi/4")
        .isCloseTo(Math.PI / 4d, within(1e-9));
    assertThat(new Segment(new Point(1, 1), new Point(2, 1)).direction())
        .as("direction of (1;1)->(2;1) is ~0")
        .isCloseTo(0, within(1e-9));
    assertThat(new Segment(new Point(2, 1), new Point(2, 2)).direction())
        .as("direction of (2;1)->(2;2) is ~pi/2")
        .isCloseTo(Math.PI / 2d, within(1e-9));
  }

  @Test
  void intersectionWith() {
    Point P00 = Point.ORIGIN;
    Point P10 = new Point(1, 0);
    Point P01 = new Point(0, 1);
    Point P11 = new Point(1, 1);
    Point P32 = new Point(3, 2);
    Point P99 = new Point(9, 9);
    Point P28 = new Point(2, 8);
    Point P37 = new Point(3, 7);
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P10, P01)))
        .as("%s->%s intersects %s->%s", P00, P11, P10, P01)
        .isNotEmpty();
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P10, P01)))
        .as("%s->%s intersects %s->%s at (0.5;0.5)", P00, P11, P10, P01)
        .contains(new Point(0.5, 0.5));
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P32, P32.sum(P11))))
        .as("%s->%s does not intersect %s->%s", P00, P11, P32, P32.sum(P11))
        .isEmpty();
    assertThat(new Segment(P01, P99).intersectionWith(new Segment(P28, P37)))
        .as("%s->%s does not intersect %s->%s", P01, P99, P28, P37)
        .isEmpty();
    assertThat(new Segment(P28, P37).intersectionWith(new Segment(P01, P99)))
        .as("%s->%s does not intersect %s->%s", P37, P28, P01, P99)
        .isEmpty();
  }

  @Test
  void intersectionWithAxisAlignedSegmentAwayFromOrigin() {
    // A vertical or horizontal segment has a zero-width bounding box: the crossing point, computed with floating
    // point arithmetic, must be accepted when it is on the segment up to rounding, wherever the segment is.
    Random random = new Random(1);
    for (double offset : new double[]{0, 1, 25, 47.3, 50, 60}) {
      int misses = 0;
      for (int i = 0; i < 2000; i++) {
        double along = 20 + 10 * random.nextDouble(); // coordinate of the crossing along the segment
        double slope = 6 * (random.nextDouble() - 0.5);
        double distance = random.nextDouble();
        double step = distance + 0.001 + random.nextDouble(); // the move ends beyond the segment
        Segment vertical = new Segment(new Point(offset, along - 2.5), new Point(offset, along + 2.5));
        Segment horizontal = new Segment(new Point(along - 2.5, offset), new Point(along + 2.5, offset));
        double sign = random.nextBoolean() ? 1 : -1; // from which side the move arrives
        Segment towardsVertical = new Segment(
            new Point(offset - sign * distance, along - slope * distance),
            new Point(offset - sign * distance + sign * step, along - slope * distance + slope * step)
        );
        Segment towardsHorizontal = new Segment(
            new Point(along - slope * distance, offset - sign * distance),
            new Point(along - slope * distance + slope * step, offset - sign * distance + sign * step)
        );
        if (towardsVertical.intersectionWith(vertical).isEmpty()) {
          misses++;
        }
        if (towardsHorizontal.intersectionWith(horizontal).isEmpty()) {
          misses++;
        }
      }
      assertThat(misses)
          .as("moves crossing an axis-aligned segment at offset %s that are not detected", offset)
          .isZero();
    }
  }

  @Test
  void intersectionWithEndpointsAndParallel() {
    Point P00 = Point.ORIGIN;
    Point P11 = new Point(1, 1);
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P11, new Point(2, 0))))
        .as("segments sharing an endpoint intersect at that endpoint")
        .contains(P11);
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(new Point(0, 1), new Point(1, 2))))
        .as("parallel segments do not intersect")
        .isEmpty();
    assertThat(new Segment(P00, new Point(2, 2)).intersectionWith(new Segment(P11, new Point(3, 3))))
        .as("overlapping collinear segments have no single intersection point")
        .isEmpty();
  }

  @Test
  void perpendicularBisector() {
    Point P10 = new Point(1, 0);
    Point P01 = new Point(0, 1);
    assertThat(new Segment(P10, P01).perpendicularBisector().slope())
        .as("bisector of %s->%s has slope=1")
        .isCloseTo(1d, within(1e-9));
    assertThat(
        new Segment(P10, P01).perpendicularBisector().intersectionWith(new Segment(P10, P01))
    )
        .as("bisector of %s->%s intersects it at center")
        .contains(new Segment(P10, P01).center());
  }
}