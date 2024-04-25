package masterproject.server.RMI;

import java.rmi.RemoteException;

public class FiltersImpl implements Filters {

    // Convert to grayscale filter
    @Override
    public int[][] convertToGrayscale(int[][] image) throws RemoteException {
        System.out.println("Apply grayscale filter");
        int rows = image.length;
        int cols = image[0].length;
        int[][] grayscaleImage = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int rgb = image[i][j];
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int grayscale = (red + green + blue) / 3;
                grayscaleImage[i][j] = 0xFF << 24 | grayscale << 16 | grayscale << 8 | grayscale;
            }
        }
        System.out.println("Filter process finished!");
        return grayscaleImage;
    }

    // Sepia Filter
    @Override
    public int[][] sepiaFilter(int[][] image) throws RemoteException {
        System.out.println("Apply sepia filter");
        int[][] sepiaImage = new int[image.length][image[0].length];

        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                int argb = image[y][x];
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Apply sepia formula
                int newR = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int newG = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int newB = (int) (0.272 * r + 0.534 * g + 0.131 * b);

                // Clip values to 0-255
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                // Combine new color values with existing alpha
                int newARGB = (argb & 0xFF000000) | (newR << 16) | (newG << 8) | newB;
                sepiaImage[y][x] = newARGB;
            }
        }
        System.out.println("Filter process finished!");
        return sepiaImage;
    }

    @Override
    public int[][] adjustBrightness(int[][] image, int brightnessValue) throws RemoteException {
        int[][] adjustedMatrix = new int[image.length][image[0].length];

        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                int argb = image[y][x];
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Adjust each color channel by adding brightness value
                r = Math.min(255, Math.max(0, r + brightnessValue));
                g = Math.min(255, Math.max(0, g + brightnessValue));
                b = Math.min(255, Math.max(0, b + brightnessValue));

                // Combine new color values with existing alpha
                int newARGB = (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
                adjustedMatrix[y][x] = newARGB;
            }
        }

        return adjustedMatrix;
    }

    @Override
    public int[][] sharpenFilter(int[][] image, float sharpenFactor) throws RemoteException {
        int[][] sharpenedMatrix = new int[image.length][image[0].length];

        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                int argb = image[y][x];
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Calculate weighted sum of surrounding pixels
                int newR = Math.round(sharpenFactor * r + (1 - sharpenFactor) * getWeightedSum(image, x, y, r));
                int newG = Math.round(sharpenFactor * g + (1 - sharpenFactor) * getWeightedSum(image, x, y, g));
                int newB = Math.round(sharpenFactor * b + (1 - sharpenFactor) * getWeightedSum(image, x, y, b));

                // Clip values to 0-255
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                // Combine new color values with existing alpha
                int newARGB = (argb & 0xFF000000) | (newR << 16) | (newG << 8) | newB;
                sharpenedMatrix[y][x] = newARGB;
            }
        }

        return sharpenedMatrix;
    }
    private static int getWeightedSum(int[][] image, int x, int y, int colorValue) {
        int sum = 0;
        int[][] weights = {
                {-1, -1, -1},
                {-1, 9, -1},
                {-1, -1, -1}
        };

        for (int yy = -1; yy <= 1; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                int neighborX = x + xx;
                int neighborY = y + yy;

                // Handle edge cases
                if (neighborX < 0 || neighborX >= image[0].length ||
                        neighborY < 0 || neighborY >= image.length) {
                    continue;
                }

                int neighborValue = (image[neighborY][neighborX] >> (16 + (colorValue == 0 ? 8 : 0))) & 0xFF;
                sum += neighborValue * weights[yy + 1][xx + 1];
            }
        }

        return sum;
    }
    public int[][] gaussianBlur(int[][] inputMatrix, double sigma) {
        int width = inputMatrix[0].length;
        int height = inputMatrix.length;

        int[][] outputMatrix = new int[height][width];

        // Create a 1D Gaussian kernel
        double[] gaussianKernel = generateGaussianKernel(sigma);

        // Apply the kernel horizontally and vertically
        int halfSize = gaussianKernel.length / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0;

                for (int i = -halfSize; i <= halfSize; i++) {
                    int colIndex = Math.min(Math.max(0, x + i), width - 1);
                    sum += gaussianKernel[i + halfSize] * inputMatrix[y][colIndex];
                }

                outputMatrix[y][x] = (int) Math.round(sum);
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double sum = 0;

                for (int i = -halfSize; i <= halfSize; i++) {
                    int rowIndex = Math.min(Math.max(0, y + i), height - 1);
                    sum += gaussianKernel[i + halfSize] * outputMatrix[rowIndex][x];
                }

                outputMatrix[y][x] = (int) Math.round(sum);
            }
        }

        return outputMatrix;
    }

    private static double[] generateGaussianKernel(double sigma) {
        // Calculate the size of the kernel based on sigma
        int size = (int) (6 * sigma);
        if (size % 2 == 0) {
            size++; // Ensure the size is odd
        }

        double[] kernel = new double[size];
        double sum = 0;

        // Generate the Gaussian kernel values
        for (int i = 0; i < size; i++) {
            double x = i - (double) size / 2;
            kernel[i] = Math.exp(-(x * x) / (2 * sigma * sigma));
            sum += kernel[i];
        }

        // Normalize the kernel
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }
}

