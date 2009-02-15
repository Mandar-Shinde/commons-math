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
package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.DenseRealMatrix;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;

/**
 * Computes covariances for pairs of arrays or columns of a matrix.
 * 
 * <p>The constructors that take <code>RealMatrix</code> or 
 * <code>double[][]</code> arguments generate correlation matrices.  The
 * columns of the input matrices are assumed to represent variable values.</p>
 * 
 * <p>The constructor argument <code>biasCorrected</code> determines whether or
 * not computed covariances are bias-corrected.</p>
 * 
 * <p>Unbiased covariances are given by the formula</p>
 * <code>cov(X, Y) = &Sigma;[(x<sub>i</sub> - E(X))(y<sub>i</sub> - E(Y))] / (n - 1)</code>
 * where <code>E(x)</code> is the mean of <code>X</code> and <code>E(Y)</code>
 * is the mean of the <code>Y</code> values.
 * 
 * <p>Non-bias-corrected estimates use <code>n</code> in place of <code>n - 1</code>
 * 
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class Covariance {
    
    /** covariance matrix */
    private final RealMatrix covarianceMatrix;

    /**
     * Create an empty covariance matrix.
     */
    public Covariance() {
        super();
        covarianceMatrix = null;
    }
    
    /**
     * Create a Covariance matrix from a rectangular array
     * whose columns represent covariates.
     * 
     * <p>The <code>biasCorrected</code> parameter determines whether or not
     * covariance estimates are bias-corrected.</p>
     * 
     * <p>The input array must be rectangular with at least two columns
     * and two rows.</p>
     * 
     * @param data rectangular array with columns representing covariates
     * @param biasCorrected true means covariances are bias-corrected
     * @throws IllegalArgumentException if the input data array is not
     * rectangular with at least two rows and two columns.
     */
    public Covariance(double[][] data, boolean biasCorrected) {
        this(new DenseRealMatrix(data), biasCorrected);
    }
    
    /**
     * Create a Covariance matrix from a rectangular array
     * whose columns represent covariates.
     * 
     * <p>The input array must be rectangular with at least two columns
     * and two rows</p>
     * 
     * @param data rectangular array with columns representing covariates
     * @throws IllegalArgumentException if the input data array is not
     * rectangular with at least two rows and two columns.
     */
    public Covariance(double[][] data) {
        this(data, true);
    }
    
    /**
     * Create a covariance matrix from a matrix whose columns
     * represent covariates.
     * 
     * <p>The <code>biasCorrected</code> parameter determines whether or not
     * covariance estimates are bias-corrected.</p>
     * 
     * <p>The matrix must have at least two columns and two rows</p>
     * 
     * @param matrix matrix with columns representing covariates
     * @param biasCorrected true means covariances are bias-corrected
     * @throws IllegalArgumentException if the input matrix does not have
     * at least two rows and two columns
     */
    public Covariance(RealMatrix matrix, boolean biasCorrected) {
       checkSufficientData(matrix);
       covarianceMatrix = computeCovariance(matrix, biasCorrected);
    }
    
    /**
     * Create a covariance matrix from a matrix whose columns
     * represent covariates.
     * 
     * <p>The matrix must have at least two columns and two rows</p>
     * 
     * @param matrix matrix with columns representing covariates
     * @throws IllegalArgumentException if the input matrix does not have
     * at least two rows and two columns
     */
    public Covariance(RealMatrix matrix) {
        this(matrix, true);
    }
    
    /**
     * Returns the covariance matrix
     * 
     * @return covariance matrix
     */
    public RealMatrix getCovarianceMatrix() {
        return covarianceMatrix;
    }
    
    /**
     * Create a covariance matrix from a matrix whose columns represent
     * covariates.
     * @param matrix input matrix (must have at least two columns and two rows)
     * @param biasCorrected determines whether or not covariance estimates are bias-corrected
     * @return covariance matrix
     */
    protected RealMatrix computeCovariance(RealMatrix matrix, boolean biasCorrected) {
        int dimension = matrix.getColumnDimension();
        Variance variance = new Variance(biasCorrected);
        RealMatrix outMatrix = new DenseRealMatrix(dimension, dimension);
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < i; j++) {
              double cov = covariance(matrix.getColumn(i), matrix.getColumn(j), biasCorrected);
              outMatrix.setEntry(i, j, cov);
              outMatrix.setEntry(j, i, cov);
            }
            outMatrix.setEntry(i, i, variance.evaluate(matrix.getColumn(i)));
        }
        return outMatrix;
    }
    
    /**
     * Computes the covariance between the two arrays.
     * 
     * <p>Array lengths must match and the common length must be at least 2.</p>
     *
     * @param xArray first data array
     * @param yArray second data array
     * @param biasCorrected if true, returned value will be bias-corrected 
     * @return returns the covariance for the two arrays 
     * @throws  IllegalArgumentException if the arrays lengths do not match or
     * there is insufficient data
     */
    public double covariance(final double[] xArray, final double[] yArray, boolean biasCorrected) 
        throws IllegalArgumentException {
        Mean mean = new Mean();
        double result = 0d;
        long length = xArray.length;
        if(length == yArray.length && length > 1) {
            double xMean = mean.evaluate(xArray);
            double yMean = mean.evaluate(yArray);
            for (int i = 0; i < xArray.length; i++) {
                double xDev = xArray[i] - xMean;
                double yDev = yArray[i] - yMean;
                result += (xDev * yDev - result) / (i + 1);
            }
        }
        else {
            throw MathRuntimeException.createIllegalArgumentException(
               "Arrays must have the same length and both must have at " +
               "least two elements. xArray has size {0}, yArray has {1} elements",
                    new Object[] {xArray.length, yArray.length});
        }
        return biasCorrected ? result * ((double) length / (double)(length - 1)) : result;
    }
    
    /**
     * Throws IllegalArgumentException of the matrix does not have at least
     * two columns and two rows
     * @param matrix matrix to check
     */
    private void checkSufficientData(final RealMatrix matrix) {
        int nRows = matrix.getRowDimension();
        int nCols = matrix.getColumnDimension();
        if (nRows < 2 || nCols < 2) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "Insufficient data:  only {0} rows and {1} columns.",
                    new Object[]{nRows, nCols});
        }
    }
}