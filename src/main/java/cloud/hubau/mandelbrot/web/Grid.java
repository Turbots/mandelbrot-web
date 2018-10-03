package cloud.hubau.mandelbrot.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grid {

	private double x1, y1, x2, y2;
	private int width, height;
	private int depth;
}
