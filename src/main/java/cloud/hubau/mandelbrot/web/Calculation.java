package cloud.hubau.mandelbrot.web;

public class Calculation {

	private double real;
	private double imaginary;
	private int x, y;
	private int max;

	public Calculation() {
	}

	public Calculation(double real, double imaginary, int x, int y, int max) {
		this.real = real;
		this.imaginary = imaginary;
		this.x = x;
		this.y = y;
		this.max = max;
	}

	public double getReal() {
		return real;
	}

	public void setReal(double real) {
		this.real = real;
	}

	public double getImaginary() {
		return imaginary;
	}

	public void setImaginary(double imaginary) {
		this.imaginary = imaginary;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
