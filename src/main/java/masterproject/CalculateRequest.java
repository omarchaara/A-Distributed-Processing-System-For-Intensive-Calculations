package masterproject;

import java.io.Serializable;

//Class to encapsulate a client request.
public class CalculateRequest implements Serializable {
    private int operation;
    private int[][] matrix1;
    private int[][] matrix2;

    public CalculateRequest(int operation, int[][] matrix1, int[][] matrix2) {
        this.operation = operation;
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int[][] getMatrix1() {
        return matrix1;
    }

    public void setMatrix1(int[][] matrix1) {
        this.matrix1 = matrix1;
    }

    public int[][] getMatrix2() {
        return matrix2;
    }

    public void setMatrix2(int[][] matrix2) {
        this.matrix2 = matrix2;
    }
}
