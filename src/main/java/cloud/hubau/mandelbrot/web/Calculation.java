package cloud.hubau.mandelbrot.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Calculation {

	private double real;
	private double imaginary;
	private int max;
}
