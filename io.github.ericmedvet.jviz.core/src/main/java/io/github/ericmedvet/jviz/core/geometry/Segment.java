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

package io.github.ericmedvet.jviz.core.geometry;

import java.util.Optional;

public record Segment(Point p1, Point p2) implements BoundedEntity {

  @Override
  public Rectangle boundingBox() {
    return new Rectangle(center(), Math.abs(p1.x() - p2.x()), Math.abs(p1.y() - p2.y()));
  }

  public Point center() {
    return new Point(p1.x() / 2d + p2.x() / 2d, p1.y() / 2d + p2.y() / 2d);
  }

  public double direction() {
    return p2.diff(p1).direction();
  }

  public Optional<Point> intersectionWith(Segment other) {
    // this.p1 + t * r = other.p1 + u * s, with t and u in [0, 1] iff the segments intersect
    double rX = p2.x() - p1.x();
    double rY = p2.y() - p1.y();
    double sX = other.p2.x() - other.p1.x();
    double sY = other.p2.y() - other.p1.y();
    double denominator = rX * sY - rY * sX;
    if (denominator == 0) {
      // parallel (or collinear)
      return Optional.empty();
    }
    double qpX = other.p1.x() - p1.x();
    double qpY = other.p1.y() - p1.y();
    double t = (qpX * sY - qpY * sX) / denominator;
    double u = (qpX * rY - qpY * rX) / denominator;
    if (t < 0 || t > 1 || u < 0 || u > 1) {
      return Optional.empty();
    }
    return Optional.of(new Point(p1.x() + t * rX, p1.y() + t * rY));
  }

  public boolean intersects(Segment other) {
    return intersectionWith(other).isEmpty();
  }

  public double length() {
    return p1.distanceTo(p2);
  }

  public Line perpendicularBisector() {
    double a = p2.x() - p1.x();
    double b = p2.y() - p1.y();
    double c = (Math.pow(p2.x(), 2) - Math.pow(p1.x(), 2) + Math.pow(p2.y(), 2) - Math.pow(p1.y(), 2)) / 2d;
    return new Line(a, b, c);
  }

  @Override
  public String toString() {
    return "s(%s->%s)".formatted(p1, p2);
  }
}