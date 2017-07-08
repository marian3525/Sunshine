package alpha.sunshine;

import android.view.View;

/**
 * Created by marian on 07.07.2017.
 */

public class ClickListener implements View.OnClickListener {
    MainActivity mainActivity;
    public ClickListener(MainActivity activity) {
        this.mainActivity = activity;
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            //check the IDs for all buttons and react accordingly
        }
    }
}
