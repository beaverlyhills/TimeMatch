package com.dkhromov.timematch;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class ClockInfo extends Drawable {

	final ReferenceClock reference;
	final TimeZone timezone;
	final Activity context;
	final TimeZoneCatalog catalog;
	
	final Paint paint = new Paint();
	final Path path = new Path();
	final RectF rect = new RectF();
	
	public ClockInfo(Activity context, ReferenceClock reference, TimeZone timezone, TimeZoneCatalog catalog) {
		this.context = context;
		this.reference = reference;
		this.timezone = timezone;
		this.catalog = catalog;
		
		paint.setAntiAlias(true);
	}
	
	@Override
	public void draw(Canvas canvas) {

		Calendar currentUTCTime = reference.getCurrentUTCTime();
		int offset = getHoursOffset(currentUTCTime);
		int minuteOffset = getMinutesOffset(currentUTCTime);
		int start = (24 + reference.getRangeStart()+offset) % 24;
		int end = (24 + start + reference.getRangeLength()) % 24;
		int now = getCurrentTime(currentUTCTime).get(Calendar.HOUR_OF_DAY);
		
		int middle = start >= 18 || start < 6 ? 6 : 18;
		
		boolean needMiddle = (24 + middle - start)%24 < reference.getRangeLength();
		
		if ( !needMiddle ) {
			middle = end;
		}
		
		boolean night = now >= 18 || now < 6;
		
		float cx = canvas.getWidth()/2;
		float cy = canvas.getHeight()/2;
		float radius = cx - context.getResources().getDimensionPixelSize(R.dimen.clock_margin);
		
		// Clock background
		paint.setStyle(Style.FILL);
		paint.setColor(context.getResources().getColor(night ? R.color.night_clock_color : R.color.day_clock_color));
		canvas.drawCircle(cx, cy, radius, paint);
		
		double startPos = Math.PI*(start%12)/6 - Math.PI/2;
		double middlePos = Math.PI*(middle%12)/6 - Math.PI/2;
		double endPos = Math.PI*(end%12)/6 - Math.PI/2;
		
		// Overlay
		boolean nightOver = start >= 18 || start < 6;
		paint.setColor(context.getResources().getColor(nightOver ? R.color.night_overlay_color : R.color.day_overlay_color));
		drawPie(canvas, cx, cy, radius, startPos, middlePos);
		if ( needMiddle ) {
			nightOver = middle >= 18 || middle < 6;
			paint.setColor(context.getResources().getColor(nightOver ? R.color.night_overlay_color : R.color.day_overlay_color));
			drawPie(canvas, cx, cy, radius, middlePos, endPos);
		}

		// Clock border
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.clock_border_width));
		paint.setColor(context.getResources().getColor(R.color.clock_border));
		canvas.drawCircle(cx, cy, radius, paint);
		
		// Numbers
		paint.setStyle(Style.FILL);
		paint.setColor(context.getResources().getColor(night ? R.color.night_arrow_color : R.color.day_arrow_color));
		int digitMargin = context.getResources().getDimensionPixelSize(R.dimen.digit_margin);
		for (int i=1; i<=12; i++) {
			double numberPos = Math.PI*(i)/6 - Math.PI/2;
			String digit = Integer.toString(i);
			float xOffset = paint.measureText(digit)/2;
			float yOffset = paint.ascent()/2;
			float x = cx - xOffset + (radius - digitMargin)*(float)Math.cos(numberPos);
			float y = cy - yOffset + (radius - digitMargin)*(float)Math.sin(numberPos);
			canvas.drawText(digit, x, y, paint);
		}
		
		int currentHour = (24 + currentUTCTime.get(Calendar.HOUR)+offset)%12;
		int currentMinute = (60 + currentUTCTime.get(Calendar.MINUTE)+minuteOffset)%60;
		double minutePos = Math.PI*currentMinute/30 - Math.PI/2;
		double hourPos = Math.PI*currentHour/6 - Math.PI/2 + Math.PI*currentMinute/360;
		
		// Arrows
		int minuteArrowMargin = context.getResources().getDimensionPixelSize(R.dimen.minute_margin);
		int hourArrowMargin = context.getResources().getDimensionPixelSize(R.dimen.hour_margin);
		paint.setColor(context.getResources().getColor(night ? R.color.night_arrow_color : R.color.day_arrow_color));
		paint.setStyle(Style.FILL);
		drawArrow(canvas, cx, cy, radius, hourPos, minuteArrowMargin, hourArrowMargin, 8);
		paint.setStrokeWidth(0);
		paint.setColor(context.getResources().getColor(!night ? R.color.night_arrow_color : R.color.day_arrow_color));
		paint.setAlpha(126);
		paint.setStyle(Style.STROKE);
		drawArrow(canvas, cx, cy, radius, hourPos, minuteArrowMargin, hourArrowMargin, 8);
		
		paint.setColor(context.getResources().getColor(night ? R.color.night_arrow_color : R.color.day_arrow_color));
		paint.setStyle(Style.FILL);
		drawArrow(canvas, cx, cy, radius, minutePos, minuteArrowMargin, minuteArrowMargin, 4);
		paint.setStrokeWidth(0);
		paint.setColor(context.getResources().getColor(!night ? R.color.night_arrow_color : R.color.day_arrow_color));
		paint.setAlpha(126);
		paint.setStyle(Style.STROKE);
		drawArrow(canvas, cx, cy, radius, minutePos, minuteArrowMargin, minuteArrowMargin, 4);
		
		paint.setStrokeWidth(2);
		canvas.drawPoint(cx, cy, paint);
	}

	private void drawArrowLine(Canvas canvas, float cx, float cy, float radius,
			double arrowPos, int arrowTail, int arrowMargin, int arrowWidth) {
		paint.setStrokeWidth(arrowWidth);
		canvas.drawLine(cx + arrowTail*(float)Math.cos(arrowPos-Math.PI), cy + arrowTail*(float)Math.sin(arrowPos-Math.PI), cx + (radius - arrowMargin)*(float)Math.cos(arrowPos), cy + (radius - arrowMargin)*(float)Math.sin(arrowPos), paint);
	}
	
	private void drawArrow(Canvas canvas, float cx, float cy, float radius,
			double arrowPos, int arrowTail, int arrowMargin, int arrowWidth) {
		path.reset();
		path.moveTo(cx + arrowTail*(float)Math.cos(arrowPos-Math.PI), cy + arrowTail*(float)Math.sin(arrowPos-Math.PI));
		path.lineTo(cx + arrowWidth*(float)Math.cos(arrowPos-Math.PI/2), cy + arrowWidth*(float)Math.sin(arrowPos-Math.PI/2));
		path.lineTo(cx + (radius - arrowMargin)*(float)Math.cos(arrowPos), cy + (radius - arrowMargin)*(float)Math.sin(arrowPos));
		path.lineTo(cx + arrowWidth*(float)Math.cos(arrowPos+Math.PI/2), cy + arrowWidth*(float)Math.sin(arrowPos+Math.PI/2));
		path.close();
		canvas.drawPath(path, paint);
	}

	private void drawPie(Canvas canvas, float cx, float cy, float radius,
			double startPos, double middlePos) {
		path.reset();
		path.moveTo(cx, cy);
		if ((Math.PI*2+middlePos-startPos)*180/Math.PI % 360 == 0) {
			path.addCircle(cx, cy, radius, Direction.CW);
		} else {
			rect.set(cx-radius, cy-radius, cx+radius, cy+radius);
			path.arcTo(rect, (float)(startPos*180/Math.PI), (float)((Math.PI*2+middlePos-startPos)*180/Math.PI));
		}
		canvas.drawPath(path, paint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}

	public CharSequence getTimeRangeString() {
		Calendar currentUTCTime = reference.getCurrentUTCTime();
		int offset = getHoursOffset(currentUTCTime);
		int minuteOffset = getMinutesOffset(currentUTCTime);
		int start = (24 + reference.getRangeStart()+offset) % 24;
		int end = (24 + start + reference.getRangeLength()) % 24;
		
		Calendar ourTime = (Calendar) currentUTCTime.clone();
		ourTime.add(Calendar.HOUR_OF_DAY, offset);
		ourTime.add(Calendar.MINUTE, minuteOffset);
		
		return String.format("%02d:%02d", start, minuteOffset)+(end == start ? "" : " - "+String.format("%02d:%02d", end, minuteOffset));
	}

	public CharSequence getName() {
		
		Calendar currentUTCTime = reference.getCurrentUTCTime();
		Calendar ourTime = getCurrentTime(currentUTCTime);
		
		return String.format("%02d:%02d", ourTime.get(Calendar.HOUR_OF_DAY), ourTime.get(Calendar.MINUTE))+" "+catalog.get(timezone.getID())+" ("+timezone.getDisplayName()+")";
	}

	private int getMinutesOffset(Calendar currentUTCTime) {
		return timezone.getOffset(currentUTCTime.getTimeInMillis())/60000 % 60;
	}

	private int getHoursOffset(Calendar currentUTCTime) {
		return timezone.getOffset(currentUTCTime.getTimeInMillis())/3600000;
	}

	private Calendar getCurrentTime(Calendar currentUTCTime) {
		Calendar ourTime = (Calendar) currentUTCTime.clone();
		ourTime.setTimeZone(timezone);
		return ourTime;
	}
	
}
