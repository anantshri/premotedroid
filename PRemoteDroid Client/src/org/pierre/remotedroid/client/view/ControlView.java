package org.pierre.remotedroid.client.view;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.activity.ControlActivity;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureRequestAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureResponseAction;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ControlView extends ImageView
{
	private static final float MOUSE_WHEEL_SENSITIVITY_FACTOR = 10;
	
	private PRemoteDroid application;
	private ControlActivity controlActivity;
	private SharedPreferences preferences;
	
	private Bitmap currentBitmap;
	private Bitmap newBitmap;
	
	private Paint paint;
	
	private boolean screenCaptureEnabled;
	private byte screenCaptureFormat;
	private boolean screenCaptureCursorEnabled;
	private float screenCaptureCursorSize;
	
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
	private float wheelDown;
	private float wheelPrevious;
	private float wheelResult;
	private float wheelBarWidth;
	
	public ControlView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.controlActivity = (ControlActivity) context;
		
		this.application = (PRemoteDroid) this.controlActivity.getApplication();
		
		this.preferences = application.getPreferences();
		
		this.paint = new Paint();
		this.paint.setColor(Color.BLACK);
		this.paint.setAntiAlias(true);
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
		}
		else
		{
			this.setImageBitmap(null);
			
			if (this.currentBitmap != null)
			{
				this.currentBitmap.recycle();
				this.currentBitmap = null;
			}
		}
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		this.screenCaptureRequest();
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
				this.screenCaptureRequest();
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
		this.wheelDown = this.wheelPrevious = event.getRawY();
		this.wheelResult = 0;
	}
	
	private void onTouchMove(MotionEvent event)
	{
		if (this.mouseMoveOrWheel)
		{
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
			canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, this.screenCaptureCursorSize, this.paint);
		}
	}
	
	public synchronized void receiveAction(ScreenCaptureResponseAction action)
	{
		if (this.newBitmap != null)
		{
			this.newBitmap.recycle();
		}
		
		this.newBitmap = BitmapFactory.decodeByteArray(action.data, 0, action.data.length);
		
		this.post(new Runnable()
		{
			public void run()
			{
				ControlView controlView = ControlView.this;
				
				if (controlView.currentBitmap != null)
				{
					controlView.currentBitmap.recycle();
				}
				
				controlView.currentBitmap = controlView.newBitmap;
				controlView.newBitmap = null;
				
				controlView.setImageBitmap(controlView.currentBitmap);
			}
		});
	}
	
	private void screenCaptureRequest()
	{
		if (this.screenCaptureEnabled)
		{
			this.application.sendAction(new ScreenCaptureRequestAction((short) this.getWidth(), (short) this.getHeight(), this.screenCaptureFormat));
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
	}
}
