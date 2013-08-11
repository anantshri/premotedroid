package com.ControllerDroid.client.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.ControllerDroid.client.R;
import com.ControllerDroid.client.activity.ControlActivity;
import com.ControllerDroid.client.app.ControllerDroid;
import com.ControllerDroid.protocol.action.MouseClickAction;
import com.ControllerDroid.protocol.action.ScreenCaptureRequestAction;
import com.ControllerDroid.protocol.action.ScreenCaptureResponseAction;

public class ControlView extends ImageView
{
	private static final float MOUSE_WHEEL_SENSITIVITY_FACTOR = 10;
	
	private ControllerDroid application;
	private ControlActivity controlActivity;
	private SharedPreferences preferences;
	
	private Paint paint;
	private Paint paint2;
	
	private boolean screenCaptureEnabled;
	private byte screenCaptureFormat;
	private boolean screenCaptureCursorEnabled;
	private float screenCaptureCursorSize;
	private boolean debugging;
	private boolean useEditText;
	private int orientation;
	
	private int zoom;
	private int fps;
	private int scale;
	
	private ClickView leftClickView;
	
	private boolean holdPossible;
	
	private long clickDelay;
	private long holdDelay;
	private float immobileDistance;
	
	private boolean mouseMoveOrWheel;
	
	private float moveSensitivity;
	private float moveAcceleration;
	private float moveDownX;
	private float moveDownY;
	private float movePreviousX;
	private float movePreviousY;
	private float moveResultX;
	private float moveResultY;
	
	private float wheelSensitivity;
	private float wheelAcceleration;
	private float wheelPrevious;
	private float wheelResult;
	private float wheelBarWidth;
	
	public ControlView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.controlActivity = (ControlActivity) context;
		
		this.application = (ControllerDroid) this.controlActivity.getApplication();
		
		this.preferences = application.getPreferences();
		
