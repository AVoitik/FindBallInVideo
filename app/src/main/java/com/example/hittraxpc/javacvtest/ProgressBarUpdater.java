package com.example.hittraxpc.javacvtest;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by HitTraxPC on 12/7/2017.
 */

public class ProgressBarUpdater implements Runnable {

        private int active = 1;
        private ProgressBar prog;
        @Override
        public void run() {
            if (active == 1) {
                prog.setVisibility(View.VISIBLE);
            }


        }

        public void setProgressBar(int active, ProgressBar prog){
            this.active = active;
            this.prog = prog;

        }

}
