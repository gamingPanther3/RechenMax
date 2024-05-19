package com.mlprograms.rechenmax;
/*
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.tasks.Task;
import static com.mlprograms.rechenmax.ToastHelper.*;

import android.app.Activity;
*/

public class InAppReview {
    /*
    private ReviewInfo reviewInfo;
    private ReviewManager reviewManager;
    private final Activity parentActivity;

    public InAppReview(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void activateReviewInfo() {
        Task<ReviewInfo> reviewManagerInfoTask = reviewManager.requestReviewFlow();
        reviewManagerInfoTask.addOnCompleteListener((task -> {
            if(task.isSuccessful()) {
                reviewInfo = task.getResult();
            } else {
                showToastShort(parentActivity.getString(R.string.inAppReviewCouldntStart), parentActivity);
            }
        }));
    }

    public void startReviewFlow() {
        if(reviewInfo != null) {
            Task<Void> flow = reviewManager.launchReviewFlow(parentActivity, reviewInfo);
            flow.addOnCompleteListener(task -> {
                showToastShort(parentActivity.getString(R.string.inAppReviewFinished), parentActivity);
            });
        }
    }
     */
}
