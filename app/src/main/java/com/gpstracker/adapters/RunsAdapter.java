package com.gpstracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gpstracker.R;
import com.gpstracker.data_clases.MyRun;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by RGarai on 24.8.2016.
 */
public class RunsAdapter extends BaseAdapter {

    private List<MyRun> mRuns;
    private Context context;
    private LayoutInflater inflater;
    private TextView deleteTextViewButton;
    private View.OnClickListener myListener;

    public RunsAdapter(Context context, List<MyRun> runs, View.OnClickListener listener) {
        this.mRuns = runs;
        this.context = context;
        this.myListener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        return mRuns.size();
    }

    @Override
    public Object getItem(int i) {
        return mRuns.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;

        Log.i("pozicia", position+"");
        //if (view == null) {
            view = inflater.inflate(R.layout.item_run, null);
            MyRun run = mRuns.get(position);
//
//            SwipeLayout swipeLayout =  (SwipeLayout) view.findViewById(R.id.swipeSingleRun);
//
//    //set show mode.
//            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
//
////    //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
////            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_wrapper));
//
//            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
//                @Override
//                public void onClose(SwipeLayout layout) {
//                    //when the SurfaceView totally cover the BottomView.
//                }
//
//                @Override
//                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
//                    //you are swiping.
//                }
//
//                @Override
//                public void onStartOpen(SwipeLayout layout) {
//
//                }
//
//                @Override
//                public void onOpen(SwipeLayout layout) {
//                    //when the BottomView totally show.
//                }
//
//                @Override
//                public void onStartClose(SwipeLayout layout) {
//
//                }
//
//                @Override
//                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
//                    //when user's hand released.
//                }
//            });
//uderline - todo I turned this off because it was ruining fluent usage of the app. switching between the weeks (pager view)
//            //text view option for deleting the run
//            deleteTextViewButton = (TextView) view.findViewById(R.id.delete_option_text_view);
//            deleteTextViewButton.setTag(run.getId());
//            deleteTextViewButton.setOnClickListener(myListener);

            //format the date data into readable date
            TextView date = (TextView) view.findViewById(R.id.date);
            SimpleDateFormat formatOut = new SimpleDateFormat("dd-MM-yyyy");
            date.setText(formatOut.format(run.getDate()));

            //format the length
            TextView length = (TextView) view.findViewById(R.id.length);
            length.setText(String.format(Locale.getDefault(), "%.3f", run.getLength()));

            //format the time
            TextView time = (TextView) view.findViewById(R.id.time);
            Date timeDate = new java.util.Date(run.getTime());
            Log.i("rungettime", run.getTime()+"");
            String timeDateFormated = new SimpleDateFormat("HH:mm:ss").format(timeDate);
            time.setText(timeDateFormated);

        //}
        return view;
    }


}
