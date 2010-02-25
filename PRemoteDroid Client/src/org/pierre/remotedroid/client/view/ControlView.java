package org.pierre.remotedroid.client.view;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.activity.ControlActivity;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.app.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;
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

public class ControlView extends ImageView implements PRemoteDroidActionReceiver
{
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
	
	private float downX;
	private float downY;
	private boolean holdPossible;
	
	private long clickDelay;
	private long holdDelay;
	private float immobileDistance;
	
	private float sensitivity;
	private float acceleration;
	
	private float previousX;
	private float previousY;
	private float resultX;
	private float resultY;
	
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
			this.application.registerActionReceiver(this);
			
			this.reloadPreferences();
		}
		else
		{
			this.application.unregisterActionReceiver(this);
			
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
				this.screenCaptureRequest();
				this.onTouchUp(event);
				break;
			}
				
			default:
				break;
		}
		
		return true;
	}
	
	protected void onTouchDown(MotionEvent event)
	{
		this.downX = this.previousX = event.getRawX();
		this.downY = this.previousY = event.getRawY();
		
		this.resultX = 0;
		this.resultY = 0;
		
		this.holdPossible = true;
	}
	
	protected void onTouchMove(MotionEvent event)
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
		
		float moveXRaw = event.getRawX() - this.previousX;
		float moveYRaw = event.getRawY() - this.previousY;
		
		moveXRaw *= this.sensitivity;
		moveYRaw *= this.sensitivity;
		
		moveXRaw = (float) ((Math.pow(Math.abs(moveXRaw), this.acceleration) * Math.signum(moveXRaw)));
		moveYRaw = (float) ((Math.pow(Math.abs(moveYRaw), this.acceleration) * Math.signum(moveYRaw)));
		
		moveXRaw += this.resultX;
		moveYRaw += this.resultY;
		
		int moveXFinal = Math.round(moveXRaw);
		int moveYFinal = Math.round(moveYRaw);
		
		this.resultX = moveXRaw - moveXFinal;
		this.resultY = moveYRaw - moveYFinal;
		
		if (moveXFinal != 0 || moveYFinal != 0)
		{
			this.controlActivity.mouseMove(moveXFinal, moveYFinal);
		}
		
		this.previousX = event.getRawX();
		this.previousY = event.getRawY();
	}
	
	protected void onTouchUp(MotionEvent event)
	{
		if (event.getEventTime() - event.getDownTime() < this.clickDelay && this.getDistanceFromDown(event) <= this.immobileDistance)
		{
			if (this.leftClickView.isPressed())
			{
				this.application.vibrate(100);
			}
			else
			{
				this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_DOWN);
				
				this.application.vibrate(50);
			}
			
			this.controlActivity.mouseClick(MouseClickAction.BUTTON_LEFT, MouseClickAction.STATE_UP);
			
			this.leftClickView.setPressed(false);
			this.leftClickView.setHold(false);
		}
	}
	
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (this.screenCaptureEnabled && this.screenCaptureCursorEnabled)
		{
			canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, this.screenCaptureCursorSize, this.paint);
		}
	}
	
	public synchronized void receiveAction(PRemoteDroidAction action)
	{
		if (action instanceof ScreenCaptureResponseAction)
		{
			ScreenCaptureResponseAction scra = (ScreenCaptureResponseAction) action;
			
			if (this.newBitmap != null)
			{
				this.newBitmap.recycle();
			}
			
			this.newBitmap = BitmapFactory.decodeByteArray(scra.data, 0, scra.data.length);
			
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
	}
	
	public void screenCaptureRequest()
	{
		if (this.screenCaptureEnabled)
		{
			this.application.sendAction(new ScreenCaptureRequestAction((short) this.getWidth(), (short) this.getHeight(), this.screenCaptureFormat));
		}
	}
	
	private double getDistanceFromDown(MotionEvent event)
	{
		return Math.sqrt(Math.pow(event.getRawX() - this.downX, 2) + Math.pow(event.getRawY() - this.downY, 2));
	}
	
	private void reloadPreferences()
	{
		float screenDensity = this.getResources().getDisplayMetrics().density;
		
		this.clickDelay = Long.parseLong(this.preferences.getString("control_click_delay", null));
		
		this.holdDelay = Long.parseLong(this.preferences.getString("control_hold_delay", null));
		
		this.immobileDistance = Float.parseFloat(this.preferences.getString("control_immobile_distance", null));
		this.immobileDistance *= screenDensity;
		
		this.sensitivity = Float.parseFloat(this.preferences.getString("control_sensitivity", null));
		this.sensitivity /= screenDensity;
		this.acceleration = Float.parseFloat(this.preferences.getString("control_acceleration", null));
		
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
	}
}
