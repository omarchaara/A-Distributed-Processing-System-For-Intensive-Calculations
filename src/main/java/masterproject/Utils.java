package masterproject;


import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;

public class Utils {

    //    Convert image to matrix
    public static int[][] imageToIntMatrix(Image image) {
        System.out.println("Convert The image to matrix...");
        // Get the width and height of the image
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[][] matrix = new int[height][width];

        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ARGB = pixelReader.getArgb(x, y);
                matrix[y][x] = ARGB;
            }
        }
        System.out.println("The image Converted to matrix...");
        return matrix;
    }
    //    Convert matrix to image
    public static Image intMatrixToImage(int[][] matrix) {
        System.out.println("Convert The matrix to image...");
        int width = matrix[0].length;
        int height = matrix.length;

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ARGB = matrix[y][x];
                pixelWriter.setArgb(x, y, ARGB);
            }
        }
        System.out.println("The matrix to Converted image...");
        return image;
    }

    //    Divide matrix
    public static ArrayList<SubMatrix> divideMatrix(int[][] image, int numWorkers) {
        System.out.println("Divide the image...");
        int rows = image.length;
        int cols = image[0].length;
        int rowsPerWorker = rows / numWorkers;
        int remainingRows = rows % numWorkers;

        ArrayList<SubMatrix> subMatrices = new ArrayList<>();
        int startRow = 0;

        for (int i = 0; i < numWorkers; i++) {
            int endRow = startRow + rowsPerWorker + (remainingRows-- > 0 ? 1 : 0);
            subMatrices.add(new SubMatrix(image, startRow, endRow, i));
            startRow = endRow;
        }
        return subMatrices;
    }

    public static int[][] mergeMatrices(ArrayList<SubMatrix> subMatrices, int rows, int cols) {
        System.out.println("Merging the matrix...");
        int[][] image = new int[rows][cols];

        subMatrices.sort(Comparator.comparingInt(subMatrix -> subMatrix.index));

        for (SubMatrix subMatrix : subMatrices) {
            int startRow = subMatrix.startRow;
            int endRow = subMatrix.endRow;

            for (int i = startRow; i < endRow; i++) {
                System.arraycopy(subMatrix.matrix[i - startRow], 0, image[i], 0, cols);
            }
        }
        System.out.println("The matrix merged successfully...");
        return image;
    }

    public static class SubMatrix implements Serializable {
        public int[][] matrix;
        public int startRow;
        public int endRow;
        public int index;

        public SubMatrix() {
        }

        public SubMatrix(int startRow, int endRow, int index) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.index = index;
        }

        public SubMatrix(int[][] matrix, int startRow, int endRow, int index) {
            this.matrix = new int[endRow - startRow][matrix[0].length];
            for (int i = startRow; i < endRow; i++) {
                System.arraycopy(matrix[i], 0, this.matrix[i - startRow], 0, matrix[i].length);
            }

            this.startRow = startRow;
            this.endRow = endRow;
            this.index = index;
        }
    }
    // Method to display a matrix
    public static void printMatrix(int[][] matrix) {
        if (matrix == null) {
            System.out.println("Matrix is null.");
            return;
        }

        for (int[] row : matrix) {
            for (int col : row) {
                System.out.print(col + " ");
            }
            System.out.println();
        }
    }


    // divdematrix for matrix application
    public static ArrayList<int[][]> divideMatrix2(int[][] matrix, int numberOfSlaves) {
        // Check for invalid input
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0 || numberOfSlaves <= 0) {
            throw new IllegalArgumentException("Invalid matrix or number of slaves");
        }

        int startRow=0; // Start row for the current submatrix
        ArrayList<int[][]> submatrices = new ArrayList<>();
        if(numberOfSlaves>matrix.length){
            for (int[] row : matrix) {
                int[][] subMatrix = {row}; // Create a submatrix with just one row
                submatrices.add(subMatrix);
            }
        }else {

            int numRows = matrix.length;
            int numColumns = matrix[0].length;

            int rowsPerSlave = numRows / numberOfSlaves;
            int extraRows = numRows % numberOfSlaves; // Extra rows to distribute among slaves
            for (int i = 0; i < numberOfSlaves; i++) {
                int rowsForThisSlave = rowsPerSlave + (i < extraRows ? 1 : 0);
                int endRow = startRow + rowsForThisSlave;

                // Create submatrix and copy elements
                int[][] subMatrix = new int[rowsForThisSlave][numColumns];
                for (int row = startRow; row < endRow; row++) {
                    System.arraycopy(matrix[row], 0, subMatrix[row - startRow], 0, numColumns);
                }
                // Add submatrix to the ArrayList
                submatrices.add(subMatrix);

                startRow = endRow; // Update start row for the next submatrix
            }
        }
        return submatrices;
    }

}
