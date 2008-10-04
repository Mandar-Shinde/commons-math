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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class transforming a symmetrical matrix to tri-diagonal shape.
 * <p>A symmetrical m &times; m matrix A can be written as the product of three matrices:
 * A = Q &times; T &times; Q<sup>T</sup> with Q an orthogonal matrix and T a symmetrical
 * tri-diagonal matrix. Both Q and T are m &times; m matrices.</p>
 * <p>Transformation to tri-diagonal shape is often not a goal by itself, but it is
 * an intermediate step in more general decomposition algorithms like {@link
 * EigenValuesDecomposition Eigen Values Decomposition}. This class is therefore
 * intended for internal use by the library and is not public. As a consequence of
 * this explicitly limited scope, many methods directly returns references to
 * internal arrays, not copies.</p>
 * @version $Revision$ $Date$
 * @since 2.0
 */
class TriDiagonalTransformer implements Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 8935390784125343332L;

    /** Householder vectors. */
    private final double householderVectors[][];

    /** Main diagonal. */
    private final double[] main;

    /** Secondary diagonal. */
    private final double[] secondary;

    /** Cached value of Q. */
    private RealMatrix cachedQ;

    /** Cached value of Qt. */
    private RealMatrix cachedQt;

    /** Cached value of T. */
    private RealMatrix cachedT;

    /**
     * Build the transformation to tri-diagonal shape of a symmetrical matrix.
     * <p>The specified matrix is assumed to be symmetrical without any check.
     * Only the upper triangular part of the matrix is used.</p>
     * @param matrix the symmetrical matrix to transform.
     * @exception InvalidMatrixException if matrix is not square
     */
    public TriDiagonalTransformer(RealMatrix matrix)
        throws InvalidMatrixException {
        if (!matrix.isSquare()) {
            throw new InvalidMatrixException("transformation to tri-diagonal requires that the matrix be square");
        }

        final int m = matrix.getRowDimension();
        householderVectors = matrix.getData();
        main      = new double[m];
        secondary = new double[m - 1];
        cachedQ   = null;
        cachedQt  = null;
        cachedT   = null;

        // transform matrix
        transform();

    }

    /**
     * Returns the matrix Q of the transform. 
     * <p>Q is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
     * @return the Q matrix
     */
    public RealMatrix getQ() {
        if (cachedQ == null) {
            cachedQ = getQT().transpose();
        }
        return cachedQ;
    }

    /**
     * Returns the transpose of the matrix Q of the transform. 
     * <p>Q is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
     * @return the Q matrix
     */
    public RealMatrix getQT() {

        if (cachedQt == null) {

            final int m = householderVectors.length;
            final double[][] qtData  = new double[m][m];

            // build up first part of the matrix by applying Householder transforms
            for (int k = m - 1; k >= 1; --k) {
                final double[] hK = householderVectors[k - 1];
                final double inv = 1.0 / (secondary[k - 1] * hK[k]);
                qtData[k][k] = 1;
                if (hK[k] != 0.0) {
                    for (int j = k; j < m; ++j) {
                        final double[] qtJ = qtData[j];
                        double beta = 0;
                        for (int i = k; i < m; ++i) {
                            beta -= qtJ[i] * hK[i];
                        }
                        beta *= inv;

                        for (int i = k; i < m; ++i) {
                            qtJ[i] -= beta * hK[i];
                        }
                    }
                }
            }
            qtData[0][0] = 1;

            // cache the matrix for subsequent calls
            cachedQt = new RealMatrixImpl(qtData, false);

        }

        // return the cached matrix
        return cachedQt;

    }

    /**
     * Returns the tri-diagonal matrix T of the transform. 
     * @return the T matrix
     */
    public RealMatrix getT() {

        if (cachedT == null) {

            final int m = main.length;
            double[][] tData = new double[m][m];
            for (int i = 0; i < m; ++i) {
                double[] tDataI = tData[i];
                tDataI[i] = main[i];
                if (i > 0) {
                    tDataI[i - 1] = secondary[i - 1];
                }
                if (i < main.length - 1) {
                    tDataI[i + 1] = secondary[i];
                }
            }

            // cache the matrix for subsequent calls
            cachedT = new RealMatrixImpl(tData, false);

        }

        // return the cached matrix
        return cachedT;

    }

    /**
     * Get the Householder vectors of the transform.
     * <p>Note that since this class is only intended for internal use,
     * it returns directly a reference to its internal arrays, not a copy.</p>
     * @return the main diagonal elements of the B matrix
     */
    double[][] getHouseholderVectorsRef() {
        return householderVectors;
    }

    /**
     * Get the main diagonal elements of the matrix T of the transform.
     * <p>Note that since this class is only intended for internal use,
     * it returns directly a reference to its internal arrays, not a copy.</p>
     * @return the main diagonal elements of the T matrix
     */
    double[] getMainDiagonalRef() {
        return main;
    }

    /**
     * Get the secondary diagonal elements of the matrix T of the transform.
     * <p>Note that since this class is only intended for internal use,
     * it returns directly a reference to its internal arrays, not a copy.</p>
     * @return the secondary diagonal elements of the T matrix
     */
    double[] getSecondaryDiagonalRef() {
        return secondary;
    }

    /**
     * Transform original matrix to tri-diagonal form.
     * <p>Transformation is done using Householder transforms.</p>
     */
    private void transform() {

        final int m = householderVectors.length;
        final double[] z = new double[m];
        for (int k = 0; k < m - 1; k++) {

            //zero-out a row and a column simultaneously
            final double[] hK = householderVectors[k];
            main[k] = hK[k];
            double xNormSqr = 0;
            for (int j = k + 1; j < m; ++j) {
                final double c = hK[j];
                xNormSqr += c * c;
            }
            final double a = (hK[k + 1] > 0) ? -Math.sqrt(xNormSqr) : Math.sqrt(xNormSqr);
            secondary[k] = a;
            if (a != 0.0) {
                // apply Householder transform from left and right simultaneously

                hK[k + 1] -= a;
                final double beta = -1 / (a * hK[k + 1]);

                // compute a = beta A v, where v is the Householder vector
                // this loop is written in such a way
                //   1) only the upper triangular part of the matrix is accessed
                //   2) access is cache-friendly for a matrix stored in rows
                Arrays.fill(z, k + 1, m, 0);
                for (int i = k + 1; i < m; ++i) {
                    final double[] hI = householderVectors[i];
                    final double hKI = hK[i];
                    double zI = hI[i] * hKI;
                    for (int j = i + 1; j < m; ++j) {
                        final double hIJ = hI[j];
                        zI   += hIJ * hK[j];
                        z[j] += hIJ * hKI;
                    }
                    z[i] = beta * (z[i] + zI);
                }

                // compute gamma = beta vT z / 2
                double gamma = 0;
                for (int i = k + 1; i < m; ++i) {
                    gamma += z[i] * hK[i];
                }
                gamma *= beta / 2;

                // compute z = z - gamma v
                for (int i = k + 1; i < m; ++i) {
                    z[i] -= gamma * hK[i];
                }

                // update matrix: A = A - v zT - z vT
                // only the upper triangular part of the matrix is updated
                for (int i = k + 1; i < m; ++i) {
                    final double[] hI = householderVectors[i];
                    for (int j = i; j < m; ++j) {
                        hI[j] -= hK[i] * z[j] + z[i] * hK[j];
                    }
                }

            }

        }
        main[m - 1] = householderVectors[m - 1][m - 1];
    }

}