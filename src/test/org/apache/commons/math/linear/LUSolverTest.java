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

package org.apache.commons.math.linear;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LUSolverTest extends TestCase {
    private double[][] testData = {
            { 1.0, 2.0, 3.0},
            { 2.0, 5.0, 3.0},
            { 1.0, 0.0, 8.0}
    };
    private double[][] luData = {
            { 2.0, 3.0, 3.0 },
            { 0.0, 5.0, 7.0 },
            { 6.0, 9.0, 8.0 }
    };
    
    // singular matrices
    private double[][] singular = {
            { 2.0, 3.0 },
            { 2.0, 3.0 }
    };
    private double[][] bigSingular = {
            { 1.0, 2.0,   3.0,    4.0 },
            { 2.0, 5.0,   3.0,    4.0 },
            { 7.0, 3.0, 256.0, 1930.0 },
            { 3.0, 7.0,   6.0,    8.0 }
    }; // 4th row = 1st + 2nd

    public LUSolverTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LUSolverTest.class);
        suite.setName("LUSolver Tests");
        return suite;
    }

    /** test threshold impact */
    public void testThreshold() {
        final RealMatrix matrix = new RealMatrixImpl(new double[][] {
                                                       { 1.0, 2.0, 3.0},
                                                       { 2.0, 5.0, 3.0},
                                                       { 4.000001, 9.0, 9.0}
                                                     }, false);
        assertFalse(new LUSolver(new LUDecompositionImpl(matrix, 1.0e-5)).isNonSingular());
        assertTrue(new LUSolver(new LUDecompositionImpl(matrix, 1.0e-10)).isNonSingular());
    }

    /** test singular */
    public void testSingular() {
        LUSolver lu =
            new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(testData, false)));
        assertTrue(lu.isNonSingular());
        lu = new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(singular, false)));
        assertFalse(lu.isNonSingular());
        lu = new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(bigSingular, false)));
        assertFalse(lu.isNonSingular());
    }

    /** test solve dimension errors */
    public void testSolveDimensionErrors() {
        LUSolver solver =
            new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(testData, false)));
        RealMatrix b = new RealMatrixImpl(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new RealVectorImplTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

    /** test solve singularity errors */
    public void testSolveSingularityErrors() {
        LUSolver solver =
            new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(singular, false)));
        RealMatrix b = new RealMatrixImpl(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new RealVectorImplTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            // expected behavior
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

    /** test solve */
    public void testSolve() {
        LUSolver solver =
            new LUSolver(new LUDecompositionImpl(new RealMatrixImpl(testData, false)));
        RealMatrix b = new RealMatrixImpl(new double[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        RealMatrix xRef = new RealMatrixImpl(new double[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        // using RealMatrix
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        // using double[]
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new RealVectorImpl(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        // using RealVectorImpl
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        // using RealVector with an alternate implementation
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            RealVectorImplTest.RealVectorTestImpl v =
                new RealVectorImplTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

    /** test determinant */
    public void testDeterminant() {
        assertEquals( -1, getDeterminant(new RealMatrixImpl(testData, false)), 1.0e-15);
        assertEquals(-10, getDeterminant(new RealMatrixImpl(luData, false)), 1.0e-14);
        assertEquals(  0, getDeterminant(new RealMatrixImpl(singular, false)), 1.0e-17);
        assertEquals(  0, getDeterminant(new RealMatrixImpl(bigSingular, false)), 1.0e-10);
    }

    private double getDeterminant(RealMatrix m) {
        return new LUSolver(new LUDecompositionImpl(m)).getDeterminant();
    }

}
