package com.example.jpeg;

import javafx.concurrent.Task;

public abstract class PressingTask extends Task<Void>
{
    private double initialProgress = 0.0;

    public double getInitialProgress() {
        return initialProgress;
    }

    public void setInitialProgress(double initialProgress) {
        this.initialProgress = initialProgress;
    }

    @Override
    public void updateProgress(double v, double v1) {
        super.updateProgress(v, v1);
    }
}
