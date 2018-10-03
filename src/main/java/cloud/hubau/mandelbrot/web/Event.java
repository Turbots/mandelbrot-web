package cloud.hubau.mandelbrot.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

	public enum Type {
		CALCULATION, RESULT
	}

	private Type type;
	private double x1, y1, x2, y2;
	private int depth;
	private int color;
}
