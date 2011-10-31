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

	private static final int DEFAULT_X_SIZE = 50;					// Size of DataSet ArrayList .
	private static final int DEFAULT_PAINT_COLOR = 0xFFFF0000;
	private static final int DEFAULT_LINE_WIDTH = 3;
	private static final int DRAWING_CYCLE = 4;

	private int mUpdatePeriod = 2;									// Screen update frequency , larger --> lager
	private int mUpdateCounter = 0;
	
	private Handler mRefreshHandler;

	private ArrayList<DataSet> mDataSets;
	
	private WaveformAdapter mAdapter = new WaveformAdapter();
	
	public WaveformAdapter getAdapter() {    //  no used here .
		return mAdapter;
	}

	// This block is used to set mAdapter : the member of WaveformAdapter .
	public void setAdapter(WaveformAdapter adapter) {		
		mAdapter = adapter;			// Set WaveformAdapter which is called adapter here to mAdapter .
	}

	// Class "WaveformAdapter" : Setup the class which can be @Overrided in WaveformActivity.java .
	public static class WaveformAdapter {
		
		public int[] getCurrentData(int set){
			return null;
		}
		
	}
	
	// Run .
	Runnable mRefreshRunnable = new Runnable() {
		public void run() {
			for (int i = 0; i < mDataSets.size(); i++) {
				if (mAdapter != null){							// getCurrentData and pushData if there has data to adapt .
					int[] value = mAdapter.getCurrentData(i);
					mDataSets.get(i).pushData(value);
				} else {										// push CurrentData again (two times) if there has no data to be adapted . 
					mDataSets.get(i).push();
				}
			}
			
			// Update four WaveformView .
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
		public int upperBound = 767;
		public int lowerBound = 0;
		public int currentValue = 0;
		private int paintColor = DEFAULT_PAINT_COLOR;
		private int lineWidth = DEFAULT_LINE_WIDTH;

		// fill the ArrayList with "0" at first .
		public DataSet(int size) {
			for (int i = 0; i < size; i++) {
				add(0);
			}
		}
		
		// get the size of "Value" , which is the number of times needed to pushValue() .
		public void pushData(int[] value) {
			if(value != null){
				int size = value.length;
				for(int i = 0; i < size ; i++){   // for loop to call pushValue() .
					pushValue(value[i]);
				}
				Log.v("Waveform", "size="+value.length);
			}
		}

		// call pushValue() with "currentValue" one more time .
		public void push(){
			pushValue(currentValue);
		}
		
		// remove first value , add current value to LSB and shift whole array .
		public void pushValue(int value) {
			currentValue = value;
			add(value);
			removeFirst();
		}
	}

	private void init() {
		mDataSets = new ArrayList<DataSet>();
		DataSet set = new DataSet(DEFAULT_X_SIZE);
		mDataSets.add(set);
		mRefreshHandler = new Handler();				// Create Handler to control start() and stop() .
	}

	public WaveformView(Context context) {    										/** ?? **/
		super(context);
		init();
	}

	public WaveformView(Context context, AttributeSet attrs) {   					/** ?? **/
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();   			// of the View .
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

			ListIterator<Integer> iter = data.listIterator();    					/** ?? **/
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
			paint.setStrokeWidth(1);
			canvas.drawLine(0, 0, width, 0, paint);
		}

	}
	
	
	/**
	*These are commends to control "WaveformView" from other "Class" .
	*/
	
	// Handler.post() Runnable to start() it .
	public void start() {
		mRefreshHandler.post(mRefreshRunnable);
	}
	// Handler removeCallbacks() Runnable to stop() it .
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
	
	public void setLineWidth(int dataSetId, int lineWidth) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.get(dataSetId).lineWidth = lineWidth;
		}
	}

	public void setCurrentData(int dataSetId, int data) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.get(dataSetId).currentValue = data;
			invalidate();
		}
	}
	
	public void setData(int dataSetId, int[] dataArray) {	// set whole array's data
		if (dataSetId >= 0 && dataSetId < mDataSets.size() && dataArray != null) {
			DataSet dataSet = mDataSets.get(dataSetId);  
			int size = dataArray.length;
			for(int i = 0; i < size ; i++){
				dataSet.pushValue(dataArray[i]);    		// do pushValue to dataSet .
			}
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
