package common.check.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.util.Log;

import com.tstech.soundlevelinstrument.presenter.MonitorPresenter;

public class VoiceCheakUtil {
	/** 默认报警检测公式 */
	public static final String defErrorCondition = "A|(B&C)";
	/** 默认预警检测公式 */
	public static final String defWarningCondition = "A|(B&C)";
	/** presenter */
	private MonitorPresenter mPresenter;
	/** 记录SPL的数值集合 */
	private List<Double> SPLValues;
	/** 默认FFT的信息集合 */
	private List<float[][]> FFTValues;
	/** 报警检测对象 */
	private VoiceCheckerInterface mErrorChecker;
	/** 报警检测对象 */
	private VoiceCheckerInterface mWarningChecker;

	public VoiceCheakUtil(MonitorPresenter presenter) {
		mPresenter = presenter;
		SPLValues = new ArrayList<Double>();
		FFTValues = new ArrayList<float[][]>();

		try {
			mErrorChecker = getChecker(getFormula(defErrorCondition));
			mWarningChecker = getChecker(getFormula(defWarningCondition));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从逆向表达式 formula中生成VoiceCheckerInterface对象
	 * 
	 * @return 检测对象
	 * @throws Exception
	 */
	public VoiceCheckerInterface getChecker(List<String> formula) throws Exception {

		Stack<VoiceCheckerInterface> stack = new Stack<VoiceCheckerInterface>();
		try {

			int size = formula.size();
			for (int i = 0; i < size; i++) {
				String item = formula.get(i);
				if (isItem(item)) {
					stack.push(getItem(item));
				} else {
					VoiceCheckerInterface checkerA = stack.pop();
					VoiceCheckerInterface checkerB = stack.pop();
					stack.push(getOperator(checkerA, checkerB, item));
				}
			}

			if (stack.size() != 1) {
				mErrorChecker = getChecker(getFormula(defErrorCondition));
				mWarningChecker = getChecker(getFormula(defWarningCondition));
				throw new Exception("Condition is Error");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Condition is Error");
		}

		return stack.pop();
	}

	/**
	 * 根据公式字符串 condition 得到逆向表达式
	 * 
	 * @param condition
	 *            表达式的字符串形式
	 * @return 后缀表达式
	 */
	public List<String> getFormula(String condition) {
		int length = condition.length();
		Stack<String> stack = new Stack<String>();
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			String item = "" + condition.charAt(i);
			if (isItem(item)) {
				result.add(item);
			} else {
				switch (item.charAt(0)) {
				case '(':
					stack.push(item);
					break;
				case ')':
					while (!stack.peek().equals("(")) {
						result.add(stack.pop());
					}
					stack.pop();
					break;
				default:
					while (!stack.empty() && isOperator(stack.peek())) {
						result.add(stack.pop());
					}
					stack.push(item);

					break;
				}
			}
		}

		while (!stack.isEmpty()) {
			result.add(stack.pop());
		}

		return result;
	}

	/**
	 * <功能判断是否为 运算对象>
	 * 
	 * @param item
	 *            如果为"A""B""C" 则为运算对象
	 * @return 如果为"A""B""C" 返回true，"&""|"放回false
	 */
	private boolean isItem(String item) {
		if (item.equals("A") || item.equals("B") || item.equals("C"))
			return true;
		return false;
	}

	/**
	 * <功能判断是否为 运算符>
	 * 
	 * @param item
	 *            如果为"&""|"则为运算对象
	 * @return 如果为"A""B""C" 返回false，"&""|"放回true
	 */
	private boolean isOperator(String item) {
		return item.equals("&") || item.equals("|");
	}

	/**
	 * <功能 生成SPL值、FFT值、主频比值 的检查对象>
	 * 
	 * @param item
	 *            "A""B""C"
	 * @return "A" 返回 SPLChecker ,"C" 返回 FFTChecker,"B" 返回 OtherCheaker
	 */
	private VoiceCheckerInterface getItem(String item) {
		switch (item.charAt(0)) {
		case 'A':
			return new SPLChecker();
		case 'B':
			return new FFTChecker();
		case 'C':
			return new OtherCheaker();
		default:
			return null;
		}
	}

	/**
	 * <功能 生成 或和与 的检查对象>
	 * 
	 * @param item
	 *            "&""|"
	 * @return "&" 返回 ANDChecker ,"|" 返回 ORChecker
	 */
	private VoiceCheckerInterface getOperator(VoiceCheckerInterface checkerA, VoiceCheckerInterface checkerB,
			String item) {
		if (item.equals("&")) {
			return new ANDChecker(checkerA, checkerB);
		} else if (item.equals("|")) {
			return new ORChecker(checkerA, checkerB);
		} else
			return null;
	}

	/**
	 * 设置判断条件
	 * 
	 * @param str
	 *            判断条件的字符串表达形式
	 * @param state
	 *            表示报警或者预警
	 * @throws Exception
	 */
	public void setCondition(String str, int state) throws ErrorConditionException, WarnConditionException {
		switch (state) {
		case VoiceCheckerInterface.ERROR:
			setErrorCondition(str);
			break;
		case VoiceCheckerInterface.WARINING:
			setWarningCondition(str);
			break;
		default:
			break;
		}
		if (str == null || str.matches("[s]*"))
			return;
	}

	private void setErrorCondition(String str) throws ErrorConditionException {
		Log.e("ard", "errorCondition:" + str);
		try {
			mErrorChecker = getChecker(getFormula(str));
		} catch (Exception e) {
			throw new ErrorConditionException("Error Condisition is Error");
		}

	}

	private void setWarningCondition(String str) throws WarnConditionException {
		Log.e("ard", "warningCondition:" + str);
		try {
			mWarningChecker = getChecker(getFormula(str));
		} catch (Exception e) {
			throw new WarnConditionException("Waring Condisition is Error");
		}

	}

	public void addSPLValue(double value) {
		SPLValues.add(value);
		check();
	}

	public void addFFTValue(float[][] freqAndValue) {
		FFTValues.add(freqAndValue);
		check();
	}

	/**
	 * 检测是否报警或者预警并同志presenter更新
	 */
	private void check() {
		// 由于是异步操作，要保持 SPL和FFT的状态为同一时刻状态，所以其中一个值为空就不检查，检测完成后移出此时刻的SPL、FFT值
		if (SPLValues.size() == 0 || FFTValues.size() == 0)
			return;
		float[] freqs = FFTValues.get(0)[0];
		float[] values = FFTValues.get(0)[1];
		double splValue = SPLValues.get(0);
		float fftValue = freqs[0];
		float percent = (float) (fftValue / splValue);

		boolean isError = mErrorChecker.cheak(splValue, fftValue, percent);
		if (isError) {
			mPresenter.updateValueDisplay(splValue, freqs, values, VoiceCheckerInterface.ERROR);
		} else {
			boolean isWarning = mWarningChecker.cheak(splValue, fftValue, percent);
			if (isWarning) {
				mPresenter.updateValueDisplay(splValue, freqs, values, VoiceCheckerInterface.WARINING);
			} else {
				mPresenter.updateValueDisplay(splValue, freqs, values, VoiceCheckerInterface.NORMAL);
			}
		}

		SPLValues.remove(0);
		FFTValues.remove(0);
		// VoiceState A = SPLChecker.cheak(splValue);
		// VoiceState B = FFTChecker.cheak(values[0]);
		// VoiceState C = OtherCheaker.cheak(splValue, values[0]);
		//
		// getState(A,B,C);
	}

	public class ErrorConditionException extends RuntimeException {
		private static final long serialVersionUID = 19930418;

		public ErrorConditionException() {
			super();
		}

		public ErrorConditionException(String detailMessage) {
			super(detailMessage);
		}
	}

	public class WarnConditionException extends RuntimeException {
		private static final long serialVersionUID = 19930418;

		public WarnConditionException() {
			super();
		}

		public WarnConditionException(String detailMessage) {
			super(detailMessage);
		}
	}

}
