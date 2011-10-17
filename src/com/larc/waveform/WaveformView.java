package com.larc.waveform;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class WaveformView extends ImageView {

	private static final int DEFAULT_X_SIZE = 50;
	private static final int DEFAULT_PAINT_COLOR = 0xFFFF0000;
	private static final int DEFAULT_LINE_WIDTH = 2;
	private static final int DRAWING_CYCLE = 4;

	private int mUpdatePeriod = 5;
	private int mUpdateCounter = 0;
	
	private Handler mRefreshHandler;

	private ArrayList<DataSet> mDataSets;
	
	private WaveformAdapter mAdapter;
	
	public WaveformAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(WaveformAdapter adapter) {
		mAdapter = adapter;
	}

	public static class WaveformAdapter {
		
		public int[] getCurrentData(int set){
			return null;
		}
		
	}

	Runnable mRefreshRunnable = new Runnable() {
		public void run() {
			for (int i = 0; i < mDataSets.size(); i++) {
				if (mAdapter != null){
					int[] value = mAdapter.getCurrentData(i);
					mDataSets.get(i).pushData(value);
				} else {
					mDataSets.get(i).push();
				}
			}
			
			mUpdateCounter++;
			if(mUpdateCounter >= DRAWING_CYCLE){
				WaveformView.this.invalidate();
				mUpdateCounter = 0;
			}
			mRefreshHandler.postDelayed(this, mUpdatePeriod);
		}
	};

	@SuppressWarnings("serial")
	private static class DataSet extends LinkedList<Integer> {
		public int upperBound = 255;
		public int lowerBound = 0;
		public int currentValue = 0;
		private int paintColor = DEFAULT_PAINT_COLOR;
		private int lineWidth = DEFAULT_LINE_WIDTH;

		public DataSet(int size) {
			for (int i = 0; i < size; i++) {
				add(0);
			}
		}
		
		
		public void pushData(int[] value) {
			if(value != null){
				int size = value.length;
				for(int i = 0; i < size ; i++){
					pushValue(value[i]);
				}
				Log.v("Waveform", "size="+value.length);
			}
		}


		public void pushValue(int value) {
			currentValue = value;
			add(value);
			removeFirst();
		}
		
		public void push(){
			pushValue(currentValue);
		}
	}

	private void init() {
		mDataSets = new ArrayList<DataSet>();
		DataSet set = new DataSet(DEFAULT_X_SIZE);
		mDataSets.add(set);
		mRefreshHandler = new Handler();
	}

	public WaveformView(Context context) {    //???
		super(context);
		init();
	}

	public WaveformView(Context context, AttributeSet attrs) {   // ???
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		for (int i = 0; i < mDataSets.size(); i++) {
			DataSet data = mDataSets.get(i);
			int size = data.size();
			if (size <= 0)
				size = 1;
			int range = data.upperBound - data.lowerBound;
			float deltaX = (float) width / (float) size;
			float deltaY = (float) height / (float) range;
			float base = (data.upperBound + data.lowerBound)/2;

			Paint paint = new Paint();
			paint.setColor(data.paintColor);
			paint.setStrokeWidth(data.lineWidth);

			ListIterator<Integer> iter = data.listIterator();    //???
			int y1 = 0;
			int y2 = 0;
			if (iter.hasNext())
				y1 = iter.next();
			for (int j = 1; j < size; j++) {
				y2 = iter.next();
				canvas.drawLine((j - 1) * deltaX, base - y1 * deltaY, j
						* deltaX, base - y2 * deltaY, paint);
				y1 = y2;
			}
			paint.setColor(Color.BLACK);
			canvas.drawLine(0, 0, width, 0, paint);
		}

	}

	public void start() {
		mRefreshHandler.post(mRefreshRunnable);
	}

	public void stop() {
		mRefreshHandler.removeCallbacks(mRefreshRunnable);
	}

	public void setUpdatePeriod(int period) {
		mUpdatePeriod = period;
	}

	public void createNewDataSet(int size) {
		DataSet set = new DataSet(size);
		mDataSets.add(set);
	}

	public void setLineColor(int dataSetId, int color) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.get(dataSetId).paintColor = color;
		}
	}

	public void setCurrentData(int dataSetId, int data) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.get(dataSetId).currentValue = data;
			invalidate();
		}
	}
	
	public void setData(int dataSetId, int[] dataArray) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size() && dataArray != null) {
			DataSet dataSet = mDataSets.get(dataSetId);
			int size = dataArray.length;
			for(int i = 0; i < size ; i++){
				dataSet.pushValue(dataArray[i]);
			}
		}
	}

	public void setLineWidth(int dataSetId, int lineWidth) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.get(dataSetId).lineWidth = lineWidth;
		}
	}

	public void removeDataSet(int dataSetId) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.remove(dataSetId);
		}
	}
	
	public void removeAllDataSet(){
		mDataSets.clear();
	}
}
