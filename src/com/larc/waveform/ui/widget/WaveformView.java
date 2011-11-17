package com.larc.waveform.ui.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class is still being tested.
 * Don't use it.
 * @author Jason
 *
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback{
	private static final int DEFAULT_X_SIZE = 50; // Size of DataSet ArrayList .
	private static final int DEFAULT_PAINT_COLOR = 0xFFFF0000;
	private static final int DEFAULT_LINE_WIDTH = 3;
	private static final int DRAWING_CYCLE = 4;
	private static final int gGRID_SIZE = 10;
	
	public static class WaveformAdapter {
		public int[] getCurrentData(int set) {
			return null;
		}
		
	}

	private int mUpdatePeriod = 2; // Screen update frequency , larger --> lager
	private int mUpdateCounter = 0;

	private RefreshThread mRefreshThread;

	private ArrayList<DataSet> mDataSets;

	private WaveformAdapter mAdapter = new WaveformAdapter();

	
	public WaveformView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public WaveformView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * function executed in constructor
	 */
	private void init() {
		mDataSets = new ArrayList<DataSet>();
		DataSet set = new DataSet(DEFAULT_X_SIZE);
		mDataSets.add(set);
	}

	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mRefreshThread != null){
			mRefreshThread.stopRefresh();
		}
		mRefreshThread = new RefreshThread();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mRefreshThread != null){
			mRefreshThread.stopRefresh();
			mRefreshThread = null;
		}
	}

	long mLastUpdateTime;  // for the Log which is used to test Update period . (currentTime - mLastUpdateTime)
	// Run .
	private class RefreshThread extends Thread{
		private volatile boolean mmIsThreadRunning = true;
		private volatile boolean mmIsDrawing = true;
		
		public void run() {
			while(mmIsThreadRunning){
				//Request data and push into dataSet
				for (int i = 0; i < mDataSets.size(); i++) {
					if (mAdapter != null) { // getCurrentData and pushData if there
											// has data to adapt .
						int[] value = mAdapter.getCurrentData(i);
						mDataSets.get(i).pushData(value);
					} else { // push CurrentData again (two times) if there has no
								// data to be adapted .
						mDataSets.get(i).push();
					}
				}

				// Update view
				mUpdateCounter++;
				if (mUpdateCounter >= DRAWING_CYCLE && mmIsDrawing) {
					long currentTime = System.currentTimeMillis();
					Canvas canvas = getHolder().lockCanvas();
					drawPicture(canvas);
					getHolder().unlockCanvasAndPost(canvas);
					
					mUpdateCounter = 0;
					mLastUpdateTime = currentTime;
				}
				try {
					Thread.sleep(mUpdatePeriod);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void stopRefresh(){
			this.mmIsThreadRunning = false;
		}

		public void startDrawing() {
			mmIsDrawing = true;
		}

		public void stopDrawing() {
			mmIsDrawing = false;
		}
	};

	@SuppressWarnings("serial")
	private static class DataSet extends LinkedList<Integer> {
		public int upperBound = 350;
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

		// get the size of "Value" , which is the number of times needed to
		// pushValue() .
		public void pushData(int[] value) {
			if (value != null) {
				int size = value.length;
				for (int i = 0; i < size; i++) { // for loop to call pushValue()
													// .
					pushValue(value[i]);
				}
				// Log.v("Waveform", "size="+value.length);
			}
		}

		// call pushValue() with "currentValue" one more time .
		public void push() {
			pushValue(currentValue);
		}

		// remove first value , add current value to LSB and shift whole array .
		public void pushValue(int value) {
			currentValue = value;
			add(value);
			removeFirst();
		}
	}

	public static Paint createPaint(int color, int width) {
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(width);
		return paint;
	}

//	@Override
//	protected void onLayout(boolean changed, int left, int top, int right,
//			int bottom) {
//		super.onLayout(changed, left, top, right, bottom);
//	}

	protected void drawPicture(Canvas canvas) {

		int width = getWidth(); // of the View .
		int height = getHeight();

		for (int i = 0; i < mDataSets.size(); i++) {
			DataSet data = mDataSets.get(i);
			int size = data.size();
			if (size <= 0)
				size = 1;
			int range = data.upperBound - data.lowerBound;
			float deltaX = (float) width / (float) size;
			float deltaY = (float) height / (float) range;
			float base = (data.upperBound + data.lowerBound) / 2;

			Paint paint = new Paint();
			paint.setColor(data.paintColor);
			paint.setStrokeWidth(data.lineWidth);

			ListIterator<Integer> iter = data.listIterator();
			
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
			paint.setColor(Color.GRAY);
			
			//draw background grid line
			for (int xgrid = 0; xgrid < width; xgrid++) {
				if (xgrid % 5 == 0) {
					paint.setStrokeWidth(2);
				}
				canvas.drawLine(xgrid * gGRID_SIZE, 0, xgrid * gGRID_SIZE, height, paint);
				paint.setStrokeWidth(1);
			}
			for (int ygrid = 0; ygrid < width; ygrid++) {
				if (ygrid % 5 == 0) {
					paint.setStrokeWidth(2);
				}
				canvas.drawLine(0, ygrid * gGRID_SIZE, width, ygrid * gGRID_SIZE, paint);
				paint.setStrokeWidth(1);
			}
		}

	}

	public void start() {
		if (mRefreshThread != null){
			mRefreshThread.startDrawing();
		}
	}

	public void stop() {
		if (mRefreshThread != null){
			mRefreshThread.stopDrawing();
		}
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

	public void setData(int dataSetId, int[] dataArray) { // set whole array's
															// data
		if (dataSetId >= 0 && dataSetId < mDataSets.size() && dataArray != null) {
			DataSet dataSet = mDataSets.get(dataSetId);
			int size = dataArray.length;
			for (int i = 0; i < size; i++) {
				dataSet.pushValue(dataArray[i]); // do pushValue to dataSet .
			}
		}
	}

	public void removeDataSet(int dataSetId) {
		if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
			mDataSets.remove(dataSetId);
		}
	}

	public void removeAllDataSet() {
		mDataSets.clear();
	}
	
	public void setAdapter(WaveformAdapter adapter) {
		mAdapter = adapter;
	}

	
}
