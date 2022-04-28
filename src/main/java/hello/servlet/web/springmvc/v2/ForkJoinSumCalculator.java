package hello.servlet.web.springmvc.v2;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.LongStream;

public class ForkJoinSumCalculator extends java.util.concurrent.RecursiveTask<Long>{
	private final long [] numbers;
	private final int start;
	private final int end;
	private final long THRESHOLD = 10_000;

	//main task 생성시 public 생성자
	public ForkJoinSumCalculator(long[] numbers){
		this(numbers, 0, numbers.length);
	}

	//recursive subtask 생성시 non public 생성자
	private ForkJoinSumCalculator(long[] numbers, int start, int end){
		this.numbers = numbers;
		this.start = start;
		this.end = end;
	}

	@Override
	protected Long compute(){
		int length = end - start; // task에서 더할 배열의 길이
		if (length <= THRESHOLD){
			return computeSequentially(); // 기준값보다 작으면 순차적으로 결과를 계산
		}
		ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);
		leftTask.fork(); // ForkJoinPool의 다른 스레드로 새로 생성한 태스크를 비동기로 실행
		ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);

		Long rightResult = rightTask.compute(); // 두 번째 서브태스크를 동기 실행, 추가 분할 일어날 수 있음.
		Long leftResult = leftTask.join(); // 첫 번째 서브태스크의 결과를 읽거나 아직 없으면 기다린다.

		return leftResult + rightResult;
	}

	//분할 더 이상 안될 때 서브태스크 결과 계산해주는 알고리즘.
	private Long computeSequentially(){
		long sum = 0;
		for (int i = start; i < end; i++) {
			sum += numbers[i];
		}
		return sum;
	}
	public static long forkJoinSum(long n){
		long[] numbers = LongStream.rangeClosed(1, n).toArray();
		ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
		return new ForkJoinPool().invoke(task);
	}

}
