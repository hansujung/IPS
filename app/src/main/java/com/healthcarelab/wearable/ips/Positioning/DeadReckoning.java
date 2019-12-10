package com.healthcarelab.wearable.ips.Positioning;

public class DeadReckoning {

    private int step_len;
    private int step_dir;
    private int step_value;
    private double last_point[];
    private double ratio[];
    private double floor[];

    public DeadReckoning(int step_len, int step_dir, int step_value, double last_point[], double ratio[], double floor[]) {
        this.step_len = step_len;
        this.step_dir = step_dir;
        this.step_value = step_value;
        this.last_point = last_point;
        this.ratio = ratio;
        this.floor = floor;
    }

    public int getStep_len() {
        return step_len;
    }

    public int getStep_dir() {
        return step_dir;
    }

    public int getStep_value() {
        return step_value;
    }

    public double[] getLast_point() {
        return last_point;
    }

    public double[] getRatio() {
        return ratio;
    }

    public double[] getFloor() {
        return floor;
    }

    public double[] RetPosition(DeadReckoning deadReckoning) {
        int bool = deadReckoning.getStep_value();
        double point[] = deadReckoning.getLast_point();
        int direction = deadReckoning.getStep_dir();
        int leng = deadReckoning.getStep_len();
        double ratio[] = deadReckoning.getRatio();
        double floor[] = deadReckoning.getFloor();

        double change_x_val = 0;
        double change_y_val = 0;

        if (bool != 0) {
            if (direction == 1) { //북쪽(엘베쪽)을 바라보고 걸었을때 좌표값 플러스(y축으로)
                change_x_val = (floor[0] / 2) * 100 + 100;
                change_y_val = point[1] + (leng / ratio[1]);
            } else if (direction == 3) { //남쪽(화물엘베쪽)을 바라보고 걸었을때 좌표값 마이너스(y축으로)
                change_x_val = (floor[0] / 2) * 100 + 100;
                change_y_val = point[1] - (leng / ratio[1]);
            } else if (direction == 2) { //동쪽(후문쪽)을 바라보고 걸었을때 좌표값 마이너스(x축으로)
                change_x_val = point[0] - (float) (leng / ratio[0]);
                change_y_val = point[1];
            } else if (direction == 4) { //서쪽(경기대쪽)을 바라보고 걸었을때 좌표값 플러스(x축으로)
                change_x_val = point[0] + (float) (leng / ratio[0]);
                change_y_val = point[1];
            }
        } else {
            change_x_val = point[0];
            change_y_val = point[1];
        }

        point[0] = change_x_val;
        point[1] = change_y_val;


        return point;
    }
}
