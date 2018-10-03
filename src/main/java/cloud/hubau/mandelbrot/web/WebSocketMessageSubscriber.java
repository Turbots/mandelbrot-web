package cloud.hubau.mandelbrot.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j class WebSocketMessageSubscriber {

	private static final int SIZE = 200;

	private final UnicastProcessor<Event> eventPublisher;
	private final WebClient webClient;

	WebSocketMessageSubscriber(UnicastProcessor<Event> eventPublisher) {
		this.eventPublisher = eventPublisher;
		webClient = WebClient.builder().baseUrl("http://35.241.137.76")
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.HOST, "mandelbrot.default.example.com")
			.build();
	}

	void onNext(Event event) {
		if (event.getType() == Event.Type.CALCULATION) {
			Flux<Integer> xFlux = Flux.range(0, SIZE);
			Flux<Integer> yFlux = Flux.range(0, SIZE);

			Flux<Tuple2<Integer, Integer>> tupleFlux = xFlux.flatMap(x -> yFlux.map(y -> Tuples.of(x, y)));

			tupleFlux
				.zipWith(Flux.interval(Duration.of(10, ChronoUnit.MILLIS)))
				.map(t ->
					calculate(event.getX1(), event.getY1(), event.getX2(), event.getY2(), t.getT1().getT1(),
						t.getT1().getT2(), event.getDepth())
						.subscribe(pixel -> {
							//log.info("(" + t.getT1().getT1() + "," + t.getT1().getT2() + ") : " + pixel.getColor());
							eventPublisher.onNext(
								new Event(Event.Type.RESULT, t.getT1().getT1(), t.getT1().getT2(), 0, 0, 0,
									pixel.getColor()));
						}))
				.doOnComplete(() -> log.info("Done."))
				.subscribe();
		}
		eventPublisher.onNext(event);
	}

	private Mono<ColoredPixel> calculate(double x1, double y1, double x2, double y2, double x, double y, int depth) {
		double stepX = (x2 - x1) / SIZE;
		double stepY = (y2 - y1) / SIZE;
		double translatedX = x * stepX;
		double translatedY = y * stepY;
		double real = x1 + translatedX;
		double imaginary = y1 + translatedY;

		Calculation calculation = new Calculation(real, imaginary, depth * 255);
		//log.info("Calculating (" + real + "," + imaginary + ")");

		return this.webClient
			.post()
			.body(BodyInserters.fromObject(calculation))
			.retrieve()
			.bodyToMono(ColoredPixel.class);
	}

	void onError(Throwable error) {
		log.error("Error when sending over Websocket connection", error);
	}

	void onComplete() {
		log.info("Connection Completed");
	}
}
