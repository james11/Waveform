package com.larc.waveform.ui.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class is still being tested. Don't use it.
 * 
 * @author Jason
 * 
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback {
	private static final int DEFAULT_X_SIZE = 500; // Size of DataSet ArrayList
													// .
	private static final int DEFAULT_PAINT_COLOR = 0xFFFF0000;
	private static final int DEFAULT_LINE_WIDTH = 3;
	private static final int GRID_SIZE = 10;
	private static final int GRID_COLOR = 0xFFCCCCCC;

	private int mUpdatePeriod = 20;

	private int mUpdateSpeed = 30;

	private RefreshThread mRefreshThread;
	private ArrayList<DataSet> mDataSets;
	private WaveformAdapter mAdapter;
//	private InputStream mInputStream;

	private Picture mGridPicture;

	public static class WaveformAdapter {
		public int[] getCurrentData(int set, int preferedSize) {
			return null;
		}

	}

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
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mRefreshThread != null) {
			mRefreshThread.stopRefresh();
		}
		mRefreshThread = new RefreshThread();
		mRefreshThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mRefreshThread != null) {
			mRefreshThread.stopRefresh();
			mRefreshThread = null;
		}
	}

	private class RefreshThread extends Thread {

		// private Handler mmHandler;
		private volatile boolean mmIsThreadRunning = true;
		private volatile boolean mmIsDrawing = true;

		private long mmLastUpdateTime = System.currentTimeMillis();

		public RefreshThread() {
			super("WaveformThread");
		}

		@Override
		public void start() {
			super.start();
		}

		public void run() {
			int width, height;
			while (mmIsThreadRunning) {
				
				width = getWidth();
				height = getHeight();
				
				// Update view
				if (mmIsDrawing) {
					for (int i = 0; i < mDataSets.size(); i++) {
						DataSet data = mDataSets.get(i);
						data.initPath(width, height);
					}
					Canvas canvas = getHolder().lockCanvas();
					canvas.drawColor(Color.WHITE);
					drawCanvas(canvas);
					getHolder().unlockCanvasAndPost(canvas);
				}

				// Request data and push into dataSet
				long currentTime = System.currentTimeMillis();
				if (mAdapter != null) {
					float intputCount = 
							((float)(currentTime - mmLastUpdateTime)) / (float)mUpdatePeriod;
					for (int i = 0; i < mDataSets.size(); i++) {
						int[] value = mAdapter.getCurrentData(i,
								(int) (intputCount * mUpdateSpeed));
						mDataSets.get(i).pushData(value);
					}
				}
				mmLastUpdateTime = currentTime;
				try {
					Thread.sleep(mUpdatePeriod);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopRefresh() {
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
		public int upperBound = 600;
		public int lowerBound = 0;
		// public int currentValue = 0;
		private int paintColor = DEFAULT_PAINT_COLOR;
		private int lineWidth = DEFAULT_LINE_WIDTH;

		private Path mmPath;

		public Path getPath() {
			return mmPath;
		}

		public void initPath(int width, int height) {
			mmPath = new Path();
			int size = size();
			if (size <= 0)
				size = 1;
			int range = upperBound - lowerBound;
			float deltaX = (float) width / (float) size;
			float deltaY = (float) height / (float) range;
			float base = height / 2;

			Paint paint = new Paint();
			paint.setColor(paintColor);
			paint.setStrokeWidth(lineWidth);

			ListIterator<Integer> iter = listIterator();

			int y = 0;
			if (iter.hasNext())
				y = iter.next();
			mmPath.moveTo(0, base - y * deltaY);
			for (int j = 1; iter.hasNext(); j++) {
				y = iter.next();
				mmPath.lineTo(j * deltaX, base - y * deltaY);
			}
		}

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
				for (int i = 0; i < size; i++) {
					push(value[i]);
				}
			}
		}

		// public void push() {
		// push(currentValue);
		// }

		public void push(int value) {
			// currentValue = value;
			addLast(value);
			removeFirst();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int width = getWidth(); // of the View .
		int height = getHeight();
		mGridPicture = getGridPicture(width, height);

	}

	private static Picture getGridPicture(int width, int height) {
		Picture picture = new Picture();
		Canvas canvas = picture.beginRecording(width, height);
		// draw background grid line
		Paint paint = new Paint();
		paint.setColor(GRID_COLOR);
		paint.setStyle(Style.STROKE);

		int largeGridSize = GRID_SIZE * 5;
		// vertical
		for (int left = 0; left < width; left += GRID_SIZE) {
			if (left % largeGridSize == 0) {
				paint.setStrokeWidth(2);
			} else {
				paint.setStrokeWidth(1);
			}
			canvas.drawLine(left, 0, left, height, paint);
		}
		// horizontal
		for (int top = 0; top < height; top += GRID_SIZE) {
			if (top % largeGridSize == 0) {
				paint.setStrokeWidth(2);
			} else {
				paint.setStrokeWidth(1);
			}
			canvas.drawLine(0, top, width, top, paint);
		}
		//draw border
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(2);
		canvas.drawRect(0, 0, width, height, paint);
		picture.endRecording();
		return picture;
	}

	protected void drawCanvas(Canvas canvas) {
		if (mGridPicture != null){
			mGridPicture.draw(canvas);
		}
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		for (int i = 0; i < mDataSets.size(); i++) {
			DataSet data = mDataSets.get(i);
			paint.setColor(data.paintColor);
			paint.setStrokeWidth(data.lineWidth);
			canvas.drawPath(data.getPath(), paint);
		}
	}

	public void start() {
		if (mRefreshThread != null) {
			mRefreshThread.startDrawing();
		}
	}

	public void stop() {
		if (mRefreshThread != null) {
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

	 public void pushData(int dataSetId, int data) {
		 if (dataSetId >= 0 && dataSetId < mDataSets.size()) {
		 mDataSets.get(dataSetId).push(data);
		 invalidate();
		 }
	 }
	
	 public void pushData(int dataSetId, int[] dataArray) {
		 if (dataSetId >= 0 && dataSetId < mDataSets.size() && dataArray != null) {
			DataSet dataSet = mDataSets.get(dataSetId);
			int size = dataArray.length;
			for (int i = 0; i < size; i++) {
				dataSet.push(dataArray[i]);
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