		this.paint = new Paint();
		this.paint.setColor(Color.BLACK);
		this.paint.setAntiAlias(true);
		// for the outer ring in the cursor dot
		this.paint2 = new Paint();
		this.paint2.setColor(Color.WHITE);
		this.paint2.setAntiAlias(true);
	}
	
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.leftClickView = (ClickView) this.controlActivity.findViewById(R.id.leftClickView);
		
	}
	
	protected synchronized void onWindowVisibilityChanged(int visibility)
	{
		super.onWindowVisibilityChanged(visibility);
		
		if (visibility == VISIBLE)
		{
			this.reloadPreferences();
			this.screenCaptureRequest();
			// Show or hide the input line
			((android.widget.LinearLayout) this.controlActivity.findViewById(R.id.inputLayout)).setVisibility(this.useEditText ? android.view.View.VISIBLE : android.view.View.GONE);
			// Set orientation
			if (debugging)
				Log.d("Note", "Orientation: " + orientation);
			
			switch (orientation)
			{
				case 0: // Automatic //
					this.controlActivity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					break;
				case 1: // Portrait
					// Froyo can't rotate both ways
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
						this.controlActivity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
					else
						this.controlActivity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				case 2: // Landscape
					// Froyo can't rotate both ways?
					// It's odd, as the device I tested this on rotates to
					// reversed landscape in automatic mode...
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
						this.controlActivity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
					else
						this.controlActivity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
			}
			
		}
		else
		{
			this.setImageBitmap(null);
		}
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		// Commented out since we're going to be in a loop
		// this.screenCaptureRequest();
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_MOVE:
			{
				this.onTouchMove(event);
				break;
			}
			
			case MotionEvent.ACTION_DOWN:
			{
				this.onTouchDown(event);
				break;
			}
			
			case MotionEvent.ACTION_UP:
			{
				this.onTouchUp(event);
				// Commented out since we're going to be in a loop
				// this.screenCaptureRequest();
				break;
			}
			
			default:
				break;
		}
		
		return true;
	}
	
	private void onTouchDown(MotionEvent event)
	{
		this.mouseMoveOrWheel = event.getX() < this.getWidth() - this.wheelBarWidth;
		
		if (this.mouseMoveOrWheel)
		{
			this.onTouchDownMouseMove(event);
		}
		else
		{
			this.onTouchDownMouseWheel(event);
		}
	}
	
	private void onTouchDownMouseMove(MotionEvent event)
	{
		this.moveDownX = this.movePreviousX = event.getRawX();
		this.moveDownY = this.movePreviousY = event.getRawY();
		
		this.moveResultX = 0;
		this.moveResultY = 0;
		
		this.holdPossible = true;
	}
	
	private void onTouchDownMouseWheel(MotionEvent event)
	{
		this.wheelPrevious = event.getRawY();
		this.wheelResult = 0;
	}
	
	private void onTouchMove(MotionEvent event)
	{
		if (this.mouseMoveOrWheel)
		{
			if (event.getPointerCount() == 2)
			{
				// a new pointer, start a wheel event
				this.mouseMoveOrWheel = false;
				onTouchDownMouseWheel(event);
				this.holdPossible = false;
			}
			else
				this.onTouchMoveMouseMove(event);
		}
		else
		{
			this.onTouchMoveMouseWheel(event);
		}
	}
	
	private void onTouchMoveMouseMove(MotionEvent event)
	{
		if (this.holdPossible)
		{
			if (this.getDistanceFromDown(event) > this.immobileDistance)
			{
				this.holdPossible = false;
			}
			else if (event.getEventTime() - event.getDownTime() > this.holdDelay)
			{
				this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_DOWN);
				
				this.holdPossible = false;
				
				this.leftClickView.setPressed(true);
				this.leftClickView.setHold(true);
				
				this.application.vibrate(100);
			}
		}
		
		float moveRawX = event.getRawX() - this.movePreviousX;
		float moveRawY = event.getRawY() - this.movePreviousY;
		
		moveRawX *= this.moveSensitivity;
		moveRawY *= this.moveSensitivity;
		
		moveRawX = (float) ((Math.pow(Math.abs(moveRawX), this.moveAcceleration) * Math.signum(moveRawX)));
		moveRawY = (float) ((Math.pow(Math.abs(moveRawY), this.moveAcceleration) * Math.signum(moveRawY)));
		
		moveRawX += this.moveResultX;
		moveRawY += this.moveResultY;
		
		int moveXFinal = Math.round(moveRawX);
		int moveYFinal = Math.round(moveRawY);
		
		if (moveXFinal != 0 || moveYFinal != 0)
		{
			this.controlActivity.mouseMove(moveXFinal, moveYFinal);
		}
		
		this.moveResultX = moveRawX - moveXFinal;
		this.moveResultY = moveRawY - moveYFinal;
		
		this.movePreviousX = event.getRawX();
		this.movePreviousY = event.getRawY();
	}
	
	private void onTouchMoveMouseWheel(MotionEvent event)
	{
		float wheelRaw = event.getRawY() - this.wheelPrevious;
		wheelRaw *= this.wheelSensitivity;
		wheelRaw = (float) ((Math.pow(Math.abs(wheelRaw), this.wheelAcceleration) * Math.signum(wheelRaw)));
		wheelRaw += this.wheelResult;
		int wheelFinal = Math.round(wheelRaw);
		
		if (wheelFinal != 0)
		{
			this.controlActivity.mouseWheel(wheelFinal);
		}
		
		this.wheelResult = wheelRaw - wheelFinal;
		this.wheelPrevious = event.getRawY();
	}
	
	private void onTouchUp(MotionEvent event)
	{
		if (this.mouseMoveOrWheel)
		{
			this.onTouchUpMouseMove(event);
		}
		else
		{
			this.onTouchUpMouseWheel(event);
		}
	}
	
	private void onTouchUpMouseMove(MotionEvent event)
	{
		if (event.getEventTime() - event.getDownTime() < this.clickDelay && this.getDistanceFromDown(event) <= this.immobileDistance)
		{
			if (this.leftClickView.isPressed())
			{
				this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_UP);
				this.application.vibrate(100);
				this.leftClickView.setPressed(false);
				this.leftClickView.setHold(false);
			}
			else
			{
				this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_DOWN);
				
				this.application.vibrate(50);
				this.leftClickView.setPressed(true);
				
				this.postDelayed(new Runnable()
				{
					public void run()
					{
						ControlView.this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_UP);
						ControlView.this.leftClickView.setPressed(false);
					}
				}, 50);
			}
		}
	}
	
	private void onTouchUpMouseWheel(MotionEvent event)
	{
		
	}
	
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (this.screenCaptureEnabled && this.screenCaptureCursorEnabled)
		{
			canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, this.screenCaptureCursorSize, this.paint2);
			canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, this.screenCaptureCursorSize - 1, this.paint);
		}
	}
	
	private BitmapFactory.Options bitmapOpts = new BitmapFactory.Options();
	
	public synchronized void receiveAction(final ScreenCaptureResponseAction action)
	{
		bitmapOpts.inSampleSize = this.scale;
		if (this.debugging)
			Log.d("Note", "Recieved an image of size " + action.dataSize + " which resulted in SampleSize of " + bitmapOpts.inSampleSize);
		
		// this.newBitmap = BitmapFactory.decodeByteArray(action.data, 0,
		// action.data.length);
		
		this.postDelayed(new Runnable()
		{
			public void run()
			{
				ControlView.this.setImageBitmap(BitmapFactory.decodeByteArray(action.data, 0, action.dataSize, bitmapOpts));
				application.sendAction(new ScreenCaptureRequestAction((short) (getWidth() / zoom), (short) (getHeight() / zoom), screenCaptureFormat));
			}
		}, this.fps);
		
	}
	
	private void screenCaptureRequest()
	{
		if (this.screenCaptureEnabled)
		{
			try
			{
				this.wait(200);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			application.sendAction(new ScreenCaptureRequestAction((short) (getWidth() / this.zoom), (short) (getHeight() / this.zoom), screenCaptureFormat));
		}
	}
	
	private double getDistanceFromDown(MotionEvent event)
	{
		return Math.sqrt(Math.pow(event.getRawX() - this.moveDownX, 2) + Math.pow(event.getRawY() - this.moveDownY, 2));
	}
	
	private void reloadPreferences()
	{
		float screenDensity = this.getResources().getDisplayMetrics().density;
		
		this.clickDelay = Long.parseLong(this.preferences.getString("control_click_delay", null));
		
		this.holdDelay = Long.parseLong(this.preferences.getString("control_hold_delay", null));
		
		this.immobileDistance = Float.parseFloat(this.preferences.getString("control_immobile_distance", null));
		this.immobileDistance *= screenDensity;
		
		this.moveSensitivity = Float.parseFloat(this.preferences.getString("control_sensitivity", null));
		this.moveSensitivity /= screenDensity;
		this.moveAcceleration = Float.parseFloat(this.preferences.getString("control_acceleration", null));
		
		this.wheelSensitivity = this.moveSensitivity / MOUSE_WHEEL_SENSITIVITY_FACTOR;
		this.wheelAcceleration = this.moveAcceleration;
		
		this.wheelBarWidth = Float.parseFloat(this.preferences.getString("wheel_bar_width", null)) * screenDensity;
		
		this.screenCaptureEnabled = this.preferences.getBoolean("screenCapture_enabled", false);
		
		String format = this.preferences.getString("screenCapture_format", null);
		if (format.equals("png"))
		{
			this.screenCaptureFormat = ScreenCaptureRequestAction.FORMAT_PNG;
		}
		else if (format.equals("jpg"))
		{
			this.screenCaptureFormat = ScreenCaptureRequestAction.FORMAT_JPG;
		}
		
		this.screenCaptureCursorEnabled = this.preferences.getBoolean("screenCapture_cursor_enabled", false);
		
		this.screenCaptureCursorSize = Float.parseFloat(this.preferences.getString("screenCapture_cursor_size", null));
		this.screenCaptureCursorSize *= screenDensity;
		
		this.setKeepScreenOn(this.preferences.getBoolean("keep_screen_on", false));
		this.debugging = this.preferences.getBoolean("debug_enabled", false);
		this.useEditText = this.preferences.getBoolean("useEditText", false);
		
		this.zoom = Integer.parseInt(this.preferences.getString("screenCapture_zoom", "4"));
		this.fps = Integer.parseInt(this.preferences.getString("screenCapture_fps", "1000"));
		this.scale = Integer.parseInt(this.preferences.getString("screenCapture_scale", "4"));
		this.orientation = Integer.parseInt(this.preferences.getString("orientation", "0"));
		
	}
}
