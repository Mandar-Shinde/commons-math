/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.analysis.function;

import org.apache.commons.math.analysis.UnivariateRealFunction;

import org.junit.Test;
import org.junit.Assert;

public class SqrtTest {
   @Test
   public void testComparison() {
       final Sqrt s = new Sqrt();
       final UnivariateRealFunction f = new UnivariateRealFunction() {
               public double value(double x) {
                   return Math.sqrt(x);
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = s.value(x);
           Assert.assertEquals("x=" + x, fX, sX, 0);
       }
   }

   @Test
   public void testDerivativeComparison() {
       final UnivariateRealFunction sPrime = (new Sqrt()).derivative();
       final UnivariateRealFunction f = new UnivariateRealFunction() {
               public double value(double x) {
                   return 1 / (2 * Math.sqrt(x));
               }
           };

       for (double x = 1e-30; x < 1e10; x *= 2) {
           final double fX = f.value(x);
           final double sX = sPrime.value(x);
           Assert.assertEquals("x=" + x, fX, sX, 0);
       }
   }
}
