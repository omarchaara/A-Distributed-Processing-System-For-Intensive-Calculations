package masterproject;

import java.io.Serializable;

public class FilterRequest implements Serializable {
    private final int[][] image;
    private final String filter;
    private final String[] args;

    public FilterRequest(int[][] image, String filter, String[] args) {
        this.image = image;
        this.filter = filter;
        this.args = args;
    }
    public int[][] getImage() {
        return image;
    }
    public String getFilter() {
        return filter;
    }
    public String[] getArgs() {
        return args;
    }
}
